/*******************************************************************************
 * Copyright (c) 2011, 2012 MARINTEK and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild U. Resheim - initial API and implementation
 *******************************************************************************/
package no.marintek.mylyn.internal.wikitext.confluence.core.tasks;

import java.io.File;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.rpc.ServiceException;

import no.marintek.mylyn.internal.wikitext.confluence.core.wsdl.AuthenticationFailedException;
import no.marintek.mylyn.internal.wikitext.confluence.core.wsdl.InvalidSessionException;
import no.marintek.mylyn.internal.wikitext.confluence.core.wsdl.RemoteException;
import no.marintek.mylyn.internal.wikitext.confluence.core.wsdl.beans.RemoteAttachment;
import no.marintek.mylyn.internal.wikitext.confluence.core.wsdl.beans.RemotePage;
import no.marintek.mylyn.internal.wikitext.confluence.core.wsdl.beans.RemotePageSummary;
import no.marintek.mylyn.internal.wikitext.confluence.core.wsdl.confluenceservice_v1.ConfluenceSoapServiceServiceLocator;
import no.marintek.mylyn.internal.wikitext.confluence.core.wsdl.confluenceservice_v1.ConfluenceserviceV1SoapBindingStub;
import no.marintek.mylyn.wikitext.confluence.core.ExtendedConfluenceLanguage;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Get;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.eclipse.mylyn.wikitext.core.parser.outline.OutlineItem;
import org.eclipse.mylyn.wikitext.core.parser.outline.OutlineParser;
import org.eclipse.mylyn.wikitext.core.util.anttask.MarkupTask;

/**
 * Downloads pages and attachments from a Confluence Wiki.
 * 
 * @author Torkild U. Resheim
 */
public abstract class AbstractWikiConversionTask extends MarkupTask {

	/**
	 * Confluence supports Basic HTTP authentication which we will use when we need to download attachments.
	 */
	protected class BasicAuthenticator extends Authenticator {

		private final String username, password;

		public BasicAuthenticator(String user, String pass) {
			username = user;
			password = pass;
		}

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			log("Logging into \"" + getRequestingPrompt() + "\" at " + getRequestingHost()); //$NON-NLS-1$ //$NON-NLS-2$
			return new PasswordAuthentication(username, password.toCharArray());
		}
	}

	/**
	 * Type to describe a wiki page.
	 */
	public static class Page {

		protected boolean exclude = false;

		/** The generated item */
		protected OutlineItem outline;

		/** Path on Confluence */
		protected String path;

		/** Namespace on Confluence */
		protected String space;

		/** Page title */
		protected String title;

		public Page() {
			// ignore
		}

		public Page(String space, String string, boolean b) {
			this.space = space;
			this.path = string;
			this.title = string;
			this.exclude = b;
		}

		public OutlineItem getOutline() {
			return outline;
		}

		public String getPath() {
			return path;
		}

		public String getSpace() {
			return space;
		}

		public String getTitle() {
			return title;
		}

		public boolean isExclude() {
			return exclude;
		}

		public void setExclude(boolean exclude) {
			this.exclude = exclude;
		}

		public void setOutline(OutlineItem outline) {
			this.outline = outline;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public void setSpace(String space) {
			this.space = space;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		@Override
		public String toString() {
			return path;
		}
	}

	public static class PageAppendum {

		String text;

		public void addText(String addendum) {
			if (this.text == null) {
				this.text = addendum;
			} else {
				this.text += addendum;
			}
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
	}

	/**
	 * CSS style sheet which contents can be embedded into the finished HTML file.
	 */
	public static class Stylesheet {

		protected final Map<String, String> attributes = new HashMap<String, String>();

		protected File file;

		protected String url;

		public void addConfiguredAttribute(StylesheetAttribute attribute) {
			attributes.put(attribute.getName(), attribute.getValue());
		}

		public File getFile() {
			return file;
		}

		public String getUrl() {
			return url;
		}

		public void setFile(File file) {
			this.file = file;
		}

		public void setUrl(String url) {
			this.url = url;
		}
	}

	public static class StylesheetAttribute {

		protected String name;

		protected String value;

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	private static ConfluenceserviceV1SoapBindingStub binding;

	protected static final String FILE_ENCODING = "utf-8"; //$NON-NLS-1$

	protected static final Pattern PAGE_NAME_PATTERN = Pattern.compile("([^#]*)(?:#(.*))?"); //$NON-NLS-1$

	/** Location for downloaded attachment */
	protected File attachmentDestination;

	protected String attachmentPrefix;

	protected String defaultAbsoluteLinkTarget;

	protected boolean emitDoctype = true;

	protected boolean formatOutput = true;

	protected boolean generatePageHeaders = true;

	protected String helpPrefix;

	protected String htmlDoctype = null;

	private String httpPassword;

	private String httpUsername;

	private int latexDpi = 120;

	protected String linkRel;

	private MarkupLanguage markupLanguage;

	protected PageAppendum pageAppendum;

	/** The destination directory */
	protected File pageDestination;

	/** The list of pages encountered in Confluence */
	protected final ArrayList<Page> pages = new ArrayList<Page>();

	protected OutlineItem rootItem;

	private String sessionToken = null;

	protected final List<Stylesheet> stylesheets = new ArrayList<Stylesheet>();

	protected boolean suppressBuiltInCssStyles = false;

	private int timeoutCount;

	protected String title;

	protected boolean useInlineCssStyles = true;

	protected String wikiBaseUrl;

	protected boolean xhtmlStrict = false;

	public void addPage(Page page) {
		if (page == null) {
			throw new IllegalArgumentException();
		}
		pages.add(page);
	}

	public void addPageAddendum(PageAppendum addendum) {
		pageAppendum = addendum;
	}

	public void addStylesheet(Stylesheet stylesheet) {
		stylesheets.add(stylesheet);
	}

	/**
	 * Determines a suitable name for the HTML file based on the wiki page having the supplied name.
	 * 
	 * @param name
	 *            the wiki page name
	 * @return the resulting file name
	 */
	protected String computeFilename(String name) {
		// ???: There should be no paths in Confluence
		if (name.lastIndexOf('/') != -1) {
			name = name.substring(name.lastIndexOf('/') + 1);
		}
		name = createMarkupLanguage().getIdGenerationStrategy().generateId(name);
		String filename = getFilenameFormat().replace("$1", name); //$NON-NLS-1$ 
		return filename;
	}

	/**
	 * Computes the outline of a page
	 * 
	 * @param page
	 *            wiki page of the content
	 * @param markupContent
	 *            markup content of the file
	 * @return
	 */
	protected OutlineItem computeOutline(Page page, String markupContent) {
		OutlineParser outlineParser = new OutlineParser();
		outlineParser.setMarkupLanguage(createMarkupLanguage());
		OutlineItem item = outlineParser.parse(markupContent);
		item.setLabel(page.getTitle());
		item.setResourcePath(computeRelativeFile(item));
		return item;
	}

	protected String computePrefixPath(File destDir, File tocParentFile) {
		String prefix = destDir.getAbsolutePath().substring(tocParentFile.getAbsolutePath().length());
		prefix = prefix.replace('\\', '/');
		if (prefix.startsWith("/")) { //$NON-NLS-1$
			prefix = prefix.substring(1);
		}
		if (prefix.endsWith("/")) { //$NON-NLS-1$
			prefix = prefix.substring(0, prefix.length() - 1);
		}
		return prefix;
	}

	protected String computeRelativeFile(OutlineItem item) {
		return null;
	}

	@Override
	protected MarkupLanguage createMarkupLanguage() throws BuildException {
		if (markupLanguage==null){
			markupLanguage = new ExtendedConfluenceLanguage(latexDpi);
		}
		return markupLanguage;
	}

	/**
	 * Downloads all attachments belonging to the page.
	 * 
	 * @param page
	 *            the page to download for
	 * @throws java.rmi.RemoteException
	 * @throws RemoteException
	 * @throws InvalidSessionException
	 * @throws MalformedURLException
	 */
	protected void downloadAttachments(RemotePage page) throws InvalidSessionException, RemoteException, java.rmi.RemoteException,
			MalformedURLException {
		RemoteAttachment[] attachments = binding.getAttachments(sessionToken, page.getId());
		if (attachments == null || attachments.length == 0) {
			return;
		}
		for (RemoteAttachment attachment : attachments) {
			URL attachurl = new URL(attachment.getUrl() + getAuthenticationURLSuffix());
			Get get = new Get();
			get.setRetries(5);
			get.setTaskName("get"); //$NON-NLS-1$
			get.setProject(getProject());
			get.setLocation(getLocation());
			get.setSrc(attachurl);
			File file = new File(attachmentDestination, attachment.getFileName());
			get.setDest(file);
			get.execute();
			processAttachment(file);

		}
	}

	/**
	 * Downloads the specified page, converts it to HTML, creates a table of contents outline and triggers downloading
	 * of child pages and attachments.
	 * 
	 * @param parent
	 *            the parent page
	 * @param page
	 *            the page to download
	 * @throws java.rmi.RemoteException
	 * @throws MalformedURLException
	 * @throws java.rmi.RemoteException
	 */
	protected void downloadPage(Page parent, Page page) {
		try {
			getProject().log(MessageFormat.format(Messages.getString("WikiToDocTask.ProcessingPage"), page.path), Project.MSG_INFO); //$NON-NLS-1$//		StringBuffer buffer = new StringBuffer(page.getContent());
			RemotePage rpage = binding.getPage(sessionToken, page.getSpace(), page.getPath());
			markupToDoc(rpage);
			downloadAttachments(rpage);
			// We're using the Confluence navigation structure to create outline
			// items. This ensures that the structure will be the same. In addition
			// we will have sub-items representing document headers.
			OutlineItem newItem = computeOutline(page, rpage.getContent());
			page.setOutline(newItem);
			if (parent == null) {
				rootItem.getChildren().add(newItem);
			} else {
				parent.getOutline().getChildren().add(newItem);
			}

			RemotePageSummary[] remotePageSummaries = binding.getChildren(sessionToken, rpage.getId());
			if (remotePageSummaries != null) {
				for (RemotePageSummary remotePageSummary : remotePageSummaries) {
					if (!isExcluded(remotePageSummary.getTitle())) {
						Page newPage = new Page(remotePageSummary.getSpace(), remotePageSummary.getTitle(), false);
						pages.add(newPage);
						downloadPage(page, newPage);
					}
				}
			}
			timeoutCount = 0;
		} catch (MalformedURLException e) {
			throw new BuildException(e);
		} catch (InvalidSessionException e) {
			throw new BuildException(e);
		} catch (RemoteException e) {
			throw new BuildException(e);
		} catch (java.rmi.RemoteException e) {
			if (e.getCause() instanceof SocketTimeoutException && timeoutCount < 10) {
				timeoutCount++;
				System.out.println("Got timeout from Confluence, trying again " + timeoutCount);
				downloadPage(parent, page);
			} else {
				throw new BuildException(e);
			}
		}
	}

	@Override
	public void execute() {

		validateSettings();
		((ExtendedConfluenceLanguage) createMarkupLanguage()).setResourcesPath(attachmentDestination);

		if (httpPassword != null && httpUsername != null) {
			Authenticator.setDefault(new BasicAuthenticator(httpUsername, httpPassword));
		}

		setMarkupLanguage("Confluence"); //$NON-NLS-1$

		rootItem = new OutlineItem(null, 0, "<root>", 0, -1, title); //$NON-NLS-1$
		login();
		ArrayList<Page> original = new ArrayList<Page>();
		original.addAll(pages);
		for (Page page : original) {
			if (!page.isExclude()) {
				downloadPage(null, page);
			}
		}
		logout();
		postProcess();
	}

	public String getAttachmentPrefix() {
		return attachmentPrefix;
	}

	/**
	 * Provides a suffix to the Confluence wiki URL required for authenticating.
	 * 
	 * @return a URL suffix
	 */
	protected String getAuthenticationURLSuffix() {
		if (httpPassword != null && httpUsername != null) {
			return "&os_authType=basic"; //$NON-NLS-1$
		} else {
			return ""; //$NON-NLS-1$
		}

	}

	public String getDefaultAbsoluteLinkTarget() {
		return defaultAbsoluteLinkTarget;
	}

	public File getDest() {
		return pageDestination;
	}

	public abstract String getFilenameFormat();

	public String getHelpPrefix() {
		return helpPrefix;
	}

	public String getHttpPassword() {
		return httpPassword;
	}

	public String getHttpUsername() {
		return httpUsername;
	}

	public String getTitle() {
		return title;
	}

	public String getWikiBaseUrl() {
		return wikiBaseUrl;
	}

	public boolean isEmitDoctype() {
		return emitDoctype;
	}

	/**
	 * Determines whether or not the page is excluded from being a part of the generated documentation.
	 * 
	 * @param path
	 *            path or name of the page
	 * @return <code>true</code> if excluded
	 */
	protected boolean isExcluded(String path) {
		for (Page page : pages) {
			if (page.exclude && path.equals(page.path)) {
				return true;
			}
		}
		return false;
	}

	public boolean isGeneratePageHeaders() {
		return generatePageHeaders;
	}

	public boolean isSuppressBuiltInCssStyles() {
		return suppressBuiltInCssStyles;
	}

	public boolean isUseInlineCssStyles() {
		return useInlineCssStyles;
	}

	public boolean isXhtmlStrict() {
		return xhtmlStrict;
	}

	/**
	 * Logs in to the Confluence service.
	 */
	private void login() {
		try {
			ConfluenceSoapServiceServiceLocator locator = new ConfluenceSoapServiceServiceLocator();
			locator.setConfluenceserviceV1EndpointAddress(getWikiBaseUrl() + "rpc/soap-axis/confluenceservice-v1"); //$NON-NLS-1$
			binding = (ConfluenceserviceV1SoapBindingStub) locator.getConfluenceserviceV1();
			binding.setTimeout(5000);
			sessionToken = binding.login(getHttpUsername(), getHttpPassword());
		} catch (AuthenticationFailedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (java.rmi.RemoteException e) {
			e.printStackTrace();
		} catch (ServiceException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Logs out of the Confluence session.
	 */
	private void logout() {
		try {
			binding.logout(sessionToken);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Implement to convert the given page to the desired format.
	 * 
	 * @param page
	 *            the page to convert
	 */
	protected abstract void markupToDoc(RemotePage page);

	/**
	 * Implement to do processing after pages has been downloaded from Confluence.
	 */
	protected abstract void postProcess();

	/**
	 * Do post processing of the markup page.
	 * 
	 * @param page
	 *            the remote page
	 * @param content
	 *            the content of the page.
	 * @return
	 */
	protected String postProcessPage(RemotePage page, String content) {
		if (pageAppendum != null) {
			String text = this.pageAppendum.text;
			String appendum = text.replace("{url}", page.getUrl()); //$NON-NLS-1$
			appendum = appendum.replace("{title}", page.getTitle()); //$NON-NLS-1$
			content += appendum;
			content += "\n"; //$NON-NLS-1$
			getProject().log(MessageFormat.format(Messages.getString("WikiToDocTask.AppendingMarkup"), page.getTitle(), appendum), //$NON-NLS-1$
					Project.MSG_VERBOSE);

		}
		return content;
	}

	/**
	 * Implement to handle additional processing of attachments
	 */
	protected void processAttachment(File file) {
	}

	public void setAttachmentPrefix(String attachmentPrefix) {
		this.attachmentPrefix = attachmentPrefix;
	}

	public void setDefaultAbsoluteLinkTarget(String defaultAbsoluteLinkTarget) {
		this.defaultAbsoluteLinkTarget = defaultAbsoluteLinkTarget;
	}

	public void setDest(File dest) {
		this.pageDestination = dest;
	}

	public void setEmitDoctype(boolean emitDoctype) {
		this.emitDoctype = emitDoctype;
	}

	public void setGeneratePageHeaders(boolean generatePageHeaders) {
		this.generatePageHeaders = generatePageHeaders;
	}

	public void setHelpPrefix(String helpPrefix) {
		this.helpPrefix = helpPrefix;
	}

	public void setHttpPassword(String httpPassword) {
		this.httpPassword = httpPassword;
	}

	public void setHttpUsername(String httpUsername) {
		this.httpUsername = httpUsername;
	}

	public void setLatexDpi(int latexDpi) {
		this.latexDpi = latexDpi;
	}

	public void setSuppressBuiltInCssStyles(boolean suppressBuiltInCssStyles) {
		this.suppressBuiltInCssStyles = suppressBuiltInCssStyles;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setUseInlineCssStyles(boolean useInlineCssStyles) {
		this.useInlineCssStyles = useInlineCssStyles;
	}

	public void setWikiBaseUrl(String wikiBaseUrl) {
		this.wikiBaseUrl = wikiBaseUrl;
	}

	public void setXhtmlStrict(boolean xhtmlStrict) {
		this.xhtmlStrict = xhtmlStrict;
	}

	protected void validateSettings() {
		if (pageDestination == null) {
			throw new IllegalArgumentException(Messages.getString("WikiToDocTask.SpecifyDestination")); //$NON-NLS-1$
		}
		if (pages.isEmpty()) {
			throw new IllegalArgumentException(Messages.getString("WikiToDocTask.SpecifyPagePaths")); //$NON-NLS-1$
		}
		for (Stylesheet stylesheet : stylesheets) {
			if (stylesheet.url == null && stylesheet.file == null) {
				throw new BuildException(Messages.getString("WikiToDocTask.MustSpecifyFileOrURL")); //$NON-NLS-1$
			}
			if (stylesheet.url != null && stylesheet.file != null) {
				throw new BuildException(Messages.getString("WikiToDocTask.CannotSpecifyBothFileAndURL")); //$NON-NLS-1$
			}
			if (stylesheet.file != null) {
				if (!stylesheet.file.exists()) {
					throw new BuildException(MessageFormat.format(Messages.getString("WikiToDocTask.StylesheetFileDoesNotExist"), //$NON-NLS-1$
							stylesheet.file));
				}
				if (!stylesheet.file.isFile()) {
					throw new BuildException(MessageFormat.format(Messages.getString("WikiToDocTask.StyleSheetFileIsNotAFile"), //$NON-NLS-1$
							stylesheet.file));
				}
				if (!stylesheet.file.canRead()) {
					throw new BuildException(MessageFormat.format(Messages.getString("WikiToDocTask.CannotReadStyleSheetFile"), stylesheet.file)); //$NON-NLS-1$
				}
			}
		}
		// Create a place for attachments
		attachmentDestination = this.pageDestination;
		if (attachmentPrefix != null) {
			attachmentDestination = new File(attachmentDestination, attachmentPrefix);
			if (!attachmentDestination.exists()) {
				if (!attachmentDestination.mkdirs()) {
					throw new BuildException(MessageFormat.format(Messages.getString("WikiToDocTask.CannotCreateAttachmentsFolder"), //$NON-NLS-1$
							pageDestination.getAbsolutePath()));
				}
			}
		}

		if (!pageDestination.exists()) {
			if (!pageDestination.mkdirs()) {
				throw new BuildException(MessageFormat.format(Messages.getString("WikiToDocTask.CannotCreateDestFolder"), //$NON-NLS-1$
						pageDestination.getAbsolutePath()));
			}
		}
	}

}

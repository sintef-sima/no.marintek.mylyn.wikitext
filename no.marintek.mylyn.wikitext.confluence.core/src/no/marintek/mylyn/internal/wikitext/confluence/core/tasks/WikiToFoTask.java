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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;

import no.marintek.mylyn.internal.wikitext.confluence.core.wsdl.beans.RemotePage;
import no.marintek.mylyn.wikitext.confluence.core.ExtendedConfluenceLanguage;

import org.apache.tools.ant.BuildException;
import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.XslfoDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.builder.XslfoDocumentBuilder.Configuration;
import org.eclipse.mylyn.wikitext.core.parser.builder.XslfoDocumentBuilder.Margins;
import org.eclipse.mylyn.wikitext.core.parser.markup.IdGenerationStrategy;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.eclipse.mylyn.wikitext.core.parser.outline.OutlineItem;
import org.eclipse.mylyn.wikitext.core.parser.outline.OutlineParser;

public class WikiToFoTask extends AbstractWikiConversionTask {

	/**
	 * As we will end up having multiple documents in the same structure we have to make sure that bookmark links to
	 * these pages will work. Hence we need to assign identifiers to these root items. This outline parser takes care of
	 * this.
	 * 
	 * @author Torkild U. Resheim
	 */
	class MultiPageOutlineParser extends OutlineParser {

		private Page rootPage;

		@Override
		public OutlineItem createRootItem() {
			String id = markupLanguage.getIdGenerationStrategy().generateId(rootPage.title);
			return createOutlineItem(null, 0, id, 0, 0, rootPage.title);
		}

		public void setRootPage(Page rootPage) {
			this.rootPage = rootPage;
		}
	}

	@Override
	protected MarkupLanguage createMarkupLanguage() throws BuildException {
		return markupLanguage;
	}

	/**
	 * We're using a custom version of the document builder as FOP really likes it's image URLs as absolute paths.
	 * 
	 * @author Torkild U. Resheim
	 */
	class NewBuilder extends XslfoDocumentBuilder {

		private final String foNamespaceUri = "http://www.w3.org/1999/XSL/Format"; //$NON-NLS-1$

		public NewBuilder(Writer out) {
			super(out);
		}

		@Override
		public void image(Attributes attributes, String url) {
			// <fo:external-graphic src="url(<path>/images/editor-command-help.png)" width="auto" height="auto"
			// content-width="auto" content-height="auto"/>
			writer.writeEmptyElement(foNamespaceUri, "external-graphic"); //$NON-NLS-1$
			writer.writeAttribute("src", String.format("url(%s)", makeImageUrl(url))); //$NON-NLS-1$//$NON-NLS-2$
			// TODO: Put back when Eclipse bugs have been applied
			// applyImageAttributes(attributes);
		}

		private String makeImageUrl(String url) {
			return new File(attachmentDestination, url).getAbsolutePath();
		}

		@Override
		public void link(Attributes attributes, String hrefOrHashName, String text) {
			writer.writeStartElement(foNamespaceUri, "basic-link"); //$NON-NLS-1$
			// SIMA: Always blue links
			writer.writeAttribute("color", "blue"); //$NON-NLS-1$ //$NON-NLS-2$
			String destinationUrl = makeUrlAbsolute(hrefOrHashName);
			//boolean internal = destinationUrl.startsWith("#"); //$NON-NLS-1$
			boolean internal = !destinationUrl.startsWith("http"); //$NON-NLS-1$
			if (internal) {
				writer.writeAttribute("internal-destination", destinationUrl); //$NON-NLS-1$
			} else {
				writer.writeAttribute("external-destination", String.format("url(%s)", destinationUrl)); //$NON-NLS-1$//$NON-NLS-2$
			}
			characters(text);
			writer.writeEndElement();// basic-link
			if (!internal) {
				characters(Messages.getString("XslfoDocumentBuilder.beforeLink")); //$NON-NLS-1$
				writer.writeStartElement(foNamespaceUri, "basic-link"); //$NON-NLS-1$
				writer.writeAttribute("external-destination", String.format("url(%s)", destinationUrl)); //$NON-NLS-1$//$NON-NLS-2$
				characters(destinationUrl);
				characters(Messages.getString("XslfoDocumentBuilder.afterLink")); //$NON-NLS-1$
				writer.writeEndElement(); // basic-link
			}
		}

	}

	private final SpecialIdGenerationStrategy strategy = new SpecialIdGenerationStrategy();

	/**
	 * @author Torkild U. Resheim
	 */
	public class SpecialConfluenceLanguage extends ExtendedConfluenceLanguage implements Cloneable {

		public SpecialConfluenceLanguage(int i) {
			super(i);
		}

		@Override
		public IdGenerationStrategy getIdGenerationStrategy() {
			return strategy;
		}
	}

	/**
	 * Confluence page links.
	 * 
	 * @author Torkild U. Resheim
	 */
	public class SpecialIdGenerationStrategy extends IdGenerationStrategy {

		@Override
		public String generateId(String headingText) {
			return headingText;
		}

	}

	private Configuration configuration;

	private File foFile;

	// Make sure we get 300dpi
	protected final MarkupLanguage markupLanguage = new SpecialConfluenceLanguage(300);

	StringBuilder sb = new StringBuilder();

	@Override
	protected OutlineItem computeOutline(Page page, String markupContent) {
		MultiPageOutlineParser outlineParser = new MultiPageOutlineParser();
		outlineParser.setMarkupLanguage(createMarkupLanguage());
		outlineParser.setRootPage(page);
		OutlineItem item = outlineParser.parse(markupContent);
		item.setLabel(page.getTitle());
		return item;
	}

	public Configuration createConfiguration() {
		configuration = new Configuration();
		return configuration;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	public String getFilenameFormat() {
		return "$1.fo"; //$NON-NLS-1$
	}

	File getFoFile() {
		return foFile;
	}

	@Override
	protected void markupToDoc(RemotePage page) {
		String markupContent = page.getContent();
		if (generatePageHeaders) {
			// Note a nice hack but we need to get the title in as a header
			markupContent = "\n\nh1. " + page.getTitle() + "\n" + markupContent; //$NON-NLS-1$ //$NON-NLS-2$
		}
		markupContent = postProcessPage(page, markupContent);
		sb.append(markupContent);
	}

	@Override
	protected void postProcess() {
		File pathDir = pageDestination;
		if (!pathDir.exists()) {
			if (!pathDir.mkdirs()) {
				throw new BuildException(MessageFormat.format(Messages.getString("WikiToDocTask.CannotCreateDestFolder"), //$NON-NLS-1$
						pathDir.getAbsolutePath()));
			}
		}
		// String fileName = computeFilename(page.getTitle());
		if (foFile == null) {
			foFile = new File(pathDir, "document.fo"); //$NON-NLS-1$
		}
		Writer writer = null;
		try {
			writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(foFile)), FILE_ENCODING);
		} catch (Exception e) {
			throw new BuildException(MessageFormat.format(Messages.getString("WikiToDocTask.CannotCreateOutputFile"), foFile, //$NON-NLS-1$
					e.getMessage()), e);
		}
		try {

			NewBuilder builder = new NewBuilder(writer);
			if (configuration != null) {
				builder.setConfiguration(configuration);
			}
			builder.getConfiguration().setPageMargins(new Margins(3f, 1f, 3f, 3f));
			builder.getConfiguration().setDate(DateFormat.getDateInstance().format(new Date()));
			builder.setOutline(rootItem);
			MarkupParser parser = new MarkupParser();
			parser.setMarkupLanguage(createMarkupLanguage());
			parser.setBuilder(builder);
			parser.parse(sb.toString());

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(new File(pathDir, "markup.confluence"));
				fos.write(sb.toString().getBytes());
				fos.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		} finally {
			try {
				writer.close();
			} catch (Exception e) {
				throw new BuildException(MessageFormat.format(Messages.getString("WikiToDocTask.CannotWriteOutputFile"), foFile, //$NON-NLS-1$
						e.getMessage()), e);
			}
		}
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public void setFoFile(File foFile) {
		this.foFile = foFile;
	}

}

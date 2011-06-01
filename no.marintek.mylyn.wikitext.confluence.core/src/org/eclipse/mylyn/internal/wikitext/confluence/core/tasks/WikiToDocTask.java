/*******************************************************************************
 * Copyright (c) 2011 MARINTEK and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MARINTEK - Initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.confluence.core.tasks;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import no.marintek.mylyn.internal.wikitext.confluence.core.wsdl.beans.RemotePage;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.eclipse.mylyn.wikitext.confluence.core.ConfluenceLanguage;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.eclipse.mylyn.wikitext.core.parser.markup.PageMapping;
import org.eclipse.mylyn.wikitext.core.parser.outline.OutlineItem;
import org.eclipse.mylyn.wikitext.core.parser.util.MarkupToEclipseToc;

/**
 * Generates Eclipse Help from a Confluence Wiki. The layout of the resulting documentation is similar to what you get
 * in Confluence. This type was based on {@link org.eclipse.mylyn.internal.wikitext.mediawiki.core.tasks.WikiToDocTask}.
 * 
 * @author Torkild U. Resheim, MARINTEK
 */
public class WikiToDocTask extends WikiConversionTask {
	protected File tocFile;

	/**
	 * Converts markup content to HTML.
	 * 
	 * @param markupLanguage
	 *            the markup dialect
	 * @param path
	 * @param markupContent
	 * @param pathNameToOutline
	 * @throws BuildException
	 */
	@Override
	protected void markupToDoc(RemotePage page) throws BuildException {
		File pathDir = dest;
		if (!pathDir.exists()) {
			if (!pathDir.mkdirs()) {
				throw new BuildException(MessageFormat.format(
						Messages.getString("WikiToDocTask.CannotCreateDestFolder"), //$NON-NLS-1$
						pathDir.getAbsolutePath()));
			}
		}
		String fileName = computeFilename(page.getTitle());
		File htmlOutputFile = new File(pathDir, fileName);
		Writer writer;
		try {
			writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(htmlOutputFile)),
					FILE_ENCODING);
		} catch (Exception e) {
			throw new BuildException(MessageFormat.format(
					Messages.getString("WikiToDocTask.CannotCreateOutputFile"), htmlOutputFile, //$NON-NLS-1$
					e.getMessage()), e);
		}
		try {
			HtmlDocumentBuilder builder = new HtmlDocumentBuilder(writer, formatOutput);
			for (Stylesheet stylesheet : stylesheets) {
				HtmlDocumentBuilder.Stylesheet builderStylesheet;

				if (stylesheet.url != null) {
					StringBuilder relativePath = new StringBuilder();
					File currentDest = pathDir;
					while (!currentDest.equals(dest)) {
						currentDest = currentDest.getParentFile();
						relativePath.append("../"); //$NON-NLS-1$
					}
					builderStylesheet = new HtmlDocumentBuilder.Stylesheet(relativePath + stylesheet.url);
				} else {
					builderStylesheet = new HtmlDocumentBuilder.Stylesheet(stylesheet.file);
				}
				builder.addCssStylesheet(builderStylesheet);

				if (!stylesheet.attributes.isEmpty()) {
					for (Map.Entry<String, String> attr : stylesheet.attributes.entrySet()) {
						builderStylesheet.getAttributes().put(attr.getKey(), attr.getValue());
					}
				}
			}

			builder.setTitle(page.getTitle());
			builder.setEmitDtd(emitDoctype);
			if (emitDoctype && htmlDoctype != null) {
				builder.setHtmlDtd(htmlDoctype);
			}
			builder.setUseInlineStyles(useInlineCssStyles);
			builder.setSuppressBuiltInStyles(suppressBuiltInCssStyles);
			builder.setLinkRel(linkRel);
			builder.setDefaultAbsoluteLinkTarget(defaultAbsoluteLinkTarget);
			builder.setPrependImagePrefix(attachmentPrefix);
			builder.setXhtmlStrict(xhtmlStrict);

			MarkupLanguage markupLanguageClone = createMarkupLanguage().clone();
			if (markupLanguageClone instanceof ConfluenceLanguage) {
				((ConfluenceLanguage) markupLanguageClone).setPageMapping(new PathPageMapping(page.getTitle(), pages));
			}

			MarkupParser parser = new MarkupParser();
			parser.setMarkupLanguage(markupLanguageClone);
			parser.setBuilder(builder);
			String markupContent = page.getContent();
			if (generatePageHeaders) {
				// Note a nice hack but we need to get the title in as a header
				markupContent = "h1. " + page.getTitle() + "\n" + markupContent; //$NON-NLS-1$ //$NON-NLS-2$
			}
			markupContent = postProcessMarkup(page, markupContent);
			parser.parse(markupContent);

		} finally {
			try {
				writer.close();
			} catch (Exception e) {
				throw new BuildException(MessageFormat.format(
						Messages.getString("WikiToDocTask.CannotWriteOutputFile"), htmlOutputFile, //$NON-NLS-1$
						e.getMessage()), e);
			}
		}
	}

	protected class PathPageMapping implements PageMapping {

		protected final String currentPath;

		protected final Map<String, Page> nameToPath = new HashMap<String, Page>();

		protected PathPageMapping(String currentPath, Collection<Page> paths) {
			this.currentPath = currentPath;
			for (Page path : pages) {
				nameToPath.put(path.title, path);
			}
		}

		@Override
		public String mapPageNameToHref(String pageName) {
			Matcher matcher = PAGE_NAME_PATTERN.matcher(pageName);
			if (matcher.matches()) {
				String name = matcher.group(1);
				String hashId = matcher.group(2);

				if (currentPath.equals(name)) {
					return hashId == null ? "#" : hashId; //$NON-NLS-1$
				}
				//FIXME: This is incorrect!
				name = createMarkupLanguage().getIdGenerationStrategy().generateId(name);
				File destDir = dest;
				File currentDest = dest;
				StringBuilder relativePath = new StringBuilder();
				while (!currentDest.equals(dest)) {
					currentDest = currentDest.getParentFile();
					relativePath.append("../"); //$NON-NLS-1$
				}
				String relativeDir = destDir.getAbsolutePath().substring(dest.getAbsolutePath().length());
				if (relativeDir.startsWith("/")) { //$NON-NLS-1$
					relativeDir = relativeDir.substring(1);
				}
				relativePath.append(relativeDir);
				if (relativePath.length() > 0 && relativePath.charAt(relativePath.length() - 1) != '/') {
					relativePath.append('/');
				}
				String fileName = computeFilename(name);

				if (hashId != null) {
					relativePath.append(fileName);
					relativePath.append('#');
					relativePath.append(hashId);
				} else {
					relativePath.append(fileName);
				}
				return relativePath.toString();
			}
			return null;
		}
	}

	@Override
	public String getFilenameFormat() {
		return "$1.html"; //$NON-NLS-1$
	}

	@Override
	protected void postProcess() {
		createToc();
	}

	/**
	 * Computes the path to a file relative to the table of contents file.
	 * 
	 * @param item
	 *            the outline item to consider
	 * @return a path
	 */
	@Override
	protected String computeTocRelativeFile(OutlineItem item) {
		File pathDestDir = dest;
		File tocParentFile = tocFile.getParentFile();
		String prefix = computePrefixPath(pathDestDir, tocParentFile);
		String relativePath = prefix + '/' + computeFilename(item.getLabel());
		relativePath = relativePath.replace('\\', '/');
		if (helpPrefix != null) {
			String helpPath = helpPrefix;
			helpPath = helpPath.replace('\\', '/');
			if (!helpPath.endsWith("/")) { //$NON-NLS-1$
				helpPath += "/"; //$NON-NLS-1$
			}
			relativePath = helpPath + relativePath;
		}
		relativePath = relativePath.replaceAll("/{2,}", "/"); //$NON-NLS-1$//$NON-NLS-2$
		return relativePath;
	}

	protected void createToc() {
		if (tocFile == null) {
			tocFile = new File(dest, "toc.xml"); //$NON-NLS-1$
		}
		getProject().log(
				MessageFormat.format(Messages.getString("WikiToDocTask.WritingTOC"), tocFile), Project.MSG_INFO); //$NON-NLS-1$

		// Make sure we get the correct path to the item.
		MarkupToEclipseToc markupToEclipseToc = new MarkupToEclipseToc() {

			@Override
			protected String computeFile(OutlineItem item) {
				return item.getResourcePath();
			}

		};
		markupToEclipseToc.setBookTitle(rootItem.getLabel());
		String tocContents = markupToEclipseToc.createToc(rootItem);

		try {
			Writer writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(tocFile)), "UTF-8"); //$NON-NLS-1$
			try {
				writer.write(tocContents);
			} finally {
				writer.close();
			}
		} catch (IOException e) {
			String message = MessageFormat.format(
					Messages.getString("WikiToDocTask.CannotWrite"), tocFile, e.getMessage()); //$NON-NLS-1$
			throw new BuildException(message, e);
		}
	}

	public File getTocFile() {
		return tocFile;
	}

	public void setTocFile(File tocFile) {
		this.tocFile = tocFile;
	}

}

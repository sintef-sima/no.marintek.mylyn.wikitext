/*******************************************************************************
 * Copyright (c) 2012 MARINTEK and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild U. Resheim, MARINTEK - Initial API and implementation
 *******************************************************************************/

package no.marintek.mylyn.internal.wikitext.confluence.core.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.mylyn.docs.epub.core.EPUB;
import org.eclipse.mylyn.docs.epub.core.ILogger;
import org.eclipse.mylyn.docs.epub.core.OPS2Publication;
import org.eclipse.mylyn.wikitext.core.parser.outline.OutlineItem;

/**
 * @author Torkild U. Resheim, MARINTEK
 */
public class WikiToEPUBTask extends WikiToDocTask {

	public WikiToEPUBTask() {
		emitDoctype = true;
		xhtmlStrict = true;
		htmlDoctype = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">";
		// Allow inline CSS
		useInlineCssStyles = true;
		suppressBuiltInCssStyles = false;
		title = "<unspecified>";
	}

	@Override
	protected String computeRelativeFile(OutlineItem item) {
		File pathDestDir = dest;
		File tocParentFile = dest;
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

	/**
	 * Using the table of contents – recursively add items to the EPUB. The order will be the same as the table of
	 * contents order.
	 * 
	 * @param oebps
	 *            the OPS publication
	 * @param item
	 *            the root item
	 */
	private void addItems(OPS2Publication oebps, OutlineItem item) {
		// The table of contents may contain the same page multiple times so we need to ensure that it is only added
		// once.
		List<OutlineItem> items = item.getChildren();
		for (OutlineItem outlineItem : items) {
			File itemFile = new File(dest.getAbsolutePath() + File.separator + outlineItem.getResourcePath());
			// Some top level items may have level 0
			if (outlineItem.getLevel() <= 1) {
				oebps.addItem(itemFile);
			}
			addItems(oebps, outlineItem);
		}
	}

	@Override
	public String getFilenameFormat() {
		return "$1.xhtml"; //$NON-NLS-1$
	}

	ArrayList<File> attachments = new ArrayList<File>();

	@Override
	protected void processAttachment(File file) {
		attachments.add(file);
	}

	@Override
	protected void postProcess() {
		EPUB epub = new EPUB();
		OPS2Publication oebps = new OPS2Publication(new ILogger() {

			@Override
			public void log(String message) {
				System.out.println(message);
			}

			@Override
			public void log(String message, Severity severity) {
				System.out.println(message);
			}
		});

		oebps.addTitle(null, null, getTitle());

		addItems(oebps, rootItem);
		for (File attachment : attachments) {
			if (!attachment.getName().endsWith(".xls")) {
				String mimetype = null;
				if (attachment.getName().endsWith(".mp4")) {
					mimetype = "video/mp4";
				}
				oebps.addItem(null, null, attachment, "images", mimetype, false, false, true);
			}
		}
		oebps.setGenerateToc(true);
		epub.add(oebps);
		try {
			epub.pack(new File(dest.getParentFile().getAbsolutePath() + File.separator + getTitle() + ".epub"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

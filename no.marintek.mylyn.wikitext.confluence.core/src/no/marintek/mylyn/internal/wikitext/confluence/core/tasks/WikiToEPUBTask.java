/*******************************************************************************
 * Copyright (c) 2012, 2013 MARINTEK and others.
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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.eclipse.mylyn.docs.epub.ant.core.AntLogger;
import org.eclipse.mylyn.docs.epub.ant.core.ContributorType;
import org.eclipse.mylyn.docs.epub.ant.core.CoverType;
import org.eclipse.mylyn.docs.epub.ant.core.CoverageType;
import org.eclipse.mylyn.docs.epub.ant.core.CreatorType;
import org.eclipse.mylyn.docs.epub.ant.core.DateType;
import org.eclipse.mylyn.docs.epub.ant.core.FileSetType;
import org.eclipse.mylyn.docs.epub.ant.core.FormatType;
import org.eclipse.mylyn.docs.epub.ant.core.IdentifierType;
import org.eclipse.mylyn.docs.epub.ant.core.ItemType;
import org.eclipse.mylyn.docs.epub.ant.core.LanguageType;
import org.eclipse.mylyn.docs.epub.ant.core.MetaType;
import org.eclipse.mylyn.docs.epub.ant.core.PublisherType;
import org.eclipse.mylyn.docs.epub.ant.core.ReferenceType;
import org.eclipse.mylyn.docs.epub.ant.core.RelationType;
import org.eclipse.mylyn.docs.epub.ant.core.RightsType;
import org.eclipse.mylyn.docs.epub.ant.core.SourceType;
import org.eclipse.mylyn.docs.epub.ant.core.SubjectType;
import org.eclipse.mylyn.docs.epub.ant.core.TitleType;
import org.eclipse.mylyn.docs.epub.ant.core.TocType;
import org.eclipse.mylyn.docs.epub.core.EPUB;
import org.eclipse.mylyn.docs.epub.core.OPS2Publication;
import org.eclipse.mylyn.docs.epub.opf.Role;
import org.eclipse.mylyn.docs.epub.opf.Type;
import org.eclipse.mylyn.wikitext.core.parser.outline.OutlineItem;

/**
 * @author Torkild U. Resheim, MARINTEK
 */
public class WikiToEPUBTask extends WikiToDocTask {

	ArrayList<File> attachments = new ArrayList<File>();

	private File epubFile;

	private ArrayList<FileSetType> filesets = null;

	private AntLogger logger;

	private final OPS2Publication oebps;

	private TocType toc = null;

	private File workingFolder;

	public WikiToEPUBTask() {
		emitDoctype = true;
		xhtmlStrict = true;
		htmlDoctype = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">";
		// Allow inline CSS
		useInlineCssStyles = true;
		suppressBuiltInCssStyles = false;
		title = "<unspecified>";
		oebps = new OPS2Publication(new AntLogger(this));
		filesets = new ArrayList<FileSetType>();
	}

	public void addConfiguredContributor(ContributorType item) {
		if (item.role == null) {
			oebps.addContributor(item.id, item.lang, item.name, null, item.fileAs);
		} else {
			oebps.addContributor(item.id, item.lang, item.name, Role.get(item.role), item.fileAs);
		}
	}

	public void addConfiguredCover(CoverType item) {
		oebps.setCover(new File(item.image), item.value);
	}

	public void addConfiguredCoverage(CoverageType coverage) {
		oebps.addCoverage(coverage.id, coverage.lang, coverage.text);
	}

	public void addConfiguredCreator(CreatorType item) {
		if (item.role == null) {
			oebps.addCreator(item.id, item.lang, item.name, null, item.fileAs);
		} else {
			oebps.addCreator(item.id, item.lang, item.name, Role.get(item.role), item.fileAs);
		}
	}

	/**
	 * @ant.not-required
	 */
	public void addConfiguredDate(DateType item) {
		oebps.addDate(item.id, item.date, item.event);
	}

	/**
	 * @ant.not-required Add fileset to publication.
	 */
	public void addConfiguredFileSet(FileSetType fs) {
		filesets.add(fs);
	}

	/**
	 * @ant.not-required
	 */
	public void addConfiguredFormat(FormatType format) {
		oebps.addFormat(format.id, format.text);
	}

	/**
	 * @ant.required
	 */
	public void addConfiguredIdentifier(IdentifierType identifier) {
		oebps.addIdentifier(identifier.id, identifier.scheme, identifier.value);
	}

	/**
	 * @ant.required
	 */
	public void addConfiguredItem(ItemType item) {
		oebps.addItem(item.id, item.lang, item.file, item.dest, item.type, item.spine, item.linear, item.noToc);
	}

	/**
	 * @ant.required
	 */
	public void addConfiguredLanguage(LanguageType language) {
		oebps.addLanguage(language.id, language.code);
	}

	public void addConfiguredMeta(MetaType item) {
		oebps.addMeta(item.name, item.content);
	}

	public void addConfiguredPublisher(PublisherType publisher) {
		oebps.addPublisher(publisher.id, publisher.lang, publisher.text);
	}

	public void addConfiguredReference(ReferenceType reference) {
		Type type = Type.get(reference.type);
		if (type == null) {
			throw new BuildException("Unknown reference type " + reference.type); //$NON-NLS-1$
		}
		oebps.addReference(reference.href, reference.title, reference.type);
	}

	public void addConfiguredRelation(RelationType relation) {
		oebps.addRelation(relation.id, relation.lang, relation.text);
	}

	public void addConfiguredRights(RightsType rights) {
		oebps.addRights(rights.id, rights.lang, rights.text);
	}

	public void addConfiguredSource(SourceType source) {
		oebps.addSource(source.id, source.lang, source.text);
	}

	public void addConfiguredSubject(SubjectType subject) {
		oebps.addSubject(subject.id, subject.lang, subject.text);
	}

	/**
	 * @ant.required
	 */
	public void addConfiguredTitle(TitleType title) {
		oebps.addTitle(title.id, title.lang, title.text);
	}

	public void addConfiguredToc(TocType toc) {
		if (this.toc != null) {
			throw new BuildException("Only one table of contents (toc) declaration is allowed."); //$NON-NLS-1$
		}
		this.toc = toc;
	}

	public void addConfiguredType(org.eclipse.mylyn.docs.epub.ant.core.TypeType type) {
		oebps.addType(type.id, type.text);
	}

	private void addFilesets() {
		for (FileSetType fs : filesets) {
			final File fsDir = fs.getDir(getProject());
			if (fsDir == null) {
				throw new BuildException("File or Resource without directory or file specified"); //$NON-NLS-1$
			} else if (!fsDir.isDirectory()) {
				throw new BuildException("Directory does not exist:" + fsDir); //$NON-NLS-1$
			}
			DirectoryScanner ds = fs.getDirectoryScanner(getProject());
			String[] includedFiles = ds.getIncludedFiles();
			for (String includedFile : includedFiles) {
				String filename = includedFile.replace('\\', '/');
				filename = filename.substring(filename.lastIndexOf('/') + 1);
				File base = ds.getBasedir();
				File found = new File(base, includedFile);
				oebps.addItem(null, fs.lang, found, fs.dest, null, false, true, false);
			}

		}

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
			File itemFile = new File(pageDestination.getAbsolutePath() + File.separator + outlineItem.getResourcePath());
			// Some top level items may have level 0
			if (outlineItem.getLevel() <= 1) {
				oebps.addItem(itemFile);
			}
			addItems(oebps, outlineItem);
		}
	}

	@Override
	protected String computeRelativeFile(OutlineItem item) {
		File pathDestDir = pageDestination;
		File tocParentFile = pageDestination;
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

	@Override
	public String getFilenameFormat() {
		return "$1.xhtml"; //$NON-NLS-1$
	}

	@Override
	protected void postProcess() {
		try {
			EPUB epub = new EPUB(logger);
			// Add all downloaded pages
			addItems(oebps, rootItem);
			// Add all downloaded attachments
			for (File attachment : attachments) {
				if (!attachment.getName().endsWith(".xls")) {
					String mimetype = null;
					if (attachment.getName().endsWith(".mp4")) {
						mimetype = "video/mp4";
					}
					oebps.addItem(null, null, attachment, "images", mimetype, false, false, true);
				}
			}
			// Add items added with "fileset" element
			addFilesets();
			epub.add(oebps);
			if (workingFolder == null) {
				epub.pack(epubFile);
			} else {
				epub.pack(epubFile, workingFolder);
			}
		} catch (Exception e) {
			throw new BuildException(e);
		}
	}

	// EPUB

	@Override
	protected void processAttachment(File file) {
		attachments.add(file);
	}

	/**
	 * @param file
	 *            path to the generated EPUB file.
	 */
	public void setFile(File file) {
		log("Building EPUB file "+file.getAbsolutePath());
		this.epubFile = file;
	}

	public void setIdentifierId(String identifierId) {
		oebps.setIdentifierId(identifierId);
	}

	/**
	 * @ant.not-required Automatically add referenced resources.
	 */
	public void setIncludeReferenced(boolean automatic) {
		oebps.setIncludeReferencedResources(automatic);
	}

	public void setWorkingFolder(File workingFolder) {
		this.workingFolder = workingFolder;
	}
}

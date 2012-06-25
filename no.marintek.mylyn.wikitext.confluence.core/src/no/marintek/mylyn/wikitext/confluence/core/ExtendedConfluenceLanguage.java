/*******************************************************************************
 * Copyright (c) 2011,2012 MARINTEK and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild U. Resheim, MARINTEK - initial API and implementation
 *******************************************************************************/
package no.marintek.mylyn.wikitext.confluence.core;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import no.marintek.mylyn.internal.wikitext.confluence.core.block.LaTexBlock;
import no.marintek.mylyn.internal.wikitext.confluence.core.block.TableBlock;
import no.marintek.mylyn.internal.wikitext.confluence.core.phrase.AttachmentPhraseModifier;
import no.marintek.mylyn.internal.wikitext.confluence.core.phrase.MappingHyperlinkPhraseModifier;

import org.eclipse.mylyn.internal.wikitext.confluence.core.block.CodeBlock;
import org.eclipse.mylyn.internal.wikitext.confluence.core.block.ColorBlock;
import org.eclipse.mylyn.internal.wikitext.confluence.core.block.ExtendedPreformattedBlock;
import org.eclipse.mylyn.internal.wikitext.confluence.core.block.ExtendedQuoteBlock;
import org.eclipse.mylyn.internal.wikitext.confluence.core.block.HeadingBlock;
import org.eclipse.mylyn.internal.wikitext.confluence.core.block.ListBlock;
import org.eclipse.mylyn.internal.wikitext.confluence.core.block.QuoteBlock;
import org.eclipse.mylyn.internal.wikitext.confluence.core.block.TableOfContentsBlock;
import org.eclipse.mylyn.internal.wikitext.confluence.core.block.TextBoxBlock;
import org.eclipse.mylyn.internal.wikitext.confluence.core.phrase.ColorPhraseModifier;
import org.eclipse.mylyn.internal.wikitext.confluence.core.phrase.ConfluenceWrappedPhraseModifier;
import org.eclipse.mylyn.internal.wikitext.confluence.core.phrase.EmphasisPhraseModifier;
import org.eclipse.mylyn.internal.wikitext.confluence.core.phrase.SimplePhraseModifier;
import org.eclipse.mylyn.internal.wikitext.confluence.core.phrase.SimpleWrappedPhraseModifier;
import org.eclipse.mylyn.wikitext.confluence.core.ConfluenceLanguage;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.SpanType;
import org.eclipse.mylyn.wikitext.core.parser.markup.Block;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;

@SuppressWarnings("restriction")
public class ExtendedConfluenceLanguage extends ConfluenceLanguage {

	private int latexDpi = 220;

	public ExtendedConfluenceLanguage() {
		setName("ExtendedConfluence"); //$NON-NLS-1$
	}

	public ExtendedConfluenceLanguage(int latexDpi) {
		this.latexDpi = latexDpi;
	}

	@Override
	protected void addStandardBlocks(List<Block> blocks, List<Block> paragraphBreakingBlocks) {
		// IMPORTANT NOTE: Most items below have order dependencies. DO NOT REORDER ITEMS BELOW!!

		HeadingBlock headingBlock = new HeadingBlock();
		blocks.add(headingBlock);
		paragraphBreakingBlocks.add(headingBlock);
		getNestedBlocks().add(headingBlock);
		ListBlock listBlock = new ListBlock();
		blocks.add(listBlock);
		paragraphBreakingBlocks.add(listBlock);
		getNestedBlocks().add(listBlock);
		blocks.add(new QuoteBlock());
		TableBlock tableBlock = new TableBlock();
		blocks.add(tableBlock);
		paragraphBreakingBlocks.add(tableBlock);
		getNestedBlocks().add(tableBlock);
		ExtendedQuoteBlock quoteBlock = new ExtendedQuoteBlock();
		blocks.add(quoteBlock);
		paragraphBreakingBlocks.add(quoteBlock);
		ExtendedPreformattedBlock noformatBlock = new ExtendedPreformattedBlock();
		blocks.add(noformatBlock);
		paragraphBreakingBlocks.add(noformatBlock);

		blocks.add(new TextBoxBlock(BlockType.PANEL, "panel")); //$NON-NLS-1$
		blocks.add(new TextBoxBlock(BlockType.NOTE, "note")); //$NON-NLS-1$
		blocks.add(new TextBoxBlock(BlockType.INFORMATION, "info")); //$NON-NLS-1$
		blocks.add(new TextBoxBlock(BlockType.WARNING, "warning")); //$NON-NLS-1$
		blocks.add(new TextBoxBlock(BlockType.TIP, "tip")); //$NON-NLS-1$
		CodeBlock codeBlock = new CodeBlock();
		blocks.add(codeBlock);
		paragraphBreakingBlocks.add(codeBlock);
		blocks.add(new TableOfContentsBlock());
		ColorBlock colorBlock = new ColorBlock();
		blocks.add(colorBlock);
		paragraphBreakingBlocks.add(colorBlock);
		blocks.add(new LaTexBlock(latexDpi));
	}

	private PageMapping pageMapping;

	@Override
	protected void addStandardPhraseModifiers(PatternBasedSyntax syntax) {
		syntax.beginGroup("(?:(?<=[\\s\\.,\\\"'?!;:\\)\\(\\[\\]])|^)(?:", 0); //$NON-NLS-1$
		syntax.add(new MappingHyperlinkPhraseModifier());
		// phraseModifierSyntax.add(new HyperlinkPhraseModifier());
		syntax.add(new SimplePhraseModifier("*", SpanType.STRONG, true)); //$NON-NLS-1$
		syntax.add(new EmphasisPhraseModifier());
		syntax.add(new SimplePhraseModifier("??", SpanType.CITATION, true)); //$NON-NLS-1$
		syntax.add(new SimplePhraseModifier("-", SpanType.DELETED, true)); //$NON-NLS-1$
		syntax.add(new SimplePhraseModifier("+", SpanType.UNDERLINED, true)); //$NON-NLS-1$
		syntax.add(new SimplePhraseModifier("^", SpanType.SUPERSCRIPT, false)); //$NON-NLS-1$
		syntax.add(new SimplePhraseModifier("~", SpanType.SUBSCRIPT, false)); //$NON-NLS-1$
		syntax.add(new SimpleWrappedPhraseModifier("{{", "}}", SpanType.MONOSPACE, false)); //$NON-NLS-1$ //$NON-NLS-2$
		syntax.add(new ConfluenceWrappedPhraseModifier("{quote}", SpanType.QUOTE, true)); //$NON-NLS-1$ 
		syntax.add(new ColorPhraseModifier());
		syntax.add(new AttachmentPhraseModifier());
		syntax.endGroup(")(?=\\W|$)", 0); //$NON-NLS-1$
	}

	/**
	 * Convert a page name to an href to the page.
	 * 
	 * @param pageName
	 *            the name of the page to target
	 * @return the href to access the page
	 * @see MarkupLanguage#getInternalLinkPattern()
	 */
	public String toInternalHref(String pageName) {
		return mapPageNameToHref(pageName);
	}

	/**
	 * Used to map a page name to an URL for use in a <i>href</i> attribute.
	 * 
	 * @param pageName
	 *            the page name
	 * @return the URL
	 */
	protected String mapPageNameToHref(String pageName) {
		if (pageMapping != null) {
			String mapping = pageMapping.mapPageNameToHref(pageName);
			if (mapping != null) {
				return mapping;
			}
		}
		return MessageFormat.format(super.internalLinkPattern, pageName);
	}

	public PageMapping getPageMapping() {
		return pageMapping;
	}

	public void setPageMapping(PageMapping pageMapping) {
		this.pageMapping = pageMapping;
	}

	private static File resourcesPath;

	public void setResourcesPath(File path) {
		resourcesPath = path;
	}

	public File getResourcesPath() {
		return resourcesPath;
	}

}

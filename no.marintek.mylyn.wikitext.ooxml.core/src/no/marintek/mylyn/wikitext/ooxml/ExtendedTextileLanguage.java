/*******************************************************************************
 * Copyright (c) 2018 Sintef Ocean
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild U. Resheim - initial API and implementation
 *******************************************************************************/
package no.marintek.mylyn.wikitext.ooxml;

import java.util.List;

import org.eclipse.mylyn.wikitext.parser.markup.Block;
import org.eclipse.mylyn.wikitext.textile.TextileLanguage;

/**
 * Simple extension to the Textile markup language that adds support for inline
 * and math blocks.
 * 
 * @author Torkild Ulvøy Resheim, Itema AS
 */
public class ExtendedTextileLanguage extends TextileLanguage {

	@Override
	protected void addBlockExtensions(List<Block> blocks, List<Block> paragraphBreakingBlocks) {
		super.addBlockExtensions(blocks, paragraphBreakingBlocks);
		blocks.add(new MathBlock());
	}

	@Override
	protected void addPhraseModifierExtensions(PatternBasedSyntax phraseModifierSyntax) {
		super.addPhraseModifierExtensions(phraseModifierSyntax);
		phraseModifierSyntax.add(new InlineMathPhraseModifier());
	}

}

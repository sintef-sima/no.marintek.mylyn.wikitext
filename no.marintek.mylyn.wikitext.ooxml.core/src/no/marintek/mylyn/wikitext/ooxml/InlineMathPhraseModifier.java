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

import org.eclipse.mylyn.wikitext.parser.DocumentBuilder.SpanType;
import org.eclipse.mylyn.wikitext.parser.markup.PatternBasedElement;
import org.eclipse.mylyn.wikitext.parser.markup.PatternBasedElementProcessor;

/**
 * A token for inline LaTeX formula spans which start with "$\" and ends with "$".
 *
 * @author Torkild Ulvøy Resheim, Itema AS
 */
public class InlineMathPhraseModifier extends PatternBasedElement {

	private static class MathPhraseModifierProcessor extends PatternBasedElementProcessor {

		@Override
		public void emit() {
			getBuilder().beginSpan(SpanType.SPAN, new MathAttributes());
			getMarkupLanguage().emitMarkupText(parser, state, getContent(this));
			getBuilder().endSpan();
		}

	}

	protected static String getContent(PatternBasedElementProcessor processor) {
		return processor.group(1);
	}

	@Override
	protected String getPattern(int groupOffset) {
		// make sure to not match $$ which would be in conflict with the math block.
		return "\\$(?!\\$)(([\\\\][^\\\\$]+)+)\\$(?!\\$)";
	}

	@Override
	protected int getPatternGroupCount() {
		return 2;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new MathPhraseModifierProcessor();
	}

}

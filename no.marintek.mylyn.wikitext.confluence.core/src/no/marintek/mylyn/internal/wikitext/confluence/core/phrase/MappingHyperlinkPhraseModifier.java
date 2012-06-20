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
package no.marintek.mylyn.internal.wikitext.confluence.core.phrase;

import no.marintek.mylyn.wikitext.confluence.core.ExtendedConfluenceLanguage;

import org.eclipse.mylyn.internal.wikitext.confluence.core.phrase.HyperlinkPhraseModifier;
import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.SpanType;
import org.eclipse.mylyn.wikitext.core.parser.LinkAttributes;
import org.eclipse.mylyn.wikitext.core.parser.markup.PatternBasedElementProcessor;

@SuppressWarnings("restriction")
public class MappingHyperlinkPhraseModifier extends HyperlinkPhraseModifier {

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new HyperlinkPhraseModifierProcessor();
	}

	private static class HyperlinkPhraseModifierProcessor extends PatternBasedElementProcessor {
		@Override
		public void emit() {
			System.err.println("--------");
			String text = group(1);
			String linkComposite = group(2);
			String[] parts = linkComposite.split("\\s*\\|\\s*"); //$NON-NLS-1$
			if (parts.length == 0) {
				// can happen if linkComposite is ' |', see bug 290434
			} else {
				if (text != null) {
					text = text.trim();
				}
				String href = parts[0];
				if (href != null) {
					href = href.trim();
				}
				String tip = parts.length > 1 ? parts[1] : null;
				if (tip != null) {
					tip = tip.trim();
				}
				if (href != null && !href.startsWith("http")) { //$NON-NLS-1$
					String pageRef = ((ExtendedConfluenceLanguage) getMarkupLanguage()).toInternalHref(href);
					if (pageRef != null) {
						// We don't have an alias so we need to use the HREF for
						// link label.
						if (text == null) {
							text = href;
						}
						href = pageRef;
					}
				}
				if (text == null || text.length() == 0) {
					text = href;
					if (text != null) {
						if (text.length() > 0 && text.charAt(0) == '#') {
							text = text.substring(1);
						}
						Attributes attributes = new LinkAttributes();
						attributes.setTitle(tip);
						getBuilder().link(attributes, href, text);
					}
				} else {
					LinkAttributes attributes = new LinkAttributes();
					attributes.setTitle(tip);
					attributes.setHref(href);
					getBuilder().beginSpan(SpanType.LINK, attributes);

					getMarkupLanguage().emitMarkupLine(parser, state, start(1), text, 0);

					getBuilder().endSpan();
				}
			}
		}
	}
}

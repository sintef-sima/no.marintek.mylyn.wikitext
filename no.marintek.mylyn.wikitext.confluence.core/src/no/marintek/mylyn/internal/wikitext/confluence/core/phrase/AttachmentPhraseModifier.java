/*******************************************************************************
 * Copyright (c) 2007, 2010 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/
package no.marintek.mylyn.internal.wikitext.confluence.core.phrase;


import org.eclipse.mylyn.internal.wikitext.confluence.core.util.Options;
import org.eclipse.mylyn.internal.wikitext.confluence.core.util.Options.Handler;
import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.VideoAttributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType;
import org.eclipse.mylyn.wikitext.core.parser.ImageAttributes;
import org.eclipse.mylyn.wikitext.core.parser.ImageAttributes.Align;
import org.eclipse.mylyn.wikitext.core.parser.builder.ExtendedHtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.markup.PatternBasedElement;
import org.eclipse.mylyn.wikitext.core.parser.markup.PatternBasedElementProcessor;

@SuppressWarnings("restriction")
public class AttachmentPhraseModifier extends PatternBasedElement {

	protected static final int CONTENT_GROUP = 1;

	private static final int OPTIONS_GROUP = 2;

	@Override
	protected String getPattern(int groupOffset) {

		return "!([^\\|!\\s]+)(?:\\|([^!]*))?!"; //$NON-NLS-1$
	}

	@Override
	protected int getPatternGroupCount() {
		return 2;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new AttachmentPhraseModifierProcessor();
	}

	private static class AttachmentPhraseModifierProcessor extends PatternBasedElementProcessor {

		@Override
		public void emit() {
			String imageUrl = group(CONTENT_GROUP);
			String imageOptions = group(OPTIONS_GROUP);
			if (imageUrl.endsWith(".mp4")) {
				handleVideo(imageUrl, imageOptions);
			} else {
				handleImage(imageUrl, imageOptions);
			}
		}

		private void handleVideo(String imageUrl, String options) {
			final VideoAttributes attributes = new VideoAttributes();
			attributes.setAutoplay(false);
			attributes.setControls(true);
			attributes.setWidth(100);
			attributes.setHeightPercentage(true);
			((ExtendedHtmlDocumentBuilder) builder).video(attributes, imageUrl);
		}

		private void handleImage(String url, String options) {
			final ImageAttributes attributes = new ImageAttributes();
			if (options != null) {
				Options.parseOptions(options, new Handler() {
					public void setOption(String key, String value) {
						if ("alt".equalsIgnoreCase(key)) { //$NON-NLS-1$
							attributes.setAlt(value);
						} else if ("align".equalsIgnoreCase(key)) { //$NON-NLS-1$
							if ("middle".equalsIgnoreCase(value)) { //$NON-NLS-1$
								attributes.setAlign(Align.Middle);
							} else if ("left".equalsIgnoreCase(value)) { //$NON-NLS-1$
								attributes.setAlign(Align.Left);
							} else if ("right".equalsIgnoreCase(value)) { //$NON-NLS-1$
								attributes.setAlign(Align.Right);
							} else if ("center".equalsIgnoreCase(value)) { //$NON-NLS-1$
								attributes.setAlign(Align.Center);
							}
						} else {
							try {
								if ("border".equalsIgnoreCase(key)) { //$NON-NLS-1$
									attributes.setBorder(Integer.parseInt(value));
								} else if ("height".equalsIgnoreCase(key)) { //$NON-NLS-1$
									attributes.setHeight(Integer.parseInt(value));
								} else if ("width".equalsIgnoreCase(key)) { //$NON-NLS-1$
									attributes.setWidth(Integer.parseInt(value));
								}
							} catch (NumberFormatException e) {
								// ignore
							}
						}
					}

					public void setOption(String option) {
						// ignore
					}
				});
			}
			if (attributes.getAlign() == Align.Center) {
				// bug 293573: confluence centers images using div
				Attributes divAttributes = new Attributes();
				divAttributes.setCssStyle("text-align: center;"); //$NON-NLS-1$
				builder.beginBlock(BlockType.DIV, divAttributes);
				attributes.setAlign(null);
				builder.image(attributes, url);
				builder.endBlock();
			} else {
				builder.image(attributes, url);
			}
		}
	}

}

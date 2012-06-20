package org.eclipse.mylyn.wikitext.core.parser.builder;

import java.io.Writer;
import java.util.regex.Pattern;


import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.VideoAttributes;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.util.XmlStreamWriter;

public class ExtendedHtmlDocumentBuilder extends HtmlDocumentBuilder {

	private static final Pattern ABSOLUTE_URL_PATTERN = Pattern.compile("[a-zA-Z]{3,8}://?.*"); //$NON-NLS-1$

	public ExtendedHtmlDocumentBuilder(Writer out) {
		super(out);
	}

	public ExtendedHtmlDocumentBuilder(Writer out, boolean formatting) {
		super(out, formatting);
	}

	@Override
	protected XmlStreamWriter createXmlStreamWriter(Writer out) {
		return super.createFormattingXmlStreamWriter(out);
	}

	public void video(Attributes attributes, String url) {
		writer.writeEmptyElement(getHtmlNsUri(), "video"); //$NON-NLS-1$
		applyVideoAttributes(attributes);
		url = prependImageUrl(url);
		writer.writeAttribute("src", makeUrlAbsolute(url)); //$NON-NLS-1$
	}

	private void applyVideoAttributes(Attributes attributes) {
		// applyAttributes(attributes);
		if (attributes instanceof VideoAttributes) {
			VideoAttributes videoAttributes = (VideoAttributes) attributes;
			writer.writeAttribute("autoplay", Boolean.toString(videoAttributes.isAutoplay()));
			writer.writeAttribute("controls", Boolean.toString(videoAttributes.isControls()));
			if (videoAttributes.getHeight() != -1) {
				String val = Integer.toString(videoAttributes.getHeight());
				if (videoAttributes.isHeightPercentage()) {
					val += "%"; //$NON-NLS-1$
				}
				writer.writeAttribute("height", val); //$NON-NLS-1$
			}
			if (videoAttributes.getWidth() != -1) {
				String val = Integer.toString(videoAttributes.getWidth());
				if (videoAttributes.isWidthPercentage()) {
					val += "%"; //$NON-NLS-1$
				}
				writer.writeAttribute("width", val); //$NON-NLS-1$
			}
		}
	}

	private String prependImageUrl(String imageUrl) {
		if (getPrependImagePrefix() == null || getPrependImagePrefix().length() == 0) {
			return imageUrl;
		}
		if (ABSOLUTE_URL_PATTERN.matcher(imageUrl).matches() || imageUrl.contains("../")) { //$NON-NLS-1$
			return imageUrl;
		}
		String url = getPrependImagePrefix();
		if (!getPrependImagePrefix().endsWith("/")) { //$NON-NLS-1$
			url += '/';
		}
		url += imageUrl;
		return url;
	}
}

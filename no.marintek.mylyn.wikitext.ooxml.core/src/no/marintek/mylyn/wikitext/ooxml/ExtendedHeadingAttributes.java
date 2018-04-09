package no.marintek.mylyn.wikitext.ooxml;

import org.eclipse.mylyn.wikitext.parser.HeadingAttributes;

public class ExtendedHeadingAttributes extends HeadingAttributes {

	private boolean pageBreakBefore;
	private boolean landscapeMode;

	public boolean isLandscapeMode() {
		return landscapeMode;
	}

	public void setLandscapeMode(boolean landscapeMode) {
		this.landscapeMode = landscapeMode;
	}

	public boolean isPageBreakBefore() {
		return pageBreakBefore;
	}

	public void setPageBreakBefore(boolean pageBreakBefore) {
		this.pageBreakBefore = pageBreakBefore;
	}
}

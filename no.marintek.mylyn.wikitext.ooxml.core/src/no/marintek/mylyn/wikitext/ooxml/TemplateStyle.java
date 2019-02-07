package no.marintek.mylyn.wikitext.ooxml;

public enum TemplateStyle {
	TITLE("Title"),
	SUBTITLE("Subtitle"),
	HEADING("Heading"),
	APPENDIX("Appendix")
	;

	private final String templateStyle;
	
	TemplateStyle(String templateStyle) {
	    this.templateStyle = templateStyle;
	}
	
	public String getTemplateStyle() {
	    return this.templateStyle;
	}
}
package no.marintek.mylyn.wikitext.elements;

/**
 * 
 * Holds style information for a table cell.
 * 
 * @author havardn
 * 
 */
public class TableCellStyle {
	private String bgColor;
	private FontWeight fontWeight;
	private String fontSize;
	private TextHAlign textHAlign;
	private TextVAlign textVAlign;
	private boolean isHeader;

	public enum FontWeight {
		normal, bold
	}

	public enum TextHAlign {
		left, right, center
	}

	public enum TextVAlign {
		top, bottom, middle
	}

	public TableCellStyle() {
		bgColor = "FFFFFF";
		fontWeight = FontWeight.normal;
		textHAlign = TextHAlign.left;
		textVAlign = TextVAlign.top;
	}

	public void setCellBgColor(String bgColor) {
		this.bgColor = bgColor;
	}

	public String getCellBgColor() {
		return this.bgColor;
	}

	public FontWeight getFontWeight() {
		return fontWeight;
	}

	public void setFontWeight(FontWeight fontWeight) {
		this.fontWeight = fontWeight;
	}

	public boolean isHeader() {
		return isHeader;
	}

	public void setHeader(boolean isHeader) {
		this.isHeader = isHeader;
	}

	public String getFontSize() {
		return fontSize;
	}

	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
	}

	public TextHAlign getTextHAlign() {
		return textHAlign;
	}

	public void setTextHAlign(TextHAlign textHAlign) {
		this.textHAlign = textHAlign;
	}

	public TextVAlign getTextVAlign() {
		return textVAlign;
	}

	public void setTextVAlign(TextVAlign textVAlign) {
		this.textVAlign = textVAlign;
	}

}

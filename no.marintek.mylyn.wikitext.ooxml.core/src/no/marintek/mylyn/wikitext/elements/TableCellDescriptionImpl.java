package no.marintek.mylyn.wikitext.elements;

/**
 * 
 * Holds information about the value and style of single table cell.
 * 
 * @author Håvard Nesse
 * 
 */
public class TableCellDescriptionImpl implements TableCellDescription {
	private String text;
	private TableCellStyle style;
	private int colspan;

	public TableCellDescriptionImpl() {
		this.text = "";
	}
	@Override
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public TableCellStyle getTableCellStyle() {
		return style;
	}

	public void setTableCellStyle(TableCellStyle style) {
		this.style = style;
	}

	@Override
	public int getColspan() {
		return colspan;
	}

	public void setColspan(int colspan) {
		this.colspan = colspan;
	}


}

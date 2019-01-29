package no.marintek.mylyn.wikitext.ooxml;

/**
 * Configuration class for plot/chart rendering.
 * 
 * @author Håvard Nesse
 * 
 */
public class ChartRenderHints {
	
	private boolean renderMarkers;
	
	private boolean showDataTable = true;
	
	private boolean showLegend = false;
	
	private boolean richLegend = true;
	
	private int xAxisNumberFormat;
	
	private int yAxisNumberFormat;

	public static int AUTOFORMAT = 0;
	public static int GENERAL = 1;
	public static int SCIENTIFIC = 2;

	
	public ChartRenderHints() {
		this.renderMarkers = true;
		this.xAxisNumberFormat = AUTOFORMAT;
		this.yAxisNumberFormat = AUTOFORMAT;
	}

	public boolean isRenderMarkers() {
		return renderMarkers;
	}

	public void setRenderMarkers(boolean renderMarkers) {
		this.renderMarkers = renderMarkers;
	}

	public int getxAxisNumberFormat() {
		return xAxisNumberFormat;
	}

	public void setxAxisNumberFormat(int xAxisNumberFormat) {
		this.xAxisNumberFormat = xAxisNumberFormat;
	}

	public int getyAxisNumberFormat() {
		return yAxisNumberFormat;
	}

	public void setyAxisNumberFormat(int yAxisNumberFormat) {
		this.yAxisNumberFormat = yAxisNumberFormat;
	}

	public boolean showDataTable() {
		return showDataTable;
	}

	public void setShowDataTable(boolean showDataTable) {
		this.showDataTable = showDataTable;
	}

	public boolean showLegend() {
		return showLegend;
	}

	/**
	 * 
	 * @param showLegend
	 */
	public void setShowLegend(boolean showLegend) {
		this.showLegend = showLegend;
	}
	
	public boolean richLegend() {
		return richLegend;
	}
	
	/**
	 * Adds Max, Min, Mean and StdDev to legend text for dataseries
	 * @param richLegend
	 */
	public void setRichLegend(boolean richLegend) {
		this.richLegend = richLegend;
	}

}

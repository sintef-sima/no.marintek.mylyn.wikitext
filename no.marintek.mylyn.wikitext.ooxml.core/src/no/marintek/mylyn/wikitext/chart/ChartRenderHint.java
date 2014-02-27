package no.marintek.mylyn.wikitext.chart;

/**
 * Configuration class for plot/chart rendering.
 * 
 * @author havardn
 * 
 */
public class ChartRenderHint {
	private boolean renderMarkers;
	private AxisNumericFormat xAxisNumericFormat;
	private AxisNumericFormat yAxisNumericFormat;

	public enum AxisNumericFormat {
		AUTOFORMAT, GENERAL, SCIENTIFIC
	}

	public ChartRenderHint() {
		this.renderMarkers = true;
		this.xAxisNumericFormat = AxisNumericFormat.AUTOFORMAT;
		this.yAxisNumericFormat = AxisNumericFormat.AUTOFORMAT;
	}

	public boolean isRenderMarkers() {
		return renderMarkers;
	}

	public void setRenderMarkers(boolean renderMarkers) {
		this.renderMarkers = renderMarkers;
	}

	public AxisNumericFormat getxAxisNumericFormat() {
		return xAxisNumericFormat;
	}

	public void setxAxisNumericFormat(AxisNumericFormat xAxisNumericFormat) {
		this.xAxisNumericFormat = xAxisNumericFormat;
	}

	public AxisNumericFormat getyAxisNumericFormat() {
		return yAxisNumericFormat;
	}

	public void setyAxisNumericFormat(AxisNumericFormat yAxisNumericFormat) {
		this.yAxisNumericFormat = yAxisNumericFormat;
	}

}

package no.marintek.mylyn.wikitext.elements;

/**
 * Configuration class for plot/chart rendering.
 * 
 * @author Håvard Nesse
 * 
 */
public class ChartRenderHint {
	private boolean renderMarkers;
	private AxisNumberFormat xAxisNumberFormat;
	private AxisNumberFormat yAxisNumberFormat;

	public enum AxisNumberFormat {
		AUTOFORMAT, GENERAL, SCIENTIFIC
	}

	public ChartRenderHint() {
		this.renderMarkers = true;
		this.xAxisNumberFormat = AxisNumberFormat.AUTOFORMAT;
		this.yAxisNumberFormat = AxisNumberFormat.AUTOFORMAT;
	}

	public boolean isRenderMarkers() {
		return renderMarkers;
	}

	public void setRenderMarkers(boolean renderMarkers) {
		this.renderMarkers = renderMarkers;
	}

	public AxisNumberFormat getxAxisNumberFormat() {
		return xAxisNumberFormat;
	}

	public void setxAxisNumberFormat(AxisNumberFormat xAxisNumberFormat) {
		this.xAxisNumberFormat = xAxisNumberFormat;
	}

	public AxisNumberFormat getyAxisNumberFormat() {
		return yAxisNumberFormat;
	}

	public void setyAxisNumberFormat(AxisNumberFormat yAxisNumberFormat) {
		this.yAxisNumberFormat = yAxisNumberFormat;
	}

}

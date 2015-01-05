package no.marintek.mylyn.wikitext.elements;


public interface ChartDescription {

	/**
	 * Type of chart that can be created. Typically one of <b>LINE</b>, <b>SCATTER</b> or <b>BAR</b>
	 */
	public static enum ChartType {
		LINE, SCATTER, BAR
	}

	public abstract String[] getLegends();

	public abstract double[][] getxSeries();

	public abstract double[][] getySeries();

	public abstract ChartType getChartType();

	public abstract ChartRenderHints getRenderHints();

	public abstract String[] getAnnotations();

	public abstract String getYlabel();

	public abstract String getXlabel();

	public abstract String getTitle();

	public abstract String getCaption();

}
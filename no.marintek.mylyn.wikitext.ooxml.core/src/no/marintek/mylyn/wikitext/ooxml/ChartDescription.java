package no.marintek.mylyn.wikitext.ooxml;


public interface ChartDescription {

	public static final int LINE = 0;
	
	public static final int SCATTER = 1;
	
	public static final int BAR = 2;
	

	public abstract String[] getLegends();

	public abstract double[][] getXSeries();

	public abstract double[][] getYSeries();

	public abstract int getChartType();

	public abstract ChartRenderHints getRenderHints();

	public abstract String[] getAnnotations();

	public abstract String getYLabel();

	public abstract String getXLabel();

	public abstract String getTitle();

	public abstract String getCaption();

}
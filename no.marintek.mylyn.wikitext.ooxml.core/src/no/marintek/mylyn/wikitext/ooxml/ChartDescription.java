package no.marintek.mylyn.wikitext.ooxml;


public interface ChartDescription {

	public static final int LINE = 0;
	
	public static final int SCATTER = 1;
	
	public static final int BAR = 2;
	

	public abstract String[] getLegends();

	public abstract double[][] getxSeries();

	public abstract double[][] getySeries();

	public abstract int getChartType();

	public abstract ChartRenderHints getRenderHints();

	public abstract String[] getAnnotations();

	public abstract String getYlabel();

	public abstract String getXlabel();

	public abstract String getTitle();

	public abstract String getCaption();

}
package no.marintek.mylyn.wikitext.elements;

/**
 * A representation of a data set for plotting
 *
 * @author Torkild U. Resheim
 */
public class ChartDescription implements IChart {

	private String[] legends;
	private String xLabel;
	private String yLabel;
	private double[][] xSeries;
	private double[][] ySeries;
	private ChartType chartType;
	private ChartRenderHint renderHint;
	private String[] annotations;
	private String title;
	private String caption;

	public ChartDescription(String title, String[] legends, double[][] xSeries, double[][] ySeries, String xLabel, String yLabel, ChartType chartType) {
		super();
		this.legends = legends;
		this.xSeries = xSeries;
		this.ySeries = ySeries;
		this.xLabel = xLabel;
		this.yLabel = yLabel;
		this.chartType = chartType;
	}

	/* (non-Javadoc)
	 * @see no.marintek.mylyn.wikitext.chart.IChart#getLegends()
	 */
	@Override
	public String[] getLegends() {
		return legends;
	}

	/* (non-Javadoc)
	 * @see no.marintek.mylyn.wikitext.chart.IChart#getxSeries()
	 */
	@Override
	public double[][] getxSeries() {
		return xSeries;
	}
	
	public void setXSeries(double[][] xSeries){
		this.xSeries = xSeries;
	}

	public void setYSeries(double[][] ySeries){
		this.ySeries = ySeries;
	}

	/* (non-Javadoc)
	 * @see no.marintek.mylyn.wikitext.chart.IChart#getySeries()
	 */
	@Override
	public double[][] getySeries() {
		return ySeries;
	}

	public void setLegends(String[] legends) {
		this.legends = legends;
	}

	/* (non-Javadoc)
	 * @see no.marintek.mylyn.wikitext.chart.IChart#getChartType()
	 */
	@Override
	public ChartType getChartType() {
		return chartType;
	}

	public void setChartType(ChartType chartType) {
		this.chartType = chartType;
	}

	/* (non-Javadoc)
	 * @see no.marintek.mylyn.wikitext.chart.IChart#getRenderHint()
	 */
	@Override
	public ChartRenderHint getRenderHint() {
		return renderHint;
	}

	public void setRenderHint(ChartRenderHint renderHint) {
		this.renderHint = renderHint;
	}

	/* (non-Javadoc)
	 * @see no.marintek.mylyn.wikitext.chart.IChart#getAnnotations()
	 */
	@Override
	public String[] getAnnotations() {
		return annotations;
	}

	public void setAnnotations(String[] annotations) {
		this.annotations = annotations;
	}

	/* (non-Javadoc)
	 * @see no.marintek.mylyn.wikitext.chart.IChart#getYlabel()
	 */
	@Override
	public String getYlabel() {
		return yLabel;
	}

	/* (non-Javadoc)
	 * @see no.marintek.mylyn.wikitext.chart.IChart#getXlabel()
	 */
	@Override
	public String getXlabel() {
		return xLabel;
	}

	/* (non-Javadoc)
	 * @see no.marintek.mylyn.wikitext.chart.IChart#getTitle()
	 */
	@Override
	public String getTitle() {
		return title;
	}

	/* (non-Javadoc)
	 * @see no.marintek.mylyn.wikitext.chart.IChart#getCaption()
	 */
	@Override
	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;		
	}
	
}

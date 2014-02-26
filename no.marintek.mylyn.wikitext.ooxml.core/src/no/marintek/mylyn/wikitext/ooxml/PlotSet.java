package no.marintek.mylyn.wikitext.ooxml;

import no.marintek.mylin.wikitext.chart.ChartRenderHint;
import no.marintek.mylyn.wikitext.ooxml.ChartFactory.ChartType;

/**
 * A representation of a data set for plotting
 * 
 * @author Torkild U. Resheim
 */
public class PlotSet {

	String[] labels;
	double[][] xSeries;
	double[][] ySeries;
	private ChartType chartType;
	private ChartRenderHint renderHint;

	public PlotSet(String[] labels, double[][] xSeries, double[][] ySeries, ChartType chartType) {
		super();
		this.labels = labels;
		this.xSeries = xSeries;
		this.ySeries = ySeries;
		this.chartType = chartType;
	}
	
	public String[] getLabels() {
		return labels;
	}

	public double[][] getxSeries() {
		return xSeries;
	}

	public double[][] getySeries() {
		return ySeries;
	}

	public void setLabels(String[] labels) {
		this.labels = labels;
	}

	public void setxSeries(double[][] xSeries) {
		this.xSeries = xSeries;
	}

	public void setySeries(double[][] ySeries) {
		this.ySeries = ySeries;
	}

	public ChartType getChartType() {
		return chartType;
	}

	public void setChartType(ChartType chartType) {
		this.chartType = chartType;
	}

	public ChartRenderHint getRenderHint() {
		return renderHint;
	}

	public void setRenderHint(ChartRenderHint renderHint) {
		this.renderHint = renderHint;
	}
	
}

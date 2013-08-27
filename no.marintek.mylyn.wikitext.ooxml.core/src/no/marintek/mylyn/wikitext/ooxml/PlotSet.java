package no.marintek.mylyn.wikitext.ooxml;

/**
 * A representation of a data set for plotting
 * 
 * @author Torkild U. Resheim
 */
public class PlotSet {

	String[] labels;
	double[][] xSeries;
	double[][] ySeries;

	public PlotSet(String[] labels, double[][] xSeries, double[][] ySeries) {
		super();
		this.labels = labels;
		this.xSeries = xSeries;
		this.ySeries = ySeries;
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
}

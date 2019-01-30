/*******************************************************************************
 * Copyright (c) 2017 SINTEF Ocean
 * Copyright (c) 2014 MARINTEK
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild U. Resheim - initial API and implementation
 *******************************************************************************/
package no.marintek.mylyn.wikitext.ooxml;

/**
 * This interface must be implemented in order to use the charting feature in the {@link OoxmlDocumentBuilder}.
 * 
 * @author Torkild Ulvøy Resheim, Itema AS
 */
public interface ChartDescription {

	/** Draw a line chart */
	public static final int LINE = 0;
	/** Draw a scatter diagram */
	public static final int SCATTER = 1;
	/** Draw a bar chart */
	public static final int BAR = 2;

	/**
	 * The legend strings are used to describe each of the data series.
	 * 
	 * @return a list of legend strings
	 */
	public abstract String[] getLegends();

	/**
	 * The x-series arrays contains values for the x-axis,
	 * 
	 * @return x-axis values
	 */
	public abstract double[][] getXSeries();

	/**
	 * The y-series arrays contains values for the -axis,
	 * 
	 * @return y-axis values
	 */
	public abstract double[][] getYSeries();

	/**
	 * The chart type indicates what type of chart that should be rendered.
	 * <ol>
	 * <li>Line chart</li>
	 * <li>Scatter chart</li>
	 * <li>Bar chart</li>
	 * </ol>
	 * 
	 * @return the chart type
	 */
	public abstract int getChartType();

	public abstract ChartRenderHints getRenderHints();

	public abstract String[] getAnnotations();

	public abstract String getYLabel();

	public abstract String getXLabel();

	public abstract String getTitle();

	public abstract String getCaption();

}
/*******************************************************************************
 * Copyright (c) 2013 MARINTEK and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild U. Resheim - initial API and implementation
 *******************************************************************************/
package no.marintek.mylyn.wikitext.ooxml;

import javax.xml.bind.JAXBException;

import no.marintek.mylin.wikitext.chart.ChartRenderHint;

import org.docx4j.dml.CTLineProperties;
import org.docx4j.dml.CTNoFillProperties;
import org.docx4j.dml.CTPresetLineDashProperties;
import org.docx4j.dml.CTRegularTextRun;
import org.docx4j.dml.CTSRgbColor;
import org.docx4j.dml.CTSchemeColor;
import org.docx4j.dml.CTShapeProperties;
import org.docx4j.dml.CTSolidColorFillProperties;
import org.docx4j.dml.CTTextBody;
import org.docx4j.dml.CTTextBodyProperties;
import org.docx4j.dml.CTTextCharacterProperties;
import org.docx4j.dml.CTTextListStyle;
import org.docx4j.dml.CTTextParagraph;
import org.docx4j.dml.CTTextParagraphProperties;
import org.docx4j.dml.TextFont;
import org.docx4j.dml.chart.CTAxDataSource;
import org.docx4j.dml.chart.CTAxPos;
import org.docx4j.dml.chart.CTBarChart;
import org.docx4j.dml.chart.CTBarDir;
import org.docx4j.dml.chart.CTBarGrouping;
import org.docx4j.dml.chart.CTBarSer;
import org.docx4j.dml.chart.CTBoolean;
import org.docx4j.dml.chart.CTBuiltInUnit;
import org.docx4j.dml.chart.CTCatAx;
import org.docx4j.dml.chart.CTChart;
import org.docx4j.dml.chart.CTChartLines;
import org.docx4j.dml.chart.CTChartSpace;
import org.docx4j.dml.chart.CTCrossBetween;
import org.docx4j.dml.chart.CTCrosses;
import org.docx4j.dml.chart.CTDLbls;
import org.docx4j.dml.chart.CTDispBlanksAs;
import org.docx4j.dml.chart.CTDispUnits;
import org.docx4j.dml.chart.CTDouble;
import org.docx4j.dml.chart.CTGrouping;
import org.docx4j.dml.chart.CTLayout;
import org.docx4j.dml.chart.CTLayoutMode;
import org.docx4j.dml.chart.CTLayoutTarget;
import org.docx4j.dml.chart.CTLblAlgn;
import org.docx4j.dml.chart.CTLblOffset;
import org.docx4j.dml.chart.CTLegend;
import org.docx4j.dml.chart.CTLegendPos;
import org.docx4j.dml.chart.CTLineChart;
import org.docx4j.dml.chart.CTLineSer;
import org.docx4j.dml.chart.CTManualLayout;
import org.docx4j.dml.chart.CTMarker;
import org.docx4j.dml.chart.CTMarkerSize;
import org.docx4j.dml.chart.CTMarkerStyle;
import org.docx4j.dml.chart.CTNumData;
import org.docx4j.dml.chart.CTNumDataSource;
import org.docx4j.dml.chart.CTNumVal;
import org.docx4j.dml.chart.CTOrientation;
import org.docx4j.dml.chart.CTPlotArea;
import org.docx4j.dml.chart.CTScaling;
import org.docx4j.dml.chart.CTScatterChart;
import org.docx4j.dml.chart.CTScatterSer;
import org.docx4j.dml.chart.CTScatterStyle;
import org.docx4j.dml.chart.CTSerTx;
import org.docx4j.dml.chart.CTSkip;
import org.docx4j.dml.chart.CTStyle;
import org.docx4j.dml.chart.CTTextLanguageID;
import org.docx4j.dml.chart.CTTickLblPos;
import org.docx4j.dml.chart.CTTickMark;
import org.docx4j.dml.chart.CTTitle;
import org.docx4j.dml.chart.CTTx;
import org.docx4j.dml.chart.CTUnsignedInt;
import org.docx4j.dml.chart.CTValAx;
import org.docx4j.dml.chart.ObjectFactory;
import org.docx4j.dml.chart.STAxPos;
import org.docx4j.dml.chart.STBarDir;
import org.docx4j.dml.chart.STScatterStyle;
import org.docx4j.dml.chart.STTickLblPos;

/**
 * A factory for creating charts for OOXML documents. It will take a maximum of
 * eight series in each chart and supports separate data for the x and y axis of
 * each series.
 *
 * @author Torkild U. Resheim
 * @since 1.0
 */
public class ChartFactory {

	private static class Range {
		double min;
		double max;
		String format;

		public Range(double min, double max, String format) {
			this.min = min;
			this.max = max;
			this.format = format;
		}
	}

	public static final String DEFAULT_ENGINEERING_FORMAT = "0.00E+00;0.00E+00;0";

	private static final int ENGINEERING_LIMIT = 4;

	/**
	 * Type of chart that can be created.
	 */
	public static enum ChartType {
		LINE, SCATTER, BAR
	};

	private final static byte[][] COLOUR_SCHEME = new byte[][] { { 1, 2, 2 }, // black
			{ (byte) 235, 49, 55 }, // red
			{ 18, (byte) 139, 75 }, // green
			{ 29, 92, (byte) 167 }, // blue
			{ (byte) 252, 125, 50 }, // orange
			{ 101, 48, (byte) 143 }, // purple
			{ (byte) 160, 31, 38 }, // burgundy
			{ (byte) 178, 60, (byte) 147 } // pink
	};

	private static void addSeries(String[] legends, String ylabel, String xlabel, double[] ySeries, double[] xSeries,
			ObjectFactory dmlchartObjectFactory, int valueAxisId, int categoryAxisId, org.docx4j.dml.ObjectFactory dmlObjectFactory,
			CTPlotArea plotarea, CTScatterChart scatterchart, int index) {

		CTScatterSer scatterser = dmlchartObjectFactory.createCTScatterSer();
		scatterchart.getSer().add(scatterser);

		// Create object for idx
		CTUnsignedInt unsignedint2 = dmlchartObjectFactory.createCTUnsignedInt();
		scatterser.setIdx(unsignedint2);
		unsignedint2.setVal(index);

		// Create object for order
		 CTUnsignedInt unsignedint =
		 dmlchartObjectFactory.createCTUnsignedInt();
		 scatterser.setOrder(unsignedint);
		 unsignedint.setVal(index);

		// Create object for series text
		CTSerTx sertx = dmlchartObjectFactory.createCTSerTx();
		scatterser.setTx(sertx);
		sertx.setV(legends[index]);

		// Create object for spPr
		CTShapeProperties shapeproperties = dmlObjectFactory.createCTShapeProperties();
		scatterser.setSpPr(shapeproperties);

		// Create object for data point marker
		CTMarker marker = dmlchartObjectFactory.createCTMarker();
		CTMarkerSize size = dmlchartObjectFactory.createCTMarkerSize();
		size.setVal((short) 3);
		marker.setSize(size);
		CTMarkerStyle symbol = dmlchartObjectFactory.createCTMarkerStyle();
		symbol.setVal(org.docx4j.dml.chart.STMarkerStyle.CIRCLE);
		marker.setSymbol(symbol);
		scatterser.setMarker(marker);

		// Create object for scatterStyle
		CTScatterStyle scatterStyle = dmlchartObjectFactory.createCTScatterStyle();
		scatterStyle.setVal(STScatterStyle.LINE);

		// Create object for smooth line style
		CTBoolean smooth = dmlchartObjectFactory.createCTBoolean();
		smooth.setVal(Boolean.FALSE);
		scatterser.setSmooth(smooth);

		// Create object for ln
		CTLineProperties lineproperties = dmlObjectFactory.createCTLineProperties();
		shapeproperties.setLn(lineproperties);

		// Create object for solidFill
		CTSolidColorFillProperties solidcolorfillproperties = dmlObjectFactory.createCTSolidColorFillProperties();
		lineproperties.setSolidFill(solidcolorfillproperties);
		lineproperties.setW(new Integer(12700));
		CTSRgbColor srgbcolor = dmlObjectFactory.createCTSRgbColor();
		solidcolorfillproperties.setSrgbClr(srgbcolor);
		srgbcolor.setVal(COLOUR_SCHEME[index]);

		// Create object for xVal
		scatterser.setXVal(createCategoriesDataSource(dmlchartObjectFactory, xSeries));

		// Create object for yVal
		scatterser.setYVal(createValuesDataSource(dmlchartObjectFactory, ySeries));

	}

	/**
	 *
	 * @param legends
	 * @param ylabel
	 * @param xlabel
	 * @param ySeries
	 * @param xSeries
	 * @param dmlchartObjectFactory
	 * @param valueAxisId
	 * @param categoryAxisId
	 * @param dmlObjectFactory
	 * @param plotarea
	 * @param linechart
	 * @param order
	 * @param index
	 */
	private static void addSeries(String[] legends, String ylabel, String xlabel, double[] ySeries, double[] xSeries,
			org.docx4j.dml.chart.ObjectFactory dmlchartObjectFactory, int valueAxisId, int categoryAxisId,
			org.docx4j.dml.ObjectFactory dmlObjectFactory, CTPlotArea plotarea, CTLineChart linechart, int order, int index, ChartRenderHint hint) {

		// Create object for dispUnits
		CTLineSer lineser = dmlchartObjectFactory.createCTLineSer();
		linechart.getSer().add(lineser);

		// Create object for order
		CTUnsignedInt unsignedint3 = dmlchartObjectFactory.createCTUnsignedInt();
		lineser.setOrder(unsignedint3);
		unsignedint3.setVal(order);

		// Create object for val
		lineser.setVal(createValuesDataSource(dmlchartObjectFactory, ySeries));

		// Create object for spPr
		CTShapeProperties shapeproperties = dmlObjectFactory.createCTShapeProperties();
		lineser.setSpPr(shapeproperties);

		// Create object for ln
		CTLineProperties lineproperties = dmlObjectFactory.createCTLineProperties();
		shapeproperties.setLn(lineproperties);

		// Create object for solidFill
		CTSolidColorFillProperties solidcolorfillproperties = dmlObjectFactory.createCTSolidColorFillProperties();
		lineproperties.setSolidFill(solidcolorfillproperties);
		lineproperties.setW(new Integer(12700));
		CTSRgbColor srgbcolor = dmlObjectFactory.createCTSRgbColor();
		solidcolorfillproperties.setSrgbClr(srgbcolor);
		srgbcolor.setVal(COLOUR_SCHEME[index]);

		// Create object for idx
		CTUnsignedInt unsignedint5 = dmlchartObjectFactory.createCTUnsignedInt();
		lineser.setIdx(unsignedint5);
		unsignedint5.setVal(index);

		// Create object for tx
		CTSerTx sertx = dmlchartObjectFactory.createCTSerTx();
		lineser.setTx(sertx);
		sertx.setV(legends[index]);

		// Create object for marker
		CTMarker marker = dmlchartObjectFactory.createCTMarker();

		// Create object for symbol
		CTMarkerStyle markerstyle = dmlchartObjectFactory.createCTMarkerStyle();
		markerstyle.setVal(org.docx4j.dml.chart.STMarkerStyle.NONE);

		if (hint != null && hint.isRenderMarkers()) {
			// Set marker size
			CTMarkerSize size = dmlchartObjectFactory.createCTMarkerSize();
			size.setVal((short) 3);
			marker.setSize(size);

			// Set marker style
			markerstyle.setVal(org.docx4j.dml.chart.STMarkerStyle.CIRCLE);

		}

		marker.setSymbol(markerstyle);
		lineser.setMarker(marker);
				
		// Create object for smooth
		CTBoolean boolean12 = dmlchartObjectFactory.createCTBoolean();
		boolean12.setVal(Boolean.FALSE);
		lineser.setSmooth(boolean12);

		lineser.setCat(createCategoriesDataSource(dmlchartObjectFactory, xSeries));

	}

	/**
	 * 
	 * @param legends
	 * @param ylabel
	 * @param xlabel
	 * @param ySeries
	 * @param xSeries
	 * @param dmlchartObjectFactory
	 * @param valueAxisId
	 * @param categoryAxisId
	 * @param dmlObjectFactory
	 * @param plotarea
	 * @param linechart
	 * @param order
	 * @param index
	 */
	private static void addSeries(String[] legends, String ylabel, String xlabel, double[] ySeries, double[] xSeries,
			org.docx4j.dml.chart.ObjectFactory dmlchartObjectFactory, int valueAxisId, int categoryAxisId,
			org.docx4j.dml.ObjectFactory dmlObjectFactory, CTPlotArea plotarea, CTBarChart chart, int order, int index) {

		// Create object for dispUnits
		CTBarSer barser = dmlchartObjectFactory.createCTBarSer();
		chart.getSer().add(barser);

		// Create object for order
		CTUnsignedInt unsignedint3 = dmlchartObjectFactory.createCTUnsignedInt();
		barser.setOrder(unsignedint3);
		unsignedint3.setVal(order);

		// Create object for val
		barser.setVal(createValuesDataSource(dmlchartObjectFactory, ySeries));

		// Create object for spPr
		CTShapeProperties shapeproperties = dmlObjectFactory.createCTShapeProperties();
		barser.setSpPr(shapeproperties);

		// Invert bars if negative value
		CTBoolean boolean12 = dmlchartObjectFactory.createCTBoolean();
		boolean12.setVal(Boolean.FALSE);
		barser.setInvertIfNegative(boolean12);

		// Create object for ln
		CTLineProperties lineproperties = dmlObjectFactory.createCTLineProperties();
		shapeproperties.setLn(lineproperties);

		// Create object for solidFill
		CTSolidColorFillProperties solidcolorfillproperties = dmlObjectFactory.createCTSolidColorFillProperties();
		lineproperties.setSolidFill(solidcolorfillproperties);
		lineproperties.setW(new Integer(12700));
		CTSRgbColor srgbcolor = dmlObjectFactory.createCTSRgbColor();
		solidcolorfillproperties.setSrgbClr(srgbcolor);
		srgbcolor.setVal(COLOUR_SCHEME[index]);

		// Create object for idx
		CTUnsignedInt unsignedint5 = dmlchartObjectFactory.createCTUnsignedInt();
		barser.setIdx(unsignedint5);
		unsignedint5.setVal(index);

		// Create object for tx
		CTSerTx sertx = dmlchartObjectFactory.createCTSerTx();
		barser.setTx(sertx);
		sertx.setV(legends[index]);

		// Create object for marker
		// CTMarker marker = dmlchartObjectFactory.createCTMarker();
		// barser.setsetMarker(marker);

		// Create object for symbol
		// CTMarkerStyle markerstyle = dmlchartObjectFactory.createCTMarkerStyle();
		// marker.setSymbol(markerstyle);
		// markerstyle.setVal(org.docx4j.dml.chart.STMarkerStyle.NONE);

		// Create object for smooth
		// CTBoolean boolean12 = dmlchartObjectFactory.createCTBoolean();
		// boolean12.setVal(Boolean.FALSE);
		// barser.setSmooth(boolean12);

		barser.setCat(createCategoriesDataSource(dmlchartObjectFactory, xSeries));

	}

	/**
	 * Creates the data source for the horizontal axis.
	 *
	 * @param dmlchartObjectFactory
	 * @param data
	 * @return
	 */
	private static CTAxDataSource createCategoriesDataSource(ObjectFactory dmlchartObjectFactory, double[] data) {
		CTAxDataSource datasource = dmlchartObjectFactory.createCTAxDataSource();

		// Create object for numCache
		CTNumData numdata = dmlchartObjectFactory.createCTNumData();
		datasource.setNumLit(numdata);

		double max = 0;
		double min = 0;

		for (int i = 0; i < data.length; i++) {
			CTNumVal numval = dmlchartObjectFactory.createCTNumVal();
			numdata.getPt().add(numval);
			numval.setIdx(i);
			if (Double.isNaN(data[i])) {
				numval.setV(""); // NaN must be represented as empty string
									// (tested in MS Word 2010 on Windows 7)
			} else {
				if (data[i] > max) {
					max = data[i];
				}
				if (data[i] < min) {
					min = data[i];
				}
				numval.setV(Double.toString(data[i]));
			}
		}
		Range range = setRange(max, min);
		numdata.setFormatCode(range.format);

		// Create object for ptCount
		CTUnsignedInt unsignedint = dmlchartObjectFactory.createCTUnsignedInt();
		numdata.setPtCount(unsignedint);
		unsignedint.setVal(data.length);

		return datasource;
	}

	private static Range setRange(double lower, double upper) {
		double min;
		double max;
		String default_decimal_format = "0.0";
		String formatPattern = default_decimal_format;

		if (Double.isNaN(lower) || Double.isNaN(upper) || Double.isInfinite(lower) || Double.isInfinite(upper) || Double.isInfinite(upper - lower)) {
			throw new IllegalArgumentException("Illegal range: lower=" + lower + ", upper=" + upper);
		}

		if (lower == upper) {
			upper = lower + 1;
			if (Double.isInfinite(upper))
				throw new IllegalArgumentException("Illegal range: lower=" + lower + ", upper=" + upper);
		}

		min = lower;
		max = upper;

		if (formatPattern.equals(default_decimal_format) || formatPattern.equals(DEFAULT_ENGINEERING_FORMAT)) {
			if ((max != 0 && Math.abs(Math.log10(Math.abs(max))) >= ENGINEERING_LIMIT)
					|| (min != 0 && Math.abs(Math.log10(Math.abs(min))) >= ENGINEERING_LIMIT))
				formatPattern = DEFAULT_ENGINEERING_FORMAT;
			else
				formatPattern = default_decimal_format;
		}
		return new Range(min, max, formatPattern);
	}

	/**
	 * Creates a new OOXML chart space. Accepts a maximum of eight series.
	 *
	 * @param title
	 *            the chart title
	 * @param legends
	 *            series names
	 * @param ylabel
	 *            y-axis label
	 * @param xlabel
	 *            x-axis label
	 * @param ySeries
	 *            y-axis data series
	 * @param xSeries
	 *            x-axis data series
	 * @return the chart space instance
	 * @throws JAXBException
	 */
	public static CTChartSpace createChartSpace(String title, String ylabel, String xlabel, PlotSet plotSet) throws JAXBException {

		if ((plotSet.getxSeries().length > COLOUR_SCHEME.length) || (plotSet.getySeries().length > COLOUR_SCHEME.length)) {
			throw new IllegalArgumentException("The maximum number of series in one chart is " + COLOUR_SCHEME.length);
		}

		org.docx4j.dml.chart.ObjectFactory dmlchartObjectFactory = new org.docx4j.dml.chart.ObjectFactory();

		// Create random numbers for the axis identifiers
		int valueAxisId = (int) (Math.random() * Integer.MAX_VALUE);
		int categoryAxisId = (int) (Math.random() * Integer.MAX_VALUE);

		CTChartSpace chartspace = dmlchartObjectFactory.createCTChartSpace();

		// Create object for style
		CTStyle style = dmlchartObjectFactory.createCTStyle();
		style.setVal((short) 2);
		chartspace.setStyle(style);

		// Create object for lang
		CTTextLanguageID textlanguageid = dmlchartObjectFactory.createCTTextLanguageID();
		chartspace.setLang(textlanguageid);
		textlanguageid.setVal("en-US");

		// Create object for chart
		CTChart chart = dmlchartObjectFactory.createCTChart();
		chartspace.setChart(chart);

		// Create object for title
		chart.setTitle(createChartTitle(title));
		org.docx4j.dml.ObjectFactory dmlObjectFactory = new org.docx4j.dml.ObjectFactory();

		// Create object for autoTitleDeleted
		CTBoolean boolean2 = dmlchartObjectFactory.createCTBoolean();
		boolean2.setVal(Boolean.FALSE);
		chart.setAutoTitleDeleted(boolean2);

		// Create object for plotArea
		CTPlotArea plotarea = dmlchartObjectFactory.createCTPlotArea();
		chart.setPlotArea(plotarea);

		// Create object for layout
		plotarea.setLayout(createLayout());

		// FIXME: Create this properly if we need it
		// CTScaling createCTScaling = dmlchartObjectFactory.createCTScaling();
		// CTDouble min = dmlchartObjectFactory.createCTDouble();
		// min.setVal(4);
		//
		// createCTScaling.setMin(min);
		// CTDouble max = dmlchartObjectFactory.createCTDouble();
		// min.setVal(10);
		// createCTScaling.setMin(max);
		// catAx.setScaling(createCTScaling);

		// Create object for valAx
		plotarea.getValAxOrCatAxOrDateAx().add(createCTValAx(ylabel, valueAxisId, categoryAxisId, false));

		// Add a legend
		if (plotSet.getLabels().length > 1) {
			chart.setLegend(createLegend());
		}

		createChartLayout(dmlchartObjectFactory, chartspace, chart, dmlObjectFactory);

		if (ChartType.SCATTER.equals(plotSet.getChartType())) {

			CTValAx catAx = createCTValAx(xlabel, categoryAxisId, valueAxisId, true);
			plotarea.getValAxOrCatAxOrDateAx().add(catAx);

			// Position
			CTTickLblPos tickLblPos = dmlchartObjectFactory.createCTTickLblPos();
			tickLblPos.setVal(STTickLblPos.LOW);
			catAx.setTickLblPos(tickLblPos);

			CTAxPos pos = dmlchartObjectFactory.createCTAxPos();
			pos.setVal(STAxPos.B);
			catAx.setAxPos(pos);

			CTScatterChart scatterchart = dmlchartObjectFactory.createCTScatterChart();
			plotarea.getAreaChartOrArea3DChartOrLineChart().add(scatterchart);

			// Create object for scatter style
			CTScatterStyle scatterStyle = dmlchartObjectFactory.createCTScatterStyle();
			scatterStyle.setVal(STScatterStyle.LINE_MARKER);
			scatterchart.setScatterStyle(scatterStyle);

			// Specify X-axis relationships
			CTUnsignedInt unsignedint = dmlchartObjectFactory.createCTUnsignedInt();
			unsignedint.setVal(categoryAxisId);
			scatterchart.getAxId().add(unsignedint);

			// Specify Y-axis relationships
			CTUnsignedInt unsignedint2 = dmlchartObjectFactory.createCTUnsignedInt();
			unsignedint2.setVal(valueAxisId);
			scatterchart.getAxId().add(unsignedint2);

			// Create object for varyColors
			CTBoolean boolean11 = dmlchartObjectFactory.createCTBoolean();
			boolean11.setVal(Boolean.FALSE);
			scatterchart.setVaryColors(boolean11);

			for (int series = 0; series < plotSet.getxSeries().length; series++) {
				addSeries(plotSet.getLabels(), ylabel, xlabel, plotSet.getySeries()[series], plotSet.getxSeries()[series], dmlchartObjectFactory,
						valueAxisId, categoryAxisId, dmlObjectFactory, plotarea, scatterchart, series);
			}

		} else if (ChartType.LINE.equals(plotSet.getChartType())) {

			CTCatAx catAx = createCTCatAx(xlabel, valueAxisId, categoryAxisId);
			plotarea.getValAxOrCatAxOrDateAx().add(catAx);

			CTLineChart linechart = dmlchartObjectFactory.createCTLineChart();
			plotarea.getAreaChartOrArea3DChartOrLineChart().add(linechart);

			// Position
			CTTickLblPos tickLblPos = dmlchartObjectFactory.createCTTickLblPos();
			tickLblPos.setVal(STTickLblPos.LOW);
			catAx.setTickLblPos(tickLblPos);

			CTAxPos pos = dmlchartObjectFactory.createCTAxPos();
			pos.setVal(STAxPos.B);
			catAx.setAxPos(pos);

			// Minimum/maximum major tick marks for the horizontal axis
			int length = plotSet.getxSeries()[0].length;
			int skip = length / 10;

			if (length > 10) {
				CTSkip createCTSkip = dmlchartObjectFactory.createCTSkip();
				createCTSkip.setVal(skip);
				catAx.setTickMarkSkip(createCTSkip);
				catAx.setTickLblSkip(createCTSkip);
			}

			// Show X values
			CTBoolean xValuesHidden = dmlchartObjectFactory.createCTBoolean();
			xValuesHidden.setVal(false);
			catAx.setDelete(xValuesHidden);

			// Create object for marker
			CTBoolean boolean3 = dmlchartObjectFactory.createCTBoolean();
			boolean3.setVal(Boolean.TRUE);
			linechart.setMarker(boolean3);
	
			CTDLbls dlbls = createLabels(dmlchartObjectFactory);
			linechart.setDLbls(dlbls);

			// Create object for smooth
			CTBoolean boolean10 = dmlchartObjectFactory.createCTBoolean();
			boolean10.setVal(Boolean.FALSE);
			linechart.setSmooth(boolean10);

			// Create object for crossBetween
			CTUnsignedInt unsignedint = dmlchartObjectFactory.createCTUnsignedInt();
			unsignedint.setVal(categoryAxisId);
			linechart.getAxId().add(unsignedint);

			// Create object for dispUnits
			CTUnsignedInt unsignedint2 = dmlchartObjectFactory.createCTUnsignedInt();
			unsignedint2.setVal(valueAxisId);
			linechart.getAxId().add(unsignedint2);

			// Create object for grouping
			CTGrouping grouping = dmlchartObjectFactory.createCTGrouping();
			linechart.setGrouping(grouping);
			grouping.setVal(org.docx4j.dml.chart.STGrouping.STANDARD);

			// Create object for varyColors
			CTBoolean boolean11 = dmlchartObjectFactory.createCTBoolean();
			boolean11.setVal(Boolean.FALSE);
			linechart.setVaryColors(boolean11);

			for (int series = 0; series < plotSet.getxSeries().length; series++) {
				addSeries(plotSet.getLabels(), ylabel, xlabel, plotSet.getySeries()[series], plotSet.getxSeries()[series], dmlchartObjectFactory, valueAxisId, categoryAxisId, dmlObjectFactory,
						plotarea, linechart, series, series, plotSet.getRenderHint());
			}
		} else if (ChartType.BAR.equals(plotSet.getChartType())) {
			CTBarChart barchart = dmlchartObjectFactory.createCTBarChart();
			plotarea.getAreaChartOrArea3DChartOrLineChart().add(barchart);

			// Set bar direction
			CTBarDir dir = dmlchartObjectFactory.createCTBarDir();
			dir.setVal(STBarDir.COL);
			barchart.setBarDir(dir);

			// Create labels
			CTDLbls dlbls = createLabels(dmlchartObjectFactory);
			barchart.setDLbls(dlbls);

			// Create object for crossBetween
			CTUnsignedInt unsignedint = dmlchartObjectFactory.createCTUnsignedInt();
			unsignedint.setVal(categoryAxisId);
			barchart.getAxId().add(unsignedint);

			// Create object for dispUnits
			CTUnsignedInt unsignedint2 = dmlchartObjectFactory.createCTUnsignedInt();
			unsignedint2.setVal(valueAxisId);
			barchart.getAxId().add(unsignedint2);

			// Create object for grouping
			CTBarGrouping grouping = dmlchartObjectFactory.createCTBarGrouping();
			barchart.setGrouping(grouping);
			grouping.setVal(org.docx4j.dml.chart.STBarGrouping.CLUSTERED);

			// Create object for varyColors
			CTBoolean boolean11 = dmlchartObjectFactory.createCTBoolean();
			boolean11.setVal(Boolean.FALSE);
			barchart.setVaryColors(boolean11);

			for (int series = 0; series < plotSet.getxSeries().length; series++) {
				addSeries(plotSet.getLabels(), ylabel, xlabel, plotSet.getySeries()[series], plotSet.getxSeries()[series], dmlchartObjectFactory,
						valueAxisId, categoryAxisId, dmlObjectFactory, plotarea, barchart, series, series);
			}
		}

		// Create object for catAx
		CTCatAx catAx = createCTCatAx(xlabel, valueAxisId, categoryAxisId);
		
		// Position
		CTTickLblPos tickLblPos = dmlchartObjectFactory.createCTTickLblPos();
		tickLblPos.setVal(STTickLblPos.LOW);
		catAx.setTickLblPos(tickLblPos );
		
		CTAxPos pos = dmlchartObjectFactory.createCTAxPos();
		pos.setVal(STAxPos.B); 
		catAx.setAxPos(pos);
		
		// Show X values
		CTBoolean xValuesHidden = dmlchartObjectFactory.createCTBoolean();
		xValuesHidden.setVal(false);

		plotarea.getValAxOrCatAxOrDateAx().add(catAx);

		return chartspace;
	}

	private static void createChartLayout(org.docx4j.dml.chart.ObjectFactory dmlchartObjectFactory, CTChartSpace chartspace, CTChart chart,
			org.docx4j.dml.ObjectFactory dmlObjectFactory) {
		// Create object for plotVisOnly
		CTBoolean boolean19 = dmlchartObjectFactory.createCTBoolean();
		boolean19.setVal(Boolean.FALSE);
		chart.setPlotVisOnly(boolean19);

		// Create object for dispBlanksAs
		CTDispBlanksAs dispblanksas = dmlchartObjectFactory.createCTDispBlanksAs();
		chart.setDispBlanksAs(dispblanksas);
		dispblanksas.setVal(org.docx4j.dml.chart.STDispBlanksAs.SPAN);

		// Create object for showDLblsOverMax
		CTBoolean boolean20 = dmlchartObjectFactory.createCTBoolean();
		boolean20.setVal(Boolean.TRUE);
		chart.setShowDLblsOverMax(boolean20);

		// Create object for spPr
		CTShapeProperties shapeproperties4 = dmlObjectFactory.createCTShapeProperties();
		chartspace.setSpPr(shapeproperties4);

		// Create object for ln
		CTLineProperties lineproperties4 = dmlObjectFactory.createCTLineProperties();
		shapeproperties4.setLn(lineproperties4);

		// Create object for noFill
		CTNoFillProperties nofillproperties = dmlObjectFactory.createCTNoFillProperties();
		lineproperties4.setNoFill(nofillproperties);

		// Create object for date1904
		CTBoolean boolean21 = dmlchartObjectFactory.createCTBoolean();
		boolean21.setVal(Boolean.FALSE);
		chartspace.setDate1904(boolean21);

		// Create object for roundedCorners
		CTBoolean boolean22 = dmlchartObjectFactory.createCTBoolean();
		boolean22.setVal(Boolean.FALSE);
		chartspace.setRoundedCorners(boolean22);

		// Create object for txPr
		CTTextBody textbody4 = dmlObjectFactory.createCTTextBody();
		chartspace.setTxPr(textbody4);

		// Create object for lstStyle
		CTTextListStyle textliststyle4 = dmlObjectFactory.createCTTextListStyle();
		textbody4.setLstStyle(textliststyle4);

		// Create object for bodyPr
		CTTextBodyProperties textbodyproperties4 = dmlObjectFactory.createCTTextBodyProperties();
		textbody4.setBodyPr(textbodyproperties4);

		// Create object for p
		CTTextParagraph textparagraph4 = dmlObjectFactory.createCTTextParagraph();
		textbody4.getP().add(textparagraph4);

		// Create object for pPr
		CTTextParagraphProperties textparagraphproperties4 = dmlObjectFactory.createCTTextParagraphProperties();
		textparagraph4.setPPr(textparagraphproperties4);

		// Create object for defRPr
		CTTextCharacterProperties textcharacterproperties7 = dmlObjectFactory.createCTTextCharacterProperties();
		textparagraphproperties4.setDefRPr(textcharacterproperties7);

		// Create object for latin
		TextFont textfont = dmlObjectFactory.createTextFont();
		textcharacterproperties7.setLatin(textfont);
		textfont.setTypeface("Arial");

		// Create object for endParaRPr
		CTTextCharacterProperties textcharacterproperties8 = dmlObjectFactory.createCTTextCharacterProperties();
		textparagraph4.setEndParaRPr(textcharacterproperties8);
		textcharacterproperties8.setLang("en-US");
	}

	private static CTTitle createChartTitle(String chartTitle) {

		org.docx4j.dml.chart.ObjectFactory dmlchartObjectFactory = new org.docx4j.dml.chart.ObjectFactory();

		CTTitle title = dmlchartObjectFactory.createCTTitle();

		// Create object for layout
		CTLayout layout = dmlchartObjectFactory.createCTLayout();
		title.setLayout(layout);

		// Create object for tx
		CTTx tx = dmlchartObjectFactory.createCTTx();
		title.setTx(tx);
		org.docx4j.dml.ObjectFactory dmlObjectFactory = new org.docx4j.dml.ObjectFactory();

		// Create object for rich
		CTTextBody textbody = dmlObjectFactory.createCTTextBody();
		tx.setRich(textbody);

		// Create object for lstStyle
		CTTextListStyle textliststyle = dmlObjectFactory.createCTTextListStyle();
		textbody.setLstStyle(textliststyle);

		// Create object for bodyPr
		CTTextBodyProperties textbodyproperties = dmlObjectFactory.createCTTextBodyProperties();
		textbody.setBodyPr(textbodyproperties);

		// Create object for p
		CTTextParagraph textparagraph = dmlObjectFactory.createCTTextParagraph();
		textbody.getP().add(textparagraph);

		// Create object for pPr
		CTTextParagraphProperties textparagraphproperties = dmlObjectFactory.createCTTextParagraphProperties();
		textparagraph.setPPr(textparagraphproperties);

		// Create object for defRPr
		CTTextCharacterProperties textcharacterproperties = dmlObjectFactory.createCTTextCharacterProperties();
		textparagraphproperties.setDefRPr(textcharacterproperties);
		textcharacterproperties.setSz(new Integer(1200));

		// Create object for r
		CTRegularTextRun regulartextrun = dmlObjectFactory.createCTRegularTextRun();
		textparagraph.getEGTextRun().add(regulartextrun);

		// Create object for rPr
		CTTextCharacterProperties textcharacterproperties2 = dmlObjectFactory.createCTTextCharacterProperties();
		regulartextrun.setRPr(textcharacterproperties2);
		textcharacterproperties2.setLang("en-US");
		regulartextrun.setT(chartTitle);

		// Create object for overlay
		CTBoolean overlay = dmlchartObjectFactory.createCTBoolean();
		overlay.setVal(Boolean.FALSE);
		title.setOverlay(overlay);

		return title;
	}

	/**
	 * Create the horizontal axis
	 *
	 * @param categories
	 * @param valueAxisId
	 * @param categoryAxisId
	 *
	 * @return
	 */
	private static CTCatAx createCTCatAx(String categories, long valueAxisId, long categoryAxisId) {
		org.docx4j.dml.chart.ObjectFactory dmlchartObjectFactory = new org.docx4j.dml.chart.ObjectFactory();

		CTCatAx axis = dmlchartObjectFactory.createCTCatAx();

		// Create object for title
		axis.setTitle(createCTCatAxTitle(categories));

		// Set the major grid lines
		axis.setMajorGridlines(createGridLines());

		org.docx4j.dml.ObjectFactory dmlObjectFactory = new org.docx4j.dml.ObjectFactory();

		// Create object for spPr
		CTShapeProperties shapeproperties = dmlObjectFactory.createCTShapeProperties();
		axis.setSpPr(shapeproperties);

		// Create object for ln
		CTLineProperties lineproperties = dmlObjectFactory.createCTLineProperties();
		shapeproperties.setLn(lineproperties);

		// Create object for solidFill
		CTSolidColorFillProperties solidcolorfillproperties = dmlObjectFactory.createCTSolidColorFillProperties();
		lineproperties.setSolidFill(solidcolorfillproperties);

		// Create object for schemeClr
		CTSchemeColor schemecolor = dmlObjectFactory.createCTSchemeColor();
		solidcolorfillproperties.setSchemeClr(schemecolor);
		schemecolor.setVal(org.docx4j.dml.STSchemeColorVal.TX_1);

		// Create object for axId
		CTUnsignedInt unsignedint = dmlchartObjectFactory.createCTUnsignedInt();
		unsignedint.setVal(categoryAxisId);
		axis.setAxId(unsignedint);

		// Create object for scaling
		CTScaling scaling = dmlchartObjectFactory.createCTScaling();
		axis.setScaling(scaling);

		// Create object for orientation
		CTOrientation orientation = dmlchartObjectFactory.createCTOrientation();
		scaling.setOrientation(orientation);
		orientation.setVal(org.docx4j.dml.chart.STOrientation.MIN_MAX);

		// Create object for delete
		CTBoolean boolean2 = dmlchartObjectFactory.createCTBoolean();
		boolean2.setVal(Boolean.FALSE);
		axis.setDelete(boolean2);

		// Create object for axPos
		CTAxPos axpos = dmlchartObjectFactory.createCTAxPos();
		axis.setAxPos(axpos);
		axpos.setVal(org.docx4j.dml.chart.STAxPos.T);

		// Create object for majorTickMark
		CTTickMark tickmark = dmlchartObjectFactory.createCTTickMark();
		axis.setMajorTickMark(tickmark);
		tickmark.setVal(org.docx4j.dml.chart.STTickMark.OUT);

		// Create object for minorTickMark
		CTTickMark tickmark2 = dmlchartObjectFactory.createCTTickMark();
		axis.setMinorTickMark(tickmark2);
		tickmark2.setVal(org.docx4j.dml.chart.STTickMark.IN);

		// Create object for tickLblPos
		CTTickLblPos ticklblpos = dmlchartObjectFactory.createCTTickLblPos();
		axis.setTickLblPos(ticklblpos);
		ticklblpos.setVal(org.docx4j.dml.chart.STTickLblPos.LOW);

		// Create object for crossAx
		CTUnsignedInt unsignedint2 = dmlchartObjectFactory.createCTUnsignedInt();
		axis.setCrossAx(unsignedint2);
		unsignedint2.setVal(valueAxisId);

		// Create object for crosses
		CTCrosses crosses = dmlchartObjectFactory.createCTCrosses();
		axis.setCrosses(crosses);
		crosses.setVal(org.docx4j.dml.chart.STCrosses.AUTO_ZERO);

		// This element specifies that this axis is a date or text axis based
		// on the data that is used for the axis labels, not a specific choice.
		CTBoolean boolean3 = dmlchartObjectFactory.createCTBoolean();
		boolean3.setVal(Boolean.FALSE);
		axis.setAuto(boolean3);

		// Create object for lblOffset
		CTLblOffset lbloffset = dmlchartObjectFactory.createCTLblOffset();
		axis.setLblOffset(lbloffset);
		lbloffset.setVal(new Integer(100));

		// Create object for lblAlgn
		CTLblAlgn lblalgn = dmlchartObjectFactory.createCTLblAlgn();
		axis.setLblAlgn(lblalgn);
		lblalgn.setVal(org.docx4j.dml.chart.STLblAlgn.CTR);

		// Create object for noMultiLvlLbl
		CTBoolean boolean4 = dmlchartObjectFactory.createCTBoolean();
		boolean4.setVal(Boolean.FALSE);
		axis.setNoMultiLvlLbl(boolean4);

		return axis;
	}

	/**
	 * Create the title of the horizontal axis and returns it
	 *
	 * @param titleText
	 *
	 * @return the horizontal title
	 */
	private static CTTitle createCTCatAxTitle(String titleText) {

		org.docx4j.dml.chart.ObjectFactory dmlchartObjectFactory = new org.docx4j.dml.chart.ObjectFactory();

		CTTitle title = dmlchartObjectFactory.createCTTitle();

		// Create object for layout
		CTLayout layout = dmlchartObjectFactory.createCTLayout();
		title.setLayout(layout);

		// Create object for tx
		CTTx tx = dmlchartObjectFactory.createCTTx();
		title.setTx(tx);

		org.docx4j.dml.ObjectFactory dmlObjectFactory = new org.docx4j.dml.ObjectFactory();

		// Create object for rich
		CTTextBody textbody = dmlObjectFactory.createCTTextBody();
		tx.setRich(textbody);

		// Create object for lstStyle
		CTTextListStyle textliststyle = dmlObjectFactory.createCTTextListStyle();
		textbody.setLstStyle(textliststyle);

		// Create object for bodyPr
		CTTextBodyProperties textbodyproperties = dmlObjectFactory.createCTTextBodyProperties();
		textbody.setBodyPr(textbodyproperties);

		// Create object for p
		CTTextParagraph textparagraph = dmlObjectFactory.createCTTextParagraph();
		textbody.getP().add(textparagraph);

		// Create object for pPr
		CTTextParagraphProperties textparagraphproperties = dmlObjectFactory.createCTTextParagraphProperties();
		textparagraph.setPPr(textparagraphproperties);

		// Create object for defRPr
		CTTextCharacterProperties textcharacterproperties = dmlObjectFactory.createCTTextCharacterProperties();
		textparagraphproperties.setDefRPr(textcharacterproperties);
		textcharacterproperties.setB(Boolean.FALSE);
		textcharacterproperties.setI(Boolean.FALSE);

		// Create object for r
		CTRegularTextRun regulartextrun = dmlObjectFactory.createCTRegularTextRun();
		textparagraph.getEGTextRun().add(regulartextrun);

		// Create object for rPr
		CTTextCharacterProperties textcharacterproperties2 = dmlObjectFactory.createCTTextCharacterProperties();
		regulartextrun.setRPr(textcharacterproperties2);
		textcharacterproperties2.setLang("en-US");
		regulartextrun.setT(titleText);

		// Create object for overlay
		CTBoolean overlay = dmlchartObjectFactory.createCTBoolean();
		overlay.setVal(Boolean.FALSE);
		title.setOverlay(overlay);

		return title;
	}

	/**
	 * Create the vertical axis
	 *
	 * @param values
	 * @param valueAxisId
	 * @param categoryAxisId
	 *
	 * @return
	 */
	private static CTValAx createCTValAx(String values, long valueAxisId, long categoryAxisId, boolean horizontal) {
		org.docx4j.dml.chart.ObjectFactory dmlchartObjectFactory = new org.docx4j.dml.chart.ObjectFactory();

		CTValAx axis = dmlchartObjectFactory.createCTValAx();

		// Create object for title
		axis.setTitle(createCTValAxTitle(values, horizontal));

		// Set the major grid lines
		axis.setMajorGridlines(createGridLines());

		org.docx4j.dml.ObjectFactory dmlObjectFactory = new org.docx4j.dml.ObjectFactory();

		// Create object for spPr
		CTShapeProperties shapeproperties = dmlObjectFactory.createCTShapeProperties();
		axis.setSpPr(shapeproperties);

		// Create object for ln
		CTLineProperties lineproperties = dmlObjectFactory.createCTLineProperties();
		shapeproperties.setLn(lineproperties);

		// Create object for solidFill
		CTSolidColorFillProperties solidcolorfillproperties = dmlObjectFactory.createCTSolidColorFillProperties();
		lineproperties.setSolidFill(solidcolorfillproperties);

		// Create object for schemeClr
		CTSchemeColor schemecolor = dmlObjectFactory.createCTSchemeColor();
		solidcolorfillproperties.setSchemeClr(schemecolor);
		schemecolor.setVal(org.docx4j.dml.STSchemeColorVal.TX_1);

		// Create object for axId
		CTUnsignedInt unsignedint = dmlchartObjectFactory.createCTUnsignedInt();
		unsignedint.setVal(valueAxisId);
		axis.setAxId(unsignedint);

		// Create object for scaling
		CTScaling scaling = dmlchartObjectFactory.createCTScaling();
		axis.setScaling(scaling);

		// Create object for orientation
		CTOrientation orientation = dmlchartObjectFactory.createCTOrientation();
		scaling.setOrientation(orientation);
		orientation.setVal(org.docx4j.dml.chart.STOrientation.MIN_MAX);

		// Create object for delete
		CTBoolean boolean2 = dmlchartObjectFactory.createCTBoolean();
		boolean2.setVal(Boolean.FALSE);
		axis.setDelete(boolean2);

		// Create object for axPos
		CTAxPos axpos = dmlchartObjectFactory.createCTAxPos();
		axis.setAxPos(axpos);
		axpos.setVal(org.docx4j.dml.chart.STAxPos.L);

		// Create object for majorTickMark
		CTTickMark tickmark = dmlchartObjectFactory.createCTTickMark();
		axis.setMajorTickMark(tickmark);
		tickmark.setVal(org.docx4j.dml.chart.STTickMark.IN);

		// Create object for minorTickMark
		CTTickMark tickmark2 = dmlchartObjectFactory.createCTTickMark();
		axis.setMinorTickMark(tickmark2);
		tickmark2.setVal(org.docx4j.dml.chart.STTickMark.NONE);

		// Create object for tickLblPos
		CTTickLblPos ticklblpos = dmlchartObjectFactory.createCTTickLblPos();
		axis.setTickLblPos(ticklblpos);
		ticklblpos.setVal(org.docx4j.dml.chart.STTickLblPos.NEXT_TO);

		// Create object for crossAx
		CTUnsignedInt unsignedint2 = dmlchartObjectFactory.createCTUnsignedInt();
		axis.setCrossAx(unsignedint2);
		unsignedint2.setVal(categoryAxisId);

		// Create object for crosses
		CTCrosses crosses = dmlchartObjectFactory.createCTCrosses();
		axis.setCrosses(crosses);
		crosses.setVal(org.docx4j.dml.chart.STCrosses.AUTO_ZERO);

		// Create object for dispUnits
		CTDispUnits dispunits = dmlchartObjectFactory.createCTDispUnits();
		dispunits.setBuiltInUnit(new CTBuiltInUnit());
		axis.setDispUnits(dispunits);

		// Create object for crossBetween
		CTCrossBetween crossbetween = dmlchartObjectFactory.createCTCrossBetween();
		axis.setCrossBetween(crossbetween);
		crossbetween.setVal(org.docx4j.dml.chart.STCrossBetween.BETWEEN);

		return axis;
	}

	private static CTTitle createCTValAxTitle(String titleText, boolean horizontal) {

		org.docx4j.dml.chart.ObjectFactory dmlchartObjectFactory = new org.docx4j.dml.chart.ObjectFactory();

		CTTitle title = dmlchartObjectFactory.createCTTitle();

		// Create object for layout
		CTLayout layout = dmlchartObjectFactory.createCTLayout();
		title.setLayout(layout);

		// Create object for tx
		CTTx tx = dmlchartObjectFactory.createCTTx();
		title.setTx(tx);
		org.docx4j.dml.ObjectFactory dmlObjectFactory = new org.docx4j.dml.ObjectFactory();

		// Create object for rich
		CTTextBody textbody = dmlObjectFactory.createCTTextBody();
		tx.setRich(textbody);

		// Create object for lstStyle
		CTTextListStyle textliststyle = dmlObjectFactory.createCTTextListStyle();
		textbody.setLstStyle(textliststyle);

		// Create object for bodyPr
		CTTextBodyProperties textbodyproperties = dmlObjectFactory.createCTTextBodyProperties();
		textbody.setBodyPr(textbodyproperties);
		textbodyproperties.setVert(org.docx4j.dml.STTextVerticalType.HORZ);

		// Rotate if the axis is vertical
		if (!horizontal) {
			textbodyproperties.setRot(new Integer(-5400000));
		}

		// Create object for p
		CTTextParagraph textparagraph = dmlObjectFactory.createCTTextParagraph();
		textbody.getP().add(textparagraph);

		// Create object for pPr
		CTTextParagraphProperties textparagraphproperties = dmlObjectFactory.createCTTextParagraphProperties();
		textparagraph.setPPr(textparagraphproperties);

		// Create object for defRPr
		CTTextCharacterProperties textcharacterproperties = dmlObjectFactory.createCTTextCharacterProperties();
		textparagraphproperties.setDefRPr(textcharacterproperties);
		textcharacterproperties.setB(Boolean.FALSE);
		textcharacterproperties.setI(Boolean.FALSE);

		// Create object for r
		CTRegularTextRun regulartextrun = dmlObjectFactory.createCTRegularTextRun();
		textparagraph.getEGTextRun().add(regulartextrun);

		// Create object for rPr
		CTTextCharacterProperties textcharacterproperties2 = dmlObjectFactory.createCTTextCharacterProperties();
		regulartextrun.setRPr(textcharacterproperties2);
		textcharacterproperties2.setLang("en-US");
		regulartextrun.setT(titleText);

		// Create object for overlay
		CTBoolean overlay = dmlchartObjectFactory.createCTBoolean();
		overlay.setVal(Boolean.FALSE);
		title.setOverlay(overlay);

		return title;
	}

	private static CTChartLines createGridLines() {
		org.docx4j.dml.chart.ObjectFactory dmlchartObjectFactory = new org.docx4j.dml.chart.ObjectFactory();

		CTChartLines chartlines = dmlchartObjectFactory.createCTChartLines();
		org.docx4j.dml.ObjectFactory dmlObjectFactory = new org.docx4j.dml.ObjectFactory();

		// Create object for spPr
		CTShapeProperties shapeproperties = dmlObjectFactory.createCTShapeProperties();
		chartlines.setSpPr(shapeproperties);

		// Create object for ln
		CTLineProperties lineproperties = dmlObjectFactory.createCTLineProperties();
		shapeproperties.setLn(lineproperties);

		// Create object for prstDash
		CTPresetLineDashProperties presetlinedashproperties = dmlObjectFactory.createCTPresetLineDashProperties();
		lineproperties.setPrstDash(presetlinedashproperties);
		presetlinedashproperties.setVal(org.docx4j.dml.STPresetLineDashVal.SYS_DOT);
		lineproperties.setW(new Integer(3175));

		return chartlines;
	}

	private static CTDLbls createLabels(org.docx4j.dml.chart.ObjectFactory dmlchartObjectFactory) {
		// Create object for dLbls
		CTDLbls dlbls = dmlchartObjectFactory.createCTDLbls();

		// Create object for showLegendKey
		CTBoolean boolean4 = dmlchartObjectFactory.createCTBoolean();
		boolean4.setVal(Boolean.FALSE);
		dlbls.setShowLegendKey(boolean4);

		// Create object for showVal
		CTBoolean boolean5 = dmlchartObjectFactory.createCTBoolean();
		boolean5.setVal(Boolean.FALSE);
		dlbls.setShowVal(boolean5);

		// Create object for showCatName
		CTBoolean boolean6 = dmlchartObjectFactory.createCTBoolean();
		boolean6.setVal(Boolean.FALSE);
		dlbls.setShowCatName(boolean6);

		// Create object for showSerName
		CTBoolean boolean7 = dmlchartObjectFactory.createCTBoolean();
		boolean7.setVal(Boolean.FALSE);
		dlbls.setShowSerName(boolean7);

		// Create object for showPercent
		CTBoolean boolean8 = dmlchartObjectFactory.createCTBoolean();
		boolean8.setVal(Boolean.FALSE);
		dlbls.setShowPercent(boolean8);

		// Create object for showBubbleSize
		CTBoolean boolean9 = dmlchartObjectFactory.createCTBoolean();
		boolean9.setVal(Boolean.FALSE);
		dlbls.setShowBubbleSize(boolean9);

		return dlbls;
	}

	/**
	 * Office 2011 creates an empty layout Office 2010 (on Windows) adds a
	 * manualLayout element.
	 *
	 * @return
	 */
	private static CTLayout createLayout() {
		org.docx4j.dml.chart.ObjectFactory dmlchartObjectFactory = new org.docx4j.dml.chart.ObjectFactory();
		CTLayout layout = dmlchartObjectFactory.createCTLayout();
		// Create object for manualLayout
		CTManualLayout manuallayout = dmlchartObjectFactory.createCTManualLayout();
		layout.setManualLayout(manuallayout);
		// Create object for h
		CTDouble h = dmlchartObjectFactory.createCTDouble();
		h.setVal(0.65701099862517198);
		manuallayout.setH(h);
		// Create object for layoutTarget
		CTLayoutTarget layouttarget = dmlchartObjectFactory.createCTLayoutTarget();
		manuallayout.setLayoutTarget(layouttarget);
		layouttarget.setVal(org.docx4j.dml.chart.STLayoutTarget.INNER);
		// Create object for xMode
		CTLayoutMode layoutmode = dmlchartObjectFactory.createCTLayoutMode();
		manuallayout.setXMode(layoutmode);
		layoutmode.setVal(org.docx4j.dml.chart.STLayoutMode.EDGE);
		// Create object for yMode
		CTLayoutMode layoutmode2 = dmlchartObjectFactory.createCTLayoutMode();
		manuallayout.setYMode(layoutmode2);
		layoutmode2.setVal(org.docx4j.dml.chart.STLayoutMode.EDGE);
		// Create object for w
		CTDouble double2 = dmlchartObjectFactory.createCTDouble();
		double2.setVal(0.83550470253718301);
		manuallayout.setW(double2);
		// Create object for y
		CTDouble double3 = dmlchartObjectFactory.createCTDouble();
		double3.setVal(0.17619047619047601);
		manuallayout.setY(double3);
		// Create object for x
		CTDouble double4 = dmlchartObjectFactory.createCTDouble();
		double4.setVal(0.14366196412948401);
		manuallayout.setX(double4);

		return layout;
	}

	private static CTLegend createLegend() {

		org.docx4j.dml.chart.ObjectFactory dmlchartObjectFactory = new org.docx4j.dml.chart.ObjectFactory();

		CTLegend legend = dmlchartObjectFactory.createCTLegend();
		// Create object for layout
		CTLayout layout = dmlchartObjectFactory.createCTLayout();
		legend.setLayout(layout);
		// Create object for overlay
		CTBoolean overlay = dmlchartObjectFactory.createCTBoolean();
		overlay.setVal(Boolean.TRUE);
		legend.setOverlay(overlay);
		// Create object for legendPos
		CTLegendPos legendpos = dmlchartObjectFactory.createCTLegendPos();
		legend.setLegendPos(legendpos);
		legendpos.setVal(org.docx4j.dml.chart.STLegendPos.R);

		// Add formatting to legend.
		org.docx4j.dml.ObjectFactory dmlObjectFactory = new org.docx4j.dml.ObjectFactory();
		CTShapeProperties shapeproperties = dmlObjectFactory.createCTShapeProperties();

		// Create object for solidFill
		CTSolidColorFillProperties solidcolorfillproperties = dmlObjectFactory.createCTSolidColorFillProperties();
		shapeproperties.setSolidFill(solidcolorfillproperties);
		// Create object for schemeClr
		CTSchemeColor schemecolor = dmlObjectFactory.createCTSchemeColor();
		solidcolorfillproperties.setSchemeClr(schemecolor);
		schemecolor.setVal(org.docx4j.dml.STSchemeColorVal.BG_1);
		// Create object for ln
		CTLineProperties lineproperties = dmlObjectFactory.createCTLineProperties();
		shapeproperties.setLn(lineproperties);
		// Create object for solidFill
		CTSolidColorFillProperties solidcolorfillproperties2 = dmlObjectFactory.createCTSolidColorFillProperties();
		lineproperties.setSolidFill(solidcolorfillproperties2);
		// Create object for schemeClr
		CTSchemeColor schemecolor2 = dmlObjectFactory.createCTSchemeColor();
		solidcolorfillproperties2.setSchemeClr(schemecolor2);
		schemecolor2.setVal(org.docx4j.dml.STSchemeColorVal.TX_1);
		legend.setSpPr(shapeproperties);
		return legend;
	}

	/**
	 * Creates the data source for the vertical axis.
	 *
	 * @param dmlchartObjectFactory
	 * @param data
	 *            the array to create a data set from
	 * @return a data set to for use in a chart
	 */
	private static CTNumDataSource createValuesDataSource(ObjectFactory dmlchartObjectFactory, double[] data) {
		CTNumDataSource datasource = dmlchartObjectFactory.createCTNumDataSource();

		// Create object for numCache
		CTNumData numdata = dmlchartObjectFactory.createCTNumData();
		datasource.setNumLit(numdata);

		double max = 0;
		double min = 0;

		for (int i = 0; i < data.length; i++) {
			CTNumVal numval = dmlchartObjectFactory.createCTNumVal();
			numdata.getPt().add(numval);
			numval.setIdx(i);
			if (Double.isNaN(data[i])) {
				numval.setV(""); // NaN must be represented as empty string
									// (tested in MS Word 2010 on Windows 7)
			} else {
				if (data[i] > max) {
					max = data[i];
				}
				if (data[i] < min) {
					min = data[i];
				}
				numval.setV(Double.toString(data[i]));
			}
		}
		Range range = setRange(max, min);
		numdata.setFormatCode(range.format);

		// Create object for ptCount
		CTUnsignedInt unsignedint = dmlchartObjectFactory.createCTUnsignedInt();
		numdata.setPtCount(unsignedint);
		unsignedint.setVal(data.length);
		return datasource;
	}
}

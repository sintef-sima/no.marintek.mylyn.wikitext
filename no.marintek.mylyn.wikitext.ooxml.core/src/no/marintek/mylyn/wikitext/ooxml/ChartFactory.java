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
import org.docx4j.dml.chart.CTBoolean;
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
import org.docx4j.dml.chart.CTMarkerStyle;
import org.docx4j.dml.chart.CTNumData;
import org.docx4j.dml.chart.CTNumDataSource;
import org.docx4j.dml.chart.CTNumFmt;
import org.docx4j.dml.chart.CTNumRef;
import org.docx4j.dml.chart.CTNumVal;
import org.docx4j.dml.chart.CTOrientation;
import org.docx4j.dml.chart.CTPlotArea;
import org.docx4j.dml.chart.CTScaling;
import org.docx4j.dml.chart.CTSerTx;
import org.docx4j.dml.chart.CTSkip;
import org.docx4j.dml.chart.CTStrData;
import org.docx4j.dml.chart.CTStrRef;
import org.docx4j.dml.chart.CTStrVal;
import org.docx4j.dml.chart.CTStyle;
import org.docx4j.dml.chart.CTTextLanguageID;
import org.docx4j.dml.chart.CTTickLblPos;
import org.docx4j.dml.chart.CTTickMark;
import org.docx4j.dml.chart.CTTitle;
import org.docx4j.dml.chart.CTTx;
import org.docx4j.dml.chart.CTUnsignedInt;
import org.docx4j.dml.chart.CTValAx;
import org.docx4j.dml.chart.ObjectFactory;

/**
 * A factory for creating charts for OOXML documents. It will take a maximum of
 * eight series in each chart and supports separate data for the x and y axis of
 * each series.
 *
 * @author Torkild U. Resheim
 * @since 1.0
 */
public class ChartFactory {

	private final static byte[][] COLOUR_SCHEME = new byte[][] { { 1, 2, 2 }, // black
			{ (byte) 235, 49, 55 }, // red
			{ 18, (byte) 139, 75 }, // green
			{ 29, 92, (byte) 167 }, // blue
			{ (byte) 252, 125, 50 }, // orange
			{ 101, 48, (byte) 143 }, // purple
			{ (byte) 160, 31, 38 }, // burgundy
			{ (byte) 178, 60, (byte) 147 } // pink
	};

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
	public static CTChartSpace createChartSpace(String title, String[] legends, String ylabel, String xlabel, double[][] ySeries, double[][] xSeries)
			throws JAXBException {

		if ((xSeries.length > COLOUR_SCHEME.length) || (ySeries.length > COLOUR_SCHEME.length)) {
			throw new IllegalArgumentException("The maximum number of series in one chart is " + COLOUR_SCHEME.length);
		}

		org.docx4j.dml.chart.ObjectFactory dmlchartObjectFactory = new org.docx4j.dml.chart.ObjectFactory();

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
		// Create object for valAx
		CTLineChart linechart = dmlchartObjectFactory.createCTLineChart();
		plotarea.getAreaChartOrArea3DChartOrLineChart().add(linechart);
		// Create object for marker
		CTBoolean boolean3 = dmlchartObjectFactory.createCTBoolean();
		boolean3.setVal(Boolean.TRUE);
		linechart.setMarker(boolean3);

		// Create object for dLbls
		CTDLbls dlbls = dmlchartObjectFactory.createCTDLbls();
		linechart.setDLbls(dlbls);

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

		for (int series = 0; series < xSeries.length; series++) {
			addSeries(legends, ylabel, xlabel, ySeries[series], xSeries[series], dmlchartObjectFactory, valueAxisId, categoryAxisId, dmlObjectFactory,
					plotarea, linechart, series, series);
		}

		// Create object for catAx
		CTCatAx createCTCatAx = createCTCatAx(xlabel, valueAxisId, categoryAxisId);
		// A maximum of 10 major tick marks for the horizontal axis
		int skip = xSeries[0].length / 10;
		CTSkip createCTSkip = dmlchartObjectFactory.createCTSkip();
		createCTSkip.setVal(skip);
		createCTCatAx.setTickMarkSkip(createCTSkip);
		createCTCatAx.setTickLblSkip(createCTSkip);
		plotarea.getValAxOrCatAxOrDateAx().add(createCTCatAx);

		// Create object for valAx
		plotarea.getValAxOrCatAxOrDateAx().add(createCTValAx(ylabel, valueAxisId, categoryAxisId));

		// Add a legend
		if (legends.length>1) {
			chart.setLegend(createLegend());
		}

		// Create object for plotVisOnly
		CTBoolean boolean19 = dmlchartObjectFactory.createCTBoolean();
		boolean19.setVal(Boolean.TRUE);
		chart.setPlotVisOnly(boolean19);

		// Create object for dispBlanksAs
		CTDispBlanksAs dispblanksas = dmlchartObjectFactory.createCTDispBlanksAs();
		chart.setDispBlanksAs(dispblanksas);
		dispblanksas.setVal(org.docx4j.dml.chart.STDispBlanksAs.GAP);

		// Create object for showDLblsOverMax
		CTBoolean boolean20 = dmlchartObjectFactory.createCTBoolean();
		boolean20.setVal(Boolean.FALSE);
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

		return chartspace;
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

	private static void addSeries(String[] title, String ylabel, String xlabel, double[] ySeries, double[] xSeries,
			org.docx4j.dml.chart.ObjectFactory dmlchartObjectFactory, int valueAxisId, int categoryAxisId,
			org.docx4j.dml.ObjectFactory dmlObjectFactory, CTPlotArea plotarea, CTLineChart linechart, int order, int index) {
		// Create object for dispUnits
		CTLineSer lineser = dmlchartObjectFactory.createCTLineSer();
		linechart.getSer().add(lineser);
		// Create object for order
		CTUnsignedInt unsignedint3 = dmlchartObjectFactory.createCTUnsignedInt();
		lineser.setOrder(unsignedint3);
		unsignedint3.setVal(order);
		// Create object for val
		// lineser.setVal(createCTNumDataSource());
		lineser.setVal(createValues(dmlchartObjectFactory, ySeries));
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
		
		// Create object for strRef
		CTStrRef strref = dmlchartObjectFactory.createCTStrRef();
		sertx.setStrRef(strref);
		
		// strref.setF("'Ark1'!$B$1");
		// Create object for strCache
		CTStrData strdata = dmlchartObjectFactory.createCTStrData();
		// Create object for pt
		CTStrVal strval = dmlchartObjectFactory.createCTStrVal();
		strdata.getPt().add(strval);
		strval.setIdx(0);
		strval.setV(title[index]);
		// Create object for ptCount
		CTUnsignedInt count = dmlchartObjectFactory.createCTUnsignedInt();
		count.setVal(1);
		strdata.setPtCount(count);
		strref.setStrCache(strdata);
		// Create object for marker
		CTMarker marker = dmlchartObjectFactory.createCTMarker();
		lineser.setMarker(marker);
		// Create object for symbol
		CTMarkerStyle markerstyle = dmlchartObjectFactory.createCTMarkerStyle();
		marker.setSymbol(markerstyle);
		markerstyle.setVal(org.docx4j.dml.chart.STMarkerStyle.NONE);
		// Create object for smooth
		CTBoolean boolean12 = dmlchartObjectFactory.createCTBoolean();
		boolean12.setVal(Boolean.FALSE);
		lineser.setSmooth(boolean12);
		lineser.setCat(createCategories(dmlchartObjectFactory, xSeries));

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

	/**
	 * Create the vertical axis
	 *
	 * @param values
	 * @param valueAxisId
	 * @param categoryAxisId
	 *
	 * @return
	 */
	private static CTValAx createCTValAx(String values, long valueAxisId, long categoryAxisId) {
		org.docx4j.dml.chart.ObjectFactory dmlchartObjectFactory = new org.docx4j.dml.chart.ObjectFactory();
		CTValAx valax = dmlchartObjectFactory.createCTValAx();

		// Create object for title
		valax.setTitle(createCTValAxTitle(values));

		// Set the major grid lines
		valax.setMajorGridlines(createGridLines());

		org.docx4j.dml.ObjectFactory dmlObjectFactory = new org.docx4j.dml.ObjectFactory();

		// Create object for numFmt
		CTNumFmt numfmt = dmlchartObjectFactory.createCTNumFmt();
		valax.setNumFmt(numfmt);
		numfmt.setFormatCode("General");

		// Create object for spPr
		CTShapeProperties shapeproperties = dmlObjectFactory.createCTShapeProperties();
		valax.setSpPr(shapeproperties);

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
		valax.setAxId(unsignedint);

		// Create object for scaling
		CTScaling scaling = dmlchartObjectFactory.createCTScaling();
		valax.setScaling(scaling);

		// Create object for orientation
		CTOrientation orientation = dmlchartObjectFactory.createCTOrientation();
		scaling.setOrientation(orientation);
		orientation.setVal(org.docx4j.dml.chart.STOrientation.MIN_MAX);

		// Create object for delete
		CTBoolean boolean2 = dmlchartObjectFactory.createCTBoolean();
		boolean2.setVal(Boolean.FALSE);
		valax.setDelete(boolean2);

		// Create object for axPos
		CTAxPos axpos = dmlchartObjectFactory.createCTAxPos();
		valax.setAxPos(axpos);
		axpos.setVal(org.docx4j.dml.chart.STAxPos.L);

		// Create object for majorTickMark
		CTTickMark tickmark = dmlchartObjectFactory.createCTTickMark();
		valax.setMajorTickMark(tickmark);
		tickmark.setVal(org.docx4j.dml.chart.STTickMark.IN);

		// Create object for minorTickMark
		CTTickMark tickmark2 = dmlchartObjectFactory.createCTTickMark();
		valax.setMinorTickMark(tickmark2);
		tickmark2.setVal(org.docx4j.dml.chart.STTickMark.NONE);

		// Create object for tickLblPos
		CTTickLblPos ticklblpos = dmlchartObjectFactory.createCTTickLblPos();
		valax.setTickLblPos(ticklblpos);
		ticklblpos.setVal(org.docx4j.dml.chart.STTickLblPos.NEXT_TO);

		// Create object for crossAx
		CTUnsignedInt unsignedint2 = dmlchartObjectFactory.createCTUnsignedInt();
		valax.setCrossAx(unsignedint2);
		unsignedint2.setVal(categoryAxisId);

		// Create object for crosses
		CTCrosses crosses = dmlchartObjectFactory.createCTCrosses();
		valax.setCrosses(crosses);
		crosses.setVal(org.docx4j.dml.chart.STCrosses.AUTO_ZERO);

		// Create object for dispUnits
		CTDispUnits dispunits = dmlchartObjectFactory.createCTDispUnits();
		valax.setDispUnits(dispunits);

		// Create object for crossBetween
		CTCrossBetween crossbetween = dmlchartObjectFactory.createCTCrossBetween();
		valax.setCrossBetween(crossbetween);
		crossbetween.setVal(org.docx4j.dml.chart.STCrossBetween.BETWEEN);

		return valax;
	}

	private static CTTitle createCTValAxTitle(String values) {

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
		textbodyproperties.setRot(new Integer(-5400000));
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
		regulartextrun.setT(values);

		// Create object for overlay
		CTBoolean overlay = dmlchartObjectFactory.createCTBoolean();
		overlay.setVal(Boolean.FALSE);
		title.setOverlay(overlay);

		return title;
	}

	/**
	 * Create the title of the horizontal axis and returns it
	 *
	 * @param categories
	 *
	 * @return the horizontal title
	 */
	private static CTTitle createCTCatAxTitle(String categories) {

		org.docx4j.dml.chart.ObjectFactory dmlchartObjectFactory = new org.docx4j.dml.chart.ObjectFactory();

		CTTitle title = dmlchartObjectFactory.createCTTitle();

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
		regulartextrun.setT(categories);

		// Create object for layout
		CTLayout layout = dmlchartObjectFactory.createCTLayout();
		title.setLayout(layout);

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

		CTCatAx catax = dmlchartObjectFactory.createCTCatAx();

		// Create object for title
		org.docx4j.dml.ObjectFactory dmlObjectFactory = new org.docx4j.dml.ObjectFactory();
		catax.setTitle(createCTCatAxTitle(categories));

		// Set the major grid lines
		catax.setMajorGridlines(createGridLines());

		// Create object for numFmt
		CTNumFmt numfmt = dmlchartObjectFactory.createCTNumFmt();
		catax.setNumFmt(numfmt);
		numfmt.setFormatCode("General");

		// Create object for spPr
		CTShapeProperties shapeproperties = dmlObjectFactory.createCTShapeProperties();
		catax.setSpPr(shapeproperties);

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
		catax.setAxId(unsignedint);

		// Create object for scaling
		CTScaling scaling = dmlchartObjectFactory.createCTScaling();
		catax.setScaling(scaling);

		// Create object for orientation
		CTOrientation orientation = dmlchartObjectFactory.createCTOrientation();
		scaling.setOrientation(orientation);
		orientation.setVal(org.docx4j.dml.chart.STOrientation.MIN_MAX);

		// Create object for delete
		CTBoolean boolean2 = dmlchartObjectFactory.createCTBoolean();
		boolean2.setVal(Boolean.FALSE);
		catax.setDelete(boolean2);

		// Create object for axPos
		CTAxPos axpos = dmlchartObjectFactory.createCTAxPos();
		catax.setAxPos(axpos);
		axpos.setVal(org.docx4j.dml.chart.STAxPos.B);

		// Create object for majorTickMark
		CTTickMark tickmark = dmlchartObjectFactory.createCTTickMark();
		catax.setMajorTickMark(tickmark);
		tickmark.setVal(org.docx4j.dml.chart.STTickMark.IN);

		// Create object for minorTickMark
		CTTickMark tickmark2 = dmlchartObjectFactory.createCTTickMark();
		catax.setMinorTickMark(tickmark2);
		tickmark2.setVal(org.docx4j.dml.chart.STTickMark.NONE);

		// Create object for tickLblPos
		CTTickLblPos ticklblpos = dmlchartObjectFactory.createCTTickLblPos();
		catax.setTickLblPos(ticklblpos);
		ticklblpos.setVal(org.docx4j.dml.chart.STTickLblPos.NEXT_TO);

		// Create object for crossAx
		CTUnsignedInt unsignedint2 = dmlchartObjectFactory.createCTUnsignedInt();
		catax.setCrossAx(unsignedint2);
		unsignedint2.setVal(valueAxisId);

		// Create object for crosses
		CTCrosses crosses = dmlchartObjectFactory.createCTCrosses();
		catax.setCrosses(crosses);
		crosses.setVal(org.docx4j.dml.chart.STCrosses.AUTO_ZERO);

		// Create object for auto
		CTBoolean boolean3 = dmlchartObjectFactory.createCTBoolean();
		boolean3.setVal(Boolean.TRUE);
		catax.setAuto(boolean3);

		// Create object for lblOffset
		CTLblOffset lbloffset = dmlchartObjectFactory.createCTLblOffset();
		catax.setLblOffset(lbloffset);
		lbloffset.setVal(new Integer(100));

		// Create object for lblAlgn
		CTLblAlgn lblalgn = dmlchartObjectFactory.createCTLblAlgn();
		catax.setLblAlgn(lblalgn);
		lblalgn.setVal(org.docx4j.dml.chart.STLblAlgn.CTR);

		// Create object for noMultiLvlLbl
		CTBoolean boolean4 = dmlchartObjectFactory.createCTBoolean();
		boolean4.setVal(Boolean.FALSE);
		catax.setNoMultiLvlLbl(boolean4);

		return catax;
	}

	/**
	 * Creates the data source for the vertical axis.
	 *
	 * @param dmlchartObjectFactory
	 * @param data
	 *            the array to create a data set from
	 * @return a data set to for use in a chart
	 */
	private static CTNumDataSource createValues(ObjectFactory dmlchartObjectFactory, double[] data) {
		CTNumDataSource datasource = dmlchartObjectFactory.createCTNumDataSource();
		// Create object for numRef
		CTNumRef numref = dmlchartObjectFactory.createCTNumRef();
		datasource.setNumRef(numref);

		// Create object for numCache
		CTNumData numdata = dmlchartObjectFactory.createCTNumData();
		numref.setNumCache(numdata);

		for (int i = 0; i < data.length; i++) {
			CTNumVal numval = dmlchartObjectFactory.createCTNumVal();
			numdata.getPt().add(numval);
			numval.setIdx(i);
			numval.setV(Double.toString(data[i]));
		}
		numdata.setFormatCode("General");

		// Create object for ptCount
		CTUnsignedInt unsignedint = dmlchartObjectFactory.createCTUnsignedInt();
		numdata.setPtCount(unsignedint);
		unsignedint.setVal(data.length);
		return datasource;
	}

	/**
	 * Creates the data source for the horizontal axis.
	 *
	 * @param dmlchartObjectFactory
	 * @param data
	 * @return
	 */
	private static CTAxDataSource createCategories(ObjectFactory dmlchartObjectFactory, double[] data) {
		CTAxDataSource datasource = dmlchartObjectFactory.createCTAxDataSource();

		// Create object for numRef
		CTNumRef numref = dmlchartObjectFactory.createCTNumRef();
		datasource.setNumRef(numref);

		// Create object for numCache
		CTNumData numdata = dmlchartObjectFactory.createCTNumData();
		numref.setNumCache(numdata);

		for (int i = 0; i < data.length; i++) {
			CTNumVal numval = dmlchartObjectFactory.createCTNumVal();
			numdata.getPt().add(numval);
			numval.setIdx(i);
			numval.setV(Double.toString(data[i]));
		}

		numdata.setFormatCode("General");

		// Create object for ptCount
		CTUnsignedInt unsignedint = dmlchartObjectFactory.createCTUnsignedInt();
		numdata.setPtCount(unsignedint);
		unsignedint.setVal(data.length);

		return datasource;
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
		textcharacterproperties.setSmtId(new Long(0));
		// Create object for r
		CTRegularTextRun regulartextrun = dmlObjectFactory.createCTRegularTextRun();
		textparagraph.getEGTextRun().add(regulartextrun);
		// Create object for rPr
		CTTextCharacterProperties textcharacterproperties2 = dmlObjectFactory.createCTTextCharacterProperties();
		regulartextrun.setRPr(textcharacterproperties2);
		textcharacterproperties2.setLang("en-US");
		textcharacterproperties2.setSmtId(new Long(0));
		regulartextrun.setT(chartTitle);
		// Create object for overlay
		CTBoolean overlay = dmlchartObjectFactory.createCTBoolean();
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
}

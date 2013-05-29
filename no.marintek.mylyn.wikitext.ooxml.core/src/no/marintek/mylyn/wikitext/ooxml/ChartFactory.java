package no.marintek.mylyn.wikitext.ooxml;

import java.lang.Integer;

import javax.xml.bind.JAXBElement;

import org.docx4j.dml.CTLineProperties;
import org.docx4j.dml.CTShapeProperties;
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
import org.docx4j.dml.chart.CTExternalData;
import org.docx4j.dml.chart.CTGrouping;
import org.docx4j.dml.chart.CTLayout;
import org.docx4j.dml.chart.CTLblAlgn;
import org.docx4j.dml.chart.CTLblOffset;
import org.docx4j.dml.chart.CTLegend;
import org.docx4j.dml.chart.CTLegendPos;
import org.docx4j.dml.chart.CTLineChart;
import org.docx4j.dml.chart.CTLineSer;
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
import org.docx4j.dml.chart.CTStyle;
import org.docx4j.dml.chart.CTTextLanguageID;
import org.docx4j.dml.chart.CTTickLblPos;
import org.docx4j.dml.chart.CTTickMark;
import org.docx4j.dml.chart.CTTitle;
import org.docx4j.dml.chart.CTUnsignedInt;
import org.docx4j.dml.chart.CTValAx;

public class ChartFactory {

	public static CTChartSpace createIt() {

		org.docx4j.dml.chart.ObjectFactory dmlchartObjectFactory = new org.docx4j.dml.chart.ObjectFactory();

		CTChartSpace chartspace = dmlchartObjectFactory.createCTChartSpace();
		// Create object for style
		CTStyle style = dmlchartObjectFactory.createCTStyle();
		chartspace.setStyle(style);
		// Create object for lang
		CTTextLanguageID textlanguageid = dmlchartObjectFactory.createCTTextLanguageID();
		chartspace.setLang(textlanguageid);
		textlanguageid.setVal("nb-NO");
		// Create object for chart
		CTChart chart = dmlchartObjectFactory.createCTChart();
		chartspace.setChart(chart);
		// Create object for title
		CTTitle title = dmlchartObjectFactory.createCTTitle();
		chart.setTitle(title);
		// Create object for layout
		CTLayout layout = dmlchartObjectFactory.createCTLayout();
		title.setLayout(layout);
		// Create object for overlay
		CTBoolean b = dmlchartObjectFactory.createCTBoolean();
		title.setOverlay(b);
		// Create object for autoTitleDeleted
		CTBoolean boolean2 = dmlchartObjectFactory.createCTBoolean();
		chart.setAutoTitleDeleted(boolean2);
		// Create object for plotArea
		CTPlotArea plotarea = dmlchartObjectFactory.createCTPlotArea();
		chart.setPlotArea(plotarea);
		// Create object for layout
		CTLayout layout2 = dmlchartObjectFactory.createCTLayout();
		plotarea.setLayout(layout2);
		// Create object for valAx
		CTLineChart linechart = dmlchartObjectFactory.createCTLineChart();
		plotarea.getAreaChartOrArea3DChartOrLineChart().add(linechart);
		// Create object for crosses
		CTUnsignedInt unsignedint = dmlchartObjectFactory.createCTUnsignedInt();
		linechart.getAxId().add(unsignedint);
		unsignedint.setVal(-2127695320);
		// Create object for crossBetween
		CTUnsignedInt unsignedint2 = dmlchartObjectFactory.createCTUnsignedInt();
		linechart.getAxId().add(unsignedint2);
		unsignedint2.setVal(-2127713048);
		// Create object for grouping
		CTGrouping grouping = dmlchartObjectFactory.createCTGrouping();
		linechart.setGrouping(grouping);
		grouping.setVal(org.docx4j.dml.chart.STGrouping.STANDARD);
		// Create object for varyColors
		CTBoolean boolean3 = dmlchartObjectFactory.createCTBoolean();
		boolean3.setVal(Boolean.FALSE);
		linechart.setVaryColors(boolean3);
		// Create object for crossBetween
		CTLineSer lineser = dmlchartObjectFactory.createCTLineSer();
		linechart.getSer().add(lineser);
		// Create object for order
//		CTUnsignedInt unsignedint3 = dmlchartObjectFactory.createCTUnsignedInt();
//		lineser.setOrder(unsignedint3);
//		unsignedint3.setVal(0);
		
		// Create object for val
		lineser.setVal(createData());

		org.docx4j.dml.ObjectFactory dmlObjectFactory = new org.docx4j.dml.ObjectFactory();
		// Create object for spPr
		CTShapeProperties shapeproperties = dmlObjectFactory.createCTShapeProperties();
		lineser.setSpPr(shapeproperties);
		// Create object for ln
		CTLineProperties lineproperties = dmlObjectFactory.createCTLineProperties();
		shapeproperties.setLn(lineproperties);
		lineproperties.setW(new Integer(12700));
		// Create object for idx
		CTUnsignedInt unsignedint5 = dmlchartObjectFactory.createCTUnsignedInt();
		lineser.setIdx(unsignedint5);
		unsignedint5.setVal(0);
		// Create object for marker
//		CTMarker marker = dmlchartObjectFactory.createCTMarker();
//		lineser.setMarker(marker);
		// Create object for symbol
//		CTMarkerStyle markerstyle = dmlchartObjectFactory.createCTMarkerStyle();
//		marker.setSymbol(markerstyle);
//		markerstyle.setVal(org.docx4j.dml.chart.STMarkerStyle.NONE);
		// Create object for smooth
		CTBoolean boolean4 = dmlchartObjectFactory.createCTBoolean();
		lineser.setSmooth(boolean4);
		// Create object for marker
//		CTBoolean boolean5 = dmlchartObjectFactory.createCTBoolean();
//		linechart.setMarker(boolean5);
		// Create object for dLbls
//		CTDLbls dlbls = dmlchartObjectFactory.createCTDLbls();
//		linechart.setDLbls(dlbls);
		// Create object for showLegendKey
//		CTBoolean boolean6 = dmlchartObjectFactory.createCTBoolean();
//		dlbls.setShowLegendKey(boolean6);
		// Create object for showVal
//		CTBoolean boolean7 = dmlchartObjectFactory.createCTBoolean();
//		dlbls.setShowVal(boolean7);
		// Create object for showCatName
//		CTBoolean boolean8 = dmlchartObjectFactory.createCTBoolean();
//		dlbls.setShowCatName(boolean8);
		// Create object for showSerName
//		CTBoolean boolean9 = dmlchartObjectFactory.createCTBoolean();
//		dlbls.setShowSerName(boolean9);
		// Create object for showPercent
//		CTBoolean boolean10 = dmlchartObjectFactory.createCTBoolean();
//		dlbls.setShowPercent(boolean10);
		// Create object for showBubbleSize
//		CTBoolean boolean11 = dmlchartObjectFactory.createCTBoolean();
//		dlbls.setShowBubbleSize(boolean11);
		// Create object for smooth
		CTBoolean boolean12 = dmlchartObjectFactory.createCTBoolean();
		linechart.setSmooth(boolean12);
		// Create object for catAx
		CTCatAx catax = dmlchartObjectFactory.createCTCatAx();
		plotarea.getValAxOrCatAxOrDateAx().add(catax);
		// Create object for axId
		CTUnsignedInt unsignedint6 = dmlchartObjectFactory.createCTUnsignedInt();
		catax.setAxId(unsignedint6);
		unsignedint6.setVal(-2127695320);
		// Create object for scaling
		CTScaling scaling = dmlchartObjectFactory.createCTScaling();
		catax.setScaling(scaling);
		// Create object for orientation
		CTOrientation orientation = dmlchartObjectFactory.createCTOrientation();
		scaling.setOrientation(orientation);
		orientation.setVal(org.docx4j.dml.chart.STOrientation.MIN_MAX);
		// Create object for delete
		CTBoolean boolean13 = dmlchartObjectFactory.createCTBoolean();
		catax.setDelete(boolean13);
		// Create object for axPos
		CTAxPos axpos = dmlchartObjectFactory.createCTAxPos();
		catax.setAxPos(axpos);
		axpos.setVal(org.docx4j.dml.chart.STAxPos.B);
		// Create object for majorTickMark
		CTTickMark tickmark = dmlchartObjectFactory.createCTTickMark();
		catax.setMajorTickMark(tickmark);
		tickmark.setVal(org.docx4j.dml.chart.STTickMark.NONE);
		// Create object for minorTickMark
		CTTickMark tickmark2 = dmlchartObjectFactory.createCTTickMark();
		catax.setMinorTickMark(tickmark2);
		tickmark2.setVal(org.docx4j.dml.chart.STTickMark.NONE);
		// Create object for tickLblPos
		CTTickLblPos ticklblpos = dmlchartObjectFactory.createCTTickLblPos();
		catax.setTickLblPos(ticklblpos);
		ticklblpos.setVal(org.docx4j.dml.chart.STTickLblPos.NEXT_TO);
		// Create object for crossAx
		CTUnsignedInt unsignedint7 = dmlchartObjectFactory.createCTUnsignedInt();
		catax.setCrossAx(unsignedint7);
		unsignedint7.setVal(-2127713048);
		// Create object for crosses
		CTCrosses crosses = dmlchartObjectFactory.createCTCrosses();
		catax.setCrosses(crosses);
		crosses.setVal(org.docx4j.dml.chart.STCrosses.AUTO_ZERO);
		// Create object for auto
		CTBoolean boolean14 = dmlchartObjectFactory.createCTBoolean();
		catax.setAuto(boolean14);
		// Create object for lblOffset
		CTLblOffset lbloffset = dmlchartObjectFactory.createCTLblOffset();
		catax.setLblOffset(lbloffset);
		lbloffset.setVal(new Integer(100));
		// Create object for lblAlgn
		CTLblAlgn lblalgn = dmlchartObjectFactory.createCTLblAlgn();
		catax.setLblAlgn(lblalgn);
		lblalgn.setVal(org.docx4j.dml.chart.STLblAlgn.CTR);
		// Create object for noMultiLvlLbl
//		CTBoolean boolean15 = dmlchartObjectFactory.createCTBoolean();
//		catax.setNoMultiLvlLbl(boolean15);
		// Create object for valAx
		CTValAx valax = dmlchartObjectFactory.createCTValAx();
		plotarea.getValAxOrCatAxOrDateAx().add(valax);
		// Create object for title
		CTTitle title2 = dmlchartObjectFactory.createCTTitle();
		valax.setTitle(title2);
		// Create object for layout
		CTLayout layout3 = dmlchartObjectFactory.createCTLayout();
		title2.setLayout(layout3);
		// Create object for overlay
		CTBoolean boolean16 = dmlchartObjectFactory.createCTBoolean();
		title2.setOverlay(boolean16);
		// Create object for numFmt
		CTNumFmt numfmt = dmlchartObjectFactory.createCTNumFmt();
		valax.setNumFmt(numfmt);
		numfmt.setFormatCode("General");
		// Create object for axId
		CTUnsignedInt unsignedint8 = dmlchartObjectFactory.createCTUnsignedInt();
		valax.setAxId(unsignedint8);
		unsignedint8.setVal(-2127713048);
		// Create object for scaling
		CTScaling scaling2 = dmlchartObjectFactory.createCTScaling();
		valax.setScaling(scaling2);
		// Create object for orientation
		CTOrientation orientation2 = dmlchartObjectFactory.createCTOrientation();
		scaling2.setOrientation(orientation2);
		orientation2.setVal(org.docx4j.dml.chart.STOrientation.MIN_MAX);
		// Create object for delete
		CTBoolean boolean17 = dmlchartObjectFactory.createCTBoolean();
		valax.setDelete(boolean17);
		// Create object for axPos
		CTAxPos axpos2 = dmlchartObjectFactory.createCTAxPos();
		valax.setAxPos(axpos2);
		axpos2.setVal(org.docx4j.dml.chart.STAxPos.L);
		// Create object for majorGridlines
		CTChartLines chartlines = dmlchartObjectFactory.createCTChartLines();
		valax.setMajorGridlines(chartlines);
		// Create object for majorTickMark
		CTTickMark tickmark3 = dmlchartObjectFactory.createCTTickMark();
		valax.setMajorTickMark(tickmark3);
		tickmark3.setVal(org.docx4j.dml.chart.STTickMark.NONE);
		// Create object for minorTickMark
		CTTickMark tickmark4 = dmlchartObjectFactory.createCTTickMark();
		valax.setMinorTickMark(tickmark4);
		tickmark4.setVal(org.docx4j.dml.chart.STTickMark.NONE);
		// Create object for tickLblPos
		CTTickLblPos ticklblpos2 = dmlchartObjectFactory.createCTTickLblPos();
		valax.setTickLblPos(ticklblpos2);
		ticklblpos2.setVal(org.docx4j.dml.chart.STTickLblPos.NEXT_TO);
		// Create object for crossAx
		CTUnsignedInt unsignedint9 = dmlchartObjectFactory.createCTUnsignedInt();
		valax.setCrossAx(unsignedint9);
		unsignedint9.setVal(-2127695320);
		// Create object for crosses
		CTCrosses crosses2 = dmlchartObjectFactory.createCTCrosses();
		valax.setCrosses(crosses2);
		crosses2.setVal(org.docx4j.dml.chart.STCrosses.AUTO_ZERO);
		// Create object for crossBetween
		CTCrossBetween crossbetween = dmlchartObjectFactory.createCTCrossBetween();
		valax.setCrossBetween(crossbetween);
		crossbetween.setVal(org.docx4j.dml.chart.STCrossBetween.BETWEEN);
		// Create object for legend
//		CTLegend legend = dmlchartObjectFactory.createCTLegend();
//		chart.setLegend(legend);
		// Create object for layout
//		CTLayout layout4 = dmlchartObjectFactory.createCTLayout();
//		legend.setLayout(layout4);
		// Create object for overlay
//		CTBoolean boolean18 = dmlchartObjectFactory.createCTBoolean();
//		legend.setOverlay(boolean18);
		// Create object for legendPos
//		CTLegendPos legendpos = dmlchartObjectFactory.createCTLegendPos();
//		legend.setLegendPos(legendpos);
//		legendpos.setVal(org.docx4j.dml.chart.STLegendPos.R);
		// Create object for plotVisOnly
		CTBoolean boolean19 = dmlchartObjectFactory.createCTBoolean();
		chart.setPlotVisOnly(boolean19);
		// Create object for dispBlanksAs
		CTDispBlanksAs dispblanksas = dmlchartObjectFactory.createCTDispBlanksAs();
		chart.setDispBlanksAs(dispblanksas);
		dispblanksas.setVal(org.docx4j.dml.chart.STDispBlanksAs.GAP);
		// Create object for showDLblsOverMax
		CTBoolean boolean20 = dmlchartObjectFactory.createCTBoolean();
		chart.setShowDLblsOverMax(boolean20);
		// Create object for spPr
		CTShapeProperties shapeproperties2 = dmlObjectFactory.createCTShapeProperties();
		chartspace.setSpPr(shapeproperties2);
		// Create object for ln
		CTLineProperties lineproperties2 = dmlObjectFactory.createCTLineProperties();
		shapeproperties2.setLn(lineproperties2);
		lineproperties2.setW(new Integer(6350));
		// Create object for externalData
//		CTExternalData externaldata = dmlchartObjectFactory.createCTExternalData();
//		chartspace.setExternalData(externaldata);
//		// Create object for autoUpdate
//		CTBoolean boolean21 = dmlchartObjectFactory.createCTBoolean();
//		externaldata.setAutoUpdate(boolean21);
//		externaldata.setId("rId1");
		// Create object for date1904
		CTBoolean boolean22 = dmlchartObjectFactory.createCTBoolean();
		chartspace.setDate1904(boolean22);
		// Create object for roundedCorners
		CTBoolean boolean23 = dmlchartObjectFactory.createCTBoolean();
		chartspace.setRoundedCorners(boolean23);

		return chartspace;
	}

	private static CTNumDataSource createData() {
		System.out.println("ChartFactory.createData()");
		org.docx4j.dml.chart.ObjectFactory dmlchartObjectFactory = new org.docx4j.dml.chart.ObjectFactory();
		CTNumDataSource numdatasource = dmlchartObjectFactory.createCTNumDataSource();
		// Create object for numRef
		CTNumRef numref = dmlchartObjectFactory.createCTNumRef();
		numdatasource.setNumRef(numref);
//		numref.setF("'Ark1'!$A$1:$AMP$1");
		// Create object for numCache
		CTNumData numdata = dmlchartObjectFactory.createCTNumData();
		numref.setNumCache(numdata);
		
		int max = 200;
		for (int i=0;i<max;i++){
			CTNumVal numval = dmlchartObjectFactory.createCTNumVal();
			numdata.getPt().add(numval);
			numval.setIdx(i);
			numval.setV(Double.toString(Math.random()));			
		}
		numdata.setFormatCode("General");
		// Create object for ptCount
		CTUnsignedInt unsignedint = dmlchartObjectFactory.createCTUnsignedInt();
		numdata.setPtCount(unsignedint);
		unsignedint.setVal(max);

		return numdatasource;
	}
}

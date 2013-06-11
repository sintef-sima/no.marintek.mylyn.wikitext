package no.marintek.mylyn.wikitext.ooxml;

import java.io.IOException;

import javax.xml.bind.JAXBException;

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
import org.docx4j.dml.chart.CTGrouping;
import org.docx4j.dml.chart.CTLayout;
import org.docx4j.dml.chart.CTLblAlgn;
import org.docx4j.dml.chart.CTLblOffset;
import org.docx4j.dml.chart.CTLegend;
import org.docx4j.dml.chart.CTLegendPos;
import org.docx4j.dml.chart.CTLineChart;
import org.docx4j.dml.chart.CTLineSer;
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
import org.docx4j.dml.chart.ObjectFactory;

public class ChartFactory {

	/**
	 * Factory method for creating a data set from an array of doubles. The data
	 * set can be used in a OOXML chart.
	 * 
	 * @param data
	 *            the array to create a data set from
	 * @return a data set to for use in a chart
	 */
	private static CTNumDataSource createData(double[] data) {
		System.out.println("ChartFactory.createData()");
		org.docx4j.dml.chart.ObjectFactory dmlchartObjectFactory = new org.docx4j.dml.chart.ObjectFactory();
		CTNumDataSource numdatasource = dmlchartObjectFactory
				.createCTNumDataSource();
		// Create object for numRef
		CTNumRef numref = dmlchartObjectFactory.createCTNumRef();
		numdatasource.setNumRef(numref);
		// numref.setF("'Ark1'!$A$1:$AMP$1");
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

		return numdatasource;
	}

	/**
	 * Creates a chart for use in a OOXML document.
	 * 
	 * @param data
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 */
	public static CTChartSpace createChart(double[] data) throws IOException,
			JAXBException {
		
		long catAxisId = -2142488472;
		long valueAxisId = -2142485608;

		org.docx4j.dml.chart.ObjectFactory dmlchartObjectFactory = new org.docx4j.dml.chart.ObjectFactory();
		CTChartSpace chartspace = dmlchartObjectFactory.createCTChartSpace();

		// Create object for date1904
		CTBoolean boolean21 = dmlchartObjectFactory.createCTBoolean();
		chartspace.setDate1904(boolean21);

		// Create object for lang
		CTTextLanguageID textlanguageid = dmlchartObjectFactory
				.createCTTextLanguageID();
		chartspace.setLang(textlanguageid);
		textlanguageid.setVal("en-GB");

		// Create object for roundedCorners
		CTBoolean boolean23 = dmlchartObjectFactory.createCTBoolean();
		chartspace.setRoundedCorners(boolean23);

		// Create object for style
		CTStyle style = dmlchartObjectFactory.createCTStyle();
		chartspace.setStyle(style);

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

		// Create object for grouping
		CTGrouping grouping = dmlchartObjectFactory.createCTGrouping();
		linechart.setGrouping(grouping);
		grouping.setVal(org.docx4j.dml.chart.STGrouping.STANDARD);

		// Create object for varyColors
		CTBoolean boolean3 = dmlchartObjectFactory.createCTBoolean();
		boolean3.setVal(Boolean.FALSE);
		linechart.setVaryColors(boolean3);

		// Create object for crossBetween (c:ser)
		CTLineSer lineser = dmlchartObjectFactory.createCTLineSer();
		linechart.getSer().add(lineser);

		org.docx4j.dml.ObjectFactory dmlObjectFactory = new org.docx4j.dml.ObjectFactory();

		// Create object for idx
		CTUnsignedInt unsignedint5 = dmlchartObjectFactory
				.createCTUnsignedInt();
		lineser.setIdx(unsignedint5);
		unsignedint5.setVal(0);

		// Create object for order
		CTUnsignedInt unsignedint3 = dmlchartObjectFactory
				.createCTUnsignedInt();
		lineser.setOrder(unsignedint3);
		unsignedint3.setVal(0);

		// Create object for spPr
		CTShapeProperties shapeproperties = dmlObjectFactory
				.createCTShapeProperties();
		lineser.setSpPr(shapeproperties);

		// Create object for ln
		CTLineProperties lineproperties = dmlObjectFactory
				.createCTLineProperties();
		shapeproperties.setLn(lineproperties);
		lineproperties.setW(new Integer(12700));

		// Create object for val
		lineser.setVal(createData(data));

		// Create object for marker
		CTBoolean boolean4 = dmlchartObjectFactory.createCTBoolean();
		linechart.setMarker(boolean4);
		boolean4.setVal(Boolean.TRUE);

		// Create object for smooth
		CTBoolean boolean5 = dmlchartObjectFactory.createCTBoolean();
		lineser.setSmooth(boolean5);
		boolean5.setVal(Boolean.FALSE);

		// Create object for crosses
		CTUnsignedInt unsignedint = dmlchartObjectFactory.createCTUnsignedInt();
		linechart.getAxId().add(unsignedint);
		unsignedint.setVal(catAxisId);

		// Create object for crossBetween
		CTUnsignedInt unsignedint2 = dmlchartObjectFactory
				.createCTUnsignedInt();
		linechart.getAxId().add(unsignedint2);
		unsignedint2.setVal(valueAxisId);

		// Set up labels
		createDlbls(dmlchartObjectFactory, linechart);

		// Create object for smooth
		CTBoolean boolean10 = dmlchartObjectFactory.createCTBoolean();
		linechart.setSmooth(boolean10);
		boolean10.setVal(Boolean.TRUE);

		// Set up category axis
		createCatAx(dmlchartObjectFactory, plotarea,catAxisId,valueAxisId);

		// Set up value axis
		createValAx(dmlchartObjectFactory, plotarea,valueAxisId,catAxisId);

		// Set up legend
		createLegend(dmlchartObjectFactory, chart);

		// Create object for plotVisOnly
		CTBoolean boolean19 = dmlchartObjectFactory.createCTBoolean();
		chart.setPlotVisOnly(boolean19);
		
		// Create object for dispBlanksAs
		CTDispBlanksAs dispblanksas = dmlchartObjectFactory
				.createCTDispBlanksAs();
		chart.setDispBlanksAs(dispblanksas);
		dispblanksas.setVal(org.docx4j.dml.chart.STDispBlanksAs.GAP);
		
		// Create object for showDLblsOverMax
		CTBoolean boolean20 = dmlchartObjectFactory.createCTBoolean();
		chart.setShowDLblsOverMax(boolean20);
		
		// Create object for spPr
		CTShapeProperties shapeproperties2 = dmlObjectFactory
				.createCTShapeProperties();
		chartspace.setSpPr(shapeproperties2);
		
		// Create object for ln
		CTLineProperties lineproperties2 = dmlObjectFactory
				.createCTLineProperties();
		shapeproperties2.setLn(lineproperties2);
		lineproperties2.setW(new Integer(6350));

		return chartspace;
	}

	//	<c:legend>
	//	  <c:legendPos val="r" />
	//	  <c:layout />
	//	  <c:overlay val="0" />
	//	</c:legend>
	private static void createLegend(ObjectFactory dmlchartObjectFactory,
			CTChart chart) {
		
		// Create object for legend
		CTLegend legend = dmlchartObjectFactory.createCTLegend();
		chart.setLegend(legend);
		
		// Create object for legendPos
		CTLegendPos legendpos = dmlchartObjectFactory.createCTLegendPos();
		legend.setLegendPos(legendpos);
		legendpos.setVal(org.docx4j.dml.chart.STLegendPos.R);
		
		// Create object for layout
		CTLayout layout = dmlchartObjectFactory.createCTLayout();
		legend.setLayout(layout);

		// Create object for overlay
		CTBoolean overlay = dmlchartObjectFactory.createCTBoolean();
		legend.setOverlay(overlay);
	}

	private static void createDlbls(ObjectFactory dmlchartObjectFactory,
			CTLineChart linechart) {
		CTDLbls dlbls = dmlchartObjectFactory.createCTDLbls();
		linechart.setDLbls(dlbls);
		// Create object for showLegendKey
		CTBoolean boolean4 = dmlchartObjectFactory.createCTBoolean();
		dlbls.setShowLegendKey(boolean4);
		// Create object for showVal
		CTBoolean boolean5 = dmlchartObjectFactory.createCTBoolean();
		dlbls.setShowVal(boolean5);
		// Create object for showCatName
		CTBoolean boolean6 = dmlchartObjectFactory.createCTBoolean();
		dlbls.setShowCatName(boolean6);
		// Create object for showSerName
		CTBoolean boolean7 = dmlchartObjectFactory.createCTBoolean();
		dlbls.setShowSerName(boolean7);
		// Create object for showPercent
		CTBoolean boolean8 = dmlchartObjectFactory.createCTBoolean();
		dlbls.setShowPercent(boolean8);
		// Create object for showBubbleSize
		CTBoolean boolean9 = dmlchartObjectFactory.createCTBoolean();
		dlbls.setShowBubbleSize(boolean9);
	}

	private static void createValAx(ObjectFactory dmlchartObjectFactory,
			CTPlotArea plotarea, long axisId, long crossAxisId) {

		CTValAx valax = dmlchartObjectFactory.createCTValAx();
		plotarea.getValAxOrCatAxOrDateAx().add(valax);

		// Create object for axId
		CTUnsignedInt unsignedint8 = dmlchartObjectFactory
				.createCTUnsignedInt();
		valax.setAxId(unsignedint8);
		unsignedint8.setVal(axisId);

		// Create object for scaling
		CTScaling scaling2 = dmlchartObjectFactory.createCTScaling();
		valax.setScaling(scaling2);
		
		// Create object for orientation
		CTOrientation orientation2 = dmlchartObjectFactory
				.createCTOrientation();
		scaling2.setOrientation(orientation2);
		orientation2.setVal(org.docx4j.dml.chart.STOrientation.MIN_MAX);

		// Create object for delete
		CTBoolean boolean17 = dmlchartObjectFactory.createCTBoolean();
		valax.setDelete(boolean17);
		boolean17.setVal(Boolean.FALSE);
		
		// Create object for axPos
		CTAxPos axpos2 = dmlchartObjectFactory.createCTAxPos();
		valax.setAxPos(axpos2);
		axpos2.setVal(org.docx4j.dml.chart.STAxPos.L); // left
		
		// Create object for majorGridlines
		CTChartLines chartlines = dmlchartObjectFactory.createCTChartLines();
		valax.setMajorGridlines(chartlines);

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
		numfmt.setSourceLinked(Boolean.TRUE);
		
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
		CTUnsignedInt unsignedint9 = dmlchartObjectFactory
				.createCTUnsignedInt();
		valax.setCrossAx(unsignedint9);
		unsignedint9.setVal(crossAxisId);
		
		// Create object for crosses
		CTCrosses crosses2 = dmlchartObjectFactory.createCTCrosses();
		valax.setCrosses(crosses2);
		crosses2.setVal(org.docx4j.dml.chart.STCrosses.AUTO_ZERO);
		
		// Create object for crossBetween
		CTCrossBetween crossbetween = dmlchartObjectFactory
				.createCTCrossBetween();
		valax.setCrossBetween(crossbetween);
		crossbetween.setVal(org.docx4j.dml.chart.STCrossBetween.BETWEEN);
	}

	private static void createCatAx(ObjectFactory dmlchartObjectFactory,
			CTPlotArea plotarea, long axisId, long crossAxisId) {
		
		CTCatAx catax = dmlchartObjectFactory.createCTCatAx();
		plotarea.getValAxOrCatAxOrDateAx().add(catax);

		// Create object for axId
		CTUnsignedInt unsignedint6 = dmlchartObjectFactory
				.createCTUnsignedInt();
		catax.setAxId(unsignedint6);
		unsignedint6.setVal(axisId);
		
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
		boolean13.setVal(Boolean.FALSE);
		
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
		CTUnsignedInt unsignedint7 = dmlchartObjectFactory
				.createCTUnsignedInt();
		catax.setCrossAx(unsignedint7);
		unsignedint7.setVal(crossAxisId);
		
		// Create object for crosses
		CTCrosses crosses = dmlchartObjectFactory.createCTCrosses();
		catax.setCrosses(crosses);
		crosses.setVal(org.docx4j.dml.chart.STCrosses.AUTO_ZERO);
		
		// Create object for auto
		CTBoolean boolean14 = dmlchartObjectFactory.createCTBoolean();
		catax.setAuto(boolean14);
		boolean14.setVal(Boolean.TRUE);
		
		// Create object for lblOffset
		CTLblOffset lbloffset = dmlchartObjectFactory.createCTLblOffset();
		catax.setLblOffset(lbloffset);
		lbloffset.setVal(new Integer(100));
		
		// Create object for lblAlgn
		CTLblAlgn lblalgn = dmlchartObjectFactory.createCTLblAlgn();
		catax.setLblAlgn(lblalgn);
		lblalgn.setVal(org.docx4j.dml.chart.STLblAlgn.CTR);
		
		// Create object for noMultiLvlLbl
		CTBoolean boolean15 = dmlchartObjectFactory.createCTBoolean();
		catax.setNoMultiLvlLbl(boolean15);
		boolean15.setVal(Boolean.TRUE);

	}
}

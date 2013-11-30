/*******************************************************************************
 * Copyright (c) 2013 MARINTEK.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import no.marintek.mylyn.wikitext.ooxml.ChartFactory.ChartType;

import org.docx4j.XmlUtils;
import org.docx4j.dml.chart.CTBoolean;
import org.docx4j.dml.chart.CTChartSpace;
import org.docx4j.dml.chart.CTMarker;
import org.docx4j.dml.chart.CTMarkerSize;
import org.docx4j.dml.chart.CTStyle;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.math.CTBreakBin;
import org.docx4j.math.CTBreakBinSub;
import org.docx4j.math.CTLimLoc;
import org.docx4j.math.CTMathPr;
import org.docx4j.math.CTOMath;
import org.docx4j.math.CTOMathJc;
import org.docx4j.math.CTOMathPara;
import org.docx4j.math.CTOnOff;
import org.docx4j.math.CTString;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.DocumentSettingsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.vml.officedrawing.CTIdMap;
import org.docx4j.vml.officedrawing.CTShapeLayout;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTCharacterSpacing;
import org.docx4j.wml.CTColorSchemeMapping;
import org.docx4j.wml.CTCompat;
import org.docx4j.wml.CTCompatSetting;
import org.docx4j.wml.CTLanguage;
import org.docx4j.wml.CTLongHexNumber;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.CTProof;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.CTShapeDefaults;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.CTTwipsMeasure;
import org.docx4j.wml.CTZoom;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.Jc;
import org.docx4j.wml.Lvl;
import org.docx4j.wml.NumFmt;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.Style;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.TblGrid;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.Tr;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder;

import uk.ac.ed.ph.snuggletex.SerializationMethod;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.XMLStringOutputOptions;

/**
 *
 * @author Torkild U. Resheim
 *
 * @since 1.9
 */
public class OoxmlDocumentBuilder extends DocumentBuilder {

	private StringBuilder characters;

	private BlockType currentBlockType;

	private P currentParagraph;

	private SpanType currentSpanType;

	private String currentStyle;

	private org.docx4j.wml.ObjectFactory factory;

	private MainDocumentPart mainDocumentPart;

	private final org.docx4j.math.ObjectFactory mathFactory = new org.docx4j.math.ObjectFactory();

	private final org.docx4j.wml.ObjectFactory wmlObjectFactory = new org.docx4j.wml.ObjectFactory();

	private File outputFile;

	private String subtitle;

	private String title;

	private final org.docx4j.wml.BooleanDefaultTrue TRUE = new org.docx4j.wml.BooleanDefaultTrue();

	private WordprocessingMLPackage wordMLPackage;

	private int chartCounter = 0;

	private Tr currentTableRow;

	/** The number of columns in the current table */
	private int tableColumnCount;

	/** The number of rows in the current table */
	private int tableRowCount;

	private Tbl currentTable;

	public enum CaptionType {
		Table, Figure, Image, Equation
	}

	private int[] captionCount = new int[CaptionType.values().length];

	private Attributes currentAttributes;

	private int currentListType;

	public OoxmlDocumentBuilder() {
		characters = new StringBuilder();
	}

	@Override
	public void acronym(String text, String definition) {
		// TODO Auto-generated method stub
	}

	public void caption(String text, CaptionType captionType) {
		String type = captionType.name();

		org.docx4j.wml.ObjectFactory wmlObjectFactory = new org.docx4j.wml.ObjectFactory();

		P p = wmlObjectFactory.createP();

		// Create object for pPr
		PPr ppr = wmlObjectFactory.createPPr();
		p.setPPr(ppr);

		// Create object for pStyle
		PPrBase.PStyle pprbasepstyle = wmlObjectFactory.createPPrBasePStyle();
		ppr.setPStyle(pprbasepstyle);
		pprbasepstyle.setVal("Caption");

		// Create object for r
		R r = wmlObjectFactory.createR();
		p.getContent().add(r);

		// Create object for t (wrapped in JAXBElement)
		org.docx4j.wml.Text t = wmlObjectFactory.createText();
		JAXBElement<org.docx4j.wml.Text> textWrapped = wmlObjectFactory.createRT(t);
		r.getContent().add(textWrapped);
		t.setValue(type + " ");
		t.setSpace("preserve");

		// Create object for r
		R r2 = wmlObjectFactory.createR();
		p.getContent().add(r2);

		// Create object for fldChar (wrapped in JAXBElement)
		FldChar fldchar = wmlObjectFactory.createFldChar();
		JAXBElement<org.docx4j.wml.FldChar> fldcharWrapped = wmlObjectFactory.createRFldChar(fldchar);
		r2.getContent().add(fldcharWrapped);
		fldchar.setFldCharType(org.docx4j.wml.STFldCharType.BEGIN);

		// Create object for r
		R r3 = wmlObjectFactory.createR();
		p.getContent().add(r3);

		// Create object for instrText (wrapped in JAXBElement)
		org.docx4j.wml.Text text2 = wmlObjectFactory.createText();
		JAXBElement<org.docx4j.wml.Text> textWrapped2 = wmlObjectFactory.createRInstrText(text2);
		r3.getContent().add(textWrapped2);
		text2.setValue(" ");
		text2.setSpace("preserve");

		// Create object for r
		R r4 = wmlObjectFactory.createR();
		p.getContent().add(r4);

		// Create object for instrText (wrapped in JAXBElement)
		org.docx4j.wml.Text text3 = wmlObjectFactory.createText();
		JAXBElement<org.docx4j.wml.Text> textWrapped3 = wmlObjectFactory.createRInstrText(text3);
		r4.getContent().add(textWrapped3);
		text3.setValue("SEQ " + type + " \\* ARABIC ");
		text3.setSpace("preserve");

		// Create object for r
		R r5 = wmlObjectFactory.createR();
		p.getContent().add(r5);

		// Create object for fldChar (wrapped in JAXBElement)
		FldChar fldchar2 = wmlObjectFactory.createFldChar();
		JAXBElement<org.docx4j.wml.FldChar> fldcharWrapped2 = wmlObjectFactory.createRFldChar(fldchar2);
		r5.getContent().add(fldcharWrapped2);
		fldchar2.setFldCharType(org.docx4j.wml.STFldCharType.SEPARATE);

		// Create object for r
		R r6 = wmlObjectFactory.createR();
		p.getContent().add(r6);

		// Create object for rPr
		RPr rpr = wmlObjectFactory.createRPr();
		r6.setRPr(rpr);

		// Create object for noProof
		BooleanDefaultTrue booleandefaulttrue = wmlObjectFactory.createBooleanDefaultTrue();
		rpr.setNoProof(booleandefaulttrue);

		// Create object for t (wrapped in JAXBElement)
		org.docx4j.wml.Text text4 = wmlObjectFactory.createText();
		JAXBElement<org.docx4j.wml.Text> textWrapped4 = wmlObjectFactory.createRT(text4);
		r6.getContent().add(textWrapped4);
		text4.setValue(Integer.toString(++captionCount[captionType.ordinal()]));

		// Create object for r
		R r7 = wmlObjectFactory.createR();
		p.getContent().add(r7);

		// Create object for rPr
		RPr rpr2 = wmlObjectFactory.createRPr();
		r7.setRPr(rpr2);

		// Create object for noProof
		BooleanDefaultTrue booleandefaulttrue2 = wmlObjectFactory.createBooleanDefaultTrue();
		rpr2.setNoProof(booleandefaulttrue2);

		// Create object for fldChar (wrapped in JAXBElement)
		FldChar fldchar3 = wmlObjectFactory.createFldChar();
		JAXBElement<org.docx4j.wml.FldChar> fldcharWrapped3 = wmlObjectFactory.createRFldChar(fldchar3);
		r7.getContent().add(fldcharWrapped3);
		fldchar3.setFldCharType(org.docx4j.wml.STFldCharType.END);

		// Create object for r
		R r8 = wmlObjectFactory.createR();
		p.getContent().add(r8);

		// Create object for rPr
		RPr rpr3 = wmlObjectFactory.createRPr();
		r8.setRPr(rpr3);

		// Create object for noProof
		BooleanDefaultTrue booleandefaulttrue3 = wmlObjectFactory.createBooleanDefaultTrue();
		rpr3.setNoProof(booleandefaulttrue3);

		// Create object for t (wrapped in JAXBElement)
		org.docx4j.wml.Text text5 = wmlObjectFactory.createText();
		JAXBElement<org.docx4j.wml.Text> textWrapped5 = wmlObjectFactory.createRT(text5);
		r8.getContent().add(textWrapped5);
		text5.setValue(": " + text);

		// Create object for bookmarkStart (wrapped in JAXBElement)
		CTBookmark bookmark = wmlObjectFactory.createCTBookmark();
		JAXBElement<org.docx4j.wml.CTBookmark> bookmarkWrapped = wmlObjectFactory.createPBookmarkStart(bookmark);
		p.getContent().add(bookmarkWrapped);
		bookmark.setName("_GoBack");
		bookmark.setId(BigInteger.valueOf(0));

		// Create object for bookmarkEnd (wrapped in JAXBElement)
		CTMarkupRange markuprange = wmlObjectFactory.createCTMarkupRange();
		JAXBElement<org.docx4j.wml.CTMarkupRange> markuprangeWrapped = wmlObjectFactory.createPBookmarkEnd(markuprange);
		p.getContent().add(markuprangeWrapped);
		markuprange.setId(BigInteger.valueOf(0));

		mainDocumentPart.addObject(p);
	}

	/**
	 * Adds an image to the document.
	 *
	 * @param bytes
	 * @param file
	 * @param text alternative text
	 * @throws Exception
	 */
	private void addImageToPackage(byte[] bytes, File file, String text) throws Exception {
		BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordMLPackage, bytes);

		int docPrId = 1;
		int cNvPrId = 2;
		Inline inline = imagePart.createImageInline(file.getAbsolutePath(), text, docPrId, cNvPrId, false);
		P paragraph = addInlineImageToParagraph(inline);
		mainDocumentPart.addObject(paragraph);
	}

	private P addInlineImageToParagraph(Inline inline) {
		P paragraph = factory.createP();
		R run = factory.createR();
		paragraph.getContent().add(run);
		Drawing drawing = factory.createDrawing();
		run.getContent().add(drawing);
		drawing.getAnchorOrInline().add(inline);
		return paragraph;
	}

	/**
	 * Adds a page break to the document flow.
	 */
	private void addPageBreak() {
		org.docx4j.wml.P p = new org.docx4j.wml.P();
		org.docx4j.wml.R r = new org.docx4j.wml.R();
		org.docx4j.wml.Br br = new org.docx4j.wml.Br();
		br.setType(STBrType.PAGE);
		r.getContent().add(br);
		p.getContent().add(r);
		mainDocumentPart.addObject(p);
	}

	@Override
	public void beginBlock(BlockType type, Attributes attributes) {
		currentBlockType = type;
		currentAttributes = attributes;

		switch (currentBlockType) {
		case DEFINITION_LIST:
			currentListType = 1;
			break;
		case NUMERIC_LIST:
			currentListType = 1;
			break;
		case BULLETED_LIST:
			currentListType = 2;
			break;
		case LIST_ITEM:
			currentParagraph = factory.createP();
			applyStyle(currentParagraph, "ListParagraph");
			PPr ppr = currentParagraph.getPPr();
			PPrBase.NumPr pprbasenumpr = wmlObjectFactory.createPPrBaseNumPr();
			ppr.setNumPr(pprbasenumpr);
			// Create object for numId
			PPrBase.NumPr.NumId pprbasenumprnumid = wmlObjectFactory.createPPrBaseNumPrNumId();
			pprbasenumpr.setNumId(pprbasenumprnumid);
			pprbasenumprnumid.setVal(BigInteger.valueOf(currentListType));
			// Create object for ilvl
			PPrBase.NumPr.Ilvl pprbasenumprilvl = wmlObjectFactory.createPPrBaseNumPrIlvl();
			pprbasenumpr.setIlvl(pprbasenumprilvl);
			pprbasenumprilvl.setVal(BigInteger.valueOf(0));
			beginSpan(SpanType.SPAN, currentAttributes);
			mainDocumentPart.addObject(currentParagraph);
			break;
		case PARAGRAPH:
			currentParagraph = factory.createP();
			beginSpan(SpanType.SPAN, currentAttributes);
			mainDocumentPart.addObject(currentParagraph);
			break;
		case TABLE:
			tableColumnCount = 0;
			tableRowCount = 0;
			currentTable = wmlObjectFactory.createTbl();
			mainDocumentPart.addObject(currentTable);
			TblPr tblpr = wmlObjectFactory.createTblPr();
			currentTable.setTblPr(tblpr);

			// Create object for tblW
			TblWidth tblwidth = wmlObjectFactory.createTblWidth();
			tblpr.setTblW(tblwidth);
			tblwidth.setType("auto");
			tblwidth.setW(BigInteger.valueOf(1));
			tblpr.setTblBorders(createTableBorders());
			break;
		case TABLE_ROW:
			currentTableRow = wmlObjectFactory.createTr();
			currentTable.getContent().add(currentTableRow);
			if (tableRowCount == 1) {
				// Create object for tblGrid
				TblGrid tblgrid = wmlObjectFactory.createTblGrid();
				currentTable.setTblGrid(tblgrid);
				for (int i = 0; i < tableColumnCount; i++) {
					TblGridCol tblgridcol = wmlObjectFactory.createTblGridCol();
					tblgrid.getGridCol().add(tblgridcol);
				}
			}
			tableRowCount++;
			break;
		case TABLE_CELL_NORMAL:
		case TABLE_CELL_HEADER:
			Tc tc = wmlObjectFactory.createTc();
			JAXBElement<org.docx4j.wml.Tc> tcWrapped = wmlObjectFactory.createTrTc(tc);
			currentTableRow.getContent().add(tcWrapped);
			currentParagraph = factory.createP();
			tc.getContent().add(currentParagraph);
			// Handle the header
			if (currentBlockType.equals(BlockType.TABLE_CELL_HEADER)) {
				TcPr tcpr = wmlObjectFactory.createTcPr();
				tc.setTcPr(tcpr);
				CTShd shd = wmlObjectFactory.createCTShd();
				tcpr.setShd(shd);
				shd.setVal(org.docx4j.wml.STShd.CLEAR);
				shd.setColor("auto");
				shd.setFill("D9D9D9");
				shd.setThemeFill(org.docx4j.wml.STThemeColor.BACKGROUND_1);
				shd.setThemeFillShade("D9");
				beginSpan(SpanType.BOLD, new Attributes());
			}
			break;
		default:
			beginSpan(SpanType.SPAN, currentAttributes);
			break;
		}
	}

	@Override
	public void beginDocument() {
		try {
			factory = new org.docx4j.wml.ObjectFactory();
			// InputStream resourceAsStream =
			// OoxmlDocumentBuilder.class.getResourceAsStream("templates/template.docx");
			// wordMLPackage = WordprocessingMLPackage.load(resourceAsStream);
			wordMLPackage = WordprocessingMLPackage.createPackage();
			mainDocumentPart = wordMLPackage.getMainDocumentPart();
			mainDocumentPart.addTargetPart(addSettingsPart());
			mainDocumentPart.addTargetPart(addNumberingPart());
			if (title != null) {
				mainDocumentPart.addStyledParagraphOfText("Title", title);
			}
			if (subtitle != null) {
				mainDocumentPart.addStyledParagraphOfText("Subtitle", subtitle);
			}
			addPageBreak();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		}
	}

	private NumberingDefinitionsPart addNumberingPart() throws InvalidFormatException {
		org.docx4j.wml.ObjectFactory wmlObjectFactory = new org.docx4j.wml.ObjectFactory();
		Numbering numbering = wmlObjectFactory.createNumbering();
		NumberingDefinitionsPart ndp = new NumberingDefinitionsPart();
		ndp.setJaxbElement(numbering);

		    // Create object for abstractNum
		    Numbering.Num numberingnum = wmlObjectFactory.createNumberingNum();
		    numbering.getNum().add( numberingnum);
		        numberingnum.setNumId( BigInteger.valueOf( 1) );
		        // Create object for abstractNumId
		        Numbering.Num.AbstractNumId numberingnumabstractnumid = wmlObjectFactory.createNumberingNumAbstractNumId();
		        numberingnum.setAbstractNumId(numberingnumabstractnumid);
		            numberingnumabstractnumid.setVal( BigInteger.valueOf( 0) );
		    // Create object for abstractNum
		    Numbering.Num numberingnum2 = wmlObjectFactory.createNumberingNum();
		    numbering.getNum().add( numberingnum2);
		        numberingnum2.setNumId( BigInteger.valueOf( 2) );
		        // Create object for abstractNumId
		        Numbering.Num.AbstractNumId numberingnumabstractnumid2 = wmlObjectFactory.createNumberingNumAbstractNumId();
		        numberingnum2.setAbstractNumId(numberingnumabstractnumid2);
		            numberingnumabstractnumid2.setVal( BigInteger.valueOf( 1) );
		    // Create object for abstractNum
		    Numbering.AbstractNum numberingabstractnum = wmlObjectFactory.createNumberingAbstractNum();
		    numbering.getAbstractNum().add( numberingabstractnum);
		        numberingabstractnum.setAbstractNumId( BigInteger.valueOf( 0) );
		        // Create object for lvl
		        Lvl lvl = wmlObjectFactory.createLvl();
		        numberingabstractnum.getLvl().add( lvl);
		            lvl.setIlvl( BigInteger.valueOf( 0) );
		            // Create object for pPr
		            PPr ppr = wmlObjectFactory.createPPr();
		            lvl.setPPr(ppr);
		                // Create object for ind
		                PPrBase.Ind pprbaseind = wmlObjectFactory.createPPrBaseInd();
		                ppr.setInd(pprbaseind);
		                    pprbaseind.setLeft( BigInteger.valueOf( 720) );
		                    pprbaseind.setHanging( BigInteger.valueOf( 360) );
		            // Create object for numFmt
		            NumFmt numfmt = wmlObjectFactory.createNumFmt();
		            lvl.setNumFmt(numfmt);
		                numfmt.setVal(org.docx4j.wml.NumberFormat.DECIMAL);
		            // Create object for lvlText
		            Lvl.LvlText lvllvltext = wmlObjectFactory.createLvlLvlText();
		            lvl.setLvlText(lvllvltext);
		                lvllvltext.setVal( "%1.");
		            // Create object for lvlJc
		            Jc jc = wmlObjectFactory.createJc();
		            lvl.setLvlJc(jc);
		                jc.setVal(org.docx4j.wml.JcEnumeration.LEFT);
		            lvl.setTplc( "0409000F");
		            // Create object for start
		            Lvl.Start lvlstart = wmlObjectFactory.createLvlStart();
		            lvl.setStart(lvlstart);
		                lvlstart.setVal( BigInteger.valueOf( 1) );
		        // Create object for lvl
		        Lvl lvl2 = wmlObjectFactory.createLvl();
		        numberingabstractnum.getLvl().add( lvl2);
		            lvl2.setIlvl( BigInteger.valueOf( 1) );
		            // Create object for pPr
		            PPr ppr2 = wmlObjectFactory.createPPr();
		            lvl2.setPPr(ppr2);
		                // Create object for ind
		                PPrBase.Ind pprbaseind2 = wmlObjectFactory.createPPrBaseInd();
		                ppr2.setInd(pprbaseind2);
		                    pprbaseind2.setLeft( BigInteger.valueOf( 1440) );
		                    pprbaseind2.setHanging( BigInteger.valueOf( 360) );
		            // Create object for numFmt
		            NumFmt numfmt2 = wmlObjectFactory.createNumFmt();
		            lvl2.setNumFmt(numfmt2);
		                numfmt2.setVal(org.docx4j.wml.NumberFormat.LOWER_LETTER);
		            // Create object for lvlText
		            Lvl.LvlText lvllvltext2 = wmlObjectFactory.createLvlLvlText();
		            lvl2.setLvlText(lvllvltext2);
		                lvllvltext2.setVal( "%2.");
		            // Create object for lvlJc
		            Jc jc2 = wmlObjectFactory.createJc();
		            lvl2.setLvlJc(jc2);
		                jc2.setVal(org.docx4j.wml.JcEnumeration.LEFT);
		            lvl2.setTplc( "04090019");
		            // Create object for start
		            Lvl.Start lvlstart2 = wmlObjectFactory.createLvlStart();
		            lvl2.setStart(lvlstart2);
		                lvlstart2.setVal( BigInteger.valueOf( 1) );
		        // Create object for lvl
		        Lvl lvl3 = wmlObjectFactory.createLvl();
		        numberingabstractnum.getLvl().add( lvl3);
		            lvl3.setIlvl( BigInteger.valueOf( 2) );
		            // Create object for pPr
		            PPr ppr3 = wmlObjectFactory.createPPr();
		            lvl3.setPPr(ppr3);
		                // Create object for ind
		                PPrBase.Ind pprbaseind3 = wmlObjectFactory.createPPrBaseInd();
		                ppr3.setInd(pprbaseind3);
		                    pprbaseind3.setLeft( BigInteger.valueOf( 2160) );
		                    pprbaseind3.setHanging( BigInteger.valueOf( 180) );
		            // Create object for numFmt
		            NumFmt numfmt3 = wmlObjectFactory.createNumFmt();
		            lvl3.setNumFmt(numfmt3);
		                numfmt3.setVal(org.docx4j.wml.NumberFormat.LOWER_ROMAN);
		            // Create object for lvlText
		            Lvl.LvlText lvllvltext3 = wmlObjectFactory.createLvlLvlText();
		            lvl3.setLvlText(lvllvltext3);
		                lvllvltext3.setVal( "%3.");
		            // Create object for lvlJc
		            Jc jc3 = wmlObjectFactory.createJc();
		            lvl3.setLvlJc(jc3);
		                jc3.setVal(org.docx4j.wml.JcEnumeration.RIGHT);
		            lvl3.setTplc( "0409001B");
		            // Create object for start
		            Lvl.Start lvlstart3 = wmlObjectFactory.createLvlStart();
		            lvl3.setStart(lvlstart3);
		                lvlstart3.setVal( BigInteger.valueOf( 1) );
		        // Create object for lvl
		        Lvl lvl4 = wmlObjectFactory.createLvl();
		        numberingabstractnum.getLvl().add( lvl4);
		            lvl4.setIlvl( BigInteger.valueOf( 3) );
		            // Create object for pPr
		            PPr ppr4 = wmlObjectFactory.createPPr();
		            lvl4.setPPr(ppr4);
		                // Create object for ind
		                PPrBase.Ind pprbaseind4 = wmlObjectFactory.createPPrBaseInd();
		                ppr4.setInd(pprbaseind4);
		                    pprbaseind4.setLeft( BigInteger.valueOf( 2880) );
		                    pprbaseind4.setHanging( BigInteger.valueOf( 360) );
		            // Create object for numFmt
		            NumFmt numfmt4 = wmlObjectFactory.createNumFmt();
		            lvl4.setNumFmt(numfmt4);
		                numfmt4.setVal(org.docx4j.wml.NumberFormat.DECIMAL);
		            // Create object for lvlText
		            Lvl.LvlText lvllvltext4 = wmlObjectFactory.createLvlLvlText();
		            lvl4.setLvlText(lvllvltext4);
		                lvllvltext4.setVal( "%4.");
		            // Create object for lvlJc
		            Jc jc4 = wmlObjectFactory.createJc();
		            lvl4.setLvlJc(jc4);
		                jc4.setVal(org.docx4j.wml.JcEnumeration.LEFT);
		            lvl4.setTplc( "0409000F");
		            // Create object for start
		            Lvl.Start lvlstart4 = wmlObjectFactory.createLvlStart();
		            lvl4.setStart(lvlstart4);
		                lvlstart4.setVal( BigInteger.valueOf( 1) );
		        // Create object for lvl
		        Lvl lvl5 = wmlObjectFactory.createLvl();
		        numberingabstractnum.getLvl().add( lvl5);
		            lvl5.setIlvl( BigInteger.valueOf( 4) );
		            // Create object for pPr
		            PPr ppr5 = wmlObjectFactory.createPPr();
		            lvl5.setPPr(ppr5);
		                // Create object for ind
		                PPrBase.Ind pprbaseind5 = wmlObjectFactory.createPPrBaseInd();
		                ppr5.setInd(pprbaseind5);
		                    pprbaseind5.setLeft( BigInteger.valueOf( 3600) );
		                    pprbaseind5.setHanging( BigInteger.valueOf( 360) );
		            // Create object for numFmt
		            NumFmt numfmt5 = wmlObjectFactory.createNumFmt();
		            lvl5.setNumFmt(numfmt5);
		                numfmt5.setVal(org.docx4j.wml.NumberFormat.LOWER_LETTER);
		            // Create object for lvlText
		            Lvl.LvlText lvllvltext5 = wmlObjectFactory.createLvlLvlText();
		            lvl5.setLvlText(lvllvltext5);
		                lvllvltext5.setVal( "%5.");
		            // Create object for lvlJc
		            Jc jc5 = wmlObjectFactory.createJc();
		            lvl5.setLvlJc(jc5);
		                jc5.setVal(org.docx4j.wml.JcEnumeration.LEFT);
		            lvl5.setTplc( "04090019");
		            // Create object for start
		            Lvl.Start lvlstart5 = wmlObjectFactory.createLvlStart();
		            lvl5.setStart(lvlstart5);
		                lvlstart5.setVal( BigInteger.valueOf( 1) );
		        // Create object for lvl
		        Lvl lvl6 = wmlObjectFactory.createLvl();
		        numberingabstractnum.getLvl().add( lvl6);
		            lvl6.setIlvl( BigInteger.valueOf( 5) );
		            // Create object for pPr
		            PPr ppr6 = wmlObjectFactory.createPPr();
		            lvl6.setPPr(ppr6);
		                // Create object for ind
		                PPrBase.Ind pprbaseind6 = wmlObjectFactory.createPPrBaseInd();
		                ppr6.setInd(pprbaseind6);
		                    pprbaseind6.setLeft( BigInteger.valueOf( 4320) );
		                    pprbaseind6.setHanging( BigInteger.valueOf( 180) );
		            // Create object for numFmt
		            NumFmt numfmt6 = wmlObjectFactory.createNumFmt();
		            lvl6.setNumFmt(numfmt6);
		                numfmt6.setVal(org.docx4j.wml.NumberFormat.LOWER_ROMAN);
		            // Create object for lvlText
		            Lvl.LvlText lvllvltext6 = wmlObjectFactory.createLvlLvlText();
		            lvl6.setLvlText(lvllvltext6);
		                lvllvltext6.setVal( "%6.");
		            // Create object for lvlJc
		            Jc jc6 = wmlObjectFactory.createJc();
		            lvl6.setLvlJc(jc6);
		                jc6.setVal(org.docx4j.wml.JcEnumeration.RIGHT);
		            lvl6.setTplc( "0409001B");
		            // Create object for start
		            Lvl.Start lvlstart6 = wmlObjectFactory.createLvlStart();
		            lvl6.setStart(lvlstart6);
		                lvlstart6.setVal( BigInteger.valueOf( 1) );
		        // Create object for lvl
		        Lvl lvl7 = wmlObjectFactory.createLvl();
		        numberingabstractnum.getLvl().add( lvl7);
		            lvl7.setIlvl( BigInteger.valueOf( 6) );
		            // Create object for pPr
		            PPr ppr7 = wmlObjectFactory.createPPr();
		            lvl7.setPPr(ppr7);
		                // Create object for ind
		                PPrBase.Ind pprbaseind7 = wmlObjectFactory.createPPrBaseInd();
		                ppr7.setInd(pprbaseind7);
		                    pprbaseind7.setLeft( BigInteger.valueOf( 5040) );
		                    pprbaseind7.setHanging( BigInteger.valueOf( 360) );
		            // Create object for numFmt
		            NumFmt numfmt7 = wmlObjectFactory.createNumFmt();
		            lvl7.setNumFmt(numfmt7);
		                numfmt7.setVal(org.docx4j.wml.NumberFormat.DECIMAL);
		            // Create object for lvlText
		            Lvl.LvlText lvllvltext7 = wmlObjectFactory.createLvlLvlText();
		            lvl7.setLvlText(lvllvltext7);
		                lvllvltext7.setVal( "%7.");
		            // Create object for lvlJc
		            Jc jc7 = wmlObjectFactory.createJc();
		            lvl7.setLvlJc(jc7);
		                jc7.setVal(org.docx4j.wml.JcEnumeration.LEFT);
		            lvl7.setTplc( "0409000F");
		            // Create object for start
		            Lvl.Start lvlstart7 = wmlObjectFactory.createLvlStart();
		            lvl7.setStart(lvlstart7);
		                lvlstart7.setVal( BigInteger.valueOf( 1) );
		        // Create object for lvl
		        Lvl lvl8 = wmlObjectFactory.createLvl();
		        numberingabstractnum.getLvl().add( lvl8);
		            lvl8.setIlvl( BigInteger.valueOf( 7) );
		            // Create object for pPr
		            PPr ppr8 = wmlObjectFactory.createPPr();
		            lvl8.setPPr(ppr8);
		                // Create object for ind
		                PPrBase.Ind pprbaseind8 = wmlObjectFactory.createPPrBaseInd();
		                ppr8.setInd(pprbaseind8);
		                    pprbaseind8.setLeft( BigInteger.valueOf( 5760) );
		                    pprbaseind8.setHanging( BigInteger.valueOf( 360) );
		            // Create object for numFmt
		            NumFmt numfmt8 = wmlObjectFactory.createNumFmt();
		            lvl8.setNumFmt(numfmt8);
		                numfmt8.setVal(org.docx4j.wml.NumberFormat.LOWER_LETTER);
		            // Create object for lvlText
		            Lvl.LvlText lvllvltext8 = wmlObjectFactory.createLvlLvlText();
		            lvl8.setLvlText(lvllvltext8);
		                lvllvltext8.setVal( "%8.");
		            // Create object for lvlJc
		            Jc jc8 = wmlObjectFactory.createJc();
		            lvl8.setLvlJc(jc8);
		                jc8.setVal(org.docx4j.wml.JcEnumeration.LEFT);
		            lvl8.setTplc( "04090019");
		            // Create object for start
		            Lvl.Start lvlstart8 = wmlObjectFactory.createLvlStart();
		            lvl8.setStart(lvlstart8);
		                lvlstart8.setVal( BigInteger.valueOf( 1) );
		        // Create object for lvl
		        Lvl lvl9 = wmlObjectFactory.createLvl();
		        numberingabstractnum.getLvl().add( lvl9);
		            lvl9.setIlvl( BigInteger.valueOf( 8) );
		            // Create object for pPr
		            PPr ppr9 = wmlObjectFactory.createPPr();
		            lvl9.setPPr(ppr9);
		                // Create object for ind
		                PPrBase.Ind pprbaseind9 = wmlObjectFactory.createPPrBaseInd();
		                ppr9.setInd(pprbaseind9);
		                    pprbaseind9.setLeft( BigInteger.valueOf( 6480) );
		                    pprbaseind9.setHanging( BigInteger.valueOf( 180) );
		            // Create object for numFmt
		            NumFmt numfmt9 = wmlObjectFactory.createNumFmt();
		            lvl9.setNumFmt(numfmt9);
		                numfmt9.setVal(org.docx4j.wml.NumberFormat.LOWER_ROMAN);
		            // Create object for lvlText
		            Lvl.LvlText lvllvltext9 = wmlObjectFactory.createLvlLvlText();
		            lvl9.setLvlText(lvllvltext9);
		                lvllvltext9.setVal( "%9.");
		            // Create object for lvlJc
		            Jc jc9 = wmlObjectFactory.createJc();
		            lvl9.setLvlJc(jc9);
		                jc9.setVal(org.docx4j.wml.JcEnumeration.RIGHT);
		            lvl9.setTplc( "0409001B");
		            // Create object for start
		            Lvl.Start lvlstart9 = wmlObjectFactory.createLvlStart();
		            lvl9.setStart(lvlstart9);
		                lvlstart9.setVal( BigInteger.valueOf( 1) );
		        // Create object for nsid
		        CTLongHexNumber longhexnumber = wmlObjectFactory.createCTLongHexNumber();
		        numberingabstractnum.setNsid(longhexnumber);
		            longhexnumber.setVal( "321F1D95");
		        // Create object for multiLevelType
		        Numbering.AbstractNum.MultiLevelType numberingabstractnummultileveltype = wmlObjectFactory.createNumberingAbstractNumMultiLevelType();
		        numberingabstractnum.setMultiLevelType(numberingabstractnummultileveltype);
		            numberingabstractnummultileveltype.setVal( "hybridMultilevel");
		        // Create object for tmpl
		        CTLongHexNumber longhexnumber2 = wmlObjectFactory.createCTLongHexNumber();
		        numberingabstractnum.setTmpl(longhexnumber2);
		            longhexnumber2.setVal( "0F14D706");
		    // Create object for abstractNum
		    Numbering.AbstractNum numberingabstractnum2 = wmlObjectFactory.createNumberingAbstractNum();
		    numbering.getAbstractNum().add( numberingabstractnum2);
		        numberingabstractnum2.setAbstractNumId( BigInteger.valueOf( 1) );
		        // Create object for lvl
		        Lvl lvl10 = wmlObjectFactory.createLvl();
		        numberingabstractnum2.getLvl().add( lvl10);
		            lvl10.setIlvl( BigInteger.valueOf( 0) );
		            // Create object for pPr
		            PPr ppr10 = wmlObjectFactory.createPPr();
		            lvl10.setPPr(ppr10);
		                // Create object for ind
		                PPrBase.Ind pprbaseind10 = wmlObjectFactory.createPPrBaseInd();
		                ppr10.setInd(pprbaseind10);
		                    pprbaseind10.setLeft( BigInteger.valueOf( 720) );
		                    pprbaseind10.setHanging( BigInteger.valueOf( 360) );
		            // Create object for rPr
		            RPr rpr = wmlObjectFactory.createRPr();
		            lvl10.setRPr(rpr);
		                // Create object for rFonts
		                RFonts rfonts = wmlObjectFactory.createRFonts();
		                rpr.setRFonts(rfonts);
		                    rfonts.setAscii( "Symbol");
		                    rfonts.setHint(org.docx4j.wml.STHint.DEFAULT);
		                    rfonts.setHAnsi( "Symbol");
		            // Create object for numFmt
		            NumFmt numfmt10 = wmlObjectFactory.createNumFmt();
		            lvl10.setNumFmt(numfmt10);
		                numfmt10.setVal(org.docx4j.wml.NumberFormat.BULLET);
		            // Create object for lvlText
		            Lvl.LvlText lvllvltext10 = wmlObjectFactory.createLvlLvlText();
		            lvl10.setLvlText(lvllvltext10);
		                lvllvltext10.setVal( "");
		            // Create object for lvlJc
		            Jc jc10 = wmlObjectFactory.createJc();
		            lvl10.setLvlJc(jc10);
		                jc10.setVal(org.docx4j.wml.JcEnumeration.LEFT);
		            lvl10.setTplc( "04090001");
		            // Create object for start
		            Lvl.Start lvlstart10 = wmlObjectFactory.createLvlStart();
		            lvl10.setStart(lvlstart10);
		                lvlstart10.setVal( BigInteger.valueOf( 1) );
		        // Create object for lvl
		        Lvl lvl11 = wmlObjectFactory.createLvl();
		        numberingabstractnum2.getLvl().add( lvl11);
		            lvl11.setIlvl( BigInteger.valueOf( 1) );
		            // Create object for pPr
		            PPr ppr11 = wmlObjectFactory.createPPr();
		            lvl11.setPPr(ppr11);
		                // Create object for ind
		                PPrBase.Ind pprbaseind11 = wmlObjectFactory.createPPrBaseInd();
		                ppr11.setInd(pprbaseind11);
		                    pprbaseind11.setLeft( BigInteger.valueOf( 1440) );
		                    pprbaseind11.setHanging( BigInteger.valueOf( 360) );
		            // Create object for numFmt
		            NumFmt numfmt11 = wmlObjectFactory.createNumFmt();
		            lvl11.setNumFmt(numfmt11);
		                numfmt11.setVal(org.docx4j.wml.NumberFormat.LOWER_LETTER);
		            // Create object for lvlText
		            Lvl.LvlText lvllvltext11 = wmlObjectFactory.createLvlLvlText();
		            lvl11.setLvlText(lvllvltext11);
		                lvllvltext11.setVal( "%2.");
		            // Create object for lvlJc
		            Jc jc11 = wmlObjectFactory.createJc();
		            lvl11.setLvlJc(jc11);
		                jc11.setVal(org.docx4j.wml.JcEnumeration.LEFT);
		            lvl11.setTplc( "04090019");
		            // Create object for start
		            Lvl.Start lvlstart11 = wmlObjectFactory.createLvlStart();
		            lvl11.setStart(lvlstart11);
		                lvlstart11.setVal( BigInteger.valueOf( 1) );
		        // Create object for lvl
		        Lvl lvl12 = wmlObjectFactory.createLvl();
		        numberingabstractnum2.getLvl().add( lvl12);
		            lvl12.setIlvl( BigInteger.valueOf( 2) );
		            // Create object for pPr
		            PPr ppr12 = wmlObjectFactory.createPPr();
		            lvl12.setPPr(ppr12);
		                // Create object for ind
		                PPrBase.Ind pprbaseind12 = wmlObjectFactory.createPPrBaseInd();
		                ppr12.setInd(pprbaseind12);
		                    pprbaseind12.setLeft( BigInteger.valueOf( 2160) );
		                    pprbaseind12.setHanging( BigInteger.valueOf( 180) );
		            // Create object for numFmt
		            NumFmt numfmt12 = wmlObjectFactory.createNumFmt();
		            lvl12.setNumFmt(numfmt12);
		                numfmt12.setVal(org.docx4j.wml.NumberFormat.LOWER_ROMAN);
		            // Create object for lvlText
		            Lvl.LvlText lvllvltext12 = wmlObjectFactory.createLvlLvlText();
		            lvl12.setLvlText(lvllvltext12);
		                lvllvltext12.setVal( "%3.");
		            // Create object for lvlJc
		            Jc jc12 = wmlObjectFactory.createJc();
		            lvl12.setLvlJc(jc12);
		                jc12.setVal(org.docx4j.wml.JcEnumeration.RIGHT);
		            lvl12.setTplc( "0409001B");
		            // Create object for start
		            Lvl.Start lvlstart12 = wmlObjectFactory.createLvlStart();
		            lvl12.setStart(lvlstart12);
		                lvlstart12.setVal( BigInteger.valueOf( 1) );
		        // Create object for lvl
		        Lvl lvl13 = wmlObjectFactory.createLvl();
		        numberingabstractnum2.getLvl().add( lvl13);
		            lvl13.setIlvl( BigInteger.valueOf( 3) );
		            // Create object for pPr
		            PPr ppr13 = wmlObjectFactory.createPPr();
		            lvl13.setPPr(ppr13);
		                // Create object for ind
		                PPrBase.Ind pprbaseind13 = wmlObjectFactory.createPPrBaseInd();
		                ppr13.setInd(pprbaseind13);
		                    pprbaseind13.setLeft( BigInteger.valueOf( 2880) );
		                    pprbaseind13.setHanging( BigInteger.valueOf( 360) );
		            // Create object for numFmt
		            NumFmt numfmt13 = wmlObjectFactory.createNumFmt();
		            lvl13.setNumFmt(numfmt13);
		                numfmt13.setVal(org.docx4j.wml.NumberFormat.DECIMAL);
		            // Create object for lvlText
		            Lvl.LvlText lvllvltext13 = wmlObjectFactory.createLvlLvlText();
		            lvl13.setLvlText(lvllvltext13);
		                lvllvltext13.setVal( "%4.");
		            // Create object for lvlJc
		            Jc jc13 = wmlObjectFactory.createJc();
		            lvl13.setLvlJc(jc13);
		                jc13.setVal(org.docx4j.wml.JcEnumeration.LEFT);
		            lvl13.setTplc( "0409000F");
		            // Create object for start
		            Lvl.Start lvlstart13 = wmlObjectFactory.createLvlStart();
		            lvl13.setStart(lvlstart13);
		                lvlstart13.setVal( BigInteger.valueOf( 1) );
		        // Create object for lvl
		        Lvl lvl14 = wmlObjectFactory.createLvl();
		        numberingabstractnum2.getLvl().add( lvl14);
		            lvl14.setIlvl( BigInteger.valueOf( 4) );
		            // Create object for pPr
		            PPr ppr14 = wmlObjectFactory.createPPr();
		            lvl14.setPPr(ppr14);
		                // Create object for ind
		                PPrBase.Ind pprbaseind14 = wmlObjectFactory.createPPrBaseInd();
		                ppr14.setInd(pprbaseind14);
		                    pprbaseind14.setLeft( BigInteger.valueOf( 3600) );
		                    pprbaseind14.setHanging( BigInteger.valueOf( 360) );
		            // Create object for numFmt
		            NumFmt numfmt14 = wmlObjectFactory.createNumFmt();
		            lvl14.setNumFmt(numfmt14);
		                numfmt14.setVal(org.docx4j.wml.NumberFormat.LOWER_LETTER);
		            // Create object for lvlText
		            Lvl.LvlText lvllvltext14 = wmlObjectFactory.createLvlLvlText();
		            lvl14.setLvlText(lvllvltext14);
		                lvllvltext14.setVal( "%5.");
		            // Create object for lvlJc
		            Jc jc14 = wmlObjectFactory.createJc();
		            lvl14.setLvlJc(jc14);
		                jc14.setVal(org.docx4j.wml.JcEnumeration.LEFT);
		            lvl14.setTplc( "04090019");
		            // Create object for start
		            Lvl.Start lvlstart14 = wmlObjectFactory.createLvlStart();
		            lvl14.setStart(lvlstart14);
		                lvlstart14.setVal( BigInteger.valueOf( 1) );
		        // Create object for lvl
		        Lvl lvl15 = wmlObjectFactory.createLvl();
		        numberingabstractnum2.getLvl().add( lvl15);
		            lvl15.setIlvl( BigInteger.valueOf( 5) );
		            // Create object for pPr
		            PPr ppr15 = wmlObjectFactory.createPPr();
		            lvl15.setPPr(ppr15);
		                // Create object for ind
		                PPrBase.Ind pprbaseind15 = wmlObjectFactory.createPPrBaseInd();
		                ppr15.setInd(pprbaseind15);
		                    pprbaseind15.setLeft( BigInteger.valueOf( 4320) );
		                    pprbaseind15.setHanging( BigInteger.valueOf( 180) );
		            // Create object for numFmt
		            NumFmt numfmt15 = wmlObjectFactory.createNumFmt();
		            lvl15.setNumFmt(numfmt15);
		                numfmt15.setVal(org.docx4j.wml.NumberFormat.LOWER_ROMAN);
		            // Create object for lvlText
		            Lvl.LvlText lvllvltext15 = wmlObjectFactory.createLvlLvlText();
		            lvl15.setLvlText(lvllvltext15);
		                lvllvltext15.setVal( "%6.");
		            // Create object for lvlJc
		            Jc jc15 = wmlObjectFactory.createJc();
		            lvl15.setLvlJc(jc15);
		                jc15.setVal(org.docx4j.wml.JcEnumeration.RIGHT);
		            lvl15.setTplc( "0409001B");
		            // Create object for start
		            Lvl.Start lvlstart15 = wmlObjectFactory.createLvlStart();
		            lvl15.setStart(lvlstart15);
		                lvlstart15.setVal( BigInteger.valueOf( 1) );
		        // Create object for lvl
		        Lvl lvl16 = wmlObjectFactory.createLvl();
		        numberingabstractnum2.getLvl().add( lvl16);
		            lvl16.setIlvl( BigInteger.valueOf( 6) );
		            // Create object for pPr
		            PPr ppr16 = wmlObjectFactory.createPPr();
		            lvl16.setPPr(ppr16);
		                // Create object for ind
		                PPrBase.Ind pprbaseind16 = wmlObjectFactory.createPPrBaseInd();
		                ppr16.setInd(pprbaseind16);
		                    pprbaseind16.setLeft( BigInteger.valueOf( 5040) );
		                    pprbaseind16.setHanging( BigInteger.valueOf( 360) );
		            // Create object for numFmt
		            NumFmt numfmt16 = wmlObjectFactory.createNumFmt();
		            lvl16.setNumFmt(numfmt16);
		                numfmt16.setVal(org.docx4j.wml.NumberFormat.DECIMAL);
		            // Create object for lvlText
		            Lvl.LvlText lvllvltext16 = wmlObjectFactory.createLvlLvlText();
		            lvl16.setLvlText(lvllvltext16);
		                lvllvltext16.setVal( "%7.");
		            // Create object for lvlJc
		            Jc jc16 = wmlObjectFactory.createJc();
		            lvl16.setLvlJc(jc16);
		                jc16.setVal(org.docx4j.wml.JcEnumeration.LEFT);
		            lvl16.setTplc( "0409000F");
		            // Create object for start
		            Lvl.Start lvlstart16 = wmlObjectFactory.createLvlStart();
		            lvl16.setStart(lvlstart16);
		                lvlstart16.setVal( BigInteger.valueOf( 1) );
		        // Create object for lvl
		        Lvl lvl17 = wmlObjectFactory.createLvl();
		        numberingabstractnum2.getLvl().add( lvl17);
		            lvl17.setIlvl( BigInteger.valueOf( 7) );
		            // Create object for pPr
		            PPr ppr17 = wmlObjectFactory.createPPr();
		            lvl17.setPPr(ppr17);
		                // Create object for ind
		                PPrBase.Ind pprbaseind17 = wmlObjectFactory.createPPrBaseInd();
		                ppr17.setInd(pprbaseind17);
		                    pprbaseind17.setLeft( BigInteger.valueOf( 5760) );
		                    pprbaseind17.setHanging( BigInteger.valueOf( 360) );
		            // Create object for numFmt
		            NumFmt numfmt17 = wmlObjectFactory.createNumFmt();
		            lvl17.setNumFmt(numfmt17);
		                numfmt17.setVal(org.docx4j.wml.NumberFormat.LOWER_LETTER);
		            // Create object for lvlText
		            Lvl.LvlText lvllvltext17 = wmlObjectFactory.createLvlLvlText();
		            lvl17.setLvlText(lvllvltext17);
		                lvllvltext17.setVal( "%8.");
		            // Create object for lvlJc
		            Jc jc17 = wmlObjectFactory.createJc();
		            lvl17.setLvlJc(jc17);
		                jc17.setVal(org.docx4j.wml.JcEnumeration.LEFT);
		            lvl17.setTplc( "04090019");
		            // Create object for start
		            Lvl.Start lvlstart17 = wmlObjectFactory.createLvlStart();
		            lvl17.setStart(lvlstart17);
		                lvlstart17.setVal( BigInteger.valueOf( 1) );
		        // Create object for lvl
		        Lvl lvl18 = wmlObjectFactory.createLvl();
		        numberingabstractnum2.getLvl().add( lvl18);
		            lvl18.setIlvl( BigInteger.valueOf( 8) );
		            // Create object for pPr
		            PPr ppr18 = wmlObjectFactory.createPPr();
		            lvl18.setPPr(ppr18);
		                // Create object for ind
		                PPrBase.Ind pprbaseind18 = wmlObjectFactory.createPPrBaseInd();
		                ppr18.setInd(pprbaseind18);
		                    pprbaseind18.setLeft( BigInteger.valueOf( 6480) );
		                    pprbaseind18.setHanging( BigInteger.valueOf( 180) );
		            // Create object for numFmt
		            NumFmt numfmt18 = wmlObjectFactory.createNumFmt();
		            lvl18.setNumFmt(numfmt18);
		                numfmt18.setVal(org.docx4j.wml.NumberFormat.LOWER_ROMAN);
		            // Create object for lvlText
		            Lvl.LvlText lvllvltext18 = wmlObjectFactory.createLvlLvlText();
		            lvl18.setLvlText(lvllvltext18);
		                lvllvltext18.setVal( "%9.");
		            // Create object for lvlJc
		            Jc jc18 = wmlObjectFactory.createJc();
		            lvl18.setLvlJc(jc18);
		                jc18.setVal(org.docx4j.wml.JcEnumeration.RIGHT);
		            lvl18.setTplc( "0409001B");
		            // Create object for start
		            Lvl.Start lvlstart18 = wmlObjectFactory.createLvlStart();
		            lvl18.setStart(lvlstart18);
		                lvlstart18.setVal( BigInteger.valueOf( 1) );
		        // Create object for nsid
		        CTLongHexNumber longhexnumber3 = wmlObjectFactory.createCTLongHexNumber();
		        numberingabstractnum2.setNsid(longhexnumber3);
		            longhexnumber3.setVal( "72BA27DB");
		        // Create object for multiLevelType
		        Numbering.AbstractNum.MultiLevelType numberingabstractnummultileveltype2 = wmlObjectFactory.createNumberingAbstractNumMultiLevelType();
		        numberingabstractnum2.setMultiLevelType(numberingabstractnummultileveltype2);
		            numberingabstractnummultileveltype2.setVal( "hybridMultilevel");
		        // Create object for tmpl
		        CTLongHexNumber longhexnumber4 = wmlObjectFactory.createCTLongHexNumber();
		        numberingabstractnum2.setTmpl(longhexnumber4);
		            longhexnumber4.setVal( "75ACB30A");

		return ndp;
	}

	private DocumentSettingsPart addSettingsPart() throws InvalidFormatException {
		DocumentSettingsPart dsp = new DocumentSettingsPart();
		CTSettings settings = wmlObjectFactory.createCTSettings();
		dsp.setJaxbElement(settings);
		// Create object for defaultTabStop
		CTTwipsMeasure twipsmeasure = wmlObjectFactory.createCTTwipsMeasure();
		settings.setDefaultTabStop(twipsmeasure);
		twipsmeasure.setVal(BigInteger.valueOf(708));
		// Create object for zoom
		CTZoom zoom = wmlObjectFactory.createCTZoom();
		settings.setZoom(zoom);
		zoom.setPercent(BigInteger.valueOf(120));
		// Create object for proofState
		CTProof proof = wmlObjectFactory.createCTProof();
		settings.setProofState(proof);
		proof.setGrammar(org.docx4j.wml.STProof.CLEAN);
		// Create object for hyphenationZone
		CTTwipsMeasure twipsmeasure2 = wmlObjectFactory.createCTTwipsMeasure();
		settings.setHyphenationZone(twipsmeasure2);
		twipsmeasure2.setVal(BigInteger.valueOf(425));
		// Create object for characterSpacingControl
		CTCharacterSpacing characterspacing = wmlObjectFactory.createCTCharacterSpacing();
		settings.setCharacterSpacingControl(characterspacing);
		characterspacing.setVal(org.docx4j.wml.STCharacterSpacing.DO_NOT_COMPRESS);
		// Create object for compat
		CTCompat compat = wmlObjectFactory.createCTCompat();
		settings.setCompat(compat);
		// Create object for compatSetting
		CTCompatSetting compatsetting = wmlObjectFactory.createCTCompatSetting();
		compat.getCompatSetting().add(compatsetting);
		compatsetting.setUri("http://schemas.microsoft.com/office/word");
		compatsetting.setVal("14");
		compatsetting.setName("compatibilityMode");
		// Create object for compatSetting
		CTCompatSetting compatsetting2 = wmlObjectFactory.createCTCompatSetting();
		compat.getCompatSetting().add(compatsetting2);
		compatsetting2.setUri("http://schemas.microsoft.com/office/word");
		compatsetting2.setVal("1");
		compatsetting2.setName("overrideTableStyleFontSizeAndJustification");
		// Create object for compatSetting
		CTCompatSetting compatsetting3 = wmlObjectFactory.createCTCompatSetting();
		compat.getCompatSetting().add(compatsetting3);
		compatsetting3.setUri("http://schemas.microsoft.com/office/word");
		compatsetting3.setVal("1");
		compatsetting3.setName("enableOpenTypeFeatures");
		// Create object for compatSetting
		CTCompatSetting compatsetting4 = wmlObjectFactory.createCTCompatSetting();
		compat.getCompatSetting().add(compatsetting4);
		compatsetting4.setUri("http://schemas.microsoft.com/office/word");
		compatsetting4.setVal("1");
		compatsetting4.setName("doNotFlipMirrorIndents");
		org.docx4j.math.ObjectFactory mathObjectFactory = new org.docx4j.math.ObjectFactory();
		// Create object for mathPr
		CTMathPr mathpr = mathObjectFactory.createCTMathPr();
		settings.setMathPr(mathpr);
		// Create object for dispDef
		CTOnOff onoff = mathObjectFactory.createCTOnOff();
		mathpr.setDispDef(onoff);
		// Create object for lMargin
		org.docx4j.math.CTTwipsMeasure twipsmeasure3 = mathObjectFactory.createCTTwipsMeasure();
		mathpr.setLMargin(twipsmeasure3);
		twipsmeasure3.setVal("0");
		// Create object for rMargin
		org.docx4j.math.CTTwipsMeasure twipsmeasure4 = mathObjectFactory.createCTTwipsMeasure();
		mathpr.setRMargin(twipsmeasure4);
		twipsmeasure4.setVal("0");
		// Create object for defJc
		CTOMathJc omathjc = mathObjectFactory.createCTOMathJc();
		mathpr.setDefJc(omathjc);
		omathjc.setVal(org.docx4j.math.STJc.CENTER_GROUP);
		// Create object for wrapIndent
		org.docx4j.math.CTTwipsMeasure twipsmeasure5 = mathObjectFactory.createCTTwipsMeasure();
		mathpr.setWrapIndent(twipsmeasure5);
		twipsmeasure5.setVal("1440");
		// Create object for intLim
		CTLimLoc limloc = mathObjectFactory.createCTLimLoc();
		mathpr.setIntLim(limloc);
		limloc.setVal(org.docx4j.math.STLimLoc.SUB_SUP);
		// Create object for naryLim
		CTLimLoc limloc2 = mathObjectFactory.createCTLimLoc();
		mathpr.setNaryLim(limloc2);
		limloc2.setVal(org.docx4j.math.STLimLoc.UND_OVR);
		// Create object for mathFont
		CTString string = mathObjectFactory.createCTString();
		mathpr.setMathFont(string);
		string.setVal("Cambria Math");
		// Create object for brkBin
		CTBreakBin breakbin = mathObjectFactory.createCTBreakBin();
		mathpr.setBrkBin(breakbin);
		breakbin.setVal(org.docx4j.math.STBreakBin.BEFORE);
		// Create object for brkBinSub
		CTBreakBinSub breakbinsub = mathObjectFactory.createCTBreakBinSub();
		mathpr.setBrkBinSub(breakbinsub);
		breakbinsub.setVal("--");
		// Create object for smallFrac
		CTOnOff onoff2 = mathObjectFactory.createCTOnOff();
		mathpr.setSmallFrac(onoff2);
		onoff2.setVal(org.docx4j.sharedtypes.STOnOff.ZERO);
		// Create object for themeFontLang
		CTLanguage language = wmlObjectFactory.createCTLanguage();
		settings.setThemeFontLang(language);
		language.setVal("en-US");
		language.setEastAsia("ja-JP");
		// Create object for clrSchemeMapping
		CTColorSchemeMapping colorschememapping = wmlObjectFactory.createCTColorSchemeMapping();
		settings.setClrSchemeMapping(colorschememapping);
		colorschememapping.setBg1(org.docx4j.wml.STColorSchemeIndex.LIGHT_1);
		colorschememapping.setT1(org.docx4j.wml.STColorSchemeIndex.DARK_1);
		colorschememapping.setBg2(org.docx4j.wml.STColorSchemeIndex.LIGHT_2);
		colorschememapping.setT2(org.docx4j.wml.STColorSchemeIndex.DARK_2);
		colorschememapping.setAccent1(org.docx4j.wml.STColorSchemeIndex.ACCENT_1);
		colorschememapping.setAccent2(org.docx4j.wml.STColorSchemeIndex.ACCENT_2);
		colorschememapping.setAccent3(org.docx4j.wml.STColorSchemeIndex.ACCENT_3);
		colorschememapping.setAccent4(org.docx4j.wml.STColorSchemeIndex.ACCENT_4);
		colorschememapping.setAccent5(org.docx4j.wml.STColorSchemeIndex.ACCENT_5);
		colorschememapping.setAccent6(org.docx4j.wml.STColorSchemeIndex.ACCENT_6);
		colorschememapping.setHyperlink(org.docx4j.wml.STColorSchemeIndex.HYPERLINK);
		colorschememapping.setFollowedHyperlink(org.docx4j.wml.STColorSchemeIndex.FOLLOWED_HYPERLINK);
		// Create object for doNotAutoCompressPictures
		BooleanDefaultTrue booleandefaulttrue = wmlObjectFactory.createBooleanDefaultTrue();
		settings.setDoNotAutoCompressPictures(booleandefaulttrue);
		// Create object for shapeDefaults
		CTShapeDefaults shapedefaults = wmlObjectFactory.createCTShapeDefaults();
		settings.setShapeDefaults(shapedefaults);
		org.docx4j.vml.officedrawing.ObjectFactory vmlofficedrawingObjectFactory = new org.docx4j.vml.officedrawing.ObjectFactory();
		// Create object for shapedefaults (wrapped in JAXBElement)
		org.docx4j.vml.officedrawing.CTShapeDefaults shapedefaults2 = vmlofficedrawingObjectFactory.createCTShapeDefaults();
		JAXBElement<org.docx4j.vml.officedrawing.CTShapeDefaults> shapedefaultsWrapped = vmlofficedrawingObjectFactory
				.createShapedefaults(shapedefaults2);
		shapedefaults.getAny().add(shapedefaultsWrapped);
		shapedefaults2.setSpidmax(BigInteger.valueOf(1026));
		shapedefaults2.setExt(org.docx4j.vml.STExt.EDIT);
		// Create object for shapelayout (wrapped in JAXBElement)
		CTShapeLayout shapelayout = vmlofficedrawingObjectFactory.createCTShapeLayout();
		JAXBElement<org.docx4j.vml.officedrawing.CTShapeLayout> shapelayoutWrapped = vmlofficedrawingObjectFactory.createShapelayout(shapelayout);
		shapedefaults.getAny().add(shapelayoutWrapped);
		// Create object for idmap
		CTIdMap idmap = vmlofficedrawingObjectFactory.createCTIdMap();
		shapelayout.setIdmap(idmap);
		idmap.setExt(org.docx4j.vml.STExt.EDIT);
		idmap.setData("1");
		shapelayout.setExt(org.docx4j.vml.STExt.EDIT);
		// Create object for decimalSymbol
		CTSettings.DecimalSymbol settingsdecimalsymbol = wmlObjectFactory.createCTSettingsDecimalSymbol();
		settings.setDecimalSymbol(settingsdecimalsymbol);
		settingsdecimalsymbol.setVal(".");
		// Create object for listSeparator
		CTSettings.ListSeparator settingslistseparator = wmlObjectFactory.createCTSettingsListSeparator();
		settings.setListSeparator(settingslistseparator);
		settingslistseparator.setVal(";");
		return dsp;
	};

	/*
	 * Heading1 EnvelopeAddress NormalWeb ColorfulGrid SalutationChar
	 * ClosingChar Footer CommentText IntenseQuote Emphasis HTMLCite
	 * BodyText3Char Date TableofFigures Heading9 ListNumber Heading8
	 * HTMLKeyboard Heading7 Heading6 Heading3Char Index1 Heading5 Heading5Char
	 * Heading4 Heading3 BodyTextIndentChar Heading2 EnvelopeReturn IndexHeading
	 * Salutation Heading1Char MessageHeaderChar EndnoteReference SubtleEmphasis
	 * FooterChar Closing TOCHeading BodyText2Char DocumentMap EndnoteTextChar
	 * HeaderChar PlaceholderText Caption BodyTextIndent BodyTextIndent3
	 * ListNumber3 List2 ListNumber4 List3 ListNumber5 BodyTextIndent2 List4
	 * List5 HTMLTypewriter ListNumber2 ArticleSection FootnoteTextChar
	 * FootnoteText Normal Header HTMLSample HTMLAddressChar MacroTextChar
	 * IntenseEmphasis NoSpacing CommentSubject BodyTextIndent3Char BodyTextChar
	 * Index5 Heading2Char Index4 TitleChar Index3 Index2 Index9 Index8
	 * HTMLVariable Index7 Index6 TOAHeading NoList Title BodyTextFirstIndent2
	 * QuoteChar MacroText TableGrid ListBullet NormalIndent
	 * BodyTextFirstIndentChar DateChar FootnoteReference TOC8 ListContinue5
	 * Strong TOC9 TableofAuthorities Heading4Char NoteHeading HTMLDefinition
	 * NoteHeadingChar HTMLPreformattedChar BookTitle LineNumber ListContinue
	 * Quote HTMLPreformatted SubtleReference TOC7 ListContinue4
	 * BodyTextFirstIndent2Char TOC6 ListContinue3 TOC5 ListContinue2
	 * IntenseQuoteChar TOC4 TOC3 TOC2 TOC1 PageNumber DefaultParagraphFont
	 * CommentTextChar BodyText2 BodyText3 PlainTextChar BodyTextIndent2Char
	 * SubtitleChar Bibliography TableNormal ListParagraph HTMLAddress
	 * E-mailSignature E-mailSignatureChar BalloonText DocumentMapChar
	 * BalloonTextChar IntenseReference 111111 Signature EndnoteText
	 * SignatureChar CommentReference MessageHeader ListBullet2 ListBullet3
	 * BodyTextFirstIndent ListBullet4 ListBullet5 Heading6Char PlainText 1ai
	 * List Hyperlink HTMLCode Subtitle BodyText Heading7Char Heading9Char
	 * BlockText HTMLAcronym Heading8Char CommentSubjectChar FollowedHyperlink
	 */
	@Override
	public void beginHeading(int level, Attributes attributes) {
		currentStyle = "Heading" + level;
		styleExists();
	}

	private void styleExists() {
		Map<String, Style> knownStyles = StyleDefinitionsPart.getKnownStyles();
		if (!knownStyles.containsKey(currentStyle)) {
			throw new IllegalArgumentException("Unknown style!");
		}
	}

	@Override
	public void beginSpan(SpanType type, Attributes attributes) {
		if (characters!=null && characters.length()>0) {
			endSpan();
		}
		currentSpanType = type;
	}

	@Override
	public void characters(String text) {
		characters.append(text);
	}

	@Override
	public void charactersUnescaped(String literal) {
//		characters = literal;
	}

	/**
	 *
	 * @param chartRelId
	 * @return the chart paragraph
	 * @throws JAXBException
	 */
	private P chart(String chartRelId, String chartId) throws JAXBException {
		String ml = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
				+ "xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" w:rsidR=\"0084689C\" w:rsidRDefault=\"00D47CF0\">"
				+ "			<w:r>"
				+ "				<w:rPr>"
				+ "					<w:noProof />"
				+ "				</w:rPr>"
				+ "				<w:drawing>"
				+ "					<wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\">"
				+ "						<wp:extent cx=\"5486400\" cy=\"3200400\" />"
				+ "						<wp:effectExtent l=\"0\" t=\"0\" r=\"25400\" b=\"25400\" />"
				+ "						<wp:docPr id=\"${docPr}\" name=\"Diagram "+chartId+"\" />"
				+ "						<wp:cNvGraphicFramePr />"
				+ "						<a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
				+ "							<a:graphicData"
				+ "								uri=\"http://schemas.openxmlformats.org/drawingml/2006/chart\">"
				+ "								<c:chart"
				+ "									xmlns:c=\"http://schemas.openxmlformats.org/drawingml/2006/chart\""
				+ "									xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\""
				+ "									r:id=\"${chartRelId}\" />"
				+ "							</a:graphicData>"
				+ "						</a:graphic>"
				+ "					</wp:inline>"
				+ "				</w:drawing>"
				+ "			</w:r>"
				+ "       </w:p>";
		java.util.HashMap<String, String> mappings = new java.util.HashMap<String, String>();
		mappings.put("docPr", chartId);
		mappings.put("chartRelId", chartRelId);
		return (P) org.docx4j.XmlUtils.unmarshallFromTemplate(ml, mappings);
	}

	/**
	 * Creates a simple line chart using the given data set.
	 * <p>
	 * <b>Implement using {@link DocumentBuilderExtension} when this is read</b>
	 * </p>
	 *
	 * @param xSeries
	 *            the data set
	 */
	public void chart(String caption, String title, String ylabel, String xlabel, PlotSet plotset) {
		try {
			String prId = Integer.toString(++chartCounter);
			Chart chart = new Chart(new PartName("/word/charts/chart" + prId + ".xml"));

			CTChartSpace chartSpace = ChartFactory.createChartSpace(title, ylabel, xlabel, plotset);
			chart.setContentType(new ContentType(ContentTypes.DRAWINGML_CHART));
			chart.setJaxbElement(chartSpace);

			Relationship part = mainDocumentPart.addTargetPart(chart);
			mainDocumentPart.addObject(chart(part.getId(), prId));
			caption(caption, CaptionType.Figure);
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	private byte[] convertImageToByteArray(File file) throws FileNotFoundException, IOException {
		InputStream is = new FileInputStream(file);
		long length = file.length();
		byte[] bytes = new byte[(int) length];
		int offset = 0;
		int numRead = 0;
		while ((offset < bytes.length) && ((numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)) {
			offset += numRead;
		}
		is.close();
		return bytes;
	}


	/**
	 * Use to apply a specific style on the given paragraph.
	 *
	 * @param p
	 * @param style
	 * @return
	 */
	private PPr applyStyle(P p, String style) {
//		StyleDefinitionsPart styleDefinitionsPart = mainDocumentPart.getStyleDefinitionsPart();
		if (mainDocumentPart.getPropertyResolver().activateStyle(style)) {
			// Style is available
			org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
			org.docx4j.wml.PPr pPr = factory.createPPr();
			p.setPPr(pPr);
			org.docx4j.wml.PPrBase.PStyle pStyle = factory.createPPrBasePStyle();
			pPr.setPStyle(pStyle);
			pStyle.setVal(style);
			return pPr;
		}
		throw new RuntimeException("Missing style "+style);
		//return null;
	}

	private org.docx4j.wml.RPr createSpan(String text) {
		org.docx4j.wml.Text t = factory.createText();
		t.setSpace( "preserve");
		t.setValue(text);
		org.docx4j.wml.R run = factory.createR();
		run.getContent().add(t);
		currentParagraph.getContent().add(run);
		org.docx4j.wml.RPr rpr = factory.createRPr();
		run.setRPr(rpr);
		return rpr;
	}

	private TblBorders createTableBorders() {

		TblBorders tblborders = wmlObjectFactory.createTblBorders();
		// Create object for left
		CTBorder border = wmlObjectFactory.createCTBorder();
		tblborders.setLeft(border);
		border.setVal(org.docx4j.wml.STBorder.SINGLE);
		border.setColor("auto");
		border.setSz(BigInteger.valueOf(4));
		border.setSpace(BigInteger.valueOf(0));
		// Create object for right
		CTBorder border2 = wmlObjectFactory.createCTBorder();
		tblborders.setRight(border2);
		border2.setVal(org.docx4j.wml.STBorder.SINGLE);
		border2.setColor("auto");
		border2.setSz(BigInteger.valueOf(4));
		border2.setSpace(BigInteger.valueOf(0));
		// Create object for top
		CTBorder border3 = wmlObjectFactory.createCTBorder();
		tblborders.setTop(border3);
		border3.setVal(org.docx4j.wml.STBorder.SINGLE);
		border3.setColor("auto");
		border3.setSz(BigInteger.valueOf(4));
		border3.setSpace(BigInteger.valueOf(0));
		// Create object for bottom
		CTBorder border4 = wmlObjectFactory.createCTBorder();
		tblborders.setBottom(border4);
		border4.setVal(org.docx4j.wml.STBorder.SINGLE);
		border4.setColor("auto");
		border4.setSz(BigInteger.valueOf(4));
		border4.setSpace(BigInteger.valueOf(0));
		// Create object for insideH
		CTBorder border5 = wmlObjectFactory.createCTBorder();
		tblborders.setInsideH(border5);
		border5.setVal(org.docx4j.wml.STBorder.SINGLE);
		border5.setColor("auto");
		border5.setSz(BigInteger.valueOf(6));
		border5.setSpace(BigInteger.valueOf(0));
		// Create object for insideV
		CTBorder border6 = wmlObjectFactory.createCTBorder();
		tblborders.setInsideV(border6);
		border6.setVal(org.docx4j.wml.STBorder.SINGLE);
		border6.setColor("auto");
		border6.setSz(BigInteger.valueOf(6));
		border6.setSpace(BigInteger.valueOf(0));

		return tblborders;

	}

	@Override
	public void endBlock() {
		switch (currentBlockType) {
		case TABLE_CELL_HEADER:
		case TABLE_CELL_NORMAL:
			tableColumnCount++;
			break;
		default:
			break;
		}
		endSpan();
	}

	@Override
	public void endDocument() {
		try {
			wordMLPackage.save(outputFile);
		} catch (Docx4JException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void endHeading() {
		mainDocumentPart.addStyledParagraphOfText(currentStyle, characters.toString());
		characters.setLength(0);
	}

	@Override
	public void endSpan() {
		RPr block = createSpan(characters.toString());
		characters.setLength(0);
		if (currentSpanType==null) {
			return;
		}
		switch (currentSpanType) {
		case BOLD:
			block.setB(TRUE);
			break;
		case CITATION:
			block.setI(TRUE);
			break;
		case CODE:
			// TODO Auto-generated method stub
			break;
		case DELETED:
			block.setStrike(TRUE);
			break;
		case EMPHASIS:
			block.setB(TRUE);
			block.setI(TRUE);
			break;
		case INSERTED:
			break;
		case ITALIC:
			block.setI(TRUE);
			break;
		case LINK:
			// TODO Auto-generated method stub
			break;
		case MONOSPACE:
			// TODO Auto-generated method stub
			break;
		case QUOTE:
			block.setI(TRUE);
			break;
		case SPAN:

			// TODO Auto-generated method stub
			break;
		case STRONG:
			block.setB(TRUE);
			break;
		case SUBSCRIPT:
			break;
		case SUPERSCRIPT:
			// TODO Auto-generated method stub
			break;
		case UNDERLINED:
			U underline = new U();
			underline.setVal(UnderlineEnumeration.SINGLE);
			block.setU(underline);
			break;
		default:
			break;
		}
		currentSpanType = SpanType.SPAN;
	}

	@Override
	public void entityReference(String entity) {
		// Convert the XML entity to Unicode.
		if (entity.startsWith("#")) {
			char c = (char)Integer.parseInt(entity.substring(1));
			characters.append(c);
		} else {
			characters.append("<unknown entity reference>");
		}
	}

	public File getOutputFile() {
		return outputFile;
	}

	@Override
	public void image(Attributes attributes, String url) {
		byte[] bytes;
		try {
			File file = new File(url);
			bytes = convertImageToByteArray(file);
			String title = attributes.getTitle() == null ? attributes.getTitle() : file.getAbsolutePath();
			addImageToPackage(bytes, file, title);
			if (attributes.getTitle() != null) {
				caption(attributes.getTitle(), CaptionType.Image);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void imageLink(Attributes linkAttributes, Attributes imageAttributes, String href, String imageUrl) {
		// TODO Auto-generated method stub
	}

	/**
	 * Converts <b>LaTeX</b> to <b>Office MathML</b> and inserts it into the
	 * document.
	 *
	 * @param latex
	 *            the LaTeX code
	 */
	public void latex(String latex, Attributes attributes) {
		if (!latex.startsWith("$$")) {
			latex = "$$" + latex;
		}
		if (!latex.endsWith("$$")) {
			latex = latex + "$$";
		}
		/* Create vanilla SnuggleEngine and new SnuggleSession */
		SnuggleEngine engine = new SnuggleEngine();
		SnuggleSession session = engine.createSession();

		/* Parse some LaTeX input */
		SnuggleInput input = new SnuggleInput(latex);
		try {
			session.parseInput(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
		/* Specify how we want the resulting XML */
		XMLStringOutputOptions options = new XMLStringOutputOptions();
		options.setSerializationMethod(SerializationMethod.XHTML);
		options.setIndenting(true);
		options.setEncoding("UTF-8");
		options.setAddingMathSourceAnnotations(true);
		options.setUsingNamedEntities(true);
		// Transform the MathML to OOXML MathML
		StringWriter sw = new StringWriter();
		TransformerFactory tfactory = TransformerFactory.newInstance();
		StreamSource xsl = new StreamSource(OoxmlDocumentBuilder.class.getResourceAsStream("xslt/mml2omml.xsl"));
		String mathml = session.buildXMLString(options);
		Source xml = new StreamSource(new StringReader(mathml));
		StreamResult out = new StreamResult(sw);
		try {
			Transformer transformer = tfactory.newTransformer(xsl);
			transformer.transform(xml, out);
			currentParagraph = factory.createP();
			org.docx4j.math.CTOMathPara para = mathFactory.createCTOMathPara();
			JAXBContext jc = JAXBContext.newInstance("org.docx4j.math");
			CTOMath math = (CTOMath) XmlUtils.unmarshalString(sw.toString(), jc, CTOMath.class);
			para.getOMath().add(math);
			JAXBElement<CTOMathPara> wrapper = mathFactory.createOMathPara(para);
			currentParagraph.getContent().add(wrapper);
			mainDocumentPart.addObject(currentParagraph);

		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		if (attributes.getTitle() != null) {
			caption(attributes.getTitle(), CaptionType.Equation);
		}
	}

	@Override
	public void lineBreak() {
		org.docx4j.wml.Br br = new org.docx4j.wml.Br();
		br.setType(STBrType.TEXT_WRAPPING);
		mainDocumentPart.addObject(br);
	}

	@Override
	public void link(Attributes attributes, String hrefOrHashName, String text) {
		// TODO Auto-generated method stub
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	public void setSubTitle(String text) {
		this.subtitle = text;
	}

	public void setTitle(String text) {
		this.title = text;
	}

	/**
	 * Unzip it
	 *
	 * @param zipFile
	 *            input zip file
	 * @param output
	 *            zip file output folder
	 */
	public void unZipIt(String zipFile, String outputFolder) {

		byte[] buffer = new byte[1024];

		try {

			// create output directory is not exists
			File folder = new File(outputFolder);
			if (!folder.exists()) {
				folder.mkdir();
			}

			// get the zip file content
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {

				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator + fileName);

				System.out.println("file unzip : " + newFile.getAbsoluteFile());

				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				new File(newFile.getParent()).mkdirs();

				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();

			System.out.println("Done");

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}

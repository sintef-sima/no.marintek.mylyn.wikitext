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
import org.docx4j.dml.chart.CTChartSpace;
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
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.CTProof;
import org.docx4j.wml.CTSettings;
import org.docx4j.wml.CTShapeDefaults;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.CTTwipsMeasure;
import org.docx4j.wml.CTZoom;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
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
import org.eclipse.mylyn.wikitext.core.parser.builder.DocumentBuilderExtension;

import uk.ac.ed.ph.snuggletex.SerializationMethod;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.XMLStringOutputOptions;

/**
 * 
 * @author Torkild U. Resheim
 * @since 1.9
 */
public class OoxmlDocumentBuilder extends DocumentBuilder {

	private String characters;

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
	 * @param text
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
		if (currentBlockType.equals(BlockType.PARAGRAPH)) {
			currentParagraph = factory.createP();
		}
		// List items
//		else if (currentBlockType.equals(BlockType.LIST_ITEM)) {
//			//currentParagraph = createListItem();
//		}
		// Tables
		else if (currentBlockType.equals(BlockType.TABLE)) {
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
		}
		// Table rows
		else if (currentBlockType.equals(BlockType.TABLE_ROW)) {
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
		}

		else if (currentBlockType.equals(BlockType.TABLE_CELL_NORMAL) || currentBlockType.equals(BlockType.TABLE_CELL_HEADER)) {
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

	private DocumentSettingsPart addSettingsPart() throws InvalidFormatException {
		DocumentSettingsPart dsp = new DocumentSettingsPart();
		CTSettings settings = wmlObjectFactory.createCTSettings();
		dsp.setJaxbElement(settings);
		// Create object for defaultTabStop
		CTTwipsMeasure twipsmeasure = wmlObjectFactory.createCTTwipsMeasure(); 
		settings.setDefaultTabStop(twipsmeasure); 
		    twipsmeasure.setVal( BigInteger.valueOf( 708) ); 
		// Create object for zoom
		CTZoom zoom = wmlObjectFactory.createCTZoom(); 
		settings.setZoom(zoom); 
		    zoom.setPercent( BigInteger.valueOf( 120) ); 
		// Create object for proofState
		CTProof proof = wmlObjectFactory.createCTProof(); 
		settings.setProofState(proof); 
		    proof.setGrammar(org.docx4j.wml.STProof.CLEAN);
		// Create object for hyphenationZone
		CTTwipsMeasure twipsmeasure2 = wmlObjectFactory.createCTTwipsMeasure(); 
		settings.setHyphenationZone(twipsmeasure2); 
		    twipsmeasure2.setVal( BigInteger.valueOf( 425) ); 
		// Create object for characterSpacingControl
		CTCharacterSpacing characterspacing = wmlObjectFactory.createCTCharacterSpacing(); 
		settings.setCharacterSpacingControl(characterspacing); 
		    characterspacing.setVal(org.docx4j.wml.STCharacterSpacing.DO_NOT_COMPRESS);
		// Create object for compat
		CTCompat compat = wmlObjectFactory.createCTCompat(); 
		settings.setCompat(compat); 
		    // Create object for compatSetting
		    CTCompatSetting compatsetting = wmlObjectFactory.createCTCompatSetting(); 
		    compat.getCompatSetting().add( compatsetting); 
		        compatsetting.setUri( "http://schemas.microsoft.com/office/word"); 
		        compatsetting.setVal( "14"); 
		        compatsetting.setName( "compatibilityMode"); 
		    // Create object for compatSetting
		    CTCompatSetting compatsetting2 = wmlObjectFactory.createCTCompatSetting(); 
		    compat.getCompatSetting().add( compatsetting2); 
		        compatsetting2.setUri( "http://schemas.microsoft.com/office/word"); 
		        compatsetting2.setVal( "1"); 
		        compatsetting2.setName( "overrideTableStyleFontSizeAndJustification"); 
		    // Create object for compatSetting
		    CTCompatSetting compatsetting3 = wmlObjectFactory.createCTCompatSetting(); 
		    compat.getCompatSetting().add( compatsetting3); 
		        compatsetting3.setUri( "http://schemas.microsoft.com/office/word"); 
		        compatsetting3.setVal( "1"); 
		        compatsetting3.setName( "enableOpenTypeFeatures"); 
		    // Create object for compatSetting
		    CTCompatSetting compatsetting4 = wmlObjectFactory.createCTCompatSetting(); 
		    compat.getCompatSetting().add( compatsetting4); 
		        compatsetting4.setUri( "http://schemas.microsoft.com/office/word"); 
		        compatsetting4.setVal( "1"); 
		        compatsetting4.setName( "doNotFlipMirrorIndents"); 
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
		        twipsmeasure3.setVal( "0"); 
		    // Create object for rMargin
		    org.docx4j.math.CTTwipsMeasure twipsmeasure4 = mathObjectFactory.createCTTwipsMeasure(); 
		    mathpr.setRMargin(twipsmeasure4); 
		        twipsmeasure4.setVal( "0"); 
		    // Create object for defJc
		    CTOMathJc omathjc = mathObjectFactory.createCTOMathJc(); 
		    mathpr.setDefJc(omathjc); 
		        omathjc.setVal(org.docx4j.math.STJc.CENTER_GROUP);
		    // Create object for wrapIndent
		    org.docx4j.math.CTTwipsMeasure twipsmeasure5 = mathObjectFactory.createCTTwipsMeasure(); 
		    mathpr.setWrapIndent(twipsmeasure5); 
		        twipsmeasure5.setVal( "1440"); 
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
		        string.setVal( "Cambria Math"); 
		    // Create object for brkBin
		    CTBreakBin breakbin = mathObjectFactory.createCTBreakBin(); 
		    mathpr.setBrkBin(breakbin); 
		        breakbin.setVal(org.docx4j.math.STBreakBin.BEFORE);
		    // Create object for brkBinSub
		    CTBreakBinSub breakbinsub = mathObjectFactory.createCTBreakBinSub(); 
		    mathpr.setBrkBinSub(breakbinsub); 
		        breakbinsub.setVal( "--"); 
		    // Create object for smallFrac
		    CTOnOff onoff2 = mathObjectFactory.createCTOnOff(); 
		    mathpr.setSmallFrac(onoff2); 
		        onoff2.setVal(org.docx4j.sharedtypes.STOnOff.ZERO);
		// Create object for themeFontLang
		CTLanguage language = wmlObjectFactory.createCTLanguage(); 
		settings.setThemeFontLang(language); 
		    language.setVal( "en-US"); 
		    language.setEastAsia( "ja-JP"); 
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
		    JAXBElement<org.docx4j.vml.officedrawing.CTShapeDefaults> shapedefaultsWrapped = vmlofficedrawingObjectFactory.createShapedefaults(shapedefaults2); 
		    shapedefaults.getAny().add( shapedefaultsWrapped); 
		        shapedefaults2.setSpidmax( BigInteger.valueOf( 1026) ); 
		        shapedefaults2.setExt(org.docx4j.vml.STExt.EDIT);
		    // Create object for shapelayout (wrapped in JAXBElement) 
		    CTShapeLayout shapelayout = vmlofficedrawingObjectFactory.createCTShapeLayout(); 
		    JAXBElement<org.docx4j.vml.officedrawing.CTShapeLayout> shapelayoutWrapped = vmlofficedrawingObjectFactory.createShapelayout(shapelayout); 
		    shapedefaults.getAny().add( shapelayoutWrapped); 
		        // Create object for idmap
		        CTIdMap idmap = vmlofficedrawingObjectFactory.createCTIdMap(); 
		        shapelayout.setIdmap(idmap); 
		            idmap.setExt(org.docx4j.vml.STExt.EDIT);
		            idmap.setData( "1"); 
		        shapelayout.setExt(org.docx4j.vml.STExt.EDIT);
		// Create object for decimalSymbol
		CTSettings.DecimalSymbol settingsdecimalsymbol = wmlObjectFactory.createCTSettingsDecimalSymbol(); 
		settings.setDecimalSymbol(settingsdecimalsymbol); 
		    settingsdecimalsymbol.setVal( "."); 
		// Create object for listSeparator
		CTSettings.ListSeparator settingslistseparator = wmlObjectFactory.createCTSettingsListSeparator(); 
		settings.setListSeparator(settingslistseparator); 
		    settingslistseparator.setVal( ";");
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
		currentSpanType = type;
	}

	@Override
	public void characters(String text) {
		characters = text;
	}

	@Override
	public void charactersUnescaped(String literal) {
		characters = literal;
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
	public void chart(ChartType chartType, String caption, String title, String ylabel, String xlabel, PlotSet plotset) {
		try {
			String prId = Integer.toString(++chartCounter);
			Chart c = new Chart(new PartName("/word/charts/chart" + prId + ".xml"));
			
			CTChartSpace chart = ChartFactory.createChartSpace(chartType, title, ylabel, xlabel, plotset);
			c.setContentType(new ContentType(ContentTypes.DRAWINGML_CHART));
			c.setJaxbElement(chart);
			Relationship part = mainDocumentPart.addTargetPart(c);
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
	 * Create a basic paragraph of text.
	 * 
	 * @param text
	 * @return
	 */
	private void addTextToParagraph(String text) {
		if (text != null) {
			org.docx4j.wml.Text t = factory.createText();
			t.setValue(text);
			org.docx4j.wml.R run = factory.createR();
			run.getContent().add(t);
			currentParagraph.getContent().add(run);
		}
	}

	/**
	 * Use to apply a specific style on the given paragraph.
	 * 
	 * @param p
	 * @param style
	 * @return
	 */
	private PPr applyStyle(P p, String style) {
		StyleDefinitionsPart styleDefinitionsPart = mainDocumentPart.getStyleDefinitionsPart();
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
		if (currentBlockType.equals(BlockType.TABLE_CELL_NORMAL) || currentBlockType.equals(BlockType.TABLE_CELL_HEADER)) {
			tableColumnCount++;
			endSpan();
		} else if (currentBlockType.equals(BlockType.PARAGRAPH)) {
			addTextToParagraph(characters);
			mainDocumentPart.addObject(currentParagraph);
		} else if (currentBlockType.equals(BlockType.LIST_ITEM)) {
			P p = mainDocumentPart.addStyledParagraphOfText("ListParagraph", characters);
			PPr ppr = p.getPPr();
			PPrBase.NumPr pprbasenumpr = wmlObjectFactory.createPPrBaseNumPr();
			ppr.setNumPr(pprbasenumpr);
			// Create object for numId
			PPrBase.NumPr.NumId pprbasenumprnumid = wmlObjectFactory.createPPrBaseNumPrNumId();
			pprbasenumpr.setNumId(pprbasenumprnumid);
			pprbasenumprnumid.setVal(BigInteger.valueOf(1));
			// Create object for ilvl
			PPrBase.NumPr.Ilvl pprbasenumprilvl = wmlObjectFactory.createPPrBaseNumPrIlvl();
			pprbasenumpr.setIlvl(pprbasenumprilvl);
			pprbasenumprilvl.setVal(BigInteger.valueOf(0));
		}
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
		mainDocumentPart.addStyledParagraphOfText(currentStyle, characters);
	}

	@Override
	public void endSpan() {
		RPr block = createSpan(characters);
		if (currentSpanType == null) {
			return;
		}
		switch (currentSpanType) {
		case BOLD:
			block.setB(TRUE);
			break;
		case CITATION:
			// TODO Auto-generated method stub
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
			// TODO Auto-generated method stub
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
		characters = "";
		currentSpanType = SpanType.SPAN;
	}

	@Override
	public void entityReference(String entity) {
		// TODO Auto-generated method stub
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

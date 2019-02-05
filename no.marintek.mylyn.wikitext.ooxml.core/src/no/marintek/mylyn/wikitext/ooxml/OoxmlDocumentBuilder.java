/*******************************************************************************
 * Copyright (c) 2017-2018 SINTEF Ocean
 * Copyright (c) 2013-2016 MARINTEK
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.docx4j.UnitsOfMeasurement;
import org.docx4j.XmlUtils;
import org.docx4j.dml.Theme;
import org.docx4j.dml.chart.CTChartSpace;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.ThemePart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.NumberingDefinitionsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Body;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.CTTblPrBase.TblStyle;
import org.docx4j.wml.CTVerticalAlignRun;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.Numbering;
import org.docx4j.wml.P;
import org.docx4j.wml.P.Hyperlink;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.ParaRPr;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RStyle;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.STPageOrientation;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.SectPr.PgMar;
import org.docx4j.wml.SectPr.PgSz;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.TblGrid;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TcPrInner.GridSpan;
import org.docx4j.wml.Tr;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.eclipse.core.runtime.Assert;
import org.eclipse.mylyn.wikitext.parser.Attributes;
import org.eclipse.mylyn.wikitext.parser.DocumentBuilder;
import org.eclipse.mylyn.wikitext.parser.ImageAttributes;
import org.eclipse.mylyn.wikitext.parser.LinkAttributes;
import org.eclipse.mylyn.wikitext.parser.TableCellAttributes;
import org.scilab.forge.jlatexmath.DefaultTeXFont;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import org.scilab.forge.jlatexmath.cyrillic.CyrillicRegistration;
import org.scilab.forge.jlatexmath.greek.GreekRegistration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import no.marintek.mylyn.wikitext.ooxml.internal.ChartFactory;
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
public class OoxmlDocumentBuilder extends DocumentBuilder implements IExtendedDocumentBuilder {

	/** A4 page width */
	private static final int PAGE_WIDTH = 11907;

	/** A4 page height */
	private static final int PAGE_HEIGHT = 16840;

	/** Current set of characters, collected to be added after a block or span has ended */
	private StringBuilder characters;

	private BlockType currentBlockType;

	private P currentParagraph;
	
	private ExtendedHeadingAttributes currentHeadingAttributes;

	private SpanType currentSpanType;

	private String currentStyle;

	private org.docx4j.wml.ObjectFactory factory;

	private MainDocumentPart mainDocumentPart;

	private final org.docx4j.wml.ObjectFactory wmlObjectFactory = new org.docx4j.wml.ObjectFactory();

	private File outputFile;

	private String subtitle;

	private String title;

	private final org.docx4j.wml.BooleanDefaultTrue TRUE = new org.docx4j.wml.BooleanDefaultTrue();

	private WordprocessingMLPackage wordMLPackage;

	private int chartCounter = 0;
	private int imageCounter = 0;
	private int linkCounter = 0;

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

	/**
	 * Adds a caption text to charts, images and equations.
	 * 
	 * @param text
	 *            the caption text
	 * @param captionType
	 *            the type of caption
	 */
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

		mainDocumentPart.addObject(p);
	}

	/**
	 * Adds an image to the document.
	 *
	 * @param bytes
	 *            the image bytes
	 * @param file
	 *            path to the image file
	 * @param text
	 *            alternative text
	 * @param attributes
	 *            image attributes
	 * @throws Exception
	 */
	private P addImageToPackage(byte[] bytes, File file, String text, Attributes attributes) throws Exception {
		BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordMLPackage, bytes);
		imageCounter++;
		int docPrId = 4096 + imageCounter;
		int cNvPrId = 8192 + imageCounter;
		Inline inline = null;
		if (attributes !=null && attributes instanceof ImageAttributes) {
			// specify width and automatically scale height
			ImageAttributes imageAttributes = (ImageAttributes) attributes;
			if (imageAttributes.getHeight() > 0 && imageAttributes.getWidth() > 0) {
				long width = UnitsOfMeasurement.twipToEMU(imageAttributes.getWidth());
				long height = UnitsOfMeasurement.twipToEMU(imageAttributes.getHeight());
				inline = imagePart.createImageInline(file.getAbsolutePath(), text, docPrId, cNvPrId, width, height, false);								
			} else if (imageAttributes.getWidth() > 0) {
				long width = imageAttributes.getWidth();
				inline = imagePart.createImageInline(file.getAbsolutePath(), text, docPrId, cNvPrId, width, false);
			} else {
				inline = imagePart.createImageInline(file.getAbsolutePath(), text, docPrId, cNvPrId, false);
			}			
		} else {
			inline = imagePart.createImageInline(file.getAbsolutePath(), text, docPrId, cNvPrId, false);
		}
		P paragraph = addInlineImageToParagraph(inline);
		mainDocumentPart.addObject(paragraph);
		return paragraph;
	}

	private P addInlineImageToParagraph(Inline inline) {
		P paragraph = factory.createP();
		R run = factory.createR();
		PPr ppr = factory.createPPr();
		paragraph.setPPr(ppr);
		paragraph.getContent().add(run);
		Drawing drawing = factory.createDrawing();
		run.getContent().add(drawing);
		drawing.getAnchorOrInline().add(inline);
		return paragraph;
	}

	@Override
	public void beginBlock(BlockType type, Attributes attributes) {
		Assert.isNotNull(type, "Block type cannot be NULL");
		Assert.isNotNull(attributes, "Attributes cannot be NULL");

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
		case PREFORMATTED:
			currentParagraph = factory.createP();
			applyStyle(currentParagraph, "Preformatted");
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
			TblStyle tblStyle = new TblStyle();
			tblStyle.setVal("TableGrid");
			tblpr.setTblStyle(tblStyle);
			currentTable.setTblPr(tblpr);

			// make the table fill the entire width of the page
			TblWidth tblwidth = wmlObjectFactory.createTblWidth();
			tblpr.setTblW(tblwidth);
			tblwidth.setType("pct");
			tblwidth.setW(BigInteger.valueOf(5000));
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
			Tc normalTableCell = createTableCell();
			TcPr tcprNormal = wmlObjectFactory.createTcPr();
			
			// apply grid span if applicable
			if (currentAttributes != null && currentAttributes instanceof TableCellAttributes) {
				TableCellAttributes tableCellAttributes = (TableCellAttributes) currentAttributes;
				if (tableCellAttributes.getColspan() != null) {
					applyGridSpan(tcprNormal, Integer.parseInt(tableCellAttributes.getColspan()));
				}
			}
			TblWidth cellNormalWidth = wmlObjectFactory.createTblWidth(); 
			cellNormalWidth.setType( "auto"); 
			cellNormalWidth.setW( BigInteger.valueOf( 0) ); 
			tcprNormal.setTcW(cellNormalWidth);
			normalTableCell.setTcPr(tcprNormal);

			String backgroundColor = "FFFFFF";
			SpanType spanType = SpanType.SPAN;

			if (hasCssStyle(currentAttributes)) {
				backgroundColor = getCssValueForKey(currentAttributes, "background-color");
				backgroundColor = backgroundColor.replace("#", "");
				String fontWeight = getCssValueForKey(currentAttributes, "font-weight");
				if (fontWeight.toLowerCase().equals("bold")) {
					spanType = SpanType.BOLD;
				}
			}

			CTShd shdNormal = createCellStyle("#FFFFFF");
			tcprNormal.setShd(shdNormal);
			beginSpan(spanType, currentAttributes);
			break;
		case TABLE_CELL_HEADER:
			Tc tc = createTableCell();
			TcPr tcpr2 = wmlObjectFactory.createTcPr();
			if (currentAttributes != null && currentAttributes instanceof TableCellAttributes) {
				TableCellAttributes tableCellAttributes = (TableCellAttributes) currentAttributes;
				if (tableCellAttributes.getColspan() != null) {
					applyGridSpan(tcpr2, Integer.parseInt(tableCellAttributes.getColspan()));
				}
			}
			TblWidth cellWidth = wmlObjectFactory.createTblWidth(); 
			cellWidth.setType( "auto"); 
			cellWidth.setW( BigInteger.valueOf( 0) ); 
			tcpr2.setTcW(cellWidth);
			tc.setTcPr(tcpr2);
			// force a white background
			CTShd shdHeader = createCellStyle("#ffffff");
			tcpr2.setShd(shdHeader);
			// we want the table header text to be in bold
			beginSpan(SpanType.BOLD, new Attributes());
			break;
		default:
			if (!(currentAttributes instanceof MathAttributes)) {
				beginSpan(SpanType.SPAN, new Attributes());
			}
			break;
		}
	}

	private boolean hasCssStyle(Attributes attributes) {
		return attributes.getCssStyle() != null && !attributes.getCssStyle().isEmpty();
	}

	/**
	 * Creates a table cell, normal or header.
	 * 
	 * @return the new table cell instance
	 */
	private Tc createTableCell() {
		Tc tc = factory.createTc();
		JAXBElement<org.docx4j.wml.Tc> tcWrapped = wmlObjectFactory.createTrTc(tc);
		currentTableRow.getContent().add(tcWrapped);
		currentParagraph = factory.createP();
		PPr ppr = factory.createPPr();
		ppr.setKeepNext(new BooleanDefaultTrue());
		ppr.setKeepLines(new BooleanDefaultTrue());
		currentParagraph.setPPr(ppr);
		tc.getContent().add(currentParagraph);
		return tc;
	}

	/**
	 * Prevent splitting across pages. If there's not enough space on page, the
	 * table or span is moved to the next page.
	 *
	 * @param ppr
	 * @return
	 */
	private PPr applyKeepLinesTogether(PPr ppr) {
		if (ppr == null) {
			ppr = factory.createPPr();
		}
		ppr.setKeepLines(new BooleanDefaultTrue());
		ppr.setKeepNext(new BooleanDefaultTrue());
		return ppr;
	}

	private CTShd createCellStyle(String fillColor) {
		CTShd shd = wmlObjectFactory.createCTShd();
		shd.setVal(org.docx4j.wml.STShd.CLEAR);
		shd.setColor("auto");
		shd.setFill(fillColor);
		return shd;
	}

	private void applyGridSpan(final TcPr tcpr, final int span) {
		if (span > 1) {
			GridSpan gridSpan = wmlObjectFactory.createTcPrInnerGridSpan();
			gridSpan.setVal(BigInteger.valueOf(span));
			tcpr.setGridSpan(gridSpan);
		}
	}

	private String getCssValueForKey(Attributes attributes, String key) {
		if (attributes != null) {
			String cssStyle = attributes.getCssStyle();
			Map<String, String> styleMap = getStylesFromCssString(cssStyle);
			return styleMap.get(key) != null ? styleMap.get(key) : "";
		} else
			return "";
	}

	private Map<String, String> getStylesFromCssString(String cssStyle) {
		Map<String, String> cssStyleMap = new HashMap<String, String>();
		if (cssStyle != null) {
			String[] styles = cssStyle.split(";");
			for (String style : styles) {
				style = style.replaceAll(" ", "").trim();
				String[] keyValuePair = style.split(":");
				cssStyleMap.put(keyValuePair[0], keyValuePair[1]);
			}
		}
		return cssStyleMap;
	}

	@Override
	public void beginDocument() {
		try {
			factory = new org.docx4j.wml.ObjectFactory();
			
			// start with a fresh document
			wordMLPackage = WordprocessingMLPackage.createPackage();
			mainDocumentPart = wordMLPackage.getMainDocumentPart();
			
			try {
				// load a theme
				Theme theme = (Theme) XmlUtils.unmarshal(OoxmlDocumentBuilder.class.getResourceAsStream("templates/default/theme1.xml"));
				ThemePart themePart = new ThemePart();
				themePart.setContents(theme);
				mainDocumentPart.addTargetPart(themePart);
				
				// load the character styles
				Styles styles = (Styles)XmlUtils.unmarshal(OoxmlDocumentBuilder.class.getResourceAsStream("templates/default/styles.xml"));
				StyleDefinitionsPart stylePart = new StyleDefinitionsPart();
				stylePart.setContents(styles);
				mainDocumentPart.addTargetPart(stylePart);
				
				// Load the numbering
				Numbering numbering = (Numbering)XmlUtils.unmarshal(OoxmlDocumentBuilder.class.getResourceAsStream("templates/default/numbering.xml"));
				NumberingDefinitionsPart numberingPart = new NumberingDefinitionsPart();
				numberingPart.setContents(numbering);
				mainDocumentPart.addTargetPart(numberingPart);

			} catch (JAXBException e) {
				e.printStackTrace();
			}
									
			if (title != null) {
				mainDocumentPart.addStyledParagraphOfText("Title", title);
			}
			if (subtitle != null) {
				mainDocumentPart.addStyledParagraphOfText("Subtitle", subtitle);
			}
			// create an initial set of attributes
			currentHeadingAttributes = new ExtendedHeadingAttributes();
			pageBreak();
		} catch (Docx4JException e) {
			throw new RuntimeException("Could not begin a new document", e);
		}
	}
		
	@Override
	public void beginHeading(int level, Attributes attributes) {
		beginStyle("Heading", level, attributes);
	}
	
	@Override
	public void beginStyle(String style, int level, Attributes attributes) {
		Assert.isNotNull(attributes, "Attributes cannot be NULL");
		
		if (!(attributes instanceof ExtendedHeadingAttributes)) {
			attributes = new ExtendedHeadingAttributes(attributes);
		}
		// see if the previous heading had the landscape/portrait page setting specified - if so we need to
		// handle this as the last thing before creating a new heading.
		if (currentHeadingAttributes.isLandscapeMode() != ((ExtendedHeadingAttributes)attributes).isLandscapeMode()) {
				SectPr sectPr = factory.createSectPr();
				PgSz pgSz = factory.createSectPrPgSz();
				if (currentHeadingAttributes.isLandscapeMode()) {
					pgSz.setW(BigInteger.valueOf(PAGE_HEIGHT));
					pgSz.setH(BigInteger.valueOf(PAGE_WIDTH));
					pgSz.setOrient(STPageOrientation.LANDSCAPE);
				} else {
					pgSz.setH(BigInteger.valueOf(PAGE_HEIGHT));
					pgSz.setW(BigInteger.valueOf(PAGE_WIDTH));
					pgSz.setOrient(STPageOrientation.PORTRAIT);
				}
				sectPr.setPgSz(pgSz);
				P p = factory.createP();
				p.setPPr(factory.createPPr());
				p.getPPr().setSectPr(sectPr);
				mainDocumentPart.addObject(p);
		}
		currentAttributes = attributes;
		currentHeadingAttributes = (ExtendedHeadingAttributes)attributes;
		// add a page break before
		if (currentHeadingAttributes.isPageBreakBefore()) {
			pageBreak();
		}
		currentStyle = style + level;
		styleExists();
	}

	private void styleExists() {
		Map<String, Style> knownStyles = StyleDefinitionsPart.getKnownStyles();
		for (String name: knownStyles.keySet()){

            String key =name.toString(); 
            if (key.contains("App"))
            	System.out.println(key);  
        }
		if ((!knownStyles.containsKey(currentStyle)) && (!currentStyle.contains("Appendix"))) {
			throw new IllegalArgumentException("Unknown style!");
		}
	}

	@Override
	public void beginSpan(SpanType type, Attributes attributes) {
		Assert.isNotNull(type, "Block type cannot be NULL");
		Assert.isNotNull(attributes, "Attributes cannot be NULL");
		
		// Close any existing span if we have one
		if (characters != null && characters.length() > 0) {
			endSpan();
		}

		currentSpanType = type;
		currentAttributes = attributes;
	}

	@Override
	public void characters(String text) {
		characters.append(text);
	}

	@Override
	public void charactersUnescaped(String literal) {
//		beginBlock(BlockType.PARAGRAPH, new Attributes());
		characters.append(literal);
//		endBlock();
	}

	private P chart(String chartRelId, String chartId) throws JAXBException {
		String ml = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
				+ "xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" w:rsidR=\"0084689C\" w:rsidRDefault=\"00D47CF0\">"
				+ "			<w:r>" 
				+ "				<w:rPr>" 
				+ "					<w:noProof />"
				+ "				</w:rPr>" 
				+ "				<w:drawing>"
				+ "					<wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\">"
				+ "						<wp:extent cx=\"5730000\" cy=\"3200000\" />"
				+ "						<wp:effectExtent l=\"0\" t=\"0\" r=\"25400\" b=\"25400\" />"
				+ "						<wp:docPr id=\"${docPr}\" name=\"Diagram " + chartId + "\" />"
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
	 * Creates a chart using the given chart description.
	 * 
	 * @param cd
	 *            a description of the chart
	 */
	public void chart(ChartDescription cd) {
		try {
			String prId = Integer.toString(++chartCounter);
			org.docx4j.openpackaging.parts.DrawingML.Chart chart = new org.docx4j.openpackaging.parts.DrawingML.Chart(
					new PartName("/word/charts/chart" + prId + ".xml"));
			CTChartSpace chartSpace = ChartFactory.createChartSpace(cd.getTitle(), cd.getYLabel(), cd.getXLabel(), cd);
			chart.setContentType(new ContentType(ContentTypes.DRAWINGML_CHART));
			chart.setJaxbElement(chartSpace);

			Relationship part = mainDocumentPart.addTargetPart(chart);
			mainDocumentPart.addObject(chart(part.getId(), prId));
			// add a caption to the chart
			caption(cd.getCaption(), CaptionType.Figure);
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	private byte[] convertImageFileToByteArray(File file) throws FileNotFoundException, IOException {
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
	 */
	private PPr applyStyle(P p, String style) {
		if (mainDocumentPart.getPropertyResolver().activateStyle(style)) {
			org.docx4j.wml.ObjectFactory factory = new org.docx4j.wml.ObjectFactory();
			org.docx4j.wml.PPr pPr = factory.createPPr();
			p.setPPr(pPr);
			org.docx4j.wml.PPrBase.PStyle pStyle = factory.createPPrBasePStyle();
			pPr.setPStyle(pStyle);
			pStyle.setVal(style);
			return pPr;
		}
		throw new RuntimeException("Missing style " + style);
	}

	/**
	 * Creates a span (run) with the specified text and adds it to the current
	 * paragraph.
	 * 
	 * @param text
	 *            the text of the span
	 * @return the created run instance
	 */
	private org.docx4j.wml.RPr createSpan(String text) {
		org.docx4j.wml.Text t = factory.createText();
		t.setSpace("preserve");
		t.setValue(text);
		org.docx4j.wml.R run = factory.createR();
		run.getContent().add(t);
		currentParagraph.getContent().add(run);
		org.docx4j.wml.RPr rpr = factory.createRPr();
		run.setRPr(rpr);
		return rpr;
	}

	private org.docx4j.wml.RPr createSpanWithFontSize(String text, String fontSize) {
		HpsMeasure m = wmlObjectFactory.createHpsMeasure();
		m.setVal(new BigInteger(fontSize));

		PPr ppr = factory.createPPr();

		if (currentBlockType == BlockType.TABLE_CELL_HEADER || currentBlockType == BlockType.TABLE_CELL_NORMAL) {
			ppr = applyKeepLinesTogether(ppr);
		}

		ParaRPr prpr = factory.createParaRPr();
		prpr.setSz(m);
		prpr.setSzCs(m);
		ppr.setRPr(prpr);

		currentParagraph.setPPr(ppr);

		org.docx4j.wml.Text t = factory.createText();
		t.setSpace("preserve");
		t.setValue(text);

		org.docx4j.wml.R run = factory.createR();
		run.getContent().add(t);

		currentParagraph.getContent().add(run);

		org.docx4j.wml.RPr rpr = factory.createRPr();
		rpr.setSz(m);
		rpr.setSzCs(m);
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
		case PARAGRAPH:
			applyStyle(currentParagraph, "BodyText");
			break;
		case DIV:
			if (currentAttributes instanceof MathAttributes) {
				latex(characters.toString(), currentAttributes, false);
				characters.setLength(0);
			}
			break;
		default:
			break;
		}
		endSpan();
	}

	@Override
	public void endDocument() {
		try {
			if (currentHeadingAttributes instanceof ExtendedHeadingAttributes) {
				SectPr sectPr = factory.createSectPr();
				PgSz pgSz = factory.createSectPrPgSz();
				if (((ExtendedHeadingAttributes) currentHeadingAttributes).isLandscapeMode()) {
					pgSz.setW(BigInteger.valueOf(PAGE_HEIGHT));
					pgSz.setH(BigInteger.valueOf(PAGE_WIDTH));
					pgSz.setOrient(STPageOrientation.LANDSCAPE);
				} else {
					pgSz.setH(BigInteger.valueOf(PAGE_HEIGHT));
					pgSz.setW(BigInteger.valueOf(PAGE_WIDTH));
					pgSz.setOrient(STPageOrientation.PORTRAIT);
				}
				pgSz.setCode(new BigInteger("1"));
				sectPr.setPgSz(pgSz);
				// set the margins
				PgMar pgMar = factory.createSectPrPgMar();
				pgMar.setTop(new BigInteger("1440"));
				pgMar.setBottom(new BigInteger("1440"));
				pgMar.setLeft(new BigInteger("1440"));
				pgMar.setRight(new BigInteger("1440"));
				sectPr.setPgMar(pgMar);
				Body body = mainDocumentPart.getContents().getBody();
				body.setSectPr(sectPr);
			}
			Assert.isNotNull(outputFile, "Document output file has not been specified");
			wordMLPackage.save(outputFile);
		} catch (Docx4JException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void endHeading() {
		endStyle();
	}
	
	@Override
	public void endStyle() {
		String ml = 
				"<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">" + 
				"  <w:pPr>" + 
				"    <w:pStyle w:val=\"${currentStyle}\" />" + 
				"  </w:pPr>" + 
				"  <w:bookmarkStart w:id=\"0\" w:name=\"${anchorName}\" />" + 
				"  <w:r>" + 
				"    <w:t>${characters}</w:t>" + 
				"  </w:r>" + 
				"  <w:bookmarkEnd w:id=\"0\"/>" +
				"</w:p>";
		java.util.HashMap<String, String> mappings = new java.util.HashMap<String, String>();
		mappings.put("currentStyle", currentStyle);
		mappings.put("characters", characters.toString());
		mappings.put("anchorName", currentAttributes.getId());
		try {
			P p = (P)org.docx4j.XmlUtils.unmarshallFromTemplate(ml, mappings);
			mainDocumentPart.addObject(p);
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		characters.setLength(0);
	}

	@Override
	public void endSpan() {
		// There are no characters in the buffer so there is nothing to create a span
		// for. We just need to reset the span type.
		if (characters.length() == 0) {
			currentSpanType = SpanType.SPAN;
			return;
		}
		RPr block;
		// Set font size
		String fontSize = getCssValueForKey(currentAttributes, "font-size");
		if (!fontSize.isEmpty()) {
			block = createSpanWithFontSize(characters.toString(), fontSize);
		}
		// Set text alignment
		String textHAlign = getCssValueForKey(currentAttributes, "text-align");
		if (currentParagraph.getPPr() != null && !textHAlign.isEmpty() && !textHAlign.toLowerCase().equals("left")) {
			Jc align = factory.createJc();
			if (textHAlign.toLowerCase().equals("right")) {
				align.setVal(JcEnumeration.RIGHT);
			} else {
				align.setVal(JcEnumeration.CENTER);
			}
			currentParagraph.getPPr().setJc(align);
		}
		switch (currentSpanType) {
		case BOLD:
			block = createSpan(characters.toString());
			block.setB(TRUE);
			break;
		case CITATION:
			block = createSpan(characters.toString());
			block.setI(TRUE);
			break;
		case CODE:
			// TODO: Implement support for code blocks
			break;
		case DELETED:
			block = createSpan(characters.toString());
			block.setStrike(TRUE);
			break;
		case EMPHASIS:
			block = createSpan(characters.toString());
			block.setB(TRUE);
			block.setI(TRUE);
			break;
		case INSERTED:
			break;
		case ITALIC:
			block = createSpan(characters.toString());
			block.setI(TRUE);
			break;
		case LINK:
			block = createHyperlink();
			break;
		case MONOSPACE:
			// TODO: Implement support for monospace
			break;
		case QUOTE:
			block = createSpan(characters.toString());
			block.setI(TRUE);
			break;
		case SPAN:
			if (currentAttributes instanceof MathAttributes) {
				convertLaTeX2OoxmlMath("$$" + characters.toString() + "$$", currentAttributes, true);
				currentAttributes = new Attributes();
			} else {
				block = createSpan(characters.toString());
			}
			break;
		case STRONG:
			block = createSpan(characters.toString());
			block.setB(TRUE);
			break;
		case SUBSCRIPT:
			CTVerticalAlignRun subScript = wmlObjectFactory.createCTVerticalAlignRun();
			block = createSpan(characters.toString());
			block.setVertAlign(subScript);
			subScript.setVal(org.docx4j.wml.STVerticalAlignRun.SUBSCRIPT);
			break;
		case SUPERSCRIPT:
			CTVerticalAlignRun superScript = wmlObjectFactory.createCTVerticalAlignRun();
			block = createSpan(characters.toString());
			block.setVertAlign(superScript);
			superScript.setVal(org.docx4j.wml.STVerticalAlignRun.SUPERSCRIPT);
			break;
		case UNDERLINED:
			U underline = new U();
			underline.setVal(UnderlineEnumeration.SINGLE);
			block = createSpan(characters.toString());
			block.setU(underline);
			break;
		default:
			block = createSpan(characters.toString());
			break;
		}
		characters.setLength(0);
		currentSpanType = SpanType.SPAN;
	}

	private RPr createHyperlink() {
		RPr block;
		Hyperlink link = factory.createPHyperlink();
		String href = ((LinkAttributes) currentAttributes).getHref();
		// these are links to sections or other type of elements within the document
		if (href.startsWith("section://")) {
			link.setAnchor(href.substring("section://".length()));
		} else if (href.startsWith("http")) {
			String id = "linkId" + (++linkCounter);
			link.setId(id);
			Relationship r = new Relationship();
			r.setId(id);
			r.setTargetMode("External");
			r.setTarget(href);
			r.setType("http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink");
			mainDocumentPart.getRelationshipsPart(true).addRelationship(r);
		} else if (href.startsWith("file://")) {
			String id = "linkId" + (++linkCounter);
			link.setId(id);
			Relationship r = new Relationship();
			r.setId(id);
			r.setTargetMode("External");
			r.setTarget(href.substring(7));
			r.setType("http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink");
			mainDocumentPart.getRelationshipsPart(true).addRelationship(r);
		}
		// add the link to the paragraph
		currentParagraph.getContent().add(link);
		// creat the text for the link
		org.docx4j.wml.Text t = factory.createText();
		t.setSpace("preserve");
		t.setValue(characters.toString());
		org.docx4j.wml.R run = factory.createR();
		run.getContent().add(t);
		// add the run to the link 
		link.getContent().add(run);
		org.docx4j.wml.RPr rpr = factory.createRPr();
		// set the style of the link
		RStyle style = factory.createRStyle();
		style.setVal("Hyperlink");
		rpr.setRStyle(style);
		run.setRPr(rpr);
		block = rpr;
		return block;
	}

	@Override
	public void entityReference(String entity) {
		// Convert the XML entity to Unicode.
		if (entity.startsWith("#")) {
			char c = (char) Integer.parseInt(entity.substring(1));
			characters.append(c);
		} else {
			characters.append("<unknown entity reference>");
		}
	}

	public File getOutputFile() {
		return outputFile;
	}

	/**
	 * Includes an image with the given attributes. If the attributes parameter is
	 * an instance of {@link ImageAttributes} the height will first be checked. If
	 * set to 0 it is assumed that the width of the image is specified in twips and
	 * the height will be automatically calculated.
	 */
	@Override
	public void image(Attributes attributes, String url) {
		image(attributes, url, CaptionType.Figure);
	}
	
	/**
	 * Includes an image with the given attributes. If the attributes parameter is
	 * an instance of {@link ImageAttributes} the height will first be checked. If
	 * set to 0 it is assumed that the width of the image is specified in twips and
	 * the height will be automatically calculated.
	 */
	@Override
	public void image(Attributes attributes, String url, CaptionType captionType) {
		byte[] bytes;
		try {
			File file = new File(url);
			bytes = convertImageFileToByteArray(file);
			String title = attributes.getTitle() == null ? attributes.getTitle() : file.getAbsolutePath();
			P paragraph = addImageToPackage(bytes, file, title, attributes);
			if (attributes.getTitle() != null) {
				// make sure the caption stick to the image
				paragraph.getPPr().setKeepNext(factory.createBooleanDefaultTrue());
				caption(attributes.getTitle(), captionType);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void imageLink(Attributes linkAttributes, Attributes imageAttributes, String href, String imageUrl) {
		// TODO: Implement support for image links
	}

	public File saveLaTeX2Png(String latex) throws IOException, TranscoderException {
		DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
		String svgNS = "http://www.w3.org/2000/svg";
		Document document = domImpl.createDocument(svgNS, "svg", null);
		SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);

		SVGGraphics2D g2 = new SVGGraphics2D(ctx, true);

		DefaultTeXFont.registerAlphabet(new CyrillicRegistration());
		DefaultTeXFont.registerAlphabet(new GreekRegistration());

		TeXFormula formula = new TeXFormula(latex);
		TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 50);
		icon.setInsets(new Insets(5, 5, 5, 5));
		g2.setSVGCanvasSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
		g2.setColor(Color.white);
		g2.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());

		JLabel jl = new JLabel();
		jl.setForeground(new Color(0, 0, 0));
		icon.paintIcon(jl, g2, 0, 0);

		// TODO: Use streams instead of temporary files
		File svgFile = Files.createTempFile("ooxml", ".svg").toFile();
		FileOutputStream svgs = new FileOutputStream(svgFile);
		Writer out = new OutputStreamWriter(svgs, "UTF-8");
		g2.stream(out, true);
		svgs.flush();
		svgs.close();

		// convert the SVG to PNG
		File pngFile = Files.createTempFile("ooxml", ".png").toFile();
		TranscoderInput ti = new TranscoderInput(new FileInputStream(svgFile));
		FileOutputStream os = new FileOutputStream(pngFile);
		TranscoderOutput to = new TranscoderOutput(os);
		PNGTranscoder pt = new PNGTranscoder();
		double millimetresPerPixel = (25.4f / 300); 
		pt.addTranscodingHint(ImageTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, new Float(millimetresPerPixel)); 
		pt.transcode(ti, to);
		os.flush();
		os.close();
		
		return pngFile;
	}    
    
	public void latex(String latex, Attributes attributes, boolean editable) {
		if (!latex.startsWith("$$")) {
			latex = "$$" + latex;
		}
		if (!latex.endsWith("$$")) {
			latex = latex + "$$";
		}
		currentAttributes = attributes;
		if (editable) {
			convertLaTeX2OoxmlMath(latex, currentAttributes, false);
		} else {
			convertLaTeX2Png(latex, currentAttributes);
		}
	}
	
	public void latex(String latex, Attributes attributes) {
		latex(latex, attributes, false);
	}

	/**
	 * Use to convert LaTeX to PNG for those cases where the OOXML math renderer is
	 * not capable, or the conversion creates some weird artifacts.
	 */
	private void convertLaTeX2Png(String latex, Attributes attributes) {
		try {
			// create an SVG file 
			File svg = saveLaTeX2Png(latex);
			// add the image to the document
			image(attributes, svg.toString());
		} catch (IOException | TranscoderException e) {
			e.printStackTrace();
		}		
	}

	@SuppressWarnings("rawtypes")
	private void convertLaTeX2OoxmlMath(String latex, Attributes attributes, boolean inline)
			throws TransformerFactoryConfigurationError {
		// create vanilla SnuggleEngine and new SnuggleSession
		SnuggleEngine engine = new SnuggleEngine();
		SnuggleSession session = engine.createSession();

		/// parse some LaTeX input */
		SnuggleInput input = new SnuggleInput(latex);
		try {
			session.parseInput(input);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// specify how we want the resulting XML */
		XMLStringOutputOptions options = new XMLStringOutputOptions();
		options.setSerializationMethod(SerializationMethod.XHTML);
		options.setIndenting(true);
		options.setEncoding("UTF-8");
		options.setAddingMathSourceAnnotations(true);
		options.setUsingNamedEntities(true);

		// transform the MathML to OOXML MathML
		StringWriter sw = new StringWriter();
		TransformerFactory tfactory = TransformerFactory.newInstance();
		StreamSource xsl = new StreamSource(OoxmlDocumentBuilder.class.getResourceAsStream("xslt/mml2omml.xsl"));
		String mathml = session.buildXMLString(options);
		Source xml = new StreamSource(new StringReader(mathml));
		StreamResult out = new StreamResult(sw);
		try {
			Transformer transformer = tfactory.newTransformer(xsl);
			transformer.transform(xml, out);
			// do not create a new paragraph when the math should be presented inline
			if (!inline) {
				currentParagraph = factory.createP();
			}
			javax.xml.bind.JAXBElement omathpara = (JAXBElement) XmlUtils.unmarshalString(sw.toString());
			currentParagraph.getContent().add(omathpara);
			// use the current paragraph and no caption when inline
			if (!inline) {
				mainDocumentPart.addObject(currentParagraph);
				if (attributes.getTitle() != null) {
					caption(attributes.getTitle(), CaptionType.Equation);
				}
			}
		} catch (TransformerException | JAXBException e) {
			throw new RuntimeException("Could not convert LateX Math to OOXML Math", e);
		}
	}

	@Override
	public void lineBreak() {
		// Make sure we have a paragraph
		if (currentParagraph == null) {
			currentParagraph = factory.createP();
		}
		// Spans should not be across lines, so we need to end it if there
		// is one active.
		endSpan();
		// Add a line break
		org.docx4j.wml.Br br = new org.docx4j.wml.Br();
		br.setType(STBrType.TEXT_WRAPPING);
		currentParagraph.getContent().add(br);
	}

	@Override
	public void link(Attributes attributes, String hrefOrHashName, String text) {
		// TODO: Implement support for hyperlinks
		System.out.println("OoxmlDocumentBuilder.link()"+hrefOrHashName);
	}

	/**
	 * Specifies the file to write the docuemnt to. Note that OOXML-documents are
	 * basically a set of files compressed into one using the zip-algorithm.
	 * 
	 * @param outputFile
	 *            the output file
	 */
	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}

	/**
	 * Sets the sub-title of the document.
	 * 
	 * @param text
	 *            the subtitle text
	 */
	public void setSubTitle(String text) {
		this.subtitle = text;
	}

	/**
	 * Sets the title of the document.
	 * 
	 * @param text
	 *            the title text
	 */
	public void setTitle(String text) {
		this.title = text;
	}

	private void pageBreak() {
		org.docx4j.wml.P p = new org.docx4j.wml.P();
		org.docx4j.wml.R r = new org.docx4j.wml.R();
		org.docx4j.wml.Br br = new org.docx4j.wml.Br();
		br.setType(STBrType.PAGE);
		r.getContent().add(br);
		p.getContent().add(r);
		mainDocumentPart.addObject(p);
	}

}

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

import org.docx4j.XmlUtils;
import org.docx4j.dml.chart.CTChartSpace;
import org.docx4j.dml.diagram.CTDataModel;
import org.docx4j.dml.diagram.CTElemPropSet;
import org.docx4j.dml.diagram.ObjectFactory;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.math.CTOMath;
import org.docx4j.math.CTOMathPara;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.openpackaging.parts.DrawingML.DiagramColorsPart;
import org.docx4j.openpackaging.parts.DrawingML.DiagramDataPart;
import org.docx4j.openpackaging.parts.DrawingML.DiagramLayoutPart;
import org.docx4j.openpackaging.parts.DrawingML.DiagramStylePart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.CTBookmark;
import org.docx4j.wml.CTMarkupRange;
import org.docx4j.wml.CTTextEffect;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.Style;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.builder.DocumentBuilderExtension;
import org.glox4j.openpackaging.packages.GloxPackage;

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

	public static P createSmartArt(String layoutRelId, String dataRelId,
			String colorsRelId, String styleRelId) throws Exception {

		String ml = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
				+ "<w:r>"
				+ "<w:rPr>"
				+ "<w:noProof/>"
				+ "<w:lang w:eastAsia=\"en-US\"/>"
				+ "</w:rPr>"
				+ "<w:drawing>"
				+ "<wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\" xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" >"
				+ "<wp:extent cx=\"5486400\" cy=\"3200400\"/>"
				+ "<wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\"/>"
				+ "<wp:docPr id=\"1\" name=\"Diagram 1\"/>"
				+ "<wp:cNvGraphicFramePr/>"
				+ "<a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">"
				+ "<a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/diagram\">"
				+ "<dgm:relIds r:dm=\"${dataRelId}\" r:lo=\"${layoutRelId}\" r:qs=\"${styleRelId}\" r:cs=\"${colorsRelId}\" xmlns:dgm=\"http://schemas.openxmlformats.org/drawingml/2006/diagram\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"/>"
				+ "</a:graphicData>"
				+ "</a:graphic>"
				+ "</wp:inline>"
				+ "</w:drawing>" + "</w:r>" + "</w:p>";

		java.util.HashMap<String, String> mappings = new java.util.HashMap<String, String>();

		mappings.put("layoutRelId", layoutRelId);
		mappings.put("dataRelId", dataRelId);
		mappings.put("colorsRelId", colorsRelId);
		mappings.put("styleRelId", styleRelId);

		return (P) org.docx4j.XmlUtils.unmarshallFromTemplate(ml, mappings);
	}

	private String characters;

	private BlockType currentBlockType;

	private P currentParagraph;

	private SpanType currentSpanType;
	private String currentStyle;

	private org.docx4j.wml.ObjectFactory factory;

	private int figureCount;

	private MainDocumentPart mainDocumentPart;

	private org.docx4j.math.ObjectFactory mathFactory = new org.docx4j.math.ObjectFactory();

	private File outputFile;

	private String subtitle;

	private String title;

	private org.docx4j.wml.BooleanDefaultTrue TRUE = new org.docx4j.wml.BooleanDefaultTrue();

	private WordprocessingMLPackage wordMLPackage;

	private int chartCounter = 0;

	@Override
	public void acronym(String text, String definition) {
		// TODO Auto-generated method stub
	}

	private void addCaptionToPackage(String caption) {

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
		org.docx4j.wml.Text text = wmlObjectFactory.createText();
		JAXBElement<org.docx4j.wml.Text> textWrapped = wmlObjectFactory
				.createRT(text);
		r.getContent().add(textWrapped);
		text.setValue("Figure ");
		text.setSpace("preserve");
		// Create object for r
		R r2 = wmlObjectFactory.createR();
		p.getContent().add(r2);
		// Create object for fldChar (wrapped in JAXBElement)
		FldChar fldchar = wmlObjectFactory.createFldChar();
		JAXBElement<org.docx4j.wml.FldChar> fldcharWrapped = wmlObjectFactory
				.createRFldChar(fldchar);
		r2.getContent().add(fldcharWrapped);
		fldchar.setFldCharType(org.docx4j.wml.STFldCharType.BEGIN);
		// Create object for r
		R r3 = wmlObjectFactory.createR();
		p.getContent().add(r3);
		// Create object for instrText (wrapped in JAXBElement)
		org.docx4j.wml.Text text2 = wmlObjectFactory.createText();
		JAXBElement<org.docx4j.wml.Text> textWrapped2 = wmlObjectFactory
				.createRInstrText(text2);
		r3.getContent().add(textWrapped2);
		text2.setValue(" ");
		text2.setSpace("preserve");
		// Create object for r
		R r4 = wmlObjectFactory.createR();
		p.getContent().add(r4);
		// Create object for instrText (wrapped in JAXBElement)
		org.docx4j.wml.Text text3 = wmlObjectFactory.createText();
		JAXBElement<org.docx4j.wml.Text> textWrapped3 = wmlObjectFactory
				.createRInstrText(text3);
		r4.getContent().add(textWrapped3);
		text3.setValue("SEQ Figure \\* ARABIC ");
		text3.setSpace("preserve");
		// Create object for r
		R r5 = wmlObjectFactory.createR();
		p.getContent().add(r5);
		// Create object for fldChar (wrapped in JAXBElement)
		FldChar fldchar2 = wmlObjectFactory.createFldChar();
		JAXBElement<org.docx4j.wml.FldChar> fldcharWrapped2 = wmlObjectFactory
				.createRFldChar(fldchar2);
		r5.getContent().add(fldcharWrapped2);
		fldchar2.setFldCharType(org.docx4j.wml.STFldCharType.SEPARATE);
		// Create object for r
		R r6 = wmlObjectFactory.createR();
		p.getContent().add(r6);
		// Create object for rPr
		RPr rpr = wmlObjectFactory.createRPr();
		r6.setRPr(rpr);
		// Create object for noProof
		BooleanDefaultTrue booleandefaulttrue = wmlObjectFactory
				.createBooleanDefaultTrue();
		rpr.setNoProof(booleandefaulttrue);
		// Create object for t (wrapped in JAXBElement)
		org.docx4j.wml.Text text4 = wmlObjectFactory.createText();
		JAXBElement<org.docx4j.wml.Text> textWrapped4 = wmlObjectFactory
				.createRT(text4);
		r6.getContent().add(textWrapped4);
		text4.setValue(Integer.toString(++figureCount));
		// Create object for r
		R r7 = wmlObjectFactory.createR();
		p.getContent().add(r7);
		// Create object for rPr
		RPr rpr2 = wmlObjectFactory.createRPr();
		r7.setRPr(rpr2);
		// Create object for noProof
		BooleanDefaultTrue booleandefaulttrue2 = wmlObjectFactory
				.createBooleanDefaultTrue();
		rpr2.setNoProof(booleandefaulttrue2);
		// Create object for fldChar (wrapped in JAXBElement)
		FldChar fldchar3 = wmlObjectFactory.createFldChar();
		JAXBElement<org.docx4j.wml.FldChar> fldcharWrapped3 = wmlObjectFactory
				.createRFldChar(fldchar3);
		r7.getContent().add(fldcharWrapped3);
		fldchar3.setFldCharType(org.docx4j.wml.STFldCharType.END);
		// Create object for r
		R r8 = wmlObjectFactory.createR();
		p.getContent().add(r8);
		// Create object for rPr
		RPr rpr3 = wmlObjectFactory.createRPr();
		r8.setRPr(rpr3);
		// Create object for noProof
		BooleanDefaultTrue booleandefaulttrue3 = wmlObjectFactory
				.createBooleanDefaultTrue();
		rpr3.setNoProof(booleandefaulttrue3);
		// Create object for t (wrapped in JAXBElement)
		org.docx4j.wml.Text text5 = wmlObjectFactory.createText();
		JAXBElement<org.docx4j.wml.Text> textWrapped5 = wmlObjectFactory
				.createRT(text5);
		r8.getContent().add(textWrapped5);
		text5.setValue(": " + caption);
		// Create object for bookmarkStart (wrapped in JAXBElement)
		CTBookmark bookmark = wmlObjectFactory.createCTBookmark();
		JAXBElement<org.docx4j.wml.CTBookmark> bookmarkWrapped = wmlObjectFactory
				.createPBookmarkStart(bookmark);
		p.getContent().add(bookmarkWrapped);
		bookmark.setName("_GoBack");
		bookmark.setId(BigInteger.valueOf(0));
		// Create object for bookmarkEnd (wrapped in JAXBElement)
		CTMarkupRange markuprange = wmlObjectFactory.createCTMarkupRange();
		JAXBElement<org.docx4j.wml.CTMarkupRange> markuprangeWrapped = wmlObjectFactory
				.createPBookmarkEnd(markuprange);
		p.getContent().add(markuprangeWrapped);
		markuprange.setId(BigInteger.valueOf(0));

		mainDocumentPart.addObject(p);

	}

	public void addExampleDrawingML(String inputfilepath) throws Exception {
		// Now add the SmartArt parts from the glox
		GloxPackage gloxPackage = GloxPackage.load(new java.io.File(
				inputfilepath));
		ObjectFactory factory = new ObjectFactory();

		// Layout part
		DiagramLayoutPart layout = new DiagramLayoutPart();
		layout.setJaxbElement(gloxPackage.getDiagramLayoutPart()
				.getJaxbElement());
		gloxPackage.getDiagramLayoutPart().getJaxbElement()
				.setUniqueId("mylayout");

		DiagramColorsPart colors = new DiagramColorsPart();
		colors.unmarshal("colorsDef-accent1_2.xml");
		// colors.CreateMinimalContent("mycolors");

		DiagramStylePart style = new DiagramStylePart();
		style.unmarshal("quickStyle-simple1.xml");
		// style.CreateMinimalContent("mystyle");

		// DiagramDataPart
		DiagramDataPart data = new DiagramDataPart();

		// Get the sample data from dgm:sampData
		CTDataModel sampleDataModel = gloxPackage.getDiagramLayoutPart()
				.getJaxbElement().getSampData().getDataModel();

		// If there is none, this sample won't work
		if (sampleDataModel == null || sampleDataModel.getPtLst() == null
				|| sampleDataModel.getPtLst().getPt().size() == 0) {
			System.out
					.println("No sample data in this glox, so can't create demo docx");
			return;
			// TODO: in this case, try generating our own sample data?
		}

		CTDataModel clonedDataModel = XmlUtils.deepCopy(sampleDataModel);
		data.setJaxbElement(clonedDataModel);

		CTElemPropSet prSet = factory.createCTElemPropSet();
		prSet.setLoTypeId("mylayout");
		prSet.setQsTypeId(style.getJaxbElement().getUniqueId());
		prSet.setCsTypeId(colors.getJaxbElement().getUniqueId());

		clonedDataModel.getPtLst().getPt().get(0).setPrSet(prSet);

		String layoutRelId = wordMLPackage.getMainDocumentPart()
				.addTargetPart(layout).getId();
		String dataRelId = wordMLPackage.getMainDocumentPart()
				.addTargetPart(data).getId();
		String colorsRelId = wordMLPackage.getMainDocumentPart()
				.addTargetPart(colors).getId();
		String styleRelId = wordMLPackage.getMainDocumentPart()
				.addTargetPart(style).getId();

		// Now use it in the docx
		mainDocumentPart.addObject(createSmartArt(layoutRelId, dataRelId,
				colorsRelId, styleRelId));
	}

	private void addImageToPackage(byte[] bytes) throws Exception {
		BinaryPartAbstractImage imagePart = BinaryPartAbstractImage
				.createImagePart(wordMLPackage, bytes);

		int docPrId = 1;
		int cNvPrId = 2;
		Inline inline = imagePart.createImageInline("Filename hint",
				"Alternative text", docPrId, cNvPrId, false);
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
		Map<String, Style> knownStyles = StyleDefinitionsPart.getKnownStyles();
		if (!knownStyles.containsKey(currentStyle)) {
			throw new IllegalArgumentException("Unknown style!");
		}
	}

	@Override
	public void beginSpan(SpanType type, Attributes attributes) {
		currentSpanType = type;
	};

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
	private P chart(String chartRelId, String docPr) throws JAXBException {

		String ml = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
				+ "xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" w:rsidR=\"0084689C\" w:rsidRDefault=\"00D47CF0\">"
				+ "			<w:r>\n"
				+ "				<w:rPr>\n"
				+ "					<w:noProof />\n"
				+ "				</w:rPr>\n"
				+ "				<w:drawing>\n"
				+ "					<wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\">\n"
				+ "						<wp:extent cx=\"5486400\" cy=\"3200400\" />\n"
				+ "						<wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\" />\n"
				+ "						<wp:docPr id=\"${docPr}\" name=\"Diagram 1\" />\n"
				+ "						<wp:cNvGraphicFramePr />\n"
				+ "						<a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">\n"
				+ "							<a:graphicData\n"
				+ "								uri=\"http://schemas.openxmlformats.org/drawingml/2006/chart\">\n"
				+ "								<c:chart\n"
				+ "									xmlns:c=\"http://schemas.openxmlformats.org/drawingml/2006/chart\"\n"
				+ "									xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"\n"
				+ "									r:id=\"${chartRelId}\" />\n"
				+ "							</a:graphicData>\n"
				+ "						</a:graphic>\n"
				+ "					</wp:inline>\n"
				+ "				</w:drawing>\n"
				+ "			</w:r>\n"
				+ "       </w:p>";

		java.util.HashMap<String, String> mappings = new java.util.HashMap<String, String>();

		mappings.put("docPr", docPr);
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
	public void chart(String title, String[] names, String ylabel, String xlabel,
			double[][] ySeries, double[][] xSeries) {
		try {
			String prId = Integer.toString(++chartCounter);
			Chart c = new Chart(new PartName("/word/charts/chart" + prId+".xml"));
			CTChartSpace chart = ChartFactory.createChartSpace(title,names, ylabel,
					xlabel, ySeries, xSeries);
			c.setContentType(new ContentType(ContentTypes.DRAWINGML_CHART));
			c.setJaxbElement(chart);
			Relationship part = mainDocumentPart.addTargetPart(c);
			mainDocumentPart.addObject(chart(part.getId(), prId));
			addCaptionToPackage(title);
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	private byte[] convertImageToByteArray(File file)
			throws FileNotFoundException, IOException {
		InputStream is = new FileInputStream(file);
		long length = file.length();
		byte[] bytes = new byte[(int) length];
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
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
	private P createParagraph(String text) {
		if (text != null) {
			org.docx4j.wml.Text t = factory.createText();
			t.setValue(text);
			org.docx4j.wml.R run = factory.createR();
			run.getContent().add(t);
			currentParagraph.getContent().add(run);
		}
		return currentParagraph;
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

	@Override
	public void endBlock() {
		if (currentBlockType.equals(BlockType.PARAGRAPH)) {
			mainDocumentPart.addObject(createParagraph(characters));
		} else {
			System.err.println("Unsupported block type!");
		}
	}

	@Override
	public void endDocument() {
		try {
			wordMLPackage.save(outputFile);
//			File tmp = new File(
//					System.getProperty("user.home")
//							+ "/git/no.marintek.mylyn.wikitext/no.marintek.mylyn.wikitext.ooxml.core/test/chart/");
//			unZipIt(outputFile.getAbsolutePath(), tmp.getAbsolutePath());
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
		characters="";
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
			bytes = convertImageToByteArray(new File(url));
			addImageToPackage(bytes);
			if (attributes.getTitle() != null) {
				addCaptionToPackage(attributes.getTitle());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void imageLink(Attributes linkAttributes,
			Attributes imageAttributes, String href, String imageUrl) {
		// TODO Auto-generated method stub
	}

	/**
	 * Converts <b>LaTeX</b> to <b>Office MathML</b> and inserts it into the
	 * document.
	 * 
	 * @param latex
	 *            the LaTeX code
	 */
	public void latex(String latex) {
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
		// Create a transform factory instance.
		StringWriter sw = new StringWriter();
		TransformerFactory tfactory = TransformerFactory.newInstance();
		StreamSource xsl = new StreamSource(
				OoxmlDocumentBuilder.class
						.getResourceAsStream("xslt/mml2omml.xsl"));
		Source xml = new StreamSource(new StringReader(
				session.buildXMLString(options)));
		StreamResult out = new StreamResult(sw);
		try {
			Transformer transformer = tfactory.newTransformer(xsl);
			transformer.transform(xml, out);
			currentParagraph = factory.createP();
			org.docx4j.math.CTOMathPara para = mathFactory.createCTOMathPara();
			JAXBContext jc = JAXBContext.newInstance("org.docx4j.math");
			CTOMath math = (CTOMath) XmlUtils.unmarshalString(sw.toString(),
					jc, CTOMath.class);
			para.getOMath().add(math);
			JAXBElement<CTOMathPara> wrapper = mathFactory
					.createOMathPara(para);
			currentParagraph.getContent().add(wrapper);
			mainDocumentPart.addObject(currentParagraph);

		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void lineBreak() {
		// TODO Auto-generated method stub
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
			ZipInputStream zis = new ZipInputStream(
					new FileInputStream(zipFile));
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {

				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator
						+ fileName);

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

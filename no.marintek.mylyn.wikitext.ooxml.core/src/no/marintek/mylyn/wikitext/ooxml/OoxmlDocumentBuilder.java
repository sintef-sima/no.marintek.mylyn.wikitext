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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

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
import org.docx4j.openpackaging.parts.DrawingML.Chart;
import org.docx4j.openpackaging.parts.DrawingML.DiagramColorsPart;
import org.docx4j.openpackaging.parts.DrawingML.DiagramDataPart;
import org.docx4j.openpackaging.parts.DrawingML.DiagramLayoutPart;
import org.docx4j.openpackaging.parts.DrawingML.DiagramStylePart;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.P;
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

	private MainDocumentPart mainDocumentPart;

	private File outputFile;

	private String subtitle;

	private String title;

	private org.docx4j.wml.BooleanDefaultTrue TRUE = new org.docx4j.wml.BooleanDefaultTrue();

	private WordprocessingMLPackage wordMLPackage;

	private org.docx4j.math.ObjectFactory mathFactory = new org.docx4j.math.ObjectFactory();;

	@Override
	public void acronym(String text, String definition) {
		// TODO Auto-generated method stub
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
	 * Creates a simple line chart using the given data set.
	 * <p>
	 * <b>Implement using {@link DocumentBuilderExtension} when this is read</b>
	 * </p>
	 * 
	 * @param data
	 *            the data set
	 */
	public void chart(double[] data) {
		try {
			Chart c = new Chart();
			CTChartSpace chart = ChartFactory.createChart(data);
			c.setContentType(new ContentType(ContentTypes.DRAWINGML_CHART));
			c.setJaxbElement(chart);
			String chartRelId = wordMLPackage.getMainDocumentPart()
					.addTargetPart(c).getId();
			mainDocumentPart.addObject(chart(chartRelId));
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Docx4JException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param chartRelId
	 * @return the chart paragraph
	 * @throws JAXBException
	 */
	private P chart(String chartRelId) throws JAXBException {

		String ml = "<w:p xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\">"
				+ "			<w:r>\n"
				+ "				<w:rPr>\n"
				+ "					<w:noProof />\n"
				+ "					<w:lang w:val=\"en-GB\" />\n"
				+ "				</w:rPr>\n"
				+ "				<w:drawing>\n"
				+ "					<wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\">\n"
				+ "						<wp:extent cx=\"5756910\" cy=\"3724915\" />\n"
				+ "						<wp:effectExtent l=\"0\" t=\"0\" r=\"34290\" b=\"34290\" />\n"
				+ "						<wp:docPr id=\"1\" name=\"Diagram 1\" />\n"
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

		mappings.put("chartRelId", chartRelId);
		return (P) org.docx4j.XmlUtils.unmarshallFromTemplate(ml, mappings);
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
			// TODO Auto-generated method stub
			break;
		case INSERTED:
			block.setB(TRUE);
			block.setI(TRUE);
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
			// TODO Auto-generated method stub
			U underline = new U();
			underline.setVal(UnderlineEnumeration.SINGLE);
			block.setU(underline);
			break;
		default:
			break;
		}

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
	 * Converts <b>LaTeX</b> to <b>Office MathML</b> and inserts it into the document. 
	 * 
	 * @param latex the LaTeX code
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
}

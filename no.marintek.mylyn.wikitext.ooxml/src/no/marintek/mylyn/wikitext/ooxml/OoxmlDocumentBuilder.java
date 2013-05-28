package no.marintek.mylyn.wikitext.ooxml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.docx4j.wml.P;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.Text;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder;

public class OoxmlDocumentBuilder extends DocumentBuilder {

	private WordprocessingMLPackage wordMLPackage;
	private MainDocumentPart mainDocumentPart;
	private BlockType currentBlockType;
	private SpanType currentSpanType;

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
	public void beginDocument() {
		try {
			factory = new org.docx4j.wml.ObjectFactory();
			wordMLPackage = WordprocessingMLPackage.createPackage();
			mainDocumentPart = wordMLPackage.getMainDocumentPart();
			mainDocumentPart.addStyledParagraphOfText("Title", "Mylyn Docs OOXML Document Builder");
			mainDocumentPart.addStyledParagraphOfText("Subtitle",
					"Experimental OOXML support for MARINTEK SIMA report generators.");
			addPageBreak();
			// beginHeading(1, new Attributes());
			// characters("Known/built-in paragraph styles");
			// endHeading();
			// Map<String, Style> knownStyles =
			// StyleDefinitionsPart.getKnownStyles();
			// ArrayList<String> arrayList = new
			// ArrayList<>(knownStyles.keySet());
			// Collections.sort(arrayList);
			// for (String key : arrayList) {
			// mainDocumentPart.addParagraphOfText(key);
			// }
			// addPageBreak();

		} catch (InvalidFormatException e) {
			e.printStackTrace();
		}
	}
	
	
	//private org.docx4j.wml.RPr 
	
	

	public void bold(String text) {
		org.docx4j.wml.RPr rpr = createTextBlock(text);
		rpr.setB(TRUE);
	}

	public org.docx4j.wml.RPr createTextBlock(String text) {
		org.docx4j.wml.Text t = factory.createText();
		t.setValue(text);
		org.docx4j.wml.R run = factory.createR();
		run.getContent().add(t);
		currentParagraph.getContent().add(run);
		org.docx4j.wml.RPr rpr = factory.createRPr();
		run.setRPr(rpr);
		return rpr;
	}

	public void italic(String text) {
		org.docx4j.wml.RPr rpr = createTextBlock(text);
		rpr.setI(TRUE);
	}

	@Override
	public void endDocument() {
		try {
			wordMLPackage.save(new java.io.File("/Users/torkild/Hello.docx"));
		} catch (Docx4JException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void beginBlock(BlockType type, Attributes attributes) {
		currentBlockType = type;
		if (currentBlockType.equals(BlockType.PARAGRAPH)) {
			currentParagraph = factory.createP();
		}
	}

	@Override
	public void endBlock() {
		if (currentBlockType.equals(BlockType.PARAGRAPH)) {
			mainDocumentPart.addObject(createParagraphOfText(characters));
		}
	}

	@Override
	public void beginSpan(SpanType type, Attributes attributes) {
		currentSpanType = type;

	}

	public org.docx4j.wml.P createParagraphOfText(String text) {
		if (text != null) {
			org.docx4j.wml.Text t = factory.createText();
			t.setValue(text);
			org.docx4j.wml.R run = factory.createR();
			run.getContent().add(t); // ContentAccessor
			currentParagraph.getContent().add(run); // ContentAccessor
		}
		return currentParagraph;
	}

	@Override
	public void endSpan() {
		RPr block = createTextBlock(characters);

		switch (currentSpanType) {
		case BOLD:
			block.setB(TRUE);
			break;
		case CITATION:
			break;
		case CODE:
			break;
		case DELETED:
			block.setStrike(TRUE);
			break;
		case EMPHASIS:
			break;
		case INSERTED:
			block.setB(TRUE);
			block.setI(TRUE);
			break;
		case ITALIC:
			block.setI(TRUE);
			break;
		case LINK:
			break;
		case MONOSPACE:
			break;
		case QUOTE:
			break;
		case SPAN:
			break;
		case STRONG:
			block.setB(TRUE);
			break;
		case SUBSCRIPT:
			break;
		case SUPERSCRIPT:
			break;
		case UNDERLINED:
			U underline = new U();
	        underline.setVal(UnderlineEnumeration.SINGLE);
	        block.setU(underline);
			break;
		default:
			break;
		}

	}

	private String currentStyle;
	private String characters;
	private org.docx4j.wml.ObjectFactory factory;
	private P currentParagraph;
	private org.docx4j.wml.BooleanDefaultTrue TRUE = new org.docx4j.wml.BooleanDefaultTrue();;
	private org.docx4j.wml.BooleanDefaultFalse FALSE = new org.docx4j.wml.BooleanDefaultFalse();;

	@Override
	public void beginHeading(int level, Attributes attributes) {
		currentStyle = "Heading" + level;
		Map<String, Style> knownStyles = StyleDefinitionsPart.getKnownStyles();
		if (!knownStyles.containsKey(currentStyle)) {
			throw new IllegalArgumentException("Unknown style!");
		}
	}

	@Override
	public void endHeading() {
		mainDocumentPart.addStyledParagraphOfText(currentStyle, characters);
	}

	@Override
	public void characters(String text) {
		characters = text;

	}

	@Override
	public void entityReference(String entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void image(Attributes attributes, String url) {
		// TODO Auto-generated method stub

	}

	@Override
	public void link(Attributes attributes, String hrefOrHashName, String text) {
		// TODO Auto-generated method stub

	}

	@Override
	public void imageLink(Attributes linkAttributes, Attributes imageAttributes, String href, String imageUrl) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acronym(String text, String definition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void lineBreak() {
		// TODO Auto-generated method stub

	}

	@Override
	public void charactersUnescaped(String literal) {
		// TODO Auto-generated method stub

	}

}

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
import java.util.Map;


import org.docx4j.wml.P;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Style;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.Text;
import org.docx4j.wml.U;
import org.docx4j.wml.R;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.UnderlineEnumeration;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.StyleDefinitionsPart;
import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder;

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

	private org.docx4j.wml.BooleanDefaultFalse FALSE = new org.docx4j.wml.BooleanDefaultFalse();

	private MainDocumentPart mainDocumentPart;

	private org.docx4j.wml.BooleanDefaultTrue TRUE = new org.docx4j.wml.BooleanDefaultTrue();

	private WordprocessingMLPackage wordMLPackage;

	@Override
	public void acronym(String text, String definition) {
		// TODO Auto-generated method stub
	}

	private void addImageToPackage(byte[] bytes) throws Exception {
		BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordMLPackage, bytes);

		int docPrId = 1;
		int cNvPrId = 2;
		Inline inline = imagePart.createImageInline("Filename hint", "Alternative text", docPrId, cNvPrId, false);
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
			mainDocumentPart.addStyledParagraphOfText("Title", "Mylyn Docs OOXML Document Builder");
			mainDocumentPart.addStyledParagraphOfText("Subtitle",
					"Experimental OOXML support for MARINTEK SIMA report generators.");
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
	}

	@Override
	public void characters(String text) {
		characters = text;
	}

	@Override
	public void charactersUnescaped(String literal) {
		characters = literal;
	};

	private byte[] convertImageToByteArray(File file) throws FileNotFoundException, IOException {
		InputStream is = new FileInputStream(file);
		long length = file.length();
		byte[] bytes = new byte[(int) length];
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}
		is.close();
		return bytes;
	};

	private org.docx4j.wml.P createParagraphOfText(String text) {
		if (text != null) {
			org.docx4j.wml.Text t = factory.createText();
			t.setValue(text);
			org.docx4j.wml.R run = factory.createR();
			run.getContent().add(t); // ContentAccessor
			currentParagraph.getContent().add(run); // ContentAccessor
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
			mainDocumentPart.addObject(createParagraphOfText(characters));
		}
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

	@Override
	public void image(Attributes attributes, String url) {
		System.out.println(url);
		byte[] bytes;
		try {
			bytes = convertImageToByteArray(new File(url));
			addImageToPackage(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void imageLink(Attributes linkAttributes, Attributes imageAttributes, String href, String imageUrl) {
		// TODO Auto-generated method stub
	}

	@Override
	public void lineBreak() {
		// TODO Auto-generated method stub
	}

	@Override
	public void link(Attributes attributes, String hrefOrHashName, String text) {
		// TODO Auto-generated method stub
	}

}

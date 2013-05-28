package no.marintek.mylyn.wikitext.ooxml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.management.Attribute;

import junit.framework.TestCase;

import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.SpanType;
import org.eclipse.mylyn.wikitext.core.parser.LinkAttributes;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder.Stylesheet;
import org.eclipse.mylyn.wikitext.textile.core.TextileLanguage;
import org.junit.Test;

public class OoxmlDocumentBuilderTest extends TestCase{
	
	private static final Attributes ATTRIBUTES = new Attributes();
	private String lorem = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
	private MarkupParser parser;

	private StringWriter out;

	private OoxmlDocumentBuilder builder;
	
	
//	@Test
//	public void testBasicSetup(){
//		// default link rel
//		out = new StringWriter();
//		builder = new OoxmlDocumentBuilder();
//		builder.beginDocument();
//		builder.beginBlock(BlockType.PARAGRAPH, new Attributes());
//		builder.endBlock();
//		builder.endDocument();		
//	}
	
	@Test
	public void testHeading_1(){
		builder = new OoxmlDocumentBuilder();
		builder.beginDocument();
		for (int h=1;h<10;h++){
			builder.beginHeading(h,ATTRIBUTES);
			builder.characters("Heading "+h);
			builder.endHeading();
			builder.beginBlock(BlockType.PARAGRAPH, ATTRIBUTES);
			builder.characters(lorem);
			builder.endBlock();
			
			SpanType[] values = SpanType.values();
			for (SpanType spanType : values) {
				builder.beginSpan(spanType, ATTRIBUTES);
				builder.characters(spanType.name()+" ");
				builder.endSpan();
			}
		}
		builder.endDocument();				
	}
}

package no.marintek.mylyn.wikitext.ooxml;

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.SpanType;
import org.junit.Test;

/**
 * This is not a proper test case. It is only used to generate a OOXML (for
 * Word) document using the new API.
 *
 * @author torkild
 *
 */
public class OoxmlDocumentBuilderTest extends TestCase {

	private static final Attributes ATTRIBUTES = new Attributes();
	private final String lorem = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

	private OoxmlDocumentBuilder builder;

	@Test
	public void testHeading_1() {
		builder = new OoxmlDocumentBuilder();
		builder.beginDocument();

		//
		File file = new File("cube.png");
		builder.image(ATTRIBUTES, file.getAbsolutePath());

		for (int h = 1; h < 10; h++) {
			builder.beginHeading(h, ATTRIBUTES);
			builder.characters("Heading " + h);
			builder.endHeading();
			builder.beginBlock(BlockType.PARAGRAPH, ATTRIBUTES);
			builder.characters(lorem);
			builder.endBlock();

			SpanType[] values = SpanType.values();
			for (SpanType spanType : values) {
				builder.beginSpan(spanType, ATTRIBUTES);
				builder.characters(spanType.name() + " ");
				builder.endSpan();
			}
		}
		builder.endDocument();
	}
}

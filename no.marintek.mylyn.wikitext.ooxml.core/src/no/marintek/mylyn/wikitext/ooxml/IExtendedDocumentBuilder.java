package no.marintek.mylyn.wikitext.ooxml;

import org.eclipse.mylyn.wikitext.parser.Attributes;

import no.marintek.mylyn.wikitext.ooxml.OoxmlDocumentBuilder.CaptionType;

public interface IExtendedDocumentBuilder {
	
	public void caption(String text, CaptionType captionType);

	/**
	 * Inserts a chart at the current location
	 * 
	 * @param cd
	 *            the chart description
	 */
	public void chart(ChartDescription cd);
	
	/**
	 * Inserts an image with a caption at the current location
	 * 
	 * @param attributes
	 *            the image attributes
	 * @param url
	 *            image location
	 * @param captionType
	 *            the type of caption to use with the image
	 */
	public void image(Attributes attributes, String url, CaptionType captionType);

	/**
	 * Converts the given LaTeX math for inclusion into the OOXML Document. The
	 * resulting object will be an image.
	 * 
	 * @param latex
	 *            the LateX equation
	 * @param attributes
	 *            rendering attributes
	 */
	public void latex(String latex, Attributes attributes);

	/**
	 * Converts the given LaTeX math for inclusion into the OOXML Document. If the
	 * <i>editable</i> parameter is set to <code>true</code>, the resulting object
	 * will be of OOXML Math type. When i.e. opening this in MS Word, it will be
	 * rendered in a way so that it can be changed. If <i>editable</i> is set to
	 * <code>false</code>, the resulting object will be an image.
	 * 
	 * @param latex
	 *            the LateX equation
	 * @param attributes
	 *            rendering attributes
	 * @param editable
	 *            whether or not the resulting object should be editable
	 */
	public void latex(String latex, Attributes attributes, boolean editable);
	
	/**
	 * Begin a heading of the specified level (usually 1-6). Builder implementations may do a best-effort application of
	 * the provided attributes. Each call to this method must be matched by a corresponding call to
	 * {@link #endStyle()}.
	 *
	 * @param level
	 *            the level of the style, usually 1-6
	 * @param attributes
	 *            the attributes to apply to the style
	 * @see #endStyle()
	 */
	public abstract void beginStyle(TemplateStyle style, int level, Attributes attributes);

	/**
	 * End a span that was {@link #beginStyle(String, int, Attributes) started}.
	 *
	 * @see #beginStyle(int, Attributes)
	 */
	public abstract void endStyle();

}

/*******************************************************************************
 * Copyright (c) 2007, 2009 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild U. Resheim - initial API and implementation
 *******************************************************************************/
package no.marintek.mylyn.internal.wikitext.confluence.core.block;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.UUID;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JLabel;

import no.marintek.mylyn.wikitext.confluence.core.ExtendedConfluenceLanguage;

import org.eclipse.mylyn.internal.wikitext.confluence.core.block.AbstractConfluenceDelimitedBlock;
import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType;
import org.eclipse.mylyn.wikitext.core.parser.ImageAttributes;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

/**
 * @author Torkild U. Resheim
 */
public class LaTexBlock extends AbstractConfluenceDelimitedBlock {

	private static final double INCH_2_CM = 2.54;

	private String title;

	public LaTexBlock(int dpi) {
		super("latex"); //$NON-NLS-1$
		imageWritten = false;
		this.dpi = dpi;
	}

	@Override
	protected void beginBlock() {
		if (title != null) {
			Attributes attributes = new Attributes();
			attributes.setTitle(title);
			builder.beginBlock(BlockType.PANEL, attributes);
		}
		Attributes attributes = new Attributes();
		builder.beginBlock(BlockType.DIV, attributes);
	}

	private String cleanLatex(String string) {
		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader(new StringReader(string));
		String line;
		try {
			while ((line = br.readLine()) != null) {
				// JLaTeXMath don't like these.
				if (line.startsWith("\\begin{equation}") || line.startsWith("\\end{equation}")) { //$NON-NLS-1$ //$NON-NLS-2$
					continue;
				}
				sb.append(line.trim());
				sb.append("\n"); //$NON-NLS-1$
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	StringBuilder cleanedLatex = new StringBuilder();

	@Override
	protected void handleBlockContent(String content) {
		cleanedLatex.append(cleanLatex(content));

	}

	private void writeImage(String content) {
		if (imageWritten) {
			return;
		}
		File root = ((ExtendedConfluenceLanguage) getMarkupLanguage()).getResourcesPath();
		if (root == null) {
			throw new IllegalArgumentException("No folder for LaTeX images has been specified"); //$NON-NLS-1$
		}
		if (!root.exists()) {
			if (!root.mkdirs()) {
				throw new IllegalArgumentException("Could not create folder for LaTeX images"); //$NON-NLS-1$
			}
		}
		TeXFormula formula = new TeXFormula(content);

		// 16pt font (?) @ 72 DPI > 300dpi 
		TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, (float) (16.0 * (dpi / 72)));
		icon.setInsets(new Insets(5, 5, 5, 5));
		BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		g2.setColor(Color.white);
		g2.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());
		JLabel jl = new JLabel();
		jl.setForeground(new Color(0, 0, 0));
		icon.paintIcon(jl, g2, 0, 0);
		String id = UUID.randomUUID().toString();
		File file = new File(root, id + ".png"); //$NON-NLS-1$
		ImageAttributes img = new ImageAttributes();
		//img.setWidthPercentage(true);
		//img.setWidth(50);
		builder.image(img, file.getName());
		try {
			saveGridImage(image, file);
			//ImageIO.write(image, "png", file.getAbsoluteFile()); //$NON-NLS-1$			
			imageWritten = true;
		} catch (IOException ex) {
		}
	}

	private void saveGridImage(BufferedImage gridImage, File output) throws IOException {

		final String formatName = "png"; //$NON-NLS-1$

		for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext();) {
			ImageWriter writer = iw.next();
			ImageWriteParam writeParam = writer.getDefaultWriteParam();
			ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
			IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
			if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
				continue;
			}

			setDPIMetadata(metadata);

			final ImageOutputStream stream = ImageIO.createImageOutputStream(output);
			try {
				writer.setOutput(stream);
				writer.write(metadata, new IIOImage(gridImage, null, metadata), writeParam);
			} finally {
				stream.close();
			}
			break;
		}
	}

	private int dpi = 300;

	private void setDPIMetadata(IIOMetadata metadata) throws IIOInvalidTreeException {

		// for PNG, it's dots per millimeter
		double dotsPerMilli = dpi / 10 / INCH_2_CM;

		IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize"); //$NON-NLS-1$
		horiz.setAttribute("value", Double.toString(dotsPerMilli)); //$NON-NLS-1$

		IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize"); //$NON-NLS-1$
		vert.setAttribute("value", Double.toString(dotsPerMilli)); //$NON-NLS-1$

		IIOMetadataNode dim = new IIOMetadataNode("Dimension"); //$NON-NLS-1$
		dim.appendChild(horiz);
		dim.appendChild(vert);

		IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0"); //$NON-NLS-1$
		root.appendChild(dim);

		metadata.mergeTree("javax_imageio_1.0", root); //$NON-NLS-1$
	}

	private boolean imageWritten;

	@Override
	protected void endBlock() {
		writeImage(cleanedLatex.toString());
		if (title != null) {
			builder.endBlock(); // panel	
		}
		builder.endBlock(); // latex
	}

	@Override
	protected void resetState() {
		super.resetState();
		title = null;
		cleanedLatex.setLength(0);
	}

	@Override
	protected void setOption(String key, String value) {
//		if (key.equals("title")) { //$NON-NLS-1$
//			title = value;
//		}
	}

}

/*******************************************************************************
 * Copyright (c) 2018 Sintef Ocean
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

import org.eclipse.mylyn.wikitext.parser.DocumentBuilder.BlockType;
import org.eclipse.mylyn.wikitext.parser.markup.Block;

/**
 * Block that matches LaTeX math. The formula must start with {@code $$} and also end with {@code $$}.
 *
 * @author Torkild U. Resheim, Itema AS
 */
public class MathBlock extends Block {

	private int linecount;

	@Override
	public boolean canStart(String line, int lineOffset) {
		if (line.startsWith("$$") && linecount == 0) {
			return true;
		}
		return false;
	}

	@Override
	protected int processLineContent(String line, int offset) {
		if (linecount == 0) {
			builder.beginBlock(BlockType.DIV, new MathAttributes());
		}
		// determine the end of the math block
		else if (line.endsWith("$$")) {
			builder.characters(line);
			setClosed(true);
			return -1;
		}
		++linecount;
		if (linecount > 1 || line.trim().length() > 0) {
			builder.characters(line);
			builder.characters("\n");
		}
		return -1;
	}

	@Override
	public void setClosed(boolean closed) {
		if (closed && !isClosed()) {
			builder.endBlock();
			linecount = 0;
		}
		super.setClosed(closed);
	}

}

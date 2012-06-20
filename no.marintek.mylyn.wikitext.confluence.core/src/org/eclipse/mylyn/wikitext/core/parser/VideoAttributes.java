/*******************************************************************************
 * Copyright (c) 2012 MARINTEK and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild U. Resheim - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.wikitext.core.parser;

import org.eclipse.mylyn.wikitext.core.parser.Attributes;

public class VideoAttributes extends Attributes {

	private int width = -1;

	private boolean widthPercentage = false;

	private int height = -1;

	private boolean heightPercentage = false;

	private boolean controls;

	private boolean autoplay;

	public boolean isControls() {
		return controls;
	}

	public void setControls(boolean controls) {
		this.controls = controls;
	}

	public boolean isAutoplay() {
		return autoplay;
	}

	public void setAutoplay(boolean autoplay) {
		this.autoplay = autoplay;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * Sets whether the value passed into {@link #setWidth(int)} should be treated as a percentage or as a number of
	 * pixels. The default is false (pixels) to maintain backwards compatibility.
	 * 
	 * @param widthPercentage
	 *            pass true if the width is specified as a percentage, false if the width is specified in pixels
	 */
	public void setWidthPercentage(boolean widthPercentage) {
		this.widthPercentage = widthPercentage;
	}

	/**
	 * Tells you whether the value returned from {@link #getWidth()} is the width as a percentage or in pixels.
	 * 
	 * @return true if the width should be treated as a percentage, false if the width should be treated as an amount of
	 *         pixels
	 */
	public boolean isWidthPercentage() {
		return widthPercentage;
	}

	/**
	 * Sets whether the value passed into {@link #setHeight(int)} should be treated as a percentage or as a number of
	 * pixels. The default is false (pixels) to maintain backwards compatibility.
	 * 
	 * @param heightPercentage
	 *            pass true if the height is specified as a percentage, false if the height is specified in pixels
	 */
	public void setHeightPercentage(boolean heightPercentage) {
		this.heightPercentage = heightPercentage;
	}

	/**
	 * Tells you whether the value returned from {@link #getHeight()} is the height as a percentage or in pixels.
	 * 
	 * @return true if the height should be treated as a percentage, false if the height should be treated as an amount
	 *         of pixels
	 */
	public boolean isHeightPercentage() {
		return heightPercentage;
	}
}

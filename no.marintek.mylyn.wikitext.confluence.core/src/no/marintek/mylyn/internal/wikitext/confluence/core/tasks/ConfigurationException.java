/*******************************************************************************
 * Copyright (c) 2010 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *     MARINTEK - Added Ant task for Eclipse Help generation - bug 335280
 *******************************************************************************/

package no.marintek.mylyn.internal.wikitext.confluence.core.tasks;

import org.apache.tools.ant.BuildException;

public class ConfigurationException extends BuildException {

	private static final long serialVersionUID = 1L;

	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigurationException(String s) {
		super(s);
	}

}

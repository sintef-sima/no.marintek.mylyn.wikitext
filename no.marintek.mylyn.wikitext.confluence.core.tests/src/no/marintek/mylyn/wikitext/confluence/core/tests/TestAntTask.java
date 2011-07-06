/*******************************************************************************
 * Copyright (c) 2011 Torkild U. Resheim.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Torkild U. Resheim - initial API and implementation
 *******************************************************************************/
package no.marintek.mylyn.wikitext.confluence.core.tests;

import org.apache.tools.ant.BuildFileTest;

/**
 * Tests for the <b>markup-to-epub</b> ANT task.
 * 
 * @author Torkild U. Resheim
 */
public class TestAntTask extends BuildFileTest {


	public TestAntTask(String s) {
		super(s);
		classLoader = getClass().getClassLoader();
	}

	
 static ClassLoader classLoader;
		
	@Override
	public void setUp() {
		configureProject("ant-test.xml");
		project.setCoreLoader(classLoader);
	}

}

/*-
 *******************************************************************************
 * Copyright (c) 2015 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Torkild U. Resheim - initial API and implementation
 *******************************************************************************/
package org.dawnsci.dde.templates;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ 
	ChannelColourSchemeTemplateTest.class, 
	ColourSchemeTemplateTest.class,
	FittingFunctionTemplateTest.class, 
	LoaderTemplateTest.class,
	OperationTemplateTest.class,
	PlottingActionTemplateTest.class, 
	ToolPageActionTemplateTest.class, 
	ToolPageTemplateTest.class })
public class AllTests {
	// nothing to see here
}

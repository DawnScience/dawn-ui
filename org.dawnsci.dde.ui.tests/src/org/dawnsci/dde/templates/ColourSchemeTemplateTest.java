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

import static org.eclipse.swtbot.swt.finder.waits.Conditions.shellCloses;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class ColourSchemeTemplateTest extends AbstractTemplateTestBase {

	private static final String EXTENSION_POINT = "org.dawnsci.plotting.histogram.colourScheme";
	
	/**
	 * This test executes the wizard through the user interface. It is important
	 * that this is the first test as the result will be used for subsequent
	 * tests. Screenshots will be taken for each page in the wizard.
	 * 
	 * @throws CoreException
	 */
	@Test
	public void testWizard() throws CoreException {
		// execute the wizard through the user interface
		bot.menu("File").menu("New").menu("DAWN Plug-in Project").click();
		SWTBotShell shell = bot.shell("New DAWN Plug-in Project");
		shell.activate();

		// fill in first page
		bot.textWithLabel("&Project name:").setText(getProjectName());
		bot.textWithLabel("Identifier:").setText(getProjectName());
		bot.textWithLabel("Version:").setText("1.0.0");
		bot.textWithLabel("Name:").setText("My DAWN Tool Page");
		bot.textWithLabel("Institute:").setText("Diamond Light Source");
		bot.comboBoxWithLabel("Extension point identifier:").setSelection(EXTENSION_POINT);
		takeScreenshot(shell.widget, EXTENSION_POINT);
		bot.button("Next >").click();
		
		// fill in second page
		bot.textWithLabel("Identifier").setText(getProjectName());
		bot.textWithLabel("Name").setText("Test Colour Scheme");
		bot.comboBoxWithLabel("Red transfer function").setSelection("Linear (y=x)");
		bot.comboBoxWithLabel("Green transfer function").setSelection("Zero (y=0)");
		bot.comboBoxWithLabel("Blue transfer function").setSelection("Full (y=1)");
		bot.comboBoxWithLabel("Alpha transfer function").setSelection("Low Jet");
		takeScreenshot(shell.widget, EXTENSION_POINT);
		bot.button("Finish").click();
		
		// wait until the wizard is done
		bot.waitUntil(shellCloses(shell));
	}

	@Override
	protected String getProjectName() {
		return "org.dawnsci.dde.test.colourScheme";
	}
		
	@Override
	protected String getPluginContents(){
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<?eclipse version=\"3.4\"?>\n" + 
				"<plugin>\n" + 
				"   <extension\n" + 
				"         point=\"org.dawnsci.plotting.histogram.colourScheme\">\n" + 
				"      <colour_scheme\n" + 
				"            alpha_inverted=\"false\"\n" + 
				"            alpha_transfer_function=\"org.dawnsci.plotting.histogram.lowJet\"\n" + 
				"            blue_inverted=\"false\"\n" + 
				"            blue_transfer_function=\"org.dawnsci.plotting.histogram.full\"\n" + 
				"            green_inverted=\"false\"\n" + 
				"            green_transfer_function=\"org.dawnsci.plotting.histogram.zero\"\n" + 
				"            id=\"org.dawnsci.dde.test.colourScheme\"\n" + 
				"            name=\"Test Colour Scheme\"\n" + 
				"            red_inverted=\"false\"\n" + 
				"            red_transfer_function=\"org.dawnsci.plotting.histogram.linear\">\n" + 
				"      </colour_scheme>\n" + 
				"   </extension>\n" + 
				"</plugin>\n";
	}
}

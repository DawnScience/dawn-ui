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
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class ChannelColourSchemeTemplateTest extends AbstractTemplateTestBase {

	private static final String EXTENSION_POINT = "org.dawnsci.plotting.histogram.channelColourScheme";

	@BeforeClass
	public static void beforeClass() {
		bot = new SWTWorkbenchBot();
		bot.viewByTitle("Welcome").close();
	}
		
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
		bot.textWithLabel("Name").setText("Channel Colour Scheme");
		bot.textWithLabel("Java package name").setText("org.dawnsci.dde.test");
		bot.textWithLabel("Java class name").setText("ChannelColourScheme");
		takeScreenshot(shell.widget, EXTENSION_POINT);
		bot.button("Finish").click();
		
		// wait until the wizard is done
		bot.waitUntil(shellCloses(shell));
	}

	@Override
	protected String getProjectName() {
		return "org.dawnsci.dde.test.toolpage";
	}
		
	@Override
	protected String getPluginContents(){
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<?eclipse version=\"3.4\"?>\n" + 
				"<plugin>\n" + 
				"   <extension\n" + 
				"         point=\"org.dawnsci.plotting.histogram.channelColourScheme\">\n" + 
				"      <transfer_function\n" + 
				"            array_provider_class=\"org.dawnsci.dde.test.ChannelColourScheme\"\n" + 
				"            id=\"org.dawnsci.dde.test.toolpage\"\n" + 
				"            name=\"Channel Colour Scheme\">\n" + 
				"      </transfer_function>\n" + 
				"   </extension>\n" + 
				"</plugin>";
	
	}
}

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
public class LoaderTemplateTest extends AbstractTemplateTestBase {

	private static final String EXTENSION_POINT = "uk.ac.diamond.scisoft.analysis.io.loader";

	/**
	 * This test executes the wizard through the user interface. It is important
	 * that this is the first test as the result will be used for subsequent
	 * tests. Screenshots will be taken for each page in the wizard.
	 * 
	 * @throws CoreException
	 */
	@Test
	public void testWizard() throws CoreException {
		// the PDE perspective must be active in order to locate the wizard
		bot.perspectiveById("org.eclipse.pde.ui.PDEPerspective").activate();
		// execute the wizard through the user interface
		bot.menu("File").menu("New").menu("DAWN Plug-in Project").click();
		SWTBotShell shell = bot.shell("New DAWN Plug-in Project");
		shell.activate();

		// fill in first page
		bot.textWithLabel("&Project name:").setText(getProjectName());
		bot.textWithLabel("Identifier:").setText(getProjectName());
		bot.textWithLabel("Version:").setText("1.0.0");
		bot.textWithLabel("Name:").setText("My DAWN Loader");
		bot.textWithLabel("Institute:").setText(DIAMOND_LIGHT_SOURCE);
		bot.comboBoxWithLabel("Extension point identifier:").setSelection(EXTENSION_POINT);
		takeScreenshot(shell.widget, EXTENSION_POINT);
		bot.button("Next >").click();

		// fill in second page
		bot.textWithLabel("Java package name").setText("org.dawnsci.dde.test");
		bot.textWithLabel("Java class name").setText("Loader");
		bot.textWithLabel("File extensions").setText(".txt");
		takeScreenshot(shell.widget, EXTENSION_POINT);
		bot.button("Finish").click();

		// wait until the wizard is done
		bot.waitUntil(shellCloses(shell));
	}

	@Override
	protected String getProjectName() {
		return "org.dawnsci.dde.test.loader";
	}
		
	@Override
	protected String getPluginContents(){
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<?eclipse version=\"3.4\"?>\n" + 
				"<plugin>\n" + 
				"   <extension\n" + 
				"         point=\"uk.ac.diamond.scisoft.analysis.io.loader\">\n" + 
				"      <loader\n" + 
				"            class=\"org.dawnsci.dde.test.Loader\"\n" + 
				"            file_extension=\".txt\"\n" + 
				"            high_priority=\"true\">\n" + 
				"      </loader>\n" + 
				"   </extension>\n" + 
				"</plugin>\n";
	}

}

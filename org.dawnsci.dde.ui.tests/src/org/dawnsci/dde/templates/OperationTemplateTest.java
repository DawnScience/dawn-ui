package org.dawnsci.dde.templates;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class OperationTemplateTest extends AbstractTemplateTest {

	static final String PROJECT_NAME = "my.dawn.operation";
	private static final String EXTENSION_POINT = "org.eclipse.dawnsci.analysis.api.operation";

	@BeforeClass
	public static void beforeClass() {
		bot = new SWTWorkbenchBot();
		bot.viewByTitle("Welcome").close();
	}

	@AfterClass
	public static void sleep() {
		bot.sleep(2000);
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
		bot.textWithLabel("&Project name:").setText(PROJECT_NAME);
		bot.textWithLabel("Identifier:").setText(PROJECT_NAME);
		bot.textWithLabel("Version:").setText("1.0.0");
		bot.textWithLabel("Name:").setText("My DAWN Operation");
		bot.textWithLabel("Institute:").setText("Diamond Light Source");
		bot.comboBoxWithLabel("Extension point identifier:").setSelection(EXTENSION_POINT);
		takeScreenshot(shell.widget, EXTENSION_POINT);
		bot.button("Next >").click();
		takeScreenshot(shell.widget, EXTENSION_POINT);
		bot.button("Finish").click();
	}

	public void testDependencies() {
		// TODO: Implement test
	}

	public void testManifest() {
		// TODO: Implement test
	}

	public void testPlugin() {
		// TODO: Implement test
	}

	public void testBuildProperties() {
		// TODO: Implement test
	}

	public void testSourceFile() {
		// TODO: Implement test
	}

	@Override
	protected String getProjectName() {
		return PROJECT_NAME;
	}

}

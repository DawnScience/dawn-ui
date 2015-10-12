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
public class ToolPageTemplateTest extends AbstractTemplateTestBase {

	private static final String EXTENSION_POINT = "org.eclipse.dawnsci.plotting.api.toolPage";

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
		bot.textWithLabel("Java package name").setText("org.dawnsci.dde.test");
		bot.textWithLabel("Java class name").setText("ToolPage");
		bot.textWithLabel("Page identifier").setText(getProjectName());
		bot.textWithLabel("Tooltip").setText("Test Tool Page tooltip");
		bot.textWithLabel("Label").setText("Test Tool Page");
		bot.textWithLabel("Cheat sheet identifier").setText(getProjectName());
		bot.comboBoxWithLabel("Category").setSelection(1);
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
				"         point=\"org.eclipse.dawnsci.plotting.api.toolPage\">\n" + 
				"      <plotting_tool_page\n" + 
				"            category=\"org.dawnsci.plotting.tools.category.Profile\"\n" + 
				"            cheat_sheet_id=\"org.dawnsci.dde.test.toolpage\"\n" + 
				"            class=\"org.dawnsci.dde.test.ToolPage\"\n" + 
				"            icon=\"icons/default.gif\"\n" + 
				"            id=\"org.dawnsci.dde.test.toolpage\"\n" + 
				"            label=\"Test Tool Page\"\n" + 
				"            tooltip=\"Test Tool Page tooltip\"\n" + 
				"            visible=\"true\">\n" + 
				"      </plotting_tool_page>\n" + 
				"   </extension>\n" + 
				"</plugin>\n";
	}
}

package org.dawnsci.plotting.histogram.ui;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.dawnsci.plotting.views.ToolPageView;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.intro.IIntroPart;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

public class HistogramPluginTests {

	private static HistogramToolPage2 histogramToolPage;

	@BeforeClass
	public static void beforeClass() throws Exception {
		IIntroPart part = PlatformUI.getWorkbench().getIntroManager().getIntro();
		PlatformUI.getWorkbench().getIntroManager().closeIntro(part);

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart view = null;

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("data");
		IFile file = project.getFile("examples/pilatus300k.edf");
		IEditorPart editor = IDE.openEditor(page, file, "org.dawb.workbench.editors.ImageEditor",true);
		//org.dawb.workbench.editors.ImageEditor
		//

		IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		final IToolPageSystem sys = (IToolPageSystem)activePart.getAdapter(IToolPageSystem.class);

		view = page.showView("org.dawb.workbench.plotting.views.toolPageView.fixed",
		        "org.dawnsci.plotting.histogram.histogram_tool_page_2", IWorkbenchPage.VIEW_ACTIVATE);
		IToolPage tool = sys.getToolPage("org.dawnsci.plotting.histogram.histogram_tool_page_2");
		histogramToolPage = (HistogramToolPage2) tool;
		assertNotNull(histogramToolPage);


	}

	@Test
	public void test() {
		//uk.ac.diamond.scisoft.analysis
		//SDAPlotter.plot(String plotName, final IDataset yValues) throws Exception

		//readAndDispatchForever();
	}

	@AfterClass
	public static void afterClass() {
////		project.delete(true, true, null);
////		root.refreshLocal(IResource.DEPTH_INFINITE, null);
	}

	/**
	 * Call this to stop at this point and be able to interact with the UI
	 */
	public static void readAndDispatchForever() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}


}

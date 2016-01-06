package org.dawnsci.plotting.histogram.ui;

import static org.junit.Assert.*;

import org.dawnsci.plotting.views.ToolPageView;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.intro.IIntroPart;
import org.junit.Test;

public class HistogramLifecyclePluginTests extends PluginTestBase {

	private static HistogramToolPage2 histogramToolPage;

	@Test
	public void testColourViewerInitialSetting() throws Exception {
		IIntroPart part = PlatformUI.getWorkbench().getIntroManager()
				.getIntro();
		PlatformUI.getWorkbench().getIntroManager().closeIntro(part);

		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();

		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject("data");
		IFile file = project.getFile("examples/pilatus300k.edf");
		IEditorPart editor = IDE.openEditor(page, file,
				"org.dawb.workbench.editors.ImageEditor", true);

		IWorkbenchPart activePart = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActivePart();
		final IToolPageSystem sys = (IToolPageSystem) activePart
				.getAdapter(IToolPageSystem.class);

		page.showView(ToolPageView.FIXED_VIEW_ID,
				"org.dawnsci.plotting.histogram.histogram_tool_page_2",
				IWorkbenchPage.VIEW_ACTIVATE);
		IToolPage tool = sys
				.getToolPage("org.dawnsci.plotting.histogram.histogram_tool_page_2");
		histogramToolPage = (HistogramToolPage2) tool;
		assertNotNull(histogramToolPage);
	
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
		.closeEditor(editor, false);
		// force a deactivate to ensure no NPEs are thrown
		histogramToolPage.deactivate();
		
	}

}

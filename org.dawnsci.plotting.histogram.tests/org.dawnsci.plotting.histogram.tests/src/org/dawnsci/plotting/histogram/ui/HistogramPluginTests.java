package org.dawnsci.plotting.histogram.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.intro.IIntroPart;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class HistogramPluginTests extends PluginTestBase{

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
	public void testColourViewerInitialSetting() {
		
		// put in protected method that returns comboViewer from HistogramToolPage1 
		// read text, assert
		
		// Allow time for the trace to be created
		readAndDispatch(5);
		
		IPaletteTrace trace = histogramToolPage.getPaletteTrace();
		assertNotNull(trace);
		
		String colourSchemeName = trace.getPaletteName();
		String colourSchemeNameViewer = getSelectedColourScheme();
		assertEquals(colourSchemeName, colourSchemeNameViewer);
	}
	
	@Test
	public void testColourViewerUpdatesFromPalette(){
		
		// Allow time for the trace to be created
		readAndDispatch(5);
		
		IPaletteTrace trace = histogramToolPage.getPaletteTrace();
		assertNotNull(trace);
		
		// Pick the last colour scheme name from the list name to set on the combo viewer
		List<String> colourSchemeList = getColourSchemeList();
		String colourSchemeName = colourSchemeList.get(colourSchemeList.size()-1);
		trace.setPalette(colourSchemeName);
		
		// Allow time for listeners to fire
		readAndDispatch(5);
		
		String colourSchemeNameViewer = getSelectedColourScheme();
		assertEquals(colourSchemeName, colourSchemeNameViewer);
	}
	
	@Test
	public void testPaletteUpdatesFromComboViewer(){
		
		// Allow time for the trace to be created
		readAndDispatch(5);
		
		IPaletteTrace trace = histogramToolPage.getPaletteTrace();
		assertNotNull(trace);
		
		// Pick the last colour scheme name from the list name to set on the combo viewer
		List<String> colourSchemeList = getColourSchemeList();
		String colourSchemeNameViewer = colourSchemeList.get(colourSchemeList.size()-1);
		histogramToolPage.getColourMapViewer().setSelection(new StructuredSelection(colourSchemeNameViewer), true);
		
		// Allow time for listeners to fire
		readAndDispatch(5);
		String colourSchemeName = trace.getPaletteName();
		
		assertEquals(colourSchemeName, colourSchemeNameViewer);
	}
	
	private String getSelectedColourScheme()
	{
		return (String)((StructuredSelection) histogramToolPage.getColourMapViewer().getSelection()).getFirstElement();
	}
	
	private List<String> getColourSchemeList(){
		final IPaletteService pservice = (IPaletteService)PlatformUI.getWorkbench().getService(IPaletteService.class);
		return((List<String>)(pservice.getColorSchemes()));
	}
	
	@AfterClass
	public static void afterClass() {
////		project.delete(true, true, null);
////		root.refreshLocal(IResource.DEPTH_INFINITE, null);
	}

	@Override
	protected void createControl(Composite parent) throws Exception {
		// TODO Auto-generated method stub
		
	}


}

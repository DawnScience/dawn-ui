package org.dawnsci.plotting.histogram.ui;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
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
	
	@Test
	public void testRGBTraceUpdatesFromPalette(){
		// Allow time for the trace to be created
		readAndDispatch(5);
		
		IPaletteTrace trace = histogramToolPage.getPaletteTrace();
		assertNotNull(trace);
		
		List<String> colourSchemeList = getColourSchemeList();
		assert(colourSchemeList.size() > 0);	
		// Pick the first colour scheme name from the list name to set on thetrace
		String colourSchemeName2 = colourSchemeList.get(0);
		trace.setPalette(colourSchemeName2);
		readAndDispatch(5);
		
		ILineTrace[] rgbTracesBefore = histogramToolPage.getHistogramViewer().getRGBTraces();
		IDataset[] before = new IDataset[] {
				rgbTracesBefore[0].getData().clone(),
				rgbTracesBefore[1].getData().clone(),
				rgbTracesBefore[2].getData().clone() };

		// Pick the last colour scheme name from the list name to set on the combo viewer
		String colourSchemeName = colourSchemeList.get(colourSchemeList.size()-1);
		trace.setPalette(colourSchemeName);
		readAndDispatch(5);
		
		ILineTrace[] rgbTracesAfter = histogramToolPage.getHistogramViewer().getRGBTraces();
		IDataset[] after = new IDataset[] {
				rgbTracesAfter[0].getData().clone(),
				rgbTracesAfter[1].getData().clone(),
				rgbTracesAfter[2].getData().clone() };
		
		//Check the RGB lines have updated, i.e. the data values are no longer the same
		assertThat(before[0], is(not(after[0])));
		assertThat(before[1], is(not(after[1])));
		assertThat(before[2], is(not(after[2])));
	}
	
	@Test
	public void testPaletteUpdatesFromLockAction(){
		// Allow time for the trace to be created
		readAndDispatch(5);
		IPaletteTrace trace = histogramToolPage.getPaletteTrace();
		
		boolean lockActionState = true;
		histogramToolPage.getLockAction().setChecked(lockActionState);
		histogramToolPage.getLockAction().run();
		boolean palleteLockState = !trace.isRescaleHistogram();
		assertEquals(palleteLockState, lockActionState);
		
		lockActionState = false;
		histogramToolPage.getLockAction().setChecked(lockActionState);
		histogramToolPage.getLockAction().run();
		palleteLockState = !trace.isRescaleHistogram();
		assertEquals(palleteLockState, lockActionState);
	}
	
	@Test
	public void testLockActionUpdatesFromPalette(){
		// Allow time for the trace to be created
		readAndDispatch(5);
		IPaletteTrace trace = histogramToolPage.getPaletteTrace();
		
		boolean palleteLockState = true;
		trace.setRescaleHistogram(!palleteLockState);
		boolean lockActionState = histogramToolPage.getLockAction().isChecked();
		assertEquals(palleteLockState, lockActionState);
		
		palleteLockState = false;
		trace.setRescaleHistogram(!palleteLockState);
		lockActionState = histogramToolPage.getLockAction().isChecked();
		assertEquals(palleteLockState, lockActionState);
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

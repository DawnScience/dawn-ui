package org.dawnsci.spectrum.ui;

//import org.dawnsci.spectrum.views.SpectrumView;
import org.dawnsci.spectrum.ui.views.TraceProcessPage;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

//import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;

public class SpectrumPerspective implements IPerspectiveFactory {

	/**
	 * Old id maintained to keep old workspaces happy.
	 */
	public static final String ID = "org.dawnsci.spectrum.ui.perspective";
	/**
	 * Creates the initial layout for a page.
	 */
	public void createInitialLayout(IPageLayout layout) {

		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		addFastViews(layout);
		addViewShortcuts(layout);
		addPerspectiveShortcuts(layout);
		
		IFolderLayout navigatorFolder = layout.createFolder("navigator-folder", IPageLayout.LEFT, 0.15f, editorArea);
		navigatorFolder.addView("org.dawnsci.spectrum.ui.views.SpectrumProject");
		navigatorFolder.addView("uk.ac.diamond.sda.navigator.views.FileView");
		
		IViewLayout vLayout = layout.getViewLayout("org.dawnsci.spectrum.ui.views.SpectrumProject");
		vLayout.setCloseable(false);
		vLayout = layout.getViewLayout("uk.ac.diamond.sda.navigator.views.FileView");
		vLayout.setCloseable(false);
		
		{
			IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.LEFT, 0.2f, IPageLayout.ID_EDITOR_AREA);
			folderLayout.addView("org.dawnsci.spectrum.ui.views.SpectrumView");
			vLayout = layout.getViewLayout("org.dawnsci.spectrum.ui.views.SpectrumView");
			vLayout.setCloseable(false);
			
		}
		
		{
			IFolderLayout folderLayout = layout.createFolder("folder_1", IPageLayout.LEFT, 0.6f, IPageLayout.ID_EDITOR_AREA);
			folderLayout.addView("org.dawnsci.spectrum.ui.views.SpectrumPlot");
			vLayout = layout.getViewLayout("org.dawnsci.spectrum.ui.views.SpectrumPlot");
			vLayout.setCloseable(false);
		}
		
		{
			IFolderLayout folderLayout = layout.createFolder("folder_2", IPageLayout.TOP, 0.7f, IPageLayout.ID_EDITOR_AREA);
			folderLayout.addView("org.dawnsci.spectrum.ui.SpectrumDatasetView");
			vLayout = layout.getViewLayout("org.dawnsci.spectrum.ui.SpectrumDatasetView");
			vLayout.setCloseable(false);

		}
	}

	/**
	 * Add fast views to the perspective.
	 */
	private void addFastViews(IPageLayout layout) {
	}

	/**
	 * Add view shortcuts to the perspective.
	 */
	private void addViewShortcuts(IPageLayout layout) {
	}

	/**
	 * Add perspective shortcuts to the perspective.
	 */
	private void addPerspectiveShortcuts(IPageLayout layout) {
	}

}

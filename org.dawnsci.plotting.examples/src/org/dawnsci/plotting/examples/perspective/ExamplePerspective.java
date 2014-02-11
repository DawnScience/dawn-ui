package org.dawnsci.plotting.examples.perspective;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IFolderLayout;

public class ExamplePerspective implements IPerspectiveFactory {

	/**
	 * Creates the initial layout for a page.
	 */
	public void createInitialLayout(IPageLayout layout) {
		
		layout.setEditorAreaVisible(false);
		addFastViews(layout);
		addViewShortcuts(layout);
		addPerspectiveShortcuts(layout);
		{
			IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.TOP, 0.5f, IPageLayout.ID_EDITOR_AREA);
			folderLayout.addView("org.dawnsci.plotting.examples.imageExample");
			folderLayout.addView("org.dawnsci.plotting.examples.sectorExample");
		}
		layout.addView("org.dawnsci.plotting.examples.surfaceExample", IPageLayout.RIGHT, 0.5f, "folder");
		layout.addView("org.dawnsci.plotting.examples.barExample", IPageLayout.TOP, 0.35f, "org.dawnsci.plotting.examples.surfaceExample");
		{
			IFolderLayout folderLayout = layout.createFolder("folder_1", IPageLayout.TOP, 0.35f, "org.dawnsci.plotting.examples.sectorExample");
			folderLayout.addView("org.dawnsci.plotting.examples.xyExample");
			folderLayout.addView("org.dawnsci.plotting.examples.axisExample");
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

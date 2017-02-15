package org.dawnsci.datavis.view.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

public class DataVisPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		
		layout.setEditorAreaVisible(false);
		
		IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.LEFT, 0.2f, IPageLayout.ID_EDITOR_AREA);
		folderLayout.addView("org.dawnsci.datavis.view.parts.LoadedFilePart");
		IViewLayout vLayout = layout.getViewLayout("org.dawnsci.datavis.view.parts.LoadedFilePart");
		vLayout.setCloseable(false);

		folderLayout = layout.createFolder("folder_1", IPageLayout.LEFT, 0.7f, IPageLayout.ID_EDITOR_AREA);
		folderLayout.addView("org.dawnsci.datavis.view.parts.Plot");
		vLayout = layout.getViewLayout("org.dawnsci.datavis.view.parts.Plot");
		vLayout.setCloseable(false);



		folderLayout = layout.createFolder("folder_2", IPageLayout.RIGHT, 0.4f, IPageLayout.ID_EDITOR_AREA);
		folderLayout.addView("org.dawnsci.datavis.view.parts.DatasetPart");
		vLayout = layout.getViewLayout("org.dawnsci.datavis.view.parts.DatasetPart");
		vLayout.setCloseable(false);
	}

}
package org.dawnsci.datavis.view.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

public class DataVisPerspective implements IPerspectiveFactory {

	public static final String ID = "org.dawnsci.datavis.DataVisPerspective";
	public static final String LOADED_FILE_ID = "org.dawnsci.datavis.view.parts.LoadedFilePart";
	public void createInitialLayout(IPageLayout layout) {
		
		layout.setEditorAreaVisible(false);
		
		IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.LEFT, 0.2f, IPageLayout.ID_EDITOR_AREA);
		folderLayout.addView(LOADED_FILE_ID);
		IViewLayout vLayout = layout.getViewLayout(LOADED_FILE_ID);
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
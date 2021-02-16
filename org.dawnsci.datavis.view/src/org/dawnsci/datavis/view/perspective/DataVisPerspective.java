package org.dawnsci.datavis.view.perspective;

import org.dawnsci.datavis.api.DataVisConstants;
import org.dawnsci.datavis.view.parts.DatasetPart;
import org.dawnsci.datavis.view.parts.LoadedFilePart;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

public class DataVisPerspective implements IPerspectiveFactory {

	public static final String ID = DataVisConstants.DATAVIS_PERSPECTIVE_ID;

	private static final String PLOT_ID = "org.dawnsci.datavis.view.parts.Plot";

	public void createInitialLayout(IPageLayout layout) {
		
		layout.setEditorAreaVisible(false);
		
		IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.LEFT, 0.2f, IPageLayout.ID_EDITOR_AREA);
		folderLayout.addView(LoadedFilePart.ID);
		IViewLayout vLayout = layout.getViewLayout(LoadedFilePart.ID);
		vLayout.setCloseable(false);

		folderLayout = layout.createFolder("folder_1", IPageLayout.LEFT, 0.7f, IPageLayout.ID_EDITOR_AREA);
		folderLayout.addView(PLOT_ID);
		vLayout = layout.getViewLayout(PLOT_ID);
		vLayout.setCloseable(false);

		folderLayout = layout.createFolder("folder_2", IPageLayout.RIGHT, 0.4f, IPageLayout.ID_EDITOR_AREA);
		folderLayout.addView(DatasetPart.ID);
		vLayout = layout.getViewLayout(DatasetPart.ID);
		vLayout.setCloseable(false);
	}
}

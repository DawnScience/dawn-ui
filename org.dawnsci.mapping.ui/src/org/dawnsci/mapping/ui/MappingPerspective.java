package org.dawnsci.mapping.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

public class MappingPerspective implements IPerspectiveFactory {

	public static final String ID = "org.dawnsci.mapping.ui.MappingPerspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
	
		IFolderLayout navigatorFolder = layout.createFolder("navigator-folder", IPageLayout.LEFT, 0.15f, editorArea);
		navigatorFolder.addView("org.eclipse.ui.navigator.ProjectExplorer");
		navigatorFolder.addView("uk.ac.diamond.sda.navigator.views.FileView");
		
		IFolderLayout left = layout.createFolder("mappeddata", IPageLayout.LEFT, 0.8f, editorArea);
		left.addView("org.dawnsci.mapping.ui.mappeddataview");
		IViewLayout vLayout = layout.getViewLayout("org.dawnsci.mapping.ui.mappeddataview");
		vLayout.setCloseable(false);
		
		IFolderLayout dataLayout = layout.createFolder("map", IPageLayout.RIGHT, 0.5f, "mappeddata");
		dataLayout.addView("org.dawnsci.mapping.ui.mapview");
		vLayout = layout.getViewLayout("org.dawnsci.mapping.ui.mapview");
		vLayout.setCloseable(false);
		
		
		IFolderLayout dataoutLayout = layout.createFolder("spectrum", IPageLayout.RIGHT, 0.5f, "map");
		dataoutLayout.addView("org.dawnsci.mapping.ui.spectrumview");
		vLayout = layout.getViewLayout("org.dawnsci.mapping.ui.spectrumview");
		vLayout.setCloseable(false);
		


	}

}

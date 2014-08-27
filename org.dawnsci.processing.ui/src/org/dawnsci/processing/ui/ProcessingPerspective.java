package org.dawnsci.processing.ui;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class ProcessingPerspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(IPageLayout layout) {
		
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
	
		IFolderLayout navigatorFolder = layout.createFolder("navigator-folder", IPageLayout.LEFT, 0.15f, editorArea);
		navigatorFolder.addView("org.eclipse.ui.navigator.ProjectExplorer");
		navigatorFolder.addView("uk.ac.diamond.sda.navigator.views.FileView");
		
		IFolderLayout left = layout.createFolder("DataFileView", IPageLayout.LEFT, 0.250f, editorArea);
		left.addView("org.dawnsci.processing.ui.DataFileSliceView");
		
		IFolderLayout top = layout.createFolder("procView", IPageLayout.LEFT, 0.5f, editorArea);
		top.addView("org.dawnsci.processing.ui.processingView");
		
		IFolderLayout bottomRight = layout.createFolder("modelView", IPageLayout.BOTTOM, 0.5f, "procView");
		bottomRight.addView("org.dawnsci.processing.ui.propertySheet");
		
		IFolderLayout dataLayout = layout.createFolder("input", IPageLayout.RIGHT, 0.4f, "procView");
		dataLayout.addView("org.dawnsci.processing.ui.input");
		
		IFolderLayout dataoutLayout = layout.createFolder("output", IPageLayout.RIGHT, 0.4f, "modelView");
		dataoutLayout.addView("org.dawnsci.processing.ui.output");
		
		

	}

}

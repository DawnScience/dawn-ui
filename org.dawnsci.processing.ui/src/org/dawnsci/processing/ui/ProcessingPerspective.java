/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.processing.ui;

import org.dawnsci.processing.ui.processing.ProcessingView;
import org.dawnsci.processing.ui.slice.DataFileSliceView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

public class ProcessingPerspective implements IPerspectiveFactory {

	public static final String ID = "org.dawnsci.processing.ui.ProcessingPerspective";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
	
		IFolderLayout navigatorFolder = layout.createFolder("navigator-folder", IPageLayout.LEFT, 0.15f, editorArea);
		navigatorFolder.addView("org.eclipse.ui.navigator.ProjectExplorer");
		navigatorFolder.addView("org.dawnsci.fileviewer.FileViewer");
		
		IFolderLayout left = layout.createFolder("DataFileView", IPageLayout.LEFT, 0.3f, editorArea);
		left.addView(DataFileSliceView.ID);
		IViewLayout vLayout = layout.getViewLayout(DataFileSliceView.ID);
		vLayout.setCloseable(false);
		
		IFolderLayout dataLayout = layout.createFolder("input", IPageLayout.BOTTOM, 0.5f, "DataFileView");
		dataLayout.addView("org.dawnsci.processing.ui.input");
		vLayout = layout.getViewLayout("org.dawnsci.processing.ui.input");
		vLayout.setCloseable(false);
		
		IFolderLayout top = layout.createFolder("procView", IPageLayout.LEFT, 0.5f, editorArea);
		top.addView(ProcessingView.ID);
		vLayout = layout.getViewLayout(ProcessingView.ID);
		vLayout.setCloseable(false);
		
		IFolderLayout dataoutLayout = layout.createFolder("output", IPageLayout.BOTTOM, 0.35f, "procView");
		dataoutLayout.addView("org.dawnsci.processing.ui.output");
		vLayout = layout.getViewLayout("org.dawnsci.processing.ui.output");
		vLayout.setCloseable(false);
		
		IFolderLayout bottomRight = layout.createFolder("modelView", IPageLayout.RIGHT, 0.5f, "procView");
		bottomRight.addView("org.dawnsci.processing.ui.modelView");
		vLayout = layout.getViewLayout("org.dawnsci.processing.ui.modelView");
		vLayout.setCloseable(false);
		
		

	}

}

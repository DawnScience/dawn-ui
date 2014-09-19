/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
		
		IFolderLayout left = layout.createFolder("DataFileView", IPageLayout.LEFT, 0.3f, editorArea);
		left.addView("org.dawnsci.processing.ui.DataFileSliceView");
		
		IFolderLayout dataLayout = layout.createFolder("input", IPageLayout.BOTTOM, 0.5f, "DataFileView");
		dataLayout.addView("org.dawnsci.processing.ui.input");
		
		IFolderLayout top = layout.createFolder("procView", IPageLayout.LEFT, 0.5f, editorArea);
		top.addView("org.dawnsci.processing.ui.processingView");
		
		IFolderLayout dataoutLayout = layout.createFolder("output", IPageLayout.BOTTOM, 0.35f, "procView");
		dataoutLayout.addView("org.dawnsci.processing.ui.output");
		
		IFolderLayout bottomRight = layout.createFolder("modelView", IPageLayout.RIGHT, 0.5f, "procView");
		bottomRight.addView("org.dawnsci.processing.ui.propertySheet");
		
		

	}

}

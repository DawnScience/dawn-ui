/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.workbench.ui.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 *   DataPerspective
 *
 *   @author gerring
 *   @date Jul 19, 2010
 *   @project org.edna.workbench.application
 **/
public class DataBrowsingPerspective implements IPerspectiveFactory {

	/**
	 * Old id maintained to keep old workspaces happy.
	 */
	public static final String ID = "org.edna.workbench.application.perspective.DataPerspective";
	/**
	 * Creates the initial layout for a page.
	 */
	public void createInitialLayout(IPageLayout layout) {

		addFastViews(layout);
		addViewShortcuts(layout);
		addPerspectiveShortcuts(layout);
		
		String editorArea = layout.getEditorArea();
		IFolderLayout navigatorFolder = layout.createFolder("navigator-folder", IPageLayout.LEFT, 0.22f, editorArea);
		navigatorFolder.addView("org.eclipse.ui.navigator.ProjectExplorer");
		navigatorFolder.addView("uk.ac.diamond.sda.navigator.views.FileView");
		{
			IFolderLayout folderLayout = layout.createFolder("folder", IPageLayout.RIGHT, 0.69f, IPageLayout.ID_EDITOR_AREA);
			folderLayout.addView("org.dawb.workbench.views.dataSetView");
			folderLayout.addView("fable.imageviewer.views.ImageView:Zoom");
			folderLayout.addView("fable.imageviewer.views.LineView");
			folderLayout.addView("fable.imageviewer.views.ProfileView");
		    //folderLayout.addView("fable.imageviewer.views.ReliefView");
			//folderLayout.addView("fable.imageviewer.views.RockingCurveView");
		}
		{
			IFolderLayout folderLayout = layout.createFolder("folder_1", IPageLayout.BOTTOM, 0.81f, IPageLayout.ID_EDITOR_AREA);
			folderLayout.addView("org.dawb.passerelle.views.ValueView");
			folderLayout.addView("org.eclipse.ui.console.ConsoleView");
			folderLayout.addView("org.eclipse.ui.views.ProgressView");
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

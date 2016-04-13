/*
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.fileviewer.parts;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.dawnsci.fileviewer.FileViewer;
import org.dawnsci.fileviewer.Utils;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;

public class FileViewerE4Part {
	private static final String FILEVIEWER_SAVED_DIRECTORY = "org.dawnsci.fileviewer.saved.directory";
	private FileViewer fileViewer;
	private ScopedPreferenceStore store;

	public FileViewerE4Part() {
		store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.fileviewer");
		fileViewer = new FileViewer();
		// add to Eclipse Context in order to re use with DI
		getActiveContext().set(FileViewer.class, fileViewer);
	}

	private static IEclipseContext getActiveContext() {
		IEclipseContext parentContext = (IEclipseContext) PlatformUI.getWorkbench().getService(IEclipseContext.class);
		return parentContext.getActiveLeaf();
	}

	@PostConstruct
	public void init() {
		String path = store.getString(FILEVIEWER_SAVED_DIRECTORY);
		if (path != null && path!= "")
			fileViewer.setCurrentDirectory(path);
	}

	@PersistState
	public void saveState() {
		final String path = fileViewer.getSavedDirectory();
		store.setValue(FILEVIEWER_SAVED_DIRECTORY, path);
	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		fileViewer.getIconCache().initResources(Display.getDefault());
		fileViewer.createCompositeContents(parent);
		fileViewer.notifyRefreshFiles(null);
	}

	@PostConstruct
	public void createToolBar(MPart myPart) {
		//create the toolbar programmatically
		MToolBar toolbar = MMenuFactory.INSTANCE.createToolBar();
		//create the tool item programmatically
		MDirectToolItem parentItem = MMenuFactory.INSTANCE.createDirectToolItem();
		parentItem.setElementId("org.dawnsci.fileviewer.parent");
		parentItem.setIconURI("platform:/plugin/org.dawnsci.fileviewer/icons/arrow-090.png");
		parentItem.setTooltip(Utils.getResourceString("tool.Parent.tiptext"));
		parentItem.setContributionURI("bundleclass://org.dawnsci.fileviewer/org.dawnsci.fileviewer.handlers.ParentHandler");
		parentItem.setVisible(true);
		parentItem.setEnabled(true);
		toolbar.getChildren().add(parentItem);

		MDirectToolItem refreshItem = MMenuFactory.INSTANCE.createDirectToolItem();
		refreshItem.setElementId("org.dawnsci.fileviewer.refresh");
		refreshItem.setIconURI("platform:/plugin/org.dawnsci.fileviewer/icons/arrow-circle-double-135.png");
		refreshItem.setTooltip(Utils.getResourceString("tool.Refresh.tiptext"));
		refreshItem.setContributionURI("bundleclass://org.dawnsci.fileviewer/org.dawnsci.fileviewer.handlers.RefreshHandler");
		refreshItem.setVisible(true);
		refreshItem.setEnabled(true);
		toolbar.getChildren().add(refreshItem);

		MDirectToolItem layoutItem = MMenuFactory.INSTANCE.createDirectToolItem();
		layoutItem.setElementId("org.dawnsci.fileviewer.layout");
		layoutItem.setIconURI("platform:/plugin/org.dawnsci.fileviewer/icons/layout-design.png");
		layoutItem.setTooltip(Utils.getResourceString("tool.LayoutEdit.tiptext"));
		layoutItem.setContributionURI("bundleclass://org.dawnsci.fileviewer/org.dawnsci.fileviewer.handlers.LayoutHandler");
		layoutItem.setVisible(true);
		layoutItem.setEnabled(true);
		toolbar.getChildren().add(layoutItem);

		myPart.setToolbar(toolbar);
	}

	@Focus
	public void onFocus() {

	}

	@PreDestroy
	private void partDestroyed() {
		if (fileViewer != null)
			fileViewer.close();
	}

}
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
import javax.inject.Inject;

import org.dawnsci.fileviewer.Activator;
import org.dawnsci.fileviewer.FileViewer;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

@SuppressWarnings("restriction")
public class FileViewerE4Part {
	private static final String FILEVIEWER_SAVED_DIRECTORY = "org.dawnsci.fileviewer.saved.directory";
	private FileViewer fileViewer;
	private ScopedPreferenceStore store;

	/**
	 * Used to provide a selection to the selection service
	 */
	@Inject
	private ESelectionService tableSelectionService;

	@Inject
	private ECommandService commandService;

	@Inject
	private EHandlerService handlerService;

	/**
	 * E4 part
	 */
	public FileViewerE4Part() {
		store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawnsci.fileviewer");
		fileViewer = new FileViewer();
		// add to Eclipse Context in order to re use with DI
		Activator.getActiveContext().set(FileViewer.class, fileViewer);
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

		//set the services (used temporarily for calling handlers from widgetSelected)
		fileViewer.setSelectionService(tableSelectionService);
		fileViewer.setCommandService(commandService);
		fileViewer.setHandlerService(handlerService);

		fileViewer.createCompositeContents(parent);
		fileViewer.notifyRefreshFiles(null);
		//Expand root(s)
		fileViewer.getTreeExplorer().expandRoot();
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
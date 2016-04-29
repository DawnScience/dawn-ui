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
import org.dawnsci.fileviewer.Utils;
import org.dawnsci.fileviewer.handlers.ConvertHandler;
import org.dawnsci.fileviewer.handlers.OpenHandler;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

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
	public void postConstruct(Composite parent, MPart part) {
		fileViewer.getIconCache().initResources(Display.getDefault());
		fileViewer.createCompositeContents(parent);
		fileViewer.notifyRefreshFiles(null);
		// attach a selection listener to our jface viewer
		fileViewer.getTableViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				// set the selection to the service
				tableSelectionService.setSelection(selection);
			}
		});

		createPopupMenu(fileViewer.getTableViewer().getTable());
	}

	private void createPopupMenu(Table table) {
		// Create popup menu programmatically
		Menu menuTable = new Menu(table);
		table.setMenu(menuTable);

		MenuItem openItem = new MenuItem(menuTable, SWT.None);
		openItem.setText("Open");
		openItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ParameterizedCommand myCommand = commandService.createCommand("org.dawnsci.fileviewer.openCommand", null);
				handlerService.activateHandler("org.dawnsci.fileviewer.openCommand", new OpenHandler(fileViewer));
				handlerService.executeHandler(myCommand);
			}
		});

		MenuItem convertItem = new MenuItem(menuTable, SWT.None);
		convertItem.setText("Convert...");
		convertItem.setImage(Activator.getImage("icons/convert.png"));
		convertItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ParameterizedCommand myCommand = commandService.createCommand("org.dawnsci.fileviewer.convertCommand", null);
				handlerService.activateHandler("org.dawnsci.fileviewer.convertCommand", new ConvertHandler(fileViewer));
				handlerService.executeHandler(myCommand);
			}
		});

		table.addListener(SWT.MouseDown, new Listener(){
			@Override
			public void handleEvent(Event event) {
				TableItem[] selection = table.getSelection();
				if (selection.length != 0 && (event.button == 3)) {
					menuTable.setVisible(true);
				}
			}
		});
	}

	@PostConstruct
	public void createToolBar(MPart myPart) {
		//create the toolbar programmatically
		MToolBar toolbar = MMenuFactory.INSTANCE.createToolBar();
		//create the tool item programmatically
		MDirectToolItem parentItem = MMenuFactory.INSTANCE.createDirectToolItem();
		parentItem.setElementId(FileViewerConstants.TABLE_PARENT);
		parentItem.setIconURI("platform:/plugin/org.dawnsci.fileviewer/icons/arrow-090.png");
		parentItem.setTooltip(Utils.getResourceString("tool.Parent.tiptext"));
		parentItem.setContributionURI("bundleclass://org.dawnsci.conversion.ui/org.dawnsci.fileviewer.handlers.ParentHandler");
		parentItem.setVisible(true);
		parentItem.setEnabled(true);
		toolbar.getChildren().add(parentItem);

		MDirectToolItem refreshItem = MMenuFactory.INSTANCE.createDirectToolItem();
		refreshItem.setElementId(FileViewerConstants.TABLE_REFRESH);
		refreshItem.setIconURI("platform:/plugin/org.dawnsci.fileviewer/icons/arrow-circle-double-135.png");
		refreshItem.setTooltip(Utils.getResourceString("tool.Refresh.tiptext"));
		refreshItem.setContributionURI("bundleclass://org.dawnsci.fileviewer/org.dawnsci.fileviewer.handlers.RefreshHandler");
		refreshItem.setVisible(true);
		refreshItem.setEnabled(true);
		toolbar.getChildren().add(refreshItem);

		MDirectToolItem layoutItem = MMenuFactory.INSTANCE.createDirectToolItem();
		layoutItem.setElementId(FileViewerConstants.TABLE_LAYOUT);
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
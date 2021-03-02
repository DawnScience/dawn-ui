/*-
 * Copyright (c) 2019 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.datavis.view.parts;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.dawnsci.datavis.model.FileControllerStateEvent;
import org.dawnsci.datavis.model.FileControllerStateEventListener;
import org.dawnsci.datavis.model.IDatasetStateChanger;
import org.dawnsci.datavis.model.IFileController;
import org.dawnsci.datavis.model.IPlotController;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.model.PlotEventObject;
import org.dawnsci.datavis.model.PlotEventObject.PlotEventType;
import org.dawnsci.datavis.model.PlotModeChangeEventListener;
import org.dawnsci.january.ui.utils.SelectionUtils;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasetPart {
	protected static final Logger logger = LoggerFactory.getLogger(DatasetPart.class);

	public static final String ID = "org.dawnsci.datavis.view.parts.DatasetPart";

	@Inject ESelectionService selectionService;

	@Inject IFileController fileController;
	@Inject IPlotController plotController;

	private StackLayout layout;
	
	private Composite[] changers;
	
	private PlotEventType lastPlotEvent;
	private PlotModeChangeEventListener plotListener;

	private ISelectionListener selectionListener;

	private FileControllerStateEventListener fileStateListener;

	@PostConstruct
	public void createComposite(Composite parent) {

		parent.setLayout(GridLayoutFactory.fillDefaults().create());
		
		Combo uiSelector = new Combo(parent, SWT.READ_ONLY);
		
		uiSelector.setLayoutData(GridDataFactory.swtDefaults()
				.grab(true, false)
				.align(SWT.FILL, SWT.TOP)
				.create());

		Composite stack = new Composite(parent, SWT.NONE);
		stack.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		layout = new StackLayout();
		stack.setLayout(layout);

		changers = new Composite[2];
		String[] labels = new String[2];
		
		DatasetSetupComposite genericSetupComposite = new DatasetSetupComposite(stack, fileController, plotController,
				selectionService, getLoadedFileSelectionPartID());
		
		ProcessResultsUI processResultsUI = new ProcessResultsUI(stack,fileController, plotController);
		
		changers[0] = genericSetupComposite;
		changers[1] = processResultsUI;
		labels[0] = genericSetupComposite.getChangerName();
		labels[1] = processResultsUI.getChangerName();

		layout.topControl = genericSetupComposite;
		
		uiSelector.setItems(labels);
		uiSelector.select(0);
		
		
		SelectionListener adapter = SelectionListener.widgetSelectedAdapter(e -> {

			int index = uiSelector.getSelectionIndex();

			Composite composite = changers[index];

			layout.topControl = composite;

			if (composite instanceof IDatasetStateChanger) {
				IDatasetStateChanger c = (IDatasetStateChanger)composite;

				Object selection = selectionService.getSelection(LoadedFilePart.ID);
				if (selection instanceof ISelection) {
					List<LoadedFile> files = SelectionUtils.getFromSelection((ISelection) selection, LoadedFile.class);

					c.initialize(files);
				}

			}
			stack.layout();
		});

		uiSelector.addSelectionListener(adapter);
		
		Label progress = new Label(parent,SWT.None);
		progress.setLayoutData(GridDataFactory.fillDefaults().create());
		progress.setText("Ready");
		
		plotListener = new PlotModeChangeEventListener() {
			
			@Override
			public void plotStateEvent(PlotEventObject event) {
				if (Display.getCurrent() == null) {
					Display.getDefault().asyncExec(() -> plotStateEvent(event));
					return;
				}
				
				if (!PlotEventType.ERROR.equals(lastPlotEvent) || PlotEventType.LOADING.equals(event.getEventType())){
					progress.setText(event.getMessage());
					lastPlotEvent = event.getEventType();
				}
				
			}
			
			@Override
			public void plotModeChanged() {
				//do nothing
				
			}
		};
		
		selectionListener = new ISelectionListener() {
			@Override
			public void selectionChanged(MPart part, Object selection) {
				if (selection instanceof ISelection) {
					List<LoadedFile> files = SelectionUtils.getFromSelection((ISelection)selection, LoadedFile.class);

					Control c = layout.topControl;

					if (c instanceof IDatasetStateChanger) {
						((IDatasetStateChanger) c).updateOnSelectionChange(files.isEmpty() ? null : files.get(0));
					}
				}
			}
		};
		
		selectionService.addSelectionListener(getLoadedFileSelectionPartID(), selectionListener);
		
		plotController.addPlotModeListener(plotListener);
		
		fileStateListener = new FileControllerStateEventListener() {
			@Override
			public void stateChanged(FileControllerStateEvent event) {
				
				Control c = layout.topControl;
				
				if (c instanceof IDatasetStateChanger) {
					((IDatasetStateChanger) c).stateChanged(event);	
				}
				
			}
			@Override
			public void refreshRequest() {
				Control c = layout.topControl;
				
				if (c instanceof IDatasetStateChanger) {
					((IDatasetStateChanger) c).refreshRequest();
				}
			}
		};
		
		fileController.addStateListener(fileStateListener);
		
		
	}
	
	protected String getLoadedFileSelectionPartID() {
		return LoadedFilePart.ID;
	}

	@PreDestroy
	public void dispose() {
		fileController.removeStateListener(fileStateListener);
		selectionService.removeSelectionListener(LoadedFilePart.ID, selectionListener);
		plotController.removePlotModeListener(plotListener);
		for (Composite c : changers) {
			c.dispose();
		}
	}

	@Focus
	public void setFocus() {
		layout.topControl.setFocus();
	}
}

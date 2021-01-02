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

import org.dawnsci.datavis.api.IPlotMode;
import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.FileControllerStateEvent;
import org.dawnsci.datavis.model.FileControllerStateEventListener;
import org.dawnsci.datavis.model.IFileController;
import org.dawnsci.datavis.model.ILoadedFileInitialiser;
import org.dawnsci.datavis.model.IPlotController;
import org.dawnsci.datavis.model.IRefreshable;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.model.PlotEventObject;
import org.dawnsci.datavis.model.PlotEventObject.PlotEventType;
import org.dawnsci.datavis.model.PlotModeChangeEventListener;
import org.dawnsci.datavis.view.Activator;
import org.dawnsci.datavis.view.preference.DataVisPreferenceConstants;
import org.dawnsci.datavis.view.table.AxisSliceDialog;
import org.dawnsci.datavis.view.table.DataOptionTableViewer;
import org.dawnsci.january.model.ISliceAssist;
import org.dawnsci.january.model.ISliceChangeListener;
import org.dawnsci.january.model.NDimensions;
import org.dawnsci.january.model.SliceChangeEvent;
import org.dawnsci.january.ui.dataconfigtable.DataConfigurationTable;
import org.dawnsci.january.ui.utils.SelectionUtils;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.eclipse.january.dataset.Slice;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasetPart {
	protected static final Logger logger = LoggerFactory.getLogger(DatasetPart.class);

	public static final String ID = "org.dawnsci.datavis.view.parts.DatasetPart";

	@Inject ESelectionService selectionService;

	@Inject IFileController fileController;
	@Inject IPlotController plotController;

	private DataConfigurationTable table;
	private ComboViewer plotModeOptionsViewer;

	private DataOptionTableViewer viewer;

	private FileControllerStateEventListener fileStateListener;

	private ISliceChangeListener sliceListener;
	private ISelectionListener selectionListener;
	private PlotModeChangeEventListener plotListener;
	
	private PlotEventType lastPlotEvent;


	private StackLayout layout;

	private Composite page0, page1;

	private ProcessResultsUI processResultsUI;

	@PostConstruct
	public void createComposite(Composite parent) {

		
		IPreferenceStore ps = Activator.getDefault().getPreferenceStore();
		
		boolean coSlice = ps.getBoolean(DataVisPreferenceConstants.CO_SLICE);
		plotController.setCoSlicingEnabled(coSlice);

		parent.setLayout(GridLayoutFactory.fillDefaults().create());

		Group radio = new Group(parent, SWT.NONE);
		radio.setText("Filter");
		radio.setLayoutData(GridDataFactory.swtDefaults().grab(true, false).create());
		radio.setLayout(RowLayoutFactory.swtDefaults().create());
		Button all = new Button(radio, SWT.RADIO);
		all.setText("None");
		all.setToolTipText("Show all; do not filter any datasets out");
		all.setSelection(true);
		Button proc = new Button(radio, SWT.RADIO);
		proc.setText("Processed only");
		proc.setToolTipText("Show processed datasets only; plot selected in 1D");

		Composite stack = new Composite(parent, SWT.NONE);
		stack.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		layout = new StackLayout();
		stack.setLayout(layout);

		processResultsUI = new ProcessResultsUI(fileController, plotController);
		page0 = createPage0(stack);
		page1 = processResultsUI.createPage(stack);

		layout.topControl = page0;

		Label progress = new Label(parent, SWT.NONE);
		progress.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		progress.setText("Ready");

		createListeners(progress);
		SelectionListener adapter = SelectionListener.widgetSelectedAdapter(e -> {
			if ((layout.topControl == page0) == (e.widget == all)) {
				return; // no change
			}
			boolean isAll = e.widget == all;
			layout.topControl = isAll ? page0 : page1;
			stack.layout();
			// re-trigger selections
			if (isAll) {
				viewer.reselect();
			} else if (processResultsUI.isEmpty()) { // first time...
				deselectAll();

				Object selection = selectionService.getSelection(LoadedFilePart.ID);
				if (selection instanceof ISelection) {
					List<LoadedFile> files = SelectionUtils.getFromSelection((ISelection) selection, LoadedFile.class);

					processResultsUI.initialize(files);
				}
			} else {
				processResultsUI.reselect();
			}
		});

		all.addSelectionListener(adapter);
		proc.addSelectionListener(adapter);

		// initialize
		Object selection = selectionService.getSelection(LoadedFilePart.ID);
		if (selection instanceof ISelection) {
			LoadedFile file = SelectionUtils.getFirstFromSelection((ISelection) selection, LoadedFile.class);

			updateOnSelectionChange(file);

			plotController.forceReplot();
		}
	}

	private void deselectAll() {
		for (LoadedFile f : fileController.getLoadedFiles()) {
			for (DataOptions d : f.getSelectedDataOptions()) {
				d.setSelected(false);
			}
		}
	}

	private Composite createPage0(Composite parent) {
		Composite stack = new Composite(parent, SWT.NONE);
		stack.setLayoutData(GridDataFactory.fillDefaults().create());
		stack.setLayout(GridLayoutFactory.swtDefaults().create());

		if (plotController instanceof ILoadedFileInitialiser) {
			viewer = new DataOptionTableViewer(fileController, (ILoadedFileInitialiser) plotController);
		} else {
			viewer = new DataOptionTableViewer(fileController, null);
		}
		viewer.createControl(stack);
		viewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = viewer.getStructuredSelection();
				selectionService.setSelection(selection.getFirstElement());
				DataOptions op = SelectionUtils.getFirstFromSelection(selection, DataOptions.class);
				if (op != null) {
					updateOnSelectionChange(op);

					if (op.getParent() instanceof IRefreshable) {
						IRefreshable r = (IRefreshable)op.getParent();
						if (r.isLive()) {
							plotController.forceReplot();
						}
					}
				}
			}
		});

		Composite plotTypeComposite = new Composite(stack, SWT.NONE);
		plotTypeComposite.setLayoutData(GridDataFactory.fillDefaults().create());
		plotTypeComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());

		Label plotTypeLabel = new Label(plotTypeComposite, SWT.NONE);
		plotTypeLabel.setText("Plot Type:");
		plotTypeLabel.setLayoutData(GridDataFactory.swtDefaults().create());

		plotModeOptionsViewer = new ComboViewer(plotTypeComposite);
		plotModeOptionsViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().create());
		table = new DataConfigurationTable();

		table.createControl(stack);
		table.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		table.setSliceAssist(new ISliceAssist() {
			
			@Override
			public Slice getSlice(NDimensions ndims, int dimension) {
				AxisSliceDialog d = new AxisSliceDialog(Display.getDefault().getActiveShell(), ndims, dimension);
				d.create();
		
				if (Dialog.OK == d.open()) {
		
					Integer start = d.getStart();
					Integer stop = d.getStop();
		
					if (start == null) {
						start = 0;
					}
		
					if (stop == null) {
						stop = ndims.getSize(dimension);
					}
		
					Slice s = new Slice();
					s.setStart(start);
					s.setStop(stop);
					s.setStep(1);
					return s;
				}
				return null;
			}

			@Override
			public String getLabel() {
				return "Set from axis...";
			}
		});
		
		plotModeOptionsViewer.getCombo().setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		plotModeOptionsViewer.setContentProvider(new ArrayContentProvider());
		plotModeOptionsViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				String name = "";

				if (element instanceof IPlotMode) {
					name = ((IPlotMode) element).getName();
				}

				return name;
			}
		});

		return stack;
	}

	private void createListeners(Label progress) {
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

		plotController.addPlotModeListener(plotListener);

		plotModeOptionsViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof StructuredSelection) {
					Object ob = ((StructuredSelection)selection).getFirstElement();

					if (!(ob instanceof IPlotMode)) return;
					IPlotMode mode = (IPlotMode) ob;
					DataOptions dataOptions = SelectionUtils.getFirstFromSelection(viewer.getStructuredSelection(), DataOptions.class);

					plotController.switchPlotMode(mode, dataOptions);
					if (dataOptions != null) {
						table.setInput(dataOptions.getPlottableObject().getNDimensions());
						if (mode.supportsMultiple()) {
							table.setMaxSliceNumber(50);
						} else {
							table.setMaxSliceNumber(1);
						}
					}
					viewer.refresh();
				}
			}
		});

		selectionListener = new ISelectionListener() {

			@Override
			public void selectionChanged(MPart part, Object selection) {
				if (layout.topControl == page0 && selection instanceof ISelection) {
					LoadedFile file = SelectionUtils.getFirstFromSelection((ISelection) selection, LoadedFile.class);
					updateOnSelectionChange(file);
				}
			}
		};

		selectionService.addSelectionListener(LoadedFilePart.ID, selectionListener);

		sliceListener = new ISliceChangeListener() {

			@Override
			public void sliceChanged(SliceChangeEvent event) {
				actOnEvent(event);	
			}

			@Override
			public void axisChanged(SliceChangeEvent event) {
				actOnEvent(event);	
			}

			@Override
			public void optionsChanged(SliceChangeEvent event) {
				actOnEvent(event);	
			}

			private void actOnEvent(SliceChangeEvent event) {
				if (event.getParent() instanceof DataOptions) {
					DataOptions dOptions = (DataOptions)event.getParent();
					
					if (!dOptions.isSelected() && !dOptions.getParent().isSelected()) {
						return;
					}
					
					plotController.replotOnSlice(dOptions);
					
					return;
				}
				
				plotController.forceReplot();
			}
		};

		fileStateListener = new FileControllerStateEventListener() {

			@Override
			public void stateChanged(FileControllerStateEvent event) {
				if (layout.topControl == page1) {
					if (!event.isSelectedDataChanged() && !event.isSelectedFileChanged()) return;

					if (event.isSelectedFileChanged()) {
						processResultsUI.updateTable(event.getLoadedFile());
					}
					if (event.isSelectedDataChanged()) {
						Display.getDefault().asyncExec(() -> processResultsUI.updateSelected(event.getOption()));
					}
				}
			}

			@Override
			public void refreshRequest() {
				if (Display.getCurrent() == null) {
					Display.getDefault().asyncExec(() -> refreshRequest());
					return;
				}

				DataOptions s = SelectionUtils.getFirstFromSelection(viewer.getStructuredSelection(), DataOptions.class);

				if (s != null && s.getParent() instanceof IRefreshable) {
					table.refresh();
					viewer.refresh();
				}
			}
		};

		fileController.addStateListener(fileStateListener);
	}

	private void updateOnSelectionChange(LoadedFile file) {
		if (file == null) {
			viewer.setInput(null);
			table.setInput(null);
			plotModeOptionsViewer.setInput(null);
			return;
		}

		removeSliceListener();

		List<DataOptions> dataOptions = file.getDataOptions();
		viewer.setInput(dataOptions);

		DataOptions option = null;

		for (DataOptions op : file.getDataOptions()) {
			if (op.isSelected()) {
				option = op;
				break;
			}
		}
		
		if (option == null && !viewer.getStructuredSelection().isEmpty()) {
			DataOptions d = SelectionUtils.getFirstFromSelection(viewer.getStructuredSelection(), DataOptions.class);
			if (d != null && file.getDataOptions().contains(d)) {
				option = d;
			}
		}

		if (option == null && file.getDataOptions().size() != 0) {
			option = file.getDataOptions().get(0);
		}

		if (option != null) {
			viewer.setSelection(new StructuredSelection(option),true);
			table.setInput(option.getPlottableObject().getNDimensions());
		}

		viewer.refresh();
	}

	@PreDestroy
	public void dispose() {
		processResultsUI.dispose();
		viewer.dispose();
		fileController.removeStateListener(fileStateListener);
		selectionService.removeSelectionListener(LoadedFilePart.ID, selectionListener);
		plotController.removePlotModeListener(plotListener);
		plotController.dispose();
		
		IPreferenceStore ps = Activator.getDefault().getPreferenceStore();
		ps.setValue(DataVisPreferenceConstants.CO_SLICE,plotController.isCoSlicingEnabled());
	}

	private void updateOnSelectionChange(DataOptions op) {
		removeSliceListener(op.getParent());
		op.getPlottableObject().getNDimensions().addSliceListener(sliceListener);
		IPlotMode[] suitableModes = plotController.getPlotModes(op);
		plotModeOptionsViewer.setInput(suitableModes);
		IPlotMode m = op.getPlottableObject().getPlotMode();
		plotModeOptionsViewer.setSelection(new StructuredSelection(m));
		if (m.supportsMultiple()) {
			table.setMaxSliceNumber(50);
		} else {
			table.setMaxSliceNumber(1);
		}

		table.setInput(op.getPlottableObject().getNDimensions());

		viewer.refresh();
	}
	
	private void removeSliceListener(LoadedFile f) {
		for (DataOptions d : f.getDataOptions()) {
			d.getPlottableObject().getNDimensions().removeSliceListener(sliceListener);
		}
	}

	private void removeSliceListener() {
		for (LoadedFile f : fileController.getLoadedFiles()) {
			removeSliceListener(f);
		}
	}

	@Focus
	public void setFocus() {
		if (viewer != null) viewer.getTable().setFocus();
	}
}

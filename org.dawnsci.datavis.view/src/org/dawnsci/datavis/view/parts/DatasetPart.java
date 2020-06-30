/*-
 * Copyright (c) 2019 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.datavis.view.parts;

import java.util.Arrays;
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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public class DatasetPart {

	private static final String LOADED_FILE_PART_ID = "org.dawnsci.datavis.view.parts.LoadedFilePart";

	@Inject
	ESelectionService selectionService;

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

	@PostConstruct
	public void createComposite(Composite parent) {

		
		IPreferenceStore ps = Activator.getDefault().getPreferenceStore();
		
		boolean coSlice = ps.getBoolean(DataVisPreferenceConstants.CO_SLICE);
		plotController.setCoSlicingEnabled(coSlice);
		
		parent.setLayout(new GridLayout());

		if (plotController instanceof ILoadedFileInitialiser) {
			viewer = new DataOptionTableViewer(fileController, (ILoadedFileInitialiser) plotController);
		} else {
			viewer = new DataOptionTableViewer(fileController, null);
		}
		viewer.createControl(parent);
		viewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = viewer.getStructuredSelection();
				selectionService.setSelection(selection.getFirstElement());
				if (selection.getFirstElement() instanceof DataOptions) {
					DataOptions op = (DataOptions)selection.getFirstElement();
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

		Composite plotTypeComposite = new Composite(parent, SWT.NONE);
		plotTypeComposite.setLayoutData(GridDataFactory.fillDefaults().create());
		plotTypeComposite.setLayout(new GridLayout(2, false));

		Label plotTypeLabel = new Label(plotTypeComposite, SWT.NONE);
		plotTypeLabel.setText("Plot Type:");
		plotTypeLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		plotModeOptionsViewer = new ComboViewer(plotTypeComposite);
		plotModeOptionsViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().create());
		table = new DataConfigurationTable();

		table.createControl(parent);
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
		
		plotController.addPlotModeListener(plotListener);

		plotModeOptionsViewer.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		plotModeOptionsViewer.setContentProvider(new ArrayContentProvider());
		plotModeOptionsViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				String name = "";

				if (element instanceof IPlotMode) {
					name = ((IPlotMode)element).getName();
				}

				return name;
			}
		});

		plotModeOptionsViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof StructuredSelection) {
					Object ob = ((StructuredSelection)selection).getFirstElement();

					if (!(ob instanceof IPlotMode)) return;

					DataOptions dataOptions = SelectionUtils.getFromSelection(viewer.getStructuredSelection(), DataOptions.class).get(0);

					plotController.switchPlotMode((IPlotMode)ob,dataOptions);

					table.setInput(dataOptions.getPlottableObject().getNDimensions());
					if (((IPlotMode)ob).supportsMultiple()) {
						table.setMaxSliceNumber(50);
					} else {
						table.setMaxSliceNumber(1);
					}
					viewer.refresh();
				}
			}
		});

		selectionListener = new ISelectionListener() {

			@Override
			public void selectionChanged(MPart part, Object selection) {
				if (selection instanceof ISelection) {
					List<LoadedFile> files = SelectionUtils.getFromSelection((ISelection)selection, LoadedFile.class);

					updateOnSelectionChange(files.isEmpty() ? null : files.get(0));

				}
			}
		};

		selectionService.addSelectionListener(LOADED_FILE_PART_ID, selectionListener);

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
				}
				
				plotController.forceReplot();
			}
		};

		Object selection = selectionService.getSelection(LOADED_FILE_PART_ID);
		if (selection instanceof ISelection) {
			List<LoadedFile> files = SelectionUtils.getFromSelection((ISelection)selection, LoadedFile.class);

			updateOnSelectionChange(files.isEmpty() ? null : files.get(0));

			plotController.forceReplot();
		}
		
		fileStateListener = new FileControllerStateEventListener() {

			@Override
			public void stateChanged(FileControllerStateEvent event) {
				//do nothing
			}

			@Override
			public void refreshRequest() {

				if (Display.getCurrent() == null) {
					Display.getDefault().asyncExec(()->refreshRequest());
					return;
				}
				
				List<DataOptions> s = SelectionUtils.getFromSelection(viewer.getStructuredSelection(), DataOptions.class);

				if (!s.isEmpty() && s.get(0).getParent() instanceof IRefreshable) {
					table.refresh();
					viewer.refresh();
				}
			}
		};
		
		fileController.addStateListener(fileStateListener);
	}

	private void updateOnSelectionChange(LoadedFile file){
		if (file == null) {
			viewer.setInput(null);
			table.setInput(null);
			plotModeOptionsViewer.setInput(null);
			return;
		}

		removeSliceListener();

		List<DataOptions> dataOptions = file.getDataOptions();
		viewer.setInput(dataOptions.toArray());

		DataOptions option = null;

		for (DataOptions op : file.getDataOptions()) {
			if (op.isSelected()) {
				option = op;
				break;
			}
		}
		
		if (option == null && !viewer.getStructuredSelection().isEmpty()) {
			List<DataOptions> d = SelectionUtils.getFromSelection(viewer.getStructuredSelection(), DataOptions.class);
			if (!d.isEmpty() && file.getDataOptions().contains(d.get(0))) {
				option = d.get(0);
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
	public void dispose(){
		viewer.dispose();
		fileController.removeStateListener(fileStateListener);
		selectionService.removeSelectionListener(LOADED_FILE_PART_ID, selectionListener);
		plotController.removePlotModeListener(plotListener);
		plotController.dispose();
		
		IPreferenceStore ps = Activator.getDefault().getPreferenceStore();
		ps.setValue(DataVisPreferenceConstants.CO_SLICE,plotController.isCoSlicingEnabled());
	}

	private void updateOnSelectionChange(DataOptions op){
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

	class ViewLabelLabelProvider extends StyledCellLabelProvider {

		@Override
		public void update(ViewerCell cell) {
			Object element = cell.getElement();
			StyledString text = new StyledString();
			text.append(((DataOptions)element).getName() + " " + Arrays.toString(((DataOptions)element).getLazyDataset().getShape()));
			cell.setText(text.toString());
			super.update(cell);
		}
	}


}

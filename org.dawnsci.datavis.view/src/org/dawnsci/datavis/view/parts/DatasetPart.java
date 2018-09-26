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
import org.dawnsci.datavis.model.IPlotController;
import org.dawnsci.datavis.model.IRefreshable;
import org.dawnsci.datavis.model.ISliceChangeListener;
import org.dawnsci.datavis.model.LoadedFile;
import org.dawnsci.datavis.model.NDimensions;
import org.dawnsci.datavis.model.PlotEventObject;
import org.dawnsci.datavis.model.PlotModeChangeEventListener;
import org.dawnsci.datavis.model.PlottableObject;
import org.dawnsci.datavis.model.SliceChangeEvent;
import org.dawnsci.datavis.view.DataVisSelectionUtils;
import org.dawnsci.datavis.view.table.DataConfigurationTable;
import org.dawnsci.datavis.view.table.DataOptionTableViewer;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.e4.ui.workbench.modeling.ISelectionListener;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

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

	@PostConstruct
	public void createComposite(Composite parent) {

		parent.setLayout(new FormLayout());
		FormData checkForm = new FormData();
		checkForm.top = new FormAttachment(0,0);
		checkForm.left = new FormAttachment(0,0);
		checkForm.right = new FormAttachment(100,0);
		checkForm.bottom = new FormAttachment(75,0);
		viewer = new DataOptionTableViewer(fileController);
		viewer.createControl(parent);
		viewer.getControl().setLayoutData(checkForm);

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

		FormData comboForm = new FormData();
		comboForm.top = new FormAttachment(viewer.getControl());
		comboForm.left = new FormAttachment(0,0);
		comboForm.right = new FormAttachment(100,0);

		Composite plotTypeComposite = new Composite(parent, SWT.NONE);
		plotTypeComposite.setLayoutData(comboForm);
		plotTypeComposite.setLayout(new GridLayout(2, false));

		Label plotTypeLabel = new Label(plotTypeComposite, SWT.NONE);
		plotTypeLabel.setText("Plot Type:");
		plotTypeLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		plotModeOptionsViewer = new ComboViewer(plotTypeComposite);
		table = new DataConfigurationTable();
		
		Composite lower = new Composite(parent, SWT.None);
		lower.setLayout(GridLayoutFactory.fillDefaults().create());
		table.createControl(lower);
		table.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		
		Label progress = new Label(lower,SWT.None);
		progress.setLayoutData(GridDataFactory.fillDefaults().create());
		progress.setText("Ready");
		
		plotListener = new PlotModeChangeEventListener() {
			
			@Override
			public void plotStateEvent(PlotEventObject event) {
				if (Display.getCurrent() == null) {
					Display.getDefault().asyncExec(() -> plotStateEvent(event));
					return;
				}
				
				progress.setText(event.getMessage());
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

					if (ob instanceof IPlotMode && !ob.equals(plotController.getCurrentMode())) {
						DataOptions dataOptions = DataVisSelectionUtils.getFromSelection(viewer.getStructuredSelection(), DataOptions.class).get(0);
						if (dataOptions.isSelected() && dataOptions.getParent().isSelected()) {
							plotController.switchPlotMode((IPlotMode)ob,dataOptions);
						} else {
							PlottableObject po = dataOptions.getPlottableObject();
							NDimensions nd = po.getNDimensions();
							nd.setOptions(((IPlotMode)ob).getOptions());
							
							dataOptions.setPlottableObject(new PlottableObject((IPlotMode)ob, nd));
						}
						
						
						
						table.setInput(dataOptions.getPlottableObject().getNDimensions());
						if (((IPlotMode)ob).supportsMultiple()) {
							table.setMaxSliceNumber(50);
						} else {
							table.setMaxSliceNumber(1);
						}
						viewer.refresh();
					}
				}
			}
		});

		FormData tableForm = new FormData();
		tableForm.top = new FormAttachment(plotTypeComposite);
		tableForm.left = new FormAttachment(0,0);
		tableForm.right = new FormAttachment(100,0);
		tableForm.bottom = new FormAttachment(100,0);

		lower.setLayoutData(tableForm);

		selectionListener = new ISelectionListener() {

			@Override
			public void selectionChanged(MPart part, Object selection) {
				if (selection instanceof ISelection) {
					List<LoadedFile> files = DataVisSelectionUtils.getFromSelection((ISelection)selection, LoadedFile.class);

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
				}
				
				plotController.forceReplot();
			}
		};

		Object selection = selectionService.getSelection(LOADED_FILE_PART_ID);
		if (selection instanceof ISelection) {
			List<LoadedFile> files = DataVisSelectionUtils.getFromSelection((ISelection)selection, LoadedFile.class);

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
				
				List<DataOptions> s = DataVisSelectionUtils.getFromSelection(viewer.getStructuredSelection(), DataOptions.class);

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
			List<DataOptions> d = DataVisSelectionUtils.getFromSelection(viewer.getStructuredSelection(), DataOptions.class);
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
	}

	private void updateOnSelectionChange(DataOptions op){
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

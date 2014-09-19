/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.algorithm.ui.datareduction;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.util.GridUtils;
import org.dawnsci.algorithm.ui.Activator;
import org.dawnsci.algorithm.ui.views.runner.AbstractAlgorithmProcessPage;
import org.dawnsci.algorithm.ui.views.runner.IAlgorithmProcessContext;
import org.dawnsci.algorithm.ui.workflow.IWorkflowUpdater;
import org.dawnsci.algorithm.ui.workflow.WorkflowUpdaterCreator;
import org.dawnsci.plotting.util.PlottingUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataReductionFileSelectionPage extends AbstractAlgorithmProcessPage {

	private static final Logger logger = LoggerFactory.getLogger(DataReductionFileSelectionPage.class);

	private static final String MOML_FILE = "workflows/2D_DataReductionV2.moml";
	// temporary used as a moml file until the one above is put in another plugin with this class
	private static final String INPUT_ACTOR = "Image to process";

	private Composite mainComposite;
	private Composite recapComp;

	private IDataset image;
	private TableViewer viewer;
	private List<SelectedData> rows;

	Map<String, String> dataFilePaths = new HashMap<String, String>(5);

	private final String[] keys = new String[]{"AFilename", "Detector_response_file", "Calibration_file", "Background_file", "Mask_file"};
	private final String[] titles = new String[] {"Data", "Detector Response (Divide)", "Detector Calibration", "Background (Subtract)", "Mask"};
	private final String[] types = new String[] {"Data", "Detector", "Calibration", "Background", "Mask"};
	private String[] filePaths = new String[5];
	private boolean[] locks = new boolean[5];
	private List<IPlottingSystem> plottingSystems = new ArrayList<IPlottingSystem>(); 

	public DataReductionFileSelectionPage() {
		try {
			IPlottingSystem dataPlot = PlottingFactory.createPlottingSystem();
			plottingSystems.add(dataPlot);
			IPlottingSystem detectorPlot = PlottingFactory.createPlottingSystem();
			plottingSystems.add(detectorPlot);
			IPlottingSystem calibrationPlot = PlottingFactory.createPlottingSystem();
			plottingSystems.add(calibrationPlot);
			IPlottingSystem backgroundPlot = PlottingFactory.createPlottingSystem();
			plottingSystems.add(backgroundPlot);
			IPlottingSystem maskPlot = PlottingFactory.createPlottingSystem();
			plottingSystems.add(maskPlot);

			for (int i = 0; i < keys.length; i++) {
				dataFilePaths.put(keys[i], "");
			}
		} catch (Exception e) {
			logger.error("Error creating plottingSystems:", e);
		}
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		
		if (memento != null) {
			for(int i = 0; i< keys.length; i++){
				filePaths[i] = memento.getString(keys[i]);
				locks[i] = memento.getBoolean(keys[i]+"Lock");
			}
		}
	}

	@Override
	public void saveState(IMemento memento) {
		if (memento != null) {
			for(int i=0; i < keys.length; i++){
				TableItem item = ((TableItem)viewer.getTable().getItem(i));
				SelectedData data = (SelectedData)item.getData();
				memento.putString(keys[i], data.getPath());
				memento.putBoolean(keys[i]+"Lock", data.isLocked());
			}
		}
	}

	@Override
	public Composite createPartControl(Composite parent) {

		createPersistenceActions(algorithmViewPart.getViewSite());

		mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(new GridLayout(1, true));
		GridUtils.removeMargins(mainComposite);

		SashForm mainSash = new SashForm(mainComposite, SWT.HORIZONTAL);
		mainSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		SashForm leftSash = new SashForm(mainSash, SWT.VERTICAL);
		leftSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		SashForm middleSash = new SashForm(mainSash, SWT.VERTICAL);
		middleSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		SashForm rightSash = new SashForm(mainSash, SWT.VERTICAL);
		rightSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Composite to put the main controls in
		Composite mainRecapComp = new Composite(leftSash, SWT.BORDER);
		mainRecapComp.setLayout(new GridLayout(1, false));
		mainRecapComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));

		Label helpLabel = new Label(mainRecapComp, SWT.WRAP);
		helpLabel.setText("Select a file in the Project Explorer and lock the type. " +
				"Make sure that all your selected data has the same shape.");
		helpLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));

		recapComp = new Composite(mainRecapComp, SWT.NONE);
		recapComp.setLayout(new GridLayout(1, false));
		recapComp.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true));

		viewer = new TableViewer(recapComp, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		createColumns(viewer);
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		algorithmViewPart.getSite().setSelectionProvider(viewer);
		rows = createSelectedDataRows();
		viewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// TODO Auto-generated method stub
			}
			@Override
			public void dispose() {
				// TODO Auto-generated method stub
			}
			@Override
			public Object[] getElements(Object inputElement) {
				return rows.toArray(new SelectedData[rows.size()]);
			}
		});
		viewer.setInput(rows);

		// add the Run workflow action as a button
		ActionContributionItem aci = (ActionContributionItem)algorithmViewPart.getViewSite().getActionBars().getToolBarManager().find(IAlgorithmProcessContext.RUN_ID_STUB+getTitle());
		aci = new ActionContributionItem(aci.getAction());
		aci.fill(mainRecapComp);
		Button runWorkflowButton = (Button) aci.getWidget();
		runWorkflowButton.setText("Run Data Reduction Process");
		runWorkflowButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));

		plottingSystems.get(1).createPlotPart(leftSash, types[1], null, PlotType.IMAGE, algorithmViewPart.getSite().getPart());
		plottingSystems.get(1).setTitle(titles[1]);

		plottingSystems.get(0).createPlotPart(middleSash, types[0], null, PlotType.IMAGE, algorithmViewPart.getSite().getPart());
		plottingSystems.get(0).setTitle(titles[0]);

		plottingSystems.get(3).createPlotPart(middleSash, types[3], null, PlotType.IMAGE, algorithmViewPart.getSite().getPart());
		plottingSystems.get(3).setTitle(titles[3]);

		plottingSystems.get(2).createPlotPart(rightSash, types[2], null, PlotType.IMAGE, algorithmViewPart.getSite().getPart());
		plottingSystems.get(2).setTitle(titles[2]);

		plottingSystems.get(4).createPlotPart(rightSash, types[4], null, PlotType.IMAGE, algorithmViewPart.getSite().getPart());
		plottingSystems.get(4).setTitle(titles[4]);

		algorithmViewPart.getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(fileSelectionListener);

		// try to load the previous data saved in the memento
		for (int i = 0; i < keys.length; i++) {
			if(filePaths[i] != null)
				loadAndPlotData(plottingSystems.get(i), keys[i], filePaths[i], titles[i], true, locks[i], i);
		}
		
		viewer.refresh();

		return mainComposite;
	}

	private void loadAndPlotData(final IPlottingSystem plottingSystem, 
								 String filePathKey,
								 String filePath,
								 final String fileTitle,
								 boolean loadFromMemento,
								 boolean isLocked,
								 final int index){
		
			final IDataset image = PlottingUtils.loadData(filePath, null);
			if(index == 1){
				// if data is locked
				if(((SelectedData)viewer.getElementAt(0)).isLocked()){
					IDataset dataImage = ((SelectedData)viewer.getElementAt(0)).getImage();
					final Dataset aDataImage = (Dataset)dataImage;
					Job divide = new Job("Running Divide process") {
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							Dataset divideResult = Maths.divide(aDataImage, (Dataset)image);
							PlottingUtils.plotData(plottingSystem, fileTitle, (IDataset)divideResult);
							return Status.OK_STATUS;
						}
					};
					divide.schedule();
				} else {
					PlottingUtils.plotData(plottingSystem, fileTitle, image);
				}
			} else if (index == 3){
				// if data is locked
				if(((SelectedData)viewer.getElementAt(0)).isLocked()){
					IDataset dataImage = ((SelectedData)viewer.getElementAt(0)).getImage();
					final Dataset aDataImage = (Dataset)dataImage;
					Job subtract = new Job("Running Subtract process") {
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							Dataset subtractResult = Maths.subtract(aDataImage, (Dataset)image);
							PlottingUtils.plotData(plottingSystem, fileTitle, (IDataset)subtractResult);
							return Status.OK_STATUS;
						}
					};
					subtract.schedule();
				} else {
					PlottingUtils.plotData(plottingSystem, fileTitle, image);
				}
			} else {
				PlottingUtils.plotData(plottingSystem, fileTitle, image);
			}
			((SelectedData)viewer.getElementAt(index)).setImage(image);
			((SelectedData)viewer.getElementAt(index)).setShape(image.getShape());
			String filename = filePath.substring(filePath.lastIndexOf(File.separator));
			((SelectedData)viewer.getElementAt(index)).setFileName(filename);
			((SelectedData)viewer.getElementAt(index)).setPath(filePath);
			if(loadFromMemento)
				((SelectedData)viewer.getElementAt(index)).setLocked(isLocked);
			dataFilePaths.put(filePathKey, filePath);
	}

	private void createPersistenceActions(IViewSite iViewSite) {
		final Action saveAction = new Action("Save selected data to persistence file", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				//TODO
				try {
//					IWizard wiz = EclipseUtils.openWizard(PersistenceSavingWizard.ID, false);
//					WizardDialog wd = new  WizardDialog(Display.getCurrent().getActiveShell(), wiz);
//					wd.setTitle(wiz.getWindowTitle());
//					wd.open();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error("Error saving file:"+e);
				}
			}
		};
		saveAction.setToolTipText("Save selected data to persistence file");
		saveAction.setText("Save");
		saveAction.setImageDescriptor(Activator.getImageDescriptor("icons/save.png"));

		final Action loadAction = new Action("Load data from persistence file", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				//TODO
			}
		};
		loadAction.setToolTipText("Load data from persistence file");
		loadAction.setText("Load");
		loadAction.setImageDescriptor(Activator.getImageDescriptor("icons/load.png"));

		IToolBarManager toolMan = iViewSite.getActionBars().getToolBarManager();
		MenuManager menuMan = new MenuManager();
		toolMan.add(new Separator());
		toolMan.add(loadAction);
		menuMan.add(loadAction);
		toolMan.add(saveAction);
		toolMan.add(new Separator());
		menuMan.add(saveAction);
	}

	protected void createColumns(final TableViewer viewer) {
		
		ColumnViewerToolTipSupport.enableFor(viewer,ToolTip.NO_RECREATE);

		TableViewerColumn var = new TableViewerColumn(viewer, SWT.LEFT, 0);
		var.getColumn().setText("Lock");
		var.getColumn().setWidth(50);
		var.setLabelProvider(new SelectedDataLabelProvider(0));
		SelectedDataEditingSupport regionEditor = new SelectedDataEditingSupport(viewer, 0);
		var.setEditingSupport(regionEditor);

		var = new TableViewerColumn(viewer, SWT.LEFT, 1);
		var.getColumn().setText("Type");
		var.getColumn().setWidth(100);
		var.setLabelProvider(new SelectedDataLabelProvider(1));
		regionEditor = new SelectedDataEditingSupport(viewer, 1);
		var.setEditingSupport(regionEditor);

		var = new TableViewerColumn(viewer, SWT.LEFT, 2);
		var.getColumn().setText("File name");
		var.getColumn().setWidth(200);
		var.setLabelProvider(new SelectedDataLabelProvider(2));
		regionEditor = new SelectedDataEditingSupport(viewer, 2);
		var.setEditingSupport(regionEditor);

		var = new TableViewerColumn(viewer, SWT.CENTER, 3);
		var.getColumn().setText("Shape");
		var.getColumn().setWidth(80);
		var.setLabelProvider(new SelectedDataLabelProvider(3));
		regionEditor = new SelectedDataEditingSupport(viewer, 3);
		var.setEditingSupport(regionEditor);

	}

	private ISelectionListener fileSelectionListener = new ISelectionListener() {

		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structSelection = (IStructuredSelection)selection;
				image = PlottingUtils.loadData(structSelection);
				if(image == null) return;

				for(int i = 0; i< keys.length; i++){
					if (!((SelectedData)viewer.getElementAt(i)).isLocked()){
						String path = PlottingUtils.getFullFilePath(structSelection);
						loadAndPlotData(plottingSystems.get(i), keys[i], path, titles[i], false, false, i);
					}
				}
				viewer.refresh();
			}
		}
	};

	private List<SelectedData> createSelectedDataRows(){
		final List<SelectedData> selectedDataList = new ArrayList<SelectedData>(5);
		for(int i = 0; i < types.length; i++){
			SelectedData data = new SelectedData(types[i], new int[]{0, 0}, "-", false);
			selectedDataList.add(data);
		}
		return selectedDataList;
	}

	private String formatIntArray(int[] array){
		String str = "";
		for (int i = 0; i < array.length; i++) {
			str += String.valueOf(array[i]);
			if(i != array.length-1)
				str += ", ";
		}
		return str;
	}

	@Override
	public String getTitle() {
		return "Data Reduction";
	}

	@Override
	public void run(final IAlgorithmProcessContext context) throws Exception {
		//For now take programmatically this workflow file from uk.ac.diamond.analysis.rcp
		//should be replaced by the resource path provided in the extension point
		//TODO: make a new Diamond plugin in which to put the workflow and this datareduction page view.
		Bundle bundle = Platform.getBundle("uk.ac.diamond.scisoft.analysis.rcp");
		Path path = new Path(MOML_FILE);
		URL url = FileLocator.find(bundle, path, null);
		final String momlPath = FileLocator.toFileURL(url).getPath(); 

		IWorkflowUpdater updater = WorkflowUpdaterCreator.createWorkflowUpdater(momlPath);
		updater.updateInputActor(INPUT_ACTOR, "User Fields", dataFilePaths);
		
		final Job run = new Job("Execute Data Reduction") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					monitor.beginTask("Execute "+momlPath, 2);
					context.execute(momlPath, true, monitor);
					
				} catch (Exception e) {
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		run.schedule();
	}

	@Override
	public ISourceProvider[] getSourceProviders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dispose() {
		algorithmViewPart.getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(fileSelectionListener);
	}

	/**
	 * Check that all the locked data have the same shape
	 * @return
	 *       true if all the same shape, false otherwise
	 */
	private boolean isRunWorkflowEnabled(){
		Object input = viewer.getInput();
		List<?> model = (input instanceof List)? (List<?>) input : null;
		if(model == null) return false;
		int[] previousArray = new int[]{0, 0};
		for (int i = 0; i< model.size(); i ++) {
			if(model.get(i) instanceof SelectedData){
				SelectedData data = (SelectedData)model.get(i);
				if(data.isLocked()){
					if(i != 0 && !Arrays.equals(data.getShape(), previousArray))
						return false;
					previousArray = data.getShape();
				}
			}
		}
		return true;
	}

	/**
	 * Data bean to fill the Table viewer
	 *
	 */
	private class SelectedData {

		private IDataset image;
		private int[] shape;
		private boolean isLocked;
		private String fileName;
		private String path;
		private String type;

		public SelectedData(String type, int[] shape, String fileName, boolean isLocked) {
			this.type = type;
			this.shape = shape;
			this.fileName = fileName;
			this.isLocked = isLocked;
		}

		public void setImage(IDataset image){
			this.image = image;
		}

		public IDataset getImage(){
			return image;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getPath(){
			return path;
		}

		public void setShape(int[] shape){
			this.shape = shape;
		}

		public int[] getShape(){
			return shape;
		}

		public boolean isLocked() {
			return isLocked;
		}

		public void setLocked(boolean isLocked) {
			this.isLocked = isLocked;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
	}

	/**
	 * EditingSupport Class
	 *
	 */
	private class SelectedDataEditingSupport extends EditingSupport {

		private int column;

		public SelectedDataEditingSupport(ColumnViewer viewer, int col) {
			super(viewer);
			this.column = col;
		}
		@Override
		protected CellEditor getCellEditor(final Object element) {
			CellEditor ed = null;
			
			if(column == 0){
				ed = new CheckboxCellEditor(((TableViewer)getViewer()).getTable(), SWT.RIGHT);
				return ed;
			} else if(column > 0){
				ed = new TextCellEditor(((TableViewer)getViewer()).getTable(), SWT.RIGHT);
				return ed;
			} 
			return null;
			
		}

		@Override
		protected boolean canEdit(Object element) {
			if (column == 0) return true;
			else return false;
		}

		@Override
		protected Object getValue(Object element) {
			final SelectedData myImage = (SelectedData)element;
			switch (column){
			case 0:
				return myImage.isLocked();
			case 1:
				return myImage.getType();
			case 2:
				return myImage.getFileName();
			case 3:
				return formatIntArray(myImage.getShape());
			default:
				return null;
			}
			
		}

		@Override
		protected void setValue(Object element, Object value) {
			try {
				this.setValue(element, value, true);
			} catch (Exception e) {
				logger.debug("Error while setting table value");
				e.printStackTrace();
			}
		}
		
		private void setValue(Object element, Object value, boolean tableRefresh) throws Exception {

			final SelectedData myImage = (SelectedData) element;
			switch (column){
			case 0:
				myImage.setLocked((Boolean)value);
				ActionContributionItem item = (ActionContributionItem)algorithmViewPart.getViewSite().getActionBars().getToolBarManager().find(IAlgorithmProcessContext.RUN_ID_STUB+getTitle());
				item.getAction().setEnabled(isRunWorkflowEnabled());
				break;
			case 1:
				myImage.setType((String)value);
				break;
			case 2:
				myImage.setFileName((String)value);
				break;
			case 3:
				myImage.setShape((int[])value);
				break;
			default:
				break;
			}

			if (tableRefresh) {
				getViewer().refresh();
			}
		}
	}

	/**
	 * Table viewer Label provider
	 *
	 */
	private class SelectedDataLabelProvider extends ColumnLabelProvider {
		
		private int column;
		private Image lockedIcon;
		private Image unlockedIcon;

		public SelectedDataLabelProvider(int column) {
			this.column = column;
			ImageDescriptor id = Activator.getImageDescriptor("icons/lock_small.png");
			lockedIcon   = id.createImage();
			id = Activator.getImageDescriptor("icons/lock_open_small.png");
			unlockedIcon =  id.createImage();
		}

		@Override
		public Image getImage(Object element){
			
			if (!(element instanceof SelectedData)) return null;
			if (column==0){
				final SelectedData selectedData = (SelectedData)element;
				return selectedData.isLocked() ? lockedIcon : unlockedIcon;
			}
			return null;
		}

		@Override
		public String getText(Object element) {
			
			if (!(element instanceof SelectedData)) return null;
			final SelectedData    selectedData = (SelectedData)element;

			switch (column) {
			case 1:
				return selectedData.getType();
			case 2:
				return selectedData.getFileName();
			case 3:
				return formatIntArray(selectedData.getShape());
			default:
				return "";
			}
		}

		public String getToolTipText(Object element) {
			return "Click on the lock icon to select the data you want the workflow to be processed with.";
		}

		@Override
		public void dispose(){
			super.dispose();
			lockedIcon.dispose();
			unlockedIcon.dispose();
		}
	}

}


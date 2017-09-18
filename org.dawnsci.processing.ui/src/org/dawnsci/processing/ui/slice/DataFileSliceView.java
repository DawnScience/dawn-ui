/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.processing.ui.slice;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.dawb.common.services.ServiceManager;
import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawnsci.common.widgets.dialog.FileSelectionDialog;
import org.dawnsci.conversion.schemes.ProcessConversionScheme;
import org.dawnsci.processing.ui.Activator;
import org.dawnsci.processing.ui.ServiceHolder;
import org.dawnsci.processing.ui.preference.ProcessingConstants;
import org.dawnsci.processing.ui.processing.OperationDescriptor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.EventTracker;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.conversion.IProcessingConversionInfo;
import org.eclipse.dawnsci.analysis.api.conversion.ProcessingOutputType;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.processing.ExecutionType;
import org.eclipse.dawnsci.analysis.api.processing.IExecutionVisitor;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationInputData;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceInformation;
import org.eclipse.dawnsci.analysis.dataset.slicer.Slicer;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.image.IFileIconService;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.processing.visitor.NexusFileExecutionVisitor;

public class DataFileSliceView extends ViewPart {

	private FileManager fileManager;
	private TableViewer viewer;
	private UpdateJob job;
	private Label currentSliceLabel;
	private ChangeSliceWidget csw;
	private String selectedFile = null;
	private IOperation<? extends IOperationModel, ? extends OperationData> currentOperation = null;
	private IPlottingSystem<Composite> input;
	private IPlottingSystem<Composite> output;
	private IOperationInputData inputData = null;
	private ProcessingOutputType processingOutputType = ProcessingOutputType.PROCESSING_ONLY;
	
	String lastPath = null;
	
	private final static Logger logger = LoggerFactory.getLogger(DataFileSliceView.class);

	@Override
	public void createPartControl(Composite parent) {
		logger.info("Perspective Created: Processing");
		fileManager = new FileManager(new SetupContextHelper());
		
		parent.setLayout(new GridLayout());
		viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setContentProvider(new BasicContentProvider());
		viewer.setInput(fileManager);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof StructuredSelection) {
					inputData = null;
					EventAdmin eventAdmin = ServiceHolder.getEventAdmin();
					Map<String,IOperationInputData> props = new HashMap<>();
					props.put("data", inputData);
					eventAdmin.postEvent(new Event("org/dawnsci/events/processing/DATAUPDATE", props));
					selectedFile = (String)((StructuredSelection)event.getSelection()).getFirstElement();
					update(currentOperation);
				}
				
				
			}
		});
		ColumnViewerToolTipSupport.enableFor(viewer);
		
		DropTargetAdapter dropListener = new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				Object dropData = event.data;
				if (dropData instanceof TreeSelection) {
					TreeSelection selectedNode = (TreeSelection) dropData;
					Object obj[] = selectedNode.toArray();
					List<String> paths = new ArrayList<String>();
					for (int i = 0; i < obj.length; i++) {
						if (obj[i] instanceof IFile) {
							IFile file = (IFile) obj[i];
							paths.add(file.getLocation().toOSString());
						}
					}
					
					if (!paths.isEmpty()) fileManager.addFiles(paths.toArray(new String[paths.size()]));
					
				} else if (dropData instanceof String[]) {
					fileManager.addFiles((String[])dropData);
				}
				viewer.setInput(fileManager);
			}
		};
		
		DropTarget dt = new DropTarget(viewer.getControl(), DND.DROP_MOVE | DND.DROP_DEFAULT | DND.DROP_COPY);
		dt.setTransfer(new Transfer[] { TextTransfer.getInstance(),
				FileTransfer.getInstance(), ResourceTransfer.getInstance(),
				LocalSelectionTransfer.getTransfer() });
		dt.addDropListener(dropListener);
		
		csw = new ChangeSliceWidget(parent);
		csw.addSliceChangeListener(new ISliceChangeListener() {
			
			@Override
			public void sliceChanged(SliceChangeEvent event) {
				inputData = null;
				EventAdmin eventAdmin = ServiceHolder.getEventAdmin();
				Map<String,IOperationInputData> props = new HashMap<>();
				props.put("data", inputData);
				eventAdmin.postEvent(new Event("org/dawnsci/events/processing/DATAUPDATE", props));
				String ss = Slice.createString(csw.getCurrentSlice());
				currentSliceLabel.setText("Current slice of data: [" +ss + "]");
				currentSliceLabel.getParent().layout(true);
				update(currentOperation);
			}
		});
		
		csw.disable();
		
		final MenuManager rightClick = new MenuManager();
		createActions(rightClick);
		viewer.getControl().setMenu(rightClick.createContextMenu(viewer.getControl()));
		
		getSite().getPage().addSelectionListener("org.dawnsci.processing.ui.processingView",new ISelectionListener() {
			
			@Override
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				
				if (selection instanceof StructuredSelection && ((StructuredSelection)selection).getFirstElement() instanceof OperationDescriptor) {
					OperationDescriptor des = (OperationDescriptor)((StructuredSelection)selection).getFirstElement();
					try {
						
						currentOperation = des.getSeriesObject();
						update(currentOperation);
					} catch (InstantiationException e) {
						logger.error(e.getMessage(),e);
					}
				} else {
					if (getOperations() == null || getOperations().length == 0) {
						output.clear();
					}
				}
			}
		});
		
		currentSliceLabel = new Label(parent, SWT.WRAP);
		currentSliceLabel.setText("Current slice of data: [ - - - - -]");
		
		IWorkbenchPage page = getSite().getPage();
		IViewPart view = page.findView("org.dawnsci.processing.ui.output");
		output = (IPlottingSystem<Composite>)view.getAdapter(IPlottingSystem.class);
		
		view = page.findView("org.dawnsci.processing.ui.input");
		input = (IPlottingSystem<Composite>)view.getAdapter(IPlottingSystem.class);
		
		fileManager.addFileListener(new IFilesAddedListener() {

			@Override
			public void filesAdded(FileAddedEvent event) {
				
				String[] paths = event.getPaths();
				boolean[] success = event.getSuccess();
				
				int first = -1;
				final List<String> failedPaths = new ArrayList<String>();
				for (int i = 0; i<success.length;i++) {
					if (success[i] && first < 0) first = i;
					if (!success[i]) failedPaths.add(paths[i]);
				}
				
				String dsName = "";
				
				try {
					dsName = " ("+fileManager.getContext().getDatasetNames().get(0)+")";
				} catch (Exception e) {
					//ignore
				}
				
				final String dsn = dsName;
				
				final int f = first;
				
				Display.getDefault().syncExec(new Runnable() {
					
					@Override
					public void run() {
						if (f < 0) {
							MessageDialog.openError(getSite().getShell(), "Error loading files", "None of the selected files contained suitable datasets" + dsn+ "!");
							return;
						}
						
						if (!failedPaths.isEmpty()){
							StringBuilder sb = new StringBuilder();
							sb.append("Failed to load: ");
							for (String p : failedPaths) sb.append(p +", ");
							sb.append("did not contain suitable datasets");
							sb.append(dsn);
							
							MessageDialog.openError(getSite().getShell(), "Error loading some files", sb.toString());
						}
						
					}
				});
				
				if (f < 0) return;
				
 				final String path = paths[first];
 				
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						viewer.refresh();
						viewer.setSelection(new StructuredSelection(path),true);
					}
				});
			}
		});
		
		BundleContext ctx = FrameworkUtil.getBundle(DataFileSliceView.class).getBundleContext();
		EventHandler handler = new EventHandler() {
			
			@Override
			public void handleEvent(Event event) {
				update(currentOperation);
				
			}
		};
		
		Dictionary<String, String> props = new Hashtable<>();
		props.put(EventConstants.EVENT_TOPIC, "org/dawnsci/events/processing/PROCESSUPDATE");
		ctx.registerService(EventHandler.class, handler, props);
		
		//hook up delete key to remove from list
		viewer.getTable().addKeyListener(new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.DEL || e.keyCode == SWT.BS) {
					clearSelected();
				}
			}
		});

	}
	
	private void createActions(IContributionManager rightClick) {
		
		final IAction run = new Action("Process all files", Activator.getImageDescriptor("icons/run_workflow.gif")) {
			public void run() {
				IOperation<? extends IOperationModel, ? extends OperationData>[] ops = getOperations();

				if (ops != null) {
					final IOperation<? extends IOperationModel, ? extends OperationData>[] fop = ops;
					
					ExecutionType type = ExecutionType.PARALLEL;
					
					try {
						IPreferenceStore ps = Activator.getDefault().getPreferenceStore();
						
						if (ps.getBoolean(ProcessingConstants.FORCE_SERIES)) type = ExecutionType.SERIES;
					} catch (Exception e) {
						logger.error("Could not read preferences");
					}
					
					final ExecutionType finalType = type;

					fileManager.setProcessingConversionInfo(new IProcessingConversionInfo() {

						@Override
						public IOperation<? extends IOperationModel, ? extends OperationData>[] getOperationSeries() {
							return fop;
						}

						@Override
						public IExecutionVisitor getExecutionVisitor(String fileName) {
							return new NexusFileExecutionVisitor(fileName);
						}

						@Override
						public ProcessingOutputType getProcessingOutputType() {
							return processingOutputType;
						}

						@Override
						public ExecutionType getExecutionType() {
							return finalType;
						}

						@Override
						public int getPoolSize() {
							return -1;
						}

						@Override
						public boolean isTryParallel() {
							return true;
						}

					});
				}
				
				String filePath = fileManager.getFilePaths().get(0);
				boolean isHDF5 = false;
				try {
					Tree tree = ServiceHolder.getLoaderService().getData(filePath, null).getTree();
					isHDF5 = tree != null;
				} catch (Exception e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
				ExtendedFileSelectionDialog fsd = new ExtendedFileSelectionDialog(Display.getCurrent().getActiveShell(),isHDF5);
				if (lastPath == null) {
					final File source = new File(fileManager.getFilePaths().get(0));
					lastPath  = source.getParent();
				}
				
				fsd.setPath(lastPath);
				fsd.create();
				if (fsd.open() == Dialog.CANCEL) return;
				lastPath = fsd.getPath();
				
				File f = new File(lastPath);
				if (!f.canWrite()) {
					MessageBox dialog = 
							  new MessageBox(getViewSite().getShell(), SWT.ICON_ERROR | SWT.OK);
					dialog.setText("File save error!");
					dialog.setMessage("Could not save calibration file! (Do you have write access to this directory?)");

					dialog.open();
					return;
				}
				
				fileManager.setOutputPath(fsd.getPath());
			
				ProgressMonitorDialog dia = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());

				try {
					dia.run(true, true, new IRunnableWithProgress() {

						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException,
						InterruptedException {
							monitor.beginTask("Processing", getAmountOfWork(fileManager.getContext()));
							fileManager.getContext().setMonitor(new ProgressMonitorWrapper(monitor));
							try {
								ServiceHolder.getConversionService().process(fileManager.getContext());
							} catch (final Exception e) {
								Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										MessageDialog.openError(DataFileSliceView.this.getViewSite().getShell(), "Error processing files!", e.getMessage());
									}
								});
								
								logger.error(e.getMessage(), e);
							}
						}
					});
				} catch (InvocationTargetException e1) {
					logger.error(e1.getMessage(), e1);
				} catch (InterruptedException e1) {
					logger.error(e1.getMessage(), e1);
				}
				try {
					// track the run button in processing
					EventTracker tracker = ServiceHolder.getEventTrackerService();
					if (tracker != null)
						tracker.trackActionEvent("Processing_Run");
				} catch (Exception e) {
					logger.debug("Could not track event",e);
				}
			}
		};
		getViewSite().getActionBars().getToolBarManager().add(run);
		getViewSite().getActionBars().getMenuManager().add(run);
		rightClick.add(run);
		
		final IAction clear = new Action("Clear selected file", Activator.getImageDescriptor("icons/delete.gif")) {
			public void run() {
				clearSelected();
			}
		};
		
		final IAction clearAll = new Action("Clear all files", Activator.getImageDescriptor("icons/deleteAll.gif")) {
			public void run() {

				fileManager.clear();
				csw.disable();
				
				EventAdmin eventAdmin = ServiceHolder.getEventAdmin();
				Map<String,IDataset> props = new HashMap<>();
				props.put("data", null);
				eventAdmin.postEvent(new Event("org/dawnsci/events/processing/INITIALUPDATE", props));
				
				job = null;
				currentSliceLabel.setText("Current slice of data: [ - - - - -]");
				try {
					input.reset();
					output.reset();
					
				} catch (Exception e1) {
					logger.error("Could not clear plotting systems");
				}
				
				viewer.refresh();
			}
		};
		
		final IAction edit = new Action("Edit slice configuration", Activator.getImageDescriptor("icons/book-brown-setting.png")) {
			public void run() {
				fileManager.setUpContext();
			}
		};

		getViewSite().getActionBars().getToolBarManager().add(clear);
		getViewSite().getActionBars().getMenuManager().add(clear);
		rightClick.add(clear);
		
		getViewSite().getActionBars().getToolBarManager().add(clearAll);
		getViewSite().getActionBars().getMenuManager().add(clearAll);
		rightClick.add(clearAll);
		
		getViewSite().getActionBars().getToolBarManager().add(edit);
		getViewSite().getActionBars().getMenuManager().add(edit);
		rightClick.add(edit);
		
		getViewSite().getActionBars().getToolBarManager().add(new Separator());
		getViewSite().getActionBars().getMenuManager().add(new Separator());
		rightClick.add(new Separator());

	}
	
	private void clearSelected() {
		fileManager.getFilePaths().remove(selectedFile);
		
		if (fileManager.getFilePaths().isEmpty()) {
			fileManager.clear();
			csw.disable();
			currentSliceLabel.setText("Current slice of data: [ - - - - -]");
			EventAdmin eventAdmin = ServiceHolder.getEventAdmin();
			Map<String,IDataset> props = new HashMap<>();
			props.put("data", null);
			eventAdmin.postEvent(new Event("org/dawnsci/events/processing/INITIALUPDATE", props));
		} else {
			viewer.setSelection(new StructuredSelection(fileManager.getFilePaths().get(0)),true);
		}
		
		
		job = null;
		
		try {
			input.reset();
			output.reset();
			
		} catch (Exception e1) {
			logger.error("Could not clear plotting systems");
		}
		
		viewer.refresh();
	}
	
	private void update(final IOperation<? extends IOperationModel, ? extends OperationData> end) {
	
			if (fileManager.getContext() == null) return;
			
			if (job == null) {
				job = new UpdateJob(fileManager.getContext());
			} else {
				job.cancel();
				
			}
			job.setPath(selectedFile); 
			job.setEndOperation(end);
			job.schedule();
		
	}
	
	private void updateSliceWidget(String path) {
		try {
			IDataHolder dh = ServiceHolder.getLoaderService().getData(path, new IMonitor.Stub());
			ILazyDataset lazy = dh.getLazyDataset(fileManager.getContext().getDatasetNames().get(0));
			int[] shape = lazy.getShape();
			
			int[] dd = Slicer.getDataDimensions(shape, fileManager.getContext().getSliceDimensions());
			Slice[] slices = Slicer.getSliceArrayFromSliceDimensions(fileManager.getContext().getSliceDimensions(), shape);
			csw.setDatasetShapeInformation(shape, dd.clone(), slices);
			String ss = Slice.createString(csw.getCurrentSlice());
			currentSliceLabel.setText("Current slice of data: [" +ss + "]");
			currentSliceLabel.getParent().layout(true);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
	}
	
	private IOperation<? extends IOperationModel, ? extends OperationData>[] getOperations() {
		IViewPart view = getSite().getPage().findView("org.dawnsci.processing.ui.processingView");
		
		Object ob = view.getAdapter(IOperation.class);
		IOperation<? extends IOperationModel, ? extends OperationData>[] ops = null;
		
		if (ob == null) return null;
		
		if (ob.getClass().isArray() && Array.get(ob, 0) instanceof IOperation) {
			ops = (IOperation[])ob;
		}
		
		return ops;
	}

	private int getAmountOfWork(IConversionContext context) {
		int c = 0;
		String name = context.getDatasetNames().get(0);
		int[] dd = null;
		Map<Integer, String> sliceDimensions = context.getSliceDimensions();
		
		
		for (String path : context.getFilePaths()) {
			try {
				IMetadata metadata = ServiceHolder.getLoaderService().getMetadata(path, null);
				int[] s = metadata.getDataShapes().get(name);
				if (s == null) {
					try {
						s = ServiceHolder.getLoaderService().getData(path, null).getLazyDataset(name).getShape();
					} catch (Exception e) {
						logger.warn("Can't get shape to calculate work from");
					}
					
				}
				
				Slice[] slices = Slicer.getSliceArrayFromSliceDimensions(sliceDimensions, s);
				SliceND slice = new SliceND(s, slices);

				if (dd == null) {
					dd = Slicer.getDataDimensions(s, context.getSliceDimensions());
					Arrays.sort(dd);
				}
				c += getWork(slice,dd);
				
			} catch (Exception e) {
				logger.warn("cannot load metadata for {}, assuming one frame", path);
				c++;
			}
		}
		return c;
	}
	
	public static int getWork(SliceND slice, int[] dataDims) {
		int c = 0;
		int[] dd = dataDims.clone();
		Arrays.sort(dd);
		
		int[] nShape = slice.getShape();

		 int n = 1;
		 for (int i = 0; i < nShape.length; i++) {
			 if (Arrays.binarySearch(dd, i) < 0) n *= nShape[i];
		 }
		
		c += n;
		
		return c;
	}
	
	@Override
	public void setFocus() {
		if (viewer != null) viewer.getTable().setFocus();

	}
	
	private class UpdateJob extends Job {

		IConversionContext context;
		IOperation<? extends IOperationModel, ? extends OperationData> end;
		String path = null;
		
		public UpdateJob(IConversionContext context) {
			super("Update...");
			this.context = context;
		}
		
		public void setEndOperation(IOperation<? extends IOperationModel, ? extends OperationData> end) {
			this.end = end;
		}
		
		public void setPath(String path) {
			
			if (path == null) return;
			
			if (!path.equals(this.path)) {
			
				updateSliceWidget(path);
				this.path = path;
			}
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			output.setEnabled(false);
			EscapableSliceVisitor sliceVisitor = null;
			try {
				
				if (path == null) path = context.getFilePaths().get(0);
				
				final IDataHolder   dh = ServiceHolder.getLoaderService().getData(path, new IMonitor.Stub());
				ILazyDataset lazyDataset = dh.getLazyDataset(context.getDatasetNames().get(0));
				
				if (lazyDataset == null) {
					logger.error("Selected dataset not in file!!!!");
					return Status.CANCEL_STATUS;
				}
				//take a local view
				lazyDataset = lazyDataset.getSliceView();
				
				Map<Integer, String> axesNames = context.getAxesNames();
				
				if (axesNames != null) {
					AxesMetadata am = ServiceHolder.getLoaderService().getAxesMetadata(lazyDataset, path, axesNames, true);
					lazyDataset.setMetadata(am);
//					AxesMetadata axMeta = SlicedDataUtils.createAxisMetadata(path, lazyDataset, axesNames);
//					if (axMeta != null) lazyDataset.setMetadata(axMeta);
//					else lazyDataset.clearMetadata(AxesMetadata.class);
				}
				
				
				int[] dataDims = Slicer.getDataDimensions(lazyDataset.getShape(), context.getSliceDimensions());
				Slice[] s = csw.getCurrentSlice();
				//Plot input, probably a bit wasteful to do each time
				IDataset firstSlice = null;
				if (lazyDataset instanceof IDataset) {
					firstSlice = ((IDataset)lazyDataset).getSliceView(s);
				} else {
					firstSlice = lazyDataset.getSlice(s);
				}
				EventAdmin eventAdmin = ServiceHolder.getEventAdmin();
				Map<String,IDataset> props = new HashMap<>();
				props.put("data", firstSlice.getSliceView().squeeze());
				eventAdmin.postEvent(new Event("org/dawnsci/events/processing/INITIALUPDATE", props));
				MetadataPlotUtils.plotDataWithMetadata(firstSlice, input);
				
				
				IOperation<? extends IOperationModel, ? extends OperationData>[] ops = getOperations();
				if (ops == null) {
					output.clear();
					return Status.OK_STATUS;
				}
				//Only run what is necessary
				if (inputData != null) {
					
					if (inputData.getCurrentOperations().get(0) != end) {
						int pos = 0;
						for (int i = 0; i< ops.length; i++) {
							if (ops[i] == end) break;
							if (ops[i] == inputData.getCurrentOperations().get(0)) {
								pos = i;
								pos++;
								break;
							}
						}
						
						if (pos != 0){
							firstSlice = inputData.getInputData();
							ops = Arrays.copyOfRange(ops, pos-1, ops.length);
						}
						
					} else {
						firstSlice = inputData.getInputData();
						ops = new IOperation[]{inputData.getCurrentOperations().get(0)};
					}
					
				}
				
				SourceInformation si = new SourceInformation(path, context.getDatasetNames().get(0), lazyDataset);
				SliceInformation sli = csw.getCurrentSliceInformation();
				//TODO replace with check shape
				firstSlice.setMetadata(new SliceFromSeriesMetadata(si,sli));
				
//				OriginMetadataImpl om = new OriginMetadataImpl(lazyDataset, viewSlice, dataDims, path, context.getDatasetNames().get(0));
//				om.setCurrentSlice(csw.getCurrentSlice());

//				lazyDataset.setMetadata(om);

				sliceVisitor = getSliceVisitor(ops, lazyDataset, Slicer.getDataDimensions(lazyDataset.getShape(), context.getSliceDimensions()),monitor);
				sliceVisitor.setEndOperation(end);
				long start = System.currentTimeMillis();
				sliceVisitor.visit(firstSlice);
				inputData = sliceVisitor.getOperationInputData();
				Map<String,IOperationInputData> propsOID = new HashMap<>();
				propsOID.put("data", inputData);
				eventAdmin.postEvent(new Event("org/dawnsci/events/processing/DATAUPDATE", propsOID));
				logger.debug("Ran in: " +(System.currentTimeMillis()-start)/1000. + " s");
				Map<String,OperationException> propsE = new HashMap<>();
				propsE.put("error", null);
				eventAdmin.postEvent(new Event("org/dawnsci/events/processing/ERROR", propsE));

				
				
				} catch (OperationException e) {
					logger.info(e.getMessage(), e);
					EventAdmin eventAdmin = ServiceHolder.getEventAdmin();
					Map<String,OperationException> props = new HashMap<>();
					props.put("error", e);
					eventAdmin.postEvent(new Event("org/dawnsci/events/processing/ERROR", props));
					return Status.CANCEL_STATUS;
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					String message = e.getMessage();
					if (message == null) message = "Unexpected error!";
					EventAdmin eventAdmin = ServiceHolder.getEventAdmin();
					Map<String,OperationException> props = new HashMap<>();
					props.put("error", new OperationException(null, message));
					eventAdmin.postEvent(new Event("org/dawnsci/events/processing/ERROR", props));
					return Status.CANCEL_STATUS;
				} finally {
					if (sliceVisitor != null) {
						EventAdmin eventAdmin = ServiceHolder.getEventAdmin();
						inputData = sliceVisitor.getOperationInputData();
						Map<String,IOperationInputData> propsOID = new HashMap<>();
						propsOID.put("data", inputData);
						eventAdmin.postEvent(new Event("org/dawnsci/events/processing/DATAUPDATE", propsOID));

					}
					output.setEnabled(true);
				}
				
				return Status.OK_STATUS;
		}
		
	}
	
	private EscapableSliceVisitor getSliceVisitor(IOperation<? extends IOperationModel, ? extends OperationData>[] series,ILazyDataset lz,  
            int[] dataDims, IProgressMonitor mon) {
		return new EscapableSliceVisitor(lz,dataDims,series,getOperations(),mon,fileManager.getContext(),output);
	}
	
	@Override
	public Object getAdapter(final Class clazz) {
		if (clazz == FileManager.class) return fileManager;
		return super.getAdapter(clazz);
	}
	
	
	
	private class BasicContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof FileManager) {
				if (((FileManager)inputElement).getFilePaths() == null) return new String[0];
				return ((FileManager)inputElement).getFilePaths().toArray();
			}
				
			
			return new String[0];
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	private class ViewLabelProvider extends ColumnLabelProvider {
	
		private IFileIconService service;
		ViewLabelProvider() {
		    try {
				this.service = (IFileIconService)ServiceManager.getService(IFileIconService.class);
			} catch (Exception e) {
				// Ignored, we just have no icon then
			}
		}

		@Override
		public String getText(Object obj) {
			
			if (obj instanceof String) {
				File f = new File((String)obj);
				if (f.isFile()) return f.getName();
			}
			
			return "";
		}
		
		@Override
		public Image getImage(Object obj) {

			try {
				if (obj instanceof String) {
					return service.getIconForFile((String)obj);
				}
			} catch (Exception ignored) {
				// Not end of world if no icon!
			}
			return null;
		}

		
		@Override
		public String getToolTipText(Object obj) {
			return obj.toString();
		}
		
	}
	
	private class SetupContextHelper implements ISetupContext {

		@Override
		public IConversionContext init(String path) {
			
			IConversionContext context = ServiceHolder.getConversionService().open(path);
			
			return setupwizard(context);
		}
		
		@Override
		public boolean setup(IConversionContext context) {
			
			if (context.getSliceDimensions() != null) {
				context.getSliceDimensions().clear();
			}

			IConversionContext c = setupwizard(context);

			if (c != null) {
				job = null;
				viewer.setSelection(new StructuredSelection(context.getFilePaths().get(0)),true);
				return true;
			}
			
			return false;
		}
		
		private IConversionContext setupwizard(IConversionContext context) {
//			final SetupDimensionsWizardPage convertPage = new SetupDimensionsWizardPage(context);
			final SetUpProcessWizardPage convertPage = new SetUpProcessWizardPage(context);
			
			Wizard wiz = new Wizard() {
				//set 
				@Override
				public boolean performFinish() {
					convertPage.populateContext();
					return true;
				}
			};

			wiz.setNeedsProgressMonitor(true);
			wiz.addPage(convertPage);
			final WizardDialog wd = new WizardDialog(getSite().getShell(),wiz);
			wd.setPageSize(new Point(900, 500));
			wd.create();
			context.setConversionScheme(new ProcessConversionScheme());

			if (wd.open() == WizardDialog.OK) return context;
			
			return null;
		}
		
	}
	
	private class ExtendedFileSelectionDialog extends FileSelectionDialog {
		
		private boolean isHDF5 = false;
		
		public ExtendedFileSelectionDialog(Shell parentShell, boolean isH5) {
			super(parentShell);
			this.isHDF5 = isH5;
		}
		
		@Override
		protected Control createDialogArea(Composite parent) {
			super.createDialogArea(parent);
			if (isHDF5) {
				final Button button1 = new Button(parent, SWT.RADIO);
				final Button button2 = new Button(parent, SWT.RADIO);
				final Button button3 = new Button(parent, SWT.RADIO);
				button1.setText("Processed data only");
				button2.setText("Link original data (no data copied)");
				button3.setText("Process data into copy of original");

				switch (processingOutputType) {
				case PROCESSING_ONLY :{
					button1.setSelection(true);
					break;
				}
				case LINK_ORIGINAL :{
					button2.setSelection(true);
					break;
				}
				case ORIGINAL_AND_PROCESSED :{
					button3.setSelection(true);
					break;
				}
				default:
					button1.setSelection(true);
					processingOutputType = ProcessingOutputType.PROCESSING_ONLY;
				}

				SelectionListener l1 = new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (button1.getSelection()) {
							processingOutputType = ProcessingOutputType.PROCESSING_ONLY;
						} 
					}
				};

				button1.addSelectionListener(l1);

				SelectionListener l2 = new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (button2.getSelection()) {
							processingOutputType = ProcessingOutputType.LINK_ORIGINAL;
						} 
					}
				};

				button2.addSelectionListener(l2);

				SelectionListener l3 = new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						if (button3.getSelection()) {
							processingOutputType = ProcessingOutputType.ORIGINAL_AND_PROCESSED;
						} 
					}
				};

				button3.addSelectionListener(l3);
			}
			
			return parent;
		}
		
		@Override
		  protected Point getInitialSize() {
		    return new Point(500, 250);
		  }
	}

}

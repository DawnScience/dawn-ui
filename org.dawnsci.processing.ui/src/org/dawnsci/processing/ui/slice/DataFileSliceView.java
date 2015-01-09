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
import java.util.List;
import java.util.Map;

import org.dawb.common.services.IFileIconService;
import org.dawb.common.services.ServiceManager;
import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
import org.dawb.common.services.conversion.IConversionService;
import org.dawb.common.services.conversion.IProcessingConversionInfo;
import org.dawb.common.services.conversion.ProcessingOutputType;
import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawnsci.common.widgets.dialog.FileSelectionDialog;
import org.dawnsci.common.widgets.file.SelectorWidget;
import org.dawnsci.processing.ui.Activator;
import org.dawnsci.processing.ui.processing.OperationDescriptor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.processing.IExecutionVisitor;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationContext;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.dataset.impl.SliceND;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceInformation;
import org.eclipse.dawnsci.analysis.dataset.slicer.Slicer;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.dawnsci.hdf5.operation.HierarchicalFileExecutionVisitor;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataFileSliceView extends ViewPart {
	
	private static IConversionService cservice;
	public static void setConversionService(IConversionService s) {
		cservice = s;
	}
	private static IOperationService  oservice;
	public static void setOperationService(IOperationService s) {
		oservice = s;
	}
	private static ILoaderService lservice;
	public static void setLoaderService(ILoaderService s) {
		lservice = s;
	}

	
	private FileManager fileManager;
	private OperationEventManager eventManager;
	private TableViewer viewer;
	private UpdateJob job;
	private Label currentSliceLabel;
	private ChangeSliceWidget csw;
	private String selectedFile = null;
	private IOperation<? extends IOperationModel, ? extends OperationData> currentOperation = null;
	private IPlottingSystem input;
	private IPlottingSystem output;
	private IOperationInputData inputData = null;
	private ProcessingOutputType processingOutputType = ProcessingOutputType.PROCESSING_ONLY;
	
	String lastPath = null;
	
	private final static Logger logger = LoggerFactory.getLogger(DataFileSliceView.class);
	
	@Override
	public void createPartControl(Composite parent) {
		
		fileManager = new FileManager(new SetupContextHelper());
		
		parent.setLayout(new GridLayout());
		viewer = new TableViewer(parent);
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setContentProvider(new BasicContentProvider());
		viewer.setInput(fileManager);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof StructuredSelection) {
					inputData = null;
					eventManager.sendInputDataUpdate(null);
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
				eventManager.sendInputDataUpdate(null);
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
		output = (IPlottingSystem)view.getAdapter(IPlottingSystem.class);
		
		view = page.findView("org.dawnsci.processing.ui.input");
		input = (IPlottingSystem)view.getAdapter(IPlottingSystem.class);
		
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
				
				final int f = first;
				
				Display.getDefault().syncExec(new Runnable() {
					
					@Override
					public void run() {
						if (f < 0) {
							MessageDialog.openError(getSite().getShell(), "Error loading files", "None of the selected files contained suitable datasets!");
							return;
						}
						
						if (!failedPaths.isEmpty()){
							StringBuilder sb = new StringBuilder();
							sb.append("Failed to load: ");
							for (String p : failedPaths) sb.append(p +", ");
							sb.append("did not contain suitable datasets");
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
		
		eventManager = new OperationEventManager();
		eventManager.addOperationRunnerListener(new  IOperationGUIRunnerListener.Stub() {
			@Override
			public void updateRequested() {
				update(currentOperation);
			}
		});

	}
	
	private void createActions(IContributionManager rightClick) {
		
		final IAction run = new Action("Process all files", Activator.getImageDescriptor("icons/run_workflow.gif")) {
			public void run() {
				IOperation<? extends IOperationModel, ? extends OperationData>[] ops = getOperations();

				if (ops != null) {

					final IOperation<? extends IOperationModel, ? extends OperationData>[] fop = ops;

					fileManager.setProcessingConversionInfo(new IProcessingConversionInfo() {

						@Override
						public IOperation<? extends IOperationModel, ? extends OperationData>[] getOperationSeries() {
							return fop;
						}

						@Override
						public IExecutionVisitor getExecutionVisitor(String fileName) {
							return new HierarchicalFileExecutionVisitor(fileName);
						}

						@Override
						public ProcessingOutputType getProcessingOutputType() {
							return processingOutputType;
						}

					});
				}
				
				ExtendedFileSelectionDialog fsd = new ExtendedFileSelectionDialog(Display.getCurrent().getActiveShell());
				if (lastPath == null) {
					final File source = new File(fileManager.getFilePaths().get(0));
					lastPath  = source.getParent();
				}
				
				fsd.setPath(lastPath);
				fsd.create();
				if (fsd.open() == Dialog.CANCEL) return;
				lastPath = fsd.getPath();
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
								cservice.process(fileManager.getContext());
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
			}
		};
		getViewSite().getActionBars().getToolBarManager().add(run);
		getViewSite().getActionBars().getMenuManager().add(run);
		rightClick.add(run);
		
		final IAction clear = new Action("Clear selected file", Activator.getImageDescriptor("icons/delete.gif")) {
			public void run() {
				fileManager.getFilePaths().remove(selectedFile);
				
				if (fileManager.getFilePaths().isEmpty()) {
					fileManager.clear();
					csw.disable();
					currentSliceLabel.setText("Current slice of data: [ - - - - -]");
					eventManager.sendInitialDataUpdate(null);
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
		};
		
		final IAction clearAll = new Action("Clear all files", Activator.getImageDescriptor("icons/deleteAll.gif")) {
			public void run() {

				fileManager.clear();
				csw.disable();
				eventManager.sendInitialDataUpdate(null);
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
		
		final IAction export = new OperationExportAction("Export to Workflow", Activator.getImageDescriptor("icons/flow.png")) {
			public IOperationContext createContext() {
				
				IOperationContext ocontext  = oservice.createContext();
				ocontext.setSeries(getOperations());

				final String selectedPath  = (String)((IStructuredSelection)viewer.getSelection()).getFirstElement();
				if (selectedPath == null) {
			    	MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Please Choose File", "Please select a file to use with export.\n\nOne file only may be exported to workflows at a time.");
                    return null;
				}
				ocontext.setFilePath(selectedPath);
				
				IConversionContext ccontext = fileManager.getContext();
				if (ccontext == null) {
			    	MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Please Choose File", "Please ensure that a slice and file have been chosen.");
                    return null;
				}
				ocontext.setSlicing(ccontext.getSliceDimensions());
				ocontext.setDatasetPath(ccontext.getDatasetNames().get(0));
				
                return ocontext;
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

		getViewSite().getActionBars().getToolBarManager().add(export);
		getViewSite().getActionBars().getMenuManager().add(export);
		rightClick.add(export);

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
			IDataHolder dh = lservice.getData(path, new IMonitor.Stub());
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
				IMetadata metadata = lservice.getMetadata(path, null);
				int[] s = metadata.getDataShapes().get(name);
				
				Slice[] slices = Slicer.getSliceArrayFromSliceDimensions(sliceDimensions, s);
				SliceND slice = new SliceND(s, slices);
				int[] nShape = slice.getShape();

				if (dd == null) {
					dd = Slicer.getDataDimensions(s, context.getSliceDimensions());
					Arrays.sort(dd);
				}
				 int n = 1;
				 for (int i = 0; i < nShape.length; i++) {
					 if (Arrays.binarySearch(dd, i) < 0) n *= nShape[i];
				 }
				
				c += n;
				
			} catch (Exception e) {
				logger.warn("cannot load metadata for {}, assuming one frame", path);
				c++;
			}
		}
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
				
				final IDataHolder   dh = lservice.getData(path, new IMonitor.Stub());
				ILazyDataset lazyDataset = dh.getLazyDataset(context.getDatasetNames().get(0));
				
				if (lazyDataset == null) {
					logger.error("Selected dataset not in file!!!!");
					return Status.CANCEL_STATUS;
				}
				
				Map<Integer, String> axesNames = context.getAxesNames();
				
				if (axesNames != null) {

					AxesMetadata axMeta = SlicedDataUtils.createAxisMetadata(path, lazyDataset, axesNames);
					if (axMeta != null) lazyDataset.setMetadata(axMeta);
					else lazyDataset.clearMetadata(AxesMetadata.class);

				}
				
				
				int[] dataDims = Slicer.getDataDimensions(lazyDataset.getShape(), context.getSliceDimensions());
				Slice[] s = csw.getCurrentSlice();
				//Plot input, probably a bit wasteful to do each time
				IDataset firstSlice = lazyDataset.getSlice(s);
				eventManager.sendInitialDataUpdate(firstSlice.getSliceView().squeeze());
				SlicedDataUtils.plotDataWithMetadata(firstSlice, input, dataDims);
				
				
				IOperation<? extends IOperationModel, ? extends OperationData>[] ops = getOperations();
				if (ops == null) {
					output.clear();
					return Status.OK_STATUS;
				}
				//Only run what is necessary
				if (inputData != null) {
					
					if (inputData.getCurrentOperation() != end) {
						int pos = 0;
						for (int i = 0; i< ops.length; i++) {
							if (ops[i] == end) break;
							if (ops[i] == inputData.getCurrentOperation()) {
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
						ops = new IOperation[]{inputData.getCurrentOperation()};
					}
					
				}
				
				SourceInformation si = new SourceInformation(path, context.getDatasetNames().get(0), lazyDataset);
				SliceInformation sli = csw.getCurrentSliceInformation();
				//TODO replace with check shape
				firstSlice.setMetadata(new SliceFromSeriesMetadata(si,sli));
				
//				OriginMetadataImpl om = new OriginMetadataImpl(lazyDataset, viewSlice, dataDims, path, context.getDatasetNames().get(0));
//				om.setCurrentSlice(csw.getCurrentSlice());

//				lazyDataset.setMetadata(om);

				sliceVisitor = getSliceVisitor(ops, lazyDataset, Slicer.getDataDimensions(lazyDataset.getShape(), context.getSliceDimensions()));
				sliceVisitor.setEndOperation(end);
				long start = System.currentTimeMillis();
				sliceVisitor.visit(firstSlice, null, null);
				inputData = sliceVisitor.getOperationInputData();
				eventManager.sendInputDataUpdate(inputData);
				logger.debug("Ran in: " +(System.currentTimeMillis()-start)/1000. + " s");
				eventManager.sendErrorUpdate(null);

				
				
				} catch (OperationException e) {
					logger.error(e.getMessage(), e);
					eventManager.sendErrorUpdate(e);
					return Status.CANCEL_STATUS;
				} catch (Exception e) {
					logger.error(e.getMessage(), e);

					String message = e.getMessage();
					if (message == null) message = "Unexpected error!";
					eventManager.sendErrorUpdate(new OperationException(null, message));
					return Status.CANCEL_STATUS;
				} finally {
					if (sliceVisitor != null) {
						inputData = sliceVisitor.getOperationInputData();
						eventManager.sendInputDataUpdate(inputData);
					}
					output.setEnabled(true);
				}
				
				return Status.OK_STATUS;
		}
		
	}
	
	private EscapableSliceVisitor getSliceVisitor(IOperation<? extends IOperationModel, ? extends OperationData>[] series,ILazyDataset lz,  
            int[] dataDims) {
		return new EscapableSliceVisitor(lz,dataDims,series,null,fileManager.getContext(),output);
	}
	
	@Override
	public Object getAdapter(final Class clazz) {
		if (clazz == FileManager.class) return fileManager;
		if (clazz == OperationEventManager.class) return eventManager;
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
			
			IConversionContext context = cservice.open(path);
			
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
			context.setConversionScheme(ConversionScheme.PROCESS);

			if (wd.open() == WizardDialog.OK) return context;
			
			return null;
		}
		
	}
	
	private class ExtendedFileSelectionDialog extends FileSelectionDialog {
		
		public ExtendedFileSelectionDialog(Shell parentShell) {
			super(parentShell);
		}
		
		@Override
		protected Control createDialogArea(Composite parent) {
			super.createDialogArea(parent);
			Group group1 = new Group(parent, SWT.SHADOW_IN);
			group1.setText("Select Output");
			group1.setLayout(new RowLayout(SWT.VERTICAL));
			final Button button1 = new Button(group1, SWT.RADIO);
			button1.setText("Save only the processed data");
			if (processingOutputType == ProcessingOutputType.PROCESSING_ONLY) button1.setSelection(true);
			final Button button2 = new Button(group1, SWT.RADIO);
			button2.setText("Save both the original and processed data (nxs original data only)");
			if (processingOutputType == ProcessingOutputType.ORIGINAL_AND_PROCESSED) button2.setSelection(true);
						
			SelectionListener groupListener = new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (button2.getSelection()) {
						processingOutputType = ProcessingOutputType.ORIGINAL_AND_PROCESSED;
					} else {
						processingOutputType = ProcessingOutputType.PROCESSING_ONLY;
					}
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			};
			button1.addSelectionListener(groupListener);
			//button2.addSelectionListener(groupListener);
			
			return parent;
		}
		
	}

}

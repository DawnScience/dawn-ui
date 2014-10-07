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
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dawb.common.services.ServiceManager;
import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
import org.dawb.common.services.conversion.IConversionService;
import org.dawb.common.services.conversion.IProcessingConversionInfo;
import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawnsci.processing.ui.Activator;
import org.dawnsci.processing.ui.model.OperationDescriptor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.processing.IExecutionVisitor;
import org.eclipse.dawnsci.analysis.api.processing.IExportOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.slice.SliceVisitor;
import org.eclipse.dawnsci.analysis.api.slice.Slicer;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.metadata.OriginMetadataImpl;
import uk.ac.diamond.scisoft.analysis.processing.visitors.HierarchicalFileExecutionVisitor;

public class DataFileSliceView extends ViewPart {

//	List<String> filePaths = new ArrayList<String>();
	FileManager fileManager;
	TableViewer viewer;
	IConversionService service;
	IConversionContext context;
	ImageProcessConvertPage convertPage;
	UpdateJob job;
	Label currentSliceLabel;
	ChangeSliceWidget csw;
	String selectedFile = null;
	IOperation<? extends IOperationModel, ? extends OperationData> currentOperation = null;
	IPlottingSystem input;
	IPlottingSystem output;
	
	private final static Logger logger = LoggerFactory.getLogger(DataFileSliceView.class);
	
	@Override
	public void createPartControl(Composite parent) {
		
		try {
			service = (IConversionService)ServiceManager.getService(IConversionService.class);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		parent.setLayout(new GridLayout());
		
		viewer = new TableViewer(parent);
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setContentProvider(new BasicContentProvider());
		viewer.setInput(fileManager);
		//viewer.setInput(filePaths.toArray(new String[filePaths.size()]));
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof StructuredSelection) {
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
							//addFiles(new String[]{file.getLocation().toOSString()});
						}
					}
					
					if (!paths.isEmpty()) addFiles(paths.toArray(new String[paths.size()]));
					
				} else if (dropData instanceof String[]) {
					addFiles((String[])dropData);
//					for (String path : (String[])dropData){
//						addFile(path);
//					}
				}
				viewer.setInput(fileManager);
				//viewer.setInput(filePaths.toArray(new String[filePaths.size()]));
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
				String ss = Slice.createString(csw.getCurrentSlice());
				currentSliceLabel.setText("Current slice of data: [" +ss + "]");
				currentSliceLabel.getParent().layout(true);
				
				update(currentOperation);
			}
		});
		
		final MenuManager rightClick = new MenuManager();
		createActions(rightClick);
		viewer.getControl().setMenu(rightClick.createContextMenu(viewer.getControl()));
		
		getSite().getPage().addSelectionListener("org.dawnsci.processing.ui.processingView",new ISelectionListener() {
			
			@Override
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				selection.toString();
				
				if (selection instanceof StructuredSelection && ((StructuredSelection)selection).getFirstElement() instanceof OperationDescriptor) {
					OperationDescriptor des = (OperationDescriptor)((StructuredSelection)selection).getFirstElement();
					try {
						currentOperation = des.getSeriesObject();
						update(currentOperation);
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						logger.error(e.getMessage(),e);
					}
				}
				
				//update(null);
				
			}
		});
		
		currentSliceLabel = new Label(parent, SWT.WRAP);
		currentSliceLabel.setText("Current slice of data: [ - - - - -]");
		
		IWorkbenchPage page = getSite().getPage();
		IViewPart view = page.findView("org.dawnsci.processing.ui.output");
		output = (IPlottingSystem)view.getAdapter(IPlottingSystem.class);
		view = page.findView("org.dawnsci.processing.ui.input");
		input = (IPlottingSystem)view.getAdapter(IPlottingSystem.class);
		

	}
	
	private void createActions(IContributionManager rightClick) {
		
		final IAction run = new Action("Process all files", Activator.getImageDescriptor("icons/run_workflow.gif")) {
			public void run() {
				IOperation<? extends IOperationModel, ? extends OperationData>[] ops = getOperations();

				if (ops != null) {

					final IOperation<? extends IOperationModel, ? extends OperationData>[] fop = ops;

					context.setUserObject(new IProcessingConversionInfo() {

						@Override
						public IOperation<? extends IOperationModel, ? extends OperationData>[] getOperationSeries() {
							return fop;
						}

						@Override
						public IExecutionVisitor getExecutionVisitor(String fileName) {
							return new HierarchicalFileExecutionVisitor(fileName);
						}

					});
				}

				ProgressMonitorDialog dia = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());

				try {
					dia.run(true, true, new IRunnableWithProgress() {

						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException,
						InterruptedException {
							//TODO properly populate the number steps
							monitor.beginTask("Processing", 100);
							context.setMonitor(new ProgressMonitorWrapper(monitor));
							try {
								service.process(context);
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
		
		final IAction clear = new Action("Clear all files", Activator.getImageDescriptor("icons/delete.gif")) {
			public void run() {
				fileManager = null;
				context = null;
				try {
					input.reset();
					output.reset();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				viewer.setInput(fileManager);
			}
		};
		
		getViewSite().getActionBars().getToolBarManager().add(clear);
		getViewSite().getActionBars().getMenuManager().add(clear);
		rightClick.add(clear);
		
		final IAction edit = new Action("Edit slice configuration", Activator.getImageDescriptor("icons/clipboard-list.png")) {
			public void run() {
				Wizard wiz = new Wizard() {
					//set 
					@Override
					public boolean performFinish() {
						return true;
					}
				};
				
				wiz.setNeedsProgressMonitor(true);
				convertPage = null;
				convertPage = new ImageProcessConvertPage();
				wiz.addPage(convertPage);
				final WizardDialog wd = new WizardDialog(getSite().getShell(),wiz);
				wd.create();
				convertPage.setContext(context);
				
				if (wd.open() == WizardDialog.OK) {
					context = convertPage.getContext();
					context.setConversionScheme(ConversionScheme.PROCESS);
					
					try {
						
						update(null);
						
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				}
			}
		};

		getViewSite().getActionBars().getToolBarManager().add(edit);
		getViewSite().getActionBars().getMenuManager().add(edit);
		rightClick.add(edit);
	}
	
	private void addFiles(String[] filePath) {
		
		if (context == null) {
			context = service.open(filePath);
			job = new UpdateJob(context);

			Wizard wiz = new Wizard() {
				//set 
				@Override
				public boolean performFinish() {
					return true;
				}
			};

			wiz.setNeedsProgressMonitor(true);
			convertPage = null;
			convertPage = new ImageProcessConvertPage();
			wiz.addPage(convertPage);
			final WizardDialog wd = new WizardDialog(getSite().getShell(),wiz);
			wd.setPageSize(new Point(800, 800));
			wd.create();
			context.setConversionScheme(ConversionScheme.PROCESS);
			convertPage.setContext(context);

			if (wd.open() == WizardDialog.OK) {
				context = convertPage.getContext();
				job = new UpdateJob(context);

				try {

					update(currentOperation);

				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}

				fileManager = new FileManager(context);
				fileManager.addFileListener(new IFilesAddedListener() {

					@Override
					public void filesAdded(FileAddedEvent event) {
						update(currentOperation);
						Display.getDefault().syncExec(new Runnable() {

							@Override
							public void run() {
								viewer.refresh();
							}
						});

					}
				});
				//filePaths.add(filePath);

				IDataHolder dh;
				try {
					dh = LoaderFactory.getData(context.getFilePaths().get(0));
					ILazyDataset lazy = dh.getLazyDataset(context.getDatasetNames().get(0));
					int[] shape = lazy.getShape();

					//					datasetLabel.setText(context.getDatasetNames().get(0));
					//single image/line
					//					if (context.getSliceDimensions() == null){
					//						for (int i = 0; i < shape.length; i++) context.addSliceDimension(i, "all");
					//					}

					int[] dd = Slicer.getDataDimensions(shape, context.getSliceDimensions());
					Slice[] slices = Slicer.getSliceArrayFromSliceDimensions(context.getSliceDimensions(), shape);
					csw.setDatasetShapeInformation(shape, dd.clone(), slices);
					String ss = Slice.createString(csw.getCurrentSlice());
					currentSliceLabel.setText("Current slice of data: [" +ss + "]");
					//					Arrays.sort(dd);
					//					
					//					int work = 1;
					//					
					//					for (int i = 0; i< shape.length; i++) {
					//						if (Arrays.binarySearch(dd, i) < 0) work*=shape[i];
					//					}
					//					
					//					maxImage = work;


				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			} else {
				context = null;
			}
			
			
		} else {
			fileManager.addFiles(filePath);
		}



	}
	
	private void update(final IOperation<? extends IOperationModel, ? extends OperationData> end) {
	
			if (context == null) return;
			
			if (job != null) {
				job.cancel();
				job.setPath(selectedFile); 
				job.setEndOperation(end);
				job.schedule();
			}
		
	}
	
	private void updateSliceWidget(String path) {
		try {
			IDataHolder dh = LoaderFactory.getData(path);
			ILazyDataset lazy = dh.getLazyDataset(context.getDatasetNames().get(0));
			int[] shape = lazy.getShape();
			
			int[] dd = Slicer.getDataDimensions(shape, context.getSliceDimensions());
			Slice[] slices = Slicer.getSliceArrayFromSliceDimensions(context.getSliceDimensions(), shape);
			csw.setDatasetShapeInformation(shape, dd.clone(), slices);
			String ss = Slice.createString(csw.getCurrentSlice());
			currentSliceLabel.setText("Current slice of data: [" +ss + "]");
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
	
	private UIExecutionVisitor getOutputExecutionVisitor() {
		return new UIExecutionVisitor();
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
			try {
				
				if (path == null) path = context.getFilePaths().get(0);
				
				final IDataHolder   dh = LoaderFactory.getData(path);
				ILazyDataset lazyDataset = dh.getLazyDataset(context.getDatasetNames().get(0));
				
				Map<Integer, String> axesNames = context.getAxesNames();
				
				if (axesNames != null) {

					AxesMetadata axMeta = SlicedDataUtils.createAxisMetadata(path, lazyDataset.getRank(), axesNames);
					lazyDataset.setMetadata(axMeta);

				}
				
				final IDataset firstSlice = lazyDataset.getSlice(csw.getCurrentSlice()).squeeze();
				
				SlicedDataUtils.plotDataWithMetadata(firstSlice, input, Slicer.getDataDimensions(lazyDataset.getShape(), context.getSliceDimensions()));
				
				IOperation<? extends IOperationModel, ? extends OperationData>[] ops = getOperations();
				if (ops == null) return Status.OK_STATUS;
				EscapableSliceVisitor sliceVisitor = getSliceVisitor(ops, getOutputExecutionVisitor(), lazyDataset, Slicer.getDataDimensions(lazyDataset.getShape(), context.getSliceDimensions()));
				sliceVisitor.setEndOperation(end);
				sliceVisitor.visit(firstSlice, null, null);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					return Status.CANCEL_STATUS;
				}
				
				return Status.OK_STATUS;
		}
		
	}
	
	private EscapableSliceVisitor getSliceVisitor(IOperation<? extends IOperationModel, ? extends OperationData>[] series,UIExecutionVisitor visitor,ILazyDataset lz,  
            int[] dataDims) {
		return new EscapableSliceVisitor(lz,visitor,dataDims,series,null,context);
	}
	
	private class EscapableSliceVisitor implements SliceVisitor {

		
		private ILazyDataset lz;
		private UIExecutionVisitor visitor;
		private int[] dataDims;
		private IOperation<? extends IOperationModel, ? extends OperationData>[] series;
		private IOperation<? extends IOperationModel, ? extends OperationData> endOperation;
		private IProgressMonitor monitor;
		private IConversionContext context;
		
		public EscapableSliceVisitor(ILazyDataset lz, UIExecutionVisitor visitor, 
				                     int[] dataDims, IOperation<? extends IOperationModel, ? extends OperationData>[] series, 
				                     IProgressMonitor monitor, IConversionContext context) {
			this.lz = lz;
			this.visitor = visitor;
			this.dataDims = dataDims;
			this.series = series;
			this.monitor= monitor;
			this.context= context;
		}
		
		public void setEndOperation(IOperation<? extends IOperationModel, ? extends OperationData> op) {
			endOperation = op;
			visitor.setEndOperation(op);
		}
		
		@Override
		public void visit(IDataset slice, Slice[] slices,
				int[] shape) throws Exception {
			
			slice.addMetadata(new OriginMetadataImpl(lz, slices, dataDims));
			
			OperationData  data = new OperationData(slice, (Serializable[])null);
								
			for (IOperation<? extends IOperationModel, ? extends OperationData> i : series) {
				 if (i instanceof IExportOperation) {
					 visitor.notify(i, data, slices, shape, dataDims);
				 } else {
					 OperationData tmp = i.execute(data.getData(), null);
					 visitor.notify(i, tmp, slices, shape, dataDims); // Optionally send intermediate result
					data = i.isPassUnmodifiedData() ? data : tmp;
				 }
				
				if (i == endOperation) break;
			}
			
			visitor.executed(data, null, slices, shape, dataDims); // Send result.
		}

		@Override
		public boolean isCancelled() {
			if (monitor != null && monitor.isCanceled()) return true;
			// Overkill warning, context probably is being used here without a monitor, but just in case:
			if (context != null && context.getMonitor()!=null && context.getMonitor().isCancelled()) return true;
			return false;
		}
	}
	
	private class UIExecutionVisitor implements IExecutionVisitor {

		private IOperation<? extends IOperationModel, ? extends OperationData> endOp;
		
		public void setEndOperation(IOperation<? extends IOperationModel, ? extends OperationData> op) {
			endOp = op;
		}
		
		@Override
		public void notify(IOperation<? extends IOperationModel, ? extends OperationData> intermediateData, OperationData data,
				Slice[] slices, int[] shape, int[] dataDims) {
			
			try {
				if (intermediateData == endOp) displayData(data,dataDims);
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			
		}
		
		@Override
		public void init(IOperation<? extends IOperationModel, ? extends OperationData>[] series) throws Exception {
			
		}
		
		@Override
		public void executed(OperationData result, IMonitor monitor,
				Slice[] slices, int[] shape, int[] dataDims) throws Exception {
			
			if (endOp == null) displayData(result,dataDims);
		}
		
		@Override
		public void close() throws Exception {
			// TODO Auto-generated method stub
			
		}
		
		private void displayData(OperationData result, int[] dataDims) throws Exception {
			IDataset out = result.getData();
			
			SlicedDataUtils.plotDataWithMetadata(out, output, dataDims);

		}

	}
	
	private class BasicContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof FileManager)
				return ((FileManager)inputElement).getFilePaths().toArray();
			
			return null;
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	private class ViewLabelProvider extends ColumnLabelProvider {
	
		@Override
		public String getText(Object obj) {
			
			if (obj instanceof String) {
				File f = new File((String)obj);
				if (f.isFile()) return f.getName();
			}
			
			return getText(obj);
		}
		
		@Override
		public String getToolTipText(Object obj) {
			return obj.toString();
		}
		
	}
	

}

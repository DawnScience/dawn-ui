/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.processing.ui;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.dawb.common.services.ServiceManager;
import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
import org.dawb.common.services.conversion.IConversionService;
import org.dawb.common.services.conversion.IProcessingConversionInfo;
import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
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
import org.eclipse.dawnsci.analysis.api.metadata.MaskMetadata;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.processing.IExecutionVisitor;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.slice.SliceVisitor;
import org.eclipse.dawnsci.analysis.api.slice.Slicer;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.metadata.OriginMetadataImpl;
import uk.ac.diamond.scisoft.analysis.processing.visitors.HierarchicalFileExecutionVisitor;

public class DataFileSliceView extends ViewPart {

	List<String> filePaths = new ArrayList<String>();
	TableViewer viewer;
	IConversionService service;
	IConversionContext context;
	ImageProcessConvertPage convertPage;
	UpdateJob job;
	Label currentSliceLabel;
	ChangeSliceWidget csw;
	String selectedFile = null;
	
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
		viewer.setInput(filePaths.toArray(new String[filePaths.size()]));
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof StructuredSelection) {
					selectedFile = (String)((StructuredSelection)event.getSelection()).getFirstElement();
					update(null);
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
					for (int i = 0; i < obj.length; i++) {
						if (obj[i] instanceof IFile) {
							IFile file = (IFile) obj[i];
							addFile(file.getLocation().toOSString());
						}
					}
				} else if (dropData instanceof String[]) {
					for (String path : (String[])dropData){
						addFile(path);
					}
				}
				
				viewer.setInput(filePaths.toArray(new String[filePaths.size()]));
			}
		};
		
		DropTarget dt = new DropTarget(viewer.getControl(), DND.DROP_MOVE | DND.DROP_DEFAULT | DND.DROP_COPY);
		dt.setTransfer(new Transfer[] { TextTransfer.getInstance(),
				FileTransfer.getInstance(), ResourceTransfer.getInstance(),
				LocalSelectionTransfer.getTransfer() });
		dt.addDropListener(dropListener);
		
//		SelectorWidget sw = new SelectorWidget(parent,true,true) {
//			
//			@Override
//			public void pathChanged(String path, TypedEvent event) {
//				
//			}
//		};
//		sw.setLabel("Output:");
		
		csw = new ChangeSliceWidget(parent);
		csw.addSliceChangeListener(new ISliceChangeListener() {
			
			@Override
			public void sliceChanged(SliceChangeEvent event) {
				String ss = Slice.createString(csw.getCurrentSlice());
				currentSliceLabel.setText("Current slice of data: [" +ss + "]");
				currentSliceLabel.getParent().layout(true);
				update(null);
			}
		});
		
//		Button edit = new Button(parent, SWT.NONE);
//		edit.setText("Edit");
//		edit.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				Wizard wiz = new Wizard() {
//					//set 
//					@Override
//					public boolean performFinish() {
//						return true;
//					}
//				};
//				
//				wiz.setNeedsProgressMonitor(true);
//				convertPage = null;
//				convertPage = new ImageProcessConvertPage();
//				wiz.addPage(convertPage);
//				final WizardDialog wd = new WizardDialog(getSite().getShell(),wiz);
//				wd.create();
//				convertPage.setContext(context);
//				
//				
//
//				if (wd.open() == WizardDialog.OK) {
//					context = convertPage.getContext();
//					context.setConversionScheme(ConversionScheme.PROCESS);
//					
//					try {
//						
//						update(null);
//						
//					} catch (Exception e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//					
//				}
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
		
		
		
		final MenuManager rightClick = new MenuManager();
		createActions(rightClick);
		viewer.getControl().setMenu(rightClick.createContextMenu(viewer.getControl()));
		
//		Button run = new Button(parent, SWT.NONE);
//		run.setText("Run");
//		run.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				
//				IOperation[] ops = getOperations();
//				
//				if (ops != null) {
//					
//					final IOperation[] fop = ops;
//					
//					context.setUserObject(new IProcessingConversionInfo() {
//
//						@Override
//						public IOperation[] getOperationSeries() {
//							return fop;
//						}
//
//						@Override
//						public IExecutionVisitor getExecutionVisitor(String fileName) {
//							return new HierarchicalFileExecutionVisitor(fileName);
//						}
//
//					});
//				}
//					
//				ProgressMonitorDialog dia = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
//				
//				try {
//					dia.run(true, true, new IRunnableWithProgress() {
//
//						@Override
//						public void run(IProgressMonitor monitor) throws InvocationTargetException,
//						InterruptedException {
//							//TODO properly populate the number steps
//							monitor.beginTask("Processing", 100);
//							context.setMonitor(new ProgressMonitorWrapper(monitor));
//							try {
//								service.process(context);
//							} catch (Exception e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//
//						}
//					});
//				} catch (InvocationTargetException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				} catch (InterruptedException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
		
		getSite().getPage().addSelectionListener("org.dawnsci.processing.ui.processingView",new ISelectionListener() {
			
			@Override
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				selection.toString();
				
				if (selection instanceof StructuredSelection && ((StructuredSelection)selection).getFirstElement() instanceof OperationDescriptor) {
					OperationDescriptor des = (OperationDescriptor)((StructuredSelection)selection).getFirstElement();
					try {
						update(des.getSeriesObject());
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				//update(null);
				
			}
		});
		
		currentSliceLabel = new Label(parent, SWT.WRAP);
		currentSliceLabel.setText("Current slice of data: [ - - - - -]");
		

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
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					});
				} catch (InvocationTargetException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		};
		getViewSite().getActionBars().getToolBarManager().add(run);
		getViewSite().getActionBars().getMenuManager().add(run);
		rightClick.add(run);
		
		final IAction clear = new Action("Clear all files", Activator.getImageDescriptor("icons/delete.gif")) {
			public void run() {
				filePaths.clear();
				context = null;
				try {
					SDAPlotter.clearPlot("Output");
					SDAPlotter.clearPlot("Input");
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				viewer.setInput(filePaths.toArray(new String[filePaths.size()]));
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
	
	private void addFile(String filePath) {
		
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
			wd.create();
			convertPage.setContext(context);

			if (wd.open() == WizardDialog.OK) {
				context = convertPage.getContext();
				job = new UpdateJob(context);
				context.setConversionScheme(ConversionScheme.PROCESS);
				
				try {
					
					update(null);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				filePaths.add(filePath);
				
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
					currentSliceLabel.setText(Slice.createString(csw.getCurrentSlice()));
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		} else {
			String dsName = context.getDatasetNames().get(0);
			try {
				IDataHolder holder = LoaderFactory.getData(filePath, null);
				ILazyDataset lazyDataset = holder.getLazyDataset(dsName);
				if (lazyDataset != null) {
					filePaths.add(filePath);
					context.setFilePaths(filePaths.toArray(new String[filePaths.size()]));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
	
	private IExecutionVisitor getOutputExecutionVisitor() {
		return new IExecutionVisitor() {
			
			@Override
			public void passDataThroughUnmodified(IOperation<? extends IOperationModel, ? extends OperationData>... operations) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void notify(IOperation<? extends IOperationModel, ? extends OperationData> intermediateData, OperationData data,
					Slice[] slices, int[] shape, int[] dataDims) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isRequiredToModifyData(IOperation<? extends IOperationModel, ? extends OperationData> operation) {
				// TODO Auto-generated method stub
				return true;
			}
			
			@Override
			public void init(IOperation<? extends IOperationModel, ? extends OperationData>[] series) throws Exception {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void executed(OperationData result, IMonitor monitor,
					Slice[] slices, int[] shape, int[] dataDims) throws Exception {
				IDataset out = result.getData();
				
				out = out.squeeze();
				SDAPlotter.clearPlot("Output");
				if (out.getRank() == 2)  {
					
					List<AxesMetadata> axList = result.getData().getMetadata(AxesMetadata.class);
					
					if (axList == null || axList.isEmpty()) {
						SDAPlotter.imagePlot("Output", out);
					} else {
						ILazyDataset[] axes = axList.get(0).getAxes();
						ILazyDataset lz0 = axes[dataDims[0]];
						ILazyDataset lz1 = axes[dataDims[1]];
						IDataset ax0 = null;
						IDataset ax1 = null;
						if (lz0 != null) ax0 = lz0.getSlice().squeeze();
						if (lz1 != null) ax1 = lz1.getSlice().squeeze();
						
						SDAPlotter.imagePlot("Output", ax0,ax1,out);
					}
					
					List<MaskMetadata> mList = result.getData().getMetadata(MaskMetadata.class);
					
					if (mList == null || mList.isEmpty()) return;
					
					MaskMetadata m = mList.get(0);
					ILazyDataset mask = m.getMask();
					
					final IDataset md = mask.getSlice().squeeze();
					
					if (!Arrays.equals(md.getShape(), out.getShape())) return;
					
					Display.getDefault().syncExec(new Runnable() {
						
						@Override
						public void run() {
							IPlottingSystem system = PlottingFactory.getPlottingSystem("Output");
							if (system == null) return;
							Collection<ITrace> traces = system.getTraces(IImageTrace.class);
							if (traces == null || traces.isEmpty()) return;
							IImageTrace t = (IImageTrace)traces.iterator().next();
							t.setMask(md);
							system.repaint();							
						}
					});
					
				}
				
				if (out.getRank() == 1) {
					List<AxesMetadata> mList = result.getData().getMetadata(AxesMetadata.class);
					if (mList == null || mList.isEmpty()) {
						SDAPlotter.plot("Output", out);
					}
					
					ILazyDataset[] axes = mList.get(0).getAxes();
					ILazyDataset lz = axes[dataDims[0]];
					IDataset ax = null;
					if (lz != null) ax = lz.getSlice().squeeze();
					
					if (ax != null && Arrays.equals(ax.getShape(), out.getShape())) {
						SDAPlotter.plot("Output", ax, out);
					}
				}
				
			}
			
			@Override
			public void close() throws Exception {
				// TODO Auto-generated method stub
				
			}
		};
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
			this.path = path;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				
				if (path == null) path = context.getFilePaths().get(0);
				
				final IDataHolder   dh = LoaderFactory.getData(path);
				ILazyDataset lazyDataset = dh.getLazyDataset(context.getDatasetNames().get(0));
				
				final IDataset firstSlice = lazyDataset.getSlice(csw.getCurrentSlice()).squeeze();
//				final IDataset firstSlice = Slicer.getFirstSlice(new RichDataset(lazyDataset, null), context.getSliceDimensions());
				if (firstSlice.getRank() == 2) SDAPlotter.imagePlot("Input", firstSlice);
				else {
					SDAPlotter.plot("Input", firstSlice);
				}
				
				EscapableSliceVisitor sliceVisitor = getSliceVisitor(getOperations(), getOutputExecutionVisitor(), lazyDataset, Slicer.getDataDimensions(lazyDataset.getShape(), context.getSliceDimensions()));
				sliceVisitor.setEndOperation(end);
				sliceVisitor.visit(firstSlice, null, null);
				} catch (Exception e) {
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				}
				
				return Status.OK_STATUS;
		}
		
	}
	
	private EscapableSliceVisitor getSliceVisitor(IOperation<? extends IOperationModel, ? extends OperationData>[] series,IExecutionVisitor visitor,ILazyDataset lz,  
            int[] dataDims) {
		return new EscapableSliceVisitor(lz,visitor,dataDims,series,null,context);
	}
	
	private class EscapableSliceVisitor implements SliceVisitor {

		
		private ILazyDataset lz;
		private IExecutionVisitor visitor;
		private int[] dataDims;
		private IOperation<? extends IOperationModel, ? extends OperationData>[] series;
		private IOperation<? extends IOperationModel, ? extends OperationData> endOperation;
		private IProgressMonitor monitor;
		private IConversionContext context;
		
		public EscapableSliceVisitor(ILazyDataset lz, IExecutionVisitor visitor, 
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
		}
		
		@Override
		public void visit(IDataset slice, Slice[] slices,
				int[] shape) throws Exception {
			
			slice.addMetadata(new OriginMetadataImpl(lz, slices, dataDims));
			
			OperationData  data = new OperationData(slice, (Serializable[])null);
								
			for (IOperation<? extends IOperationModel, ? extends OperationData> i : series) {
				OperationData tmp = i.execute(data.getData(), null);
				data = visitor.isRequiredToModifyData(i) ? tmp : data;
				visitor.notify(i, data, slices, shape, dataDims); // Optionally send intermediate result
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
	
	private class BasicContentProvider implements IStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return (String[]) inputElement;
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

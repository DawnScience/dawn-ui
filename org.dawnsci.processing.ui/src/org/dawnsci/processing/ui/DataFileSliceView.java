package org.dawnsci.processing.ui;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.dawb.common.services.ServiceManager;
import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
import org.dawb.common.services.conversion.IConversionService;
import org.dawb.common.services.conversion.IProcessingConversionInfo;
import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;

import uk.ac.diamond.scisoft.analysis.processing.IExecutionVisitor;
import uk.ac.diamond.scisoft.analysis.processing.IOperation;
import uk.ac.diamond.scisoft.analysis.processing.visitors.HierarchicalFileExecutionVisitor;

public class DataFileSliceView extends ViewPart {

	List<String> filePaths = new ArrayList<String>();
	TableViewer viewer;
	IConversionService service;
	IConversionContext context;
	
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
		viewer.setLabelProvider(new LabelProvider());
		viewer.setContentProvider(new BasicContentProvider());
		viewer.setInput(filePaths.toArray(new String[filePaths.size()]));
		
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
		
		Button clear = new Button(parent, SWT.NONE);
		clear.setText("Clear");
		clear.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				filePaths.clear();
				viewer.setInput(filePaths.toArray(new String[filePaths.size()]));
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		Button run = new Button(parent, SWT.NONE);
		run.setText("Run");
		run.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {

					
				ProgressMonitorDialog dia = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());

				try {
					dia.run(true, false, new IRunnableWithProgress() {

						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException,
						InterruptedException {
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
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});

	}
	
	private void addFile(String filePath) {
		filePaths.add(filePath);
		context = service.open(filePath);
		
		Wizard wiz = new Wizard() {
			//set 
			@Override
			public boolean performFinish() {
				return true;
			}
		};
		
		wiz.setNeedsProgressMonitor(true);
		
		final ImageProcessConvertPage ipcp = new ImageProcessConvertPage();
		wiz.addPage(ipcp);
		final WizardDialog wd = new WizardDialog(getSite().getShell(),wiz);
		wd.create();
		ipcp.setContext(context);

		if (wd.open() == WizardDialog.OK) {
			context = ipcp.getContext();
			context.setConversionScheme(ConversionScheme.PROCESS);
		}

		IViewPart view = getSite().getPage().findView("org.dawnsci.processing.ui.processingView");
		
		Object ob = view.getAdapter(IOperation.class);
		IOperation[] ops = null;
		
		if (ob.getClass().isArray() && Array.get(ob, 0) instanceof IOperation) {
			ops = (IOperation[])ob;
		}
		
		if (ops != null) {
			
			final IOperation[] fop = ops;
			
			context.setUserObject(new IProcessingConversionInfo() {

				@Override
				public IOperation[] getOperationSeries() {
					return fop;
				}

				@Override
				public IExecutionVisitor getExecutionVisitor(String fileName) {
					return new HierarchicalFileExecutionVisitor(fileName);
				}

			});
		}
	}

	@Override
	public void setFocus() {
		if (viewer != null) viewer.getTable().setFocus();

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

}

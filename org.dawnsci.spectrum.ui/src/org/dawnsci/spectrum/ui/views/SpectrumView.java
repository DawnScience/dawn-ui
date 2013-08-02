package org.dawnsci.spectrum.ui.views;


import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.filter.AbstractPlottingFilter;
import org.dawnsci.plotting.api.filter.IFilterDecorator;
import org.dawnsci.plotting.api.trace.ILineTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.spectrum.ui.file.ISpectrumFile;
import org.dawnsci.spectrum.ui.file.SpectrumInMemory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class SpectrumView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.dawnsci.spectrum.ui.views.SpectrumView";

	private TableViewer viewer;
	private IAction action1;
	private Action action2;
	private Action doubleClickAction;
	private IPartListener2 listener;
	private List<String> filenames = new ArrayList<String>();
	private IPlottingSystem system;
	private SpectrumFileManager manager;
	private DropTargetAdapter dropListener;

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	 
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			return manager.getFiles().toArray();
		}
	}
	
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		
		public String getText(Object obj) {
			if (obj instanceof ISpectrumFile) {
				return ((ISpectrumFile)obj).getName();
			}
			
			return "";
		}
		
		public Image getImage(Object obj) {
			
			if (obj instanceof SpectrumInMemory) return PlatformUI.getWorkbench().
					getSharedImages().getImage(ISharedImages.IMG_DEF_VIEW);
			
			return PlatformUI.getWorkbench().
					getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}
//	class NameSorter extends ViewerSorter {
//	}

	/**
	 * The constructor.
	 */
	public SpectrumView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		
		//viewer = new TreeViewer(parent);
		
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
//		viewer.setSorter(new NameSorter());
//		viewer.setInput(manager);
		
//		IActionBars bars = getViewSite().getActionBars();
		//bars.setGlobalActionHandler(ICommonActionConstants.OPEN,new SpectrumOpenAction("Open"));
		
		// add part listener for relevant editors and views
//				listener = new IPartListener2() {
//					@Override
//					public void partVisible(IWorkbenchPartReference partRef) {
//						IWorkbenchPart part = partRef.getPart(false);
//						System.err.println("part visible: " + part.getTitle());
//						IPlottingSystem system = PartUtils.getPlottingSystem(part);
//						if (system != null) {
//							String altPath = part instanceof IEditorPart ? EclipseUtils.getFilePath(((IEditorPart) part).getEditorInput()) : null;
//							
//							if (altPath != null) {
//								File file = new File(altPath);
//								
//								filenames.add(file.getName());
//							}
//							
//							Display.getDefault().syncExec(new Runnable() {
//								
//								@Override
//								public void run() {
//									viewer.refresh();
//									
//								}
//							});
//						}
//					}
//					
//					@Override
//					public void partOpened(IWorkbenchPartReference partRef) {
//						IWorkbenchPart part = partRef.getPart(false);
//						System.err.println("part opened: " + part.getTitle());
//					}
//					
//					@Override
//					public void partInputChanged(IWorkbenchPartReference partRef) {
//						IWorkbenchPart part = partRef.getPart(false);
//						System.err.println("input changed: " + part.getTitle());
//					}
//					
//					@Override
//					public void partHidden(IWorkbenchPartReference partRef) {
//					}
//					
//					@Override
//					public void partDeactivated(IWorkbenchPartReference partRef) {
//					}
//					
//					@Override
//					public void partClosed(IWorkbenchPartReference partRef) {
//						IWorkbenchPart part = partRef.getPart(false);
//						IPlottingSystem system = PartUtils.getPlottingSystem(part);
//						if (system != null) {
//							//removePlottingSystem(system);
//						}
//					}
//					
//					@Override
//					public void partBroughtToTop(IWorkbenchPartReference partRef) {
//					}
//					
//					@Override
//					public void partActivated(IWorkbenchPartReference partRef) {
//						IWorkbenchPart part = partRef.getPart(false);
//						System.err.println("part activated: " + part.getTitle());
//					}
//				};
//				getSite().getPage().addPartListener(listener);

		
		
		IWorkbenchPage page = getSite().getPage();
		IViewPart view = page.findView("org.dawnsci.spectrum.ui.views.SpectrumPlot");
		system = (IPlottingSystem)view.getAdapter(IPlottingSystem.class);
		manager = new SpectrumFileManager(system);
		viewer.setInput(manager);
		
		manager.addFileListener( new ISpectrumFileListener() {
			
			@Override
			public void fileLoaded(final SpectrumFileOpenedEvent event) {
				Display.getDefault().syncExec(new Runnable() {
					
					@Override
					public void run() {
						viewer.refresh();
						Table tab = viewer.getTable();
						tab.setSelection(tab.getItemCount()-1);
					}
				});
			}
		});
		
		getSite().setSelectionProvider(viewer);
		
		
		//TODO make this nasty cut-paste code better
		dropListener = new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				Object dropData = event.data;
				if (dropData instanceof TreeSelection) {
					TreeSelection selectedNode = (TreeSelection) dropData;
					Object obj[] = selectedNode.toArray();
					for (int i = 0; i < obj.length; i++) {
						if (obj[i] instanceof IFile) {
							IFile file = (IFile) obj[i];
							addFile(file.getRawLocation().toOSString());
						}
					}
				}
			}
			};
			DropTarget dt = new DropTarget(viewer.getControl(), DND.DROP_MOVE | DND.DROP_DEFAULT | DND.DROP_COPY);
			dt.setTransfer(new Transfer[] { TextTransfer.getInstance(),
					FileTransfer.getInstance(), ResourceTransfer.getInstance(),
					LocalSelectionTransfer.getTransfer() });
			dt.addDropListener(dropListener);
			
			// Create the help context id for the viewer's control
			PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "org.dawnsci.spectrum.viewer");
			makeActions();
			hookContextMenu();
			hookDoubleClickAction();
			contributeToActionBars();
		}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				SpectrumView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		
		manager.add(new Action("Average") {
			public void run() {
				ISelection selection = viewer.getSelection();
				List<ISpectrumFile> list = SpectrumUtils.getSpectrumFilesList((IStructuredSelection)selection);
				ISpectrumFile file = SpectrumUtils.averageSpectrumFiles(list,system);
				
				if (file == null) {
					showMessage("Could not calculate average, need to do something smarter here!");
				}
				
				SpectrumView.this.manager.addFile(file);
			}
		});
		
		if (((IStructuredSelection)viewer.getSelection()).size() == 2) {

			manager.add(new Action("Subtract") {
				public void run() {
					ISelection selection = viewer.getSelection();
					List<ISpectrumFile> list = SpectrumUtils.getSpectrumFilesList((IStructuredSelection)selection);
					ISpectrumFile file = SpectrumUtils.subtractSpectrumFiles(list,system);

					if (file == null) {
						showMessage("Could not calculate average, need to do something smarter here!");
					}

					SpectrumView.this.manager.addFile(file);
				}
			});
		}

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
	}

	private void makeActions() {
		
		action1  = createXYFiltersActions();

		action1.setText("Stack");
		action1.setToolTipText("Stack spectra");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		action2 = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				List<?> obj = ((IStructuredSelection)selection).toList();
				for (Object ob : obj) manager.removeFile(((ISpectrumFile)ob).getPath());
			}
		};
		action2.setText("Remove");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				showMessage("Double-click detected on "+obj.toString());
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Sample View",
			message);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		getSite().getPage().removePartListener(listener);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	public void addFile(String filePath) {
		
		manager.addFile(filePath);
		
		File file = new File(filePath);
		
		filenames.add(file.getName());
		
	}
	public IAction createXYFiltersActions() {

		final IFilterDecorator dec = PlottingFactory.createFilterDecorator(system);
		final AbstractPlottingFilter norm = new AbstractPlottingFilter() {

			@Override
			public int getRank() {
				return 1;
			}

			protected IDataset[] filter(IDataset x, IDataset y) {
				Collection<ITrace>  traces = system.getTraces(ILineTrace.class);
				IDataset newY = Maths.add(DatasetUtils.norm((AbstractDataset)y),(traces.size()*0.2));
				newY.setName(y.getName());

				return new IDataset[]{x, newY};
			}
		};

		IAction normal = new Action("Stack", IAction.AS_CHECK_BOX) {
			public void run(){	
				if (isChecked()) {
					dec.addFilter(norm);
					final Collection<ITrace> traces = system.getTraces(ILineTrace.class);

					for (ITrace trace: traces) system.removeTrace(trace);
					for (ITrace trace: traces) system.addTrace(trace);
					system.autoscaleAxes();

				} else {
//					dec.removeFilter(norm);
//					dec.reset();
					dec.clear();
				}
			}
		};
		
		return normal;
	}
}
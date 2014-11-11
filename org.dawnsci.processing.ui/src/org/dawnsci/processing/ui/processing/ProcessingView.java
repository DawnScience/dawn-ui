/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.processing.ui.processing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.dawb.common.services.IPersistenceService;
import org.dawb.common.services.IPersistentFile;
import org.dawb.common.services.ServiceManager;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.util.list.ListUtils;
import org.dawnsci.common.widgets.dialog.FileSelectionDialog;
import org.dawnsci.common.widgets.table.ISeriesItemDescriptor;
import org.dawnsci.common.widgets.table.SeriesTable;
import org.dawnsci.processing.ui.Activator;
import org.dawnsci.processing.ui.model.OperationDescriptor;
import org.dawnsci.processing.ui.preference.ProcessingConstants;
import org.dawnsci.processing.ui.slice.IOperationErrorInformer;
import org.dawnsci.processing.ui.slice.OperationInformerImpl;
import org.eclipse.core.resources.IFile;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A view for constructing and executing a processing pipeline.
 * 
 * IDEA initiate pipeline from secondary id that make the pipeline static unless they edit it.
 * 
 * @author Matthew Gerring
 *
 */
public class ProcessingView extends ViewPart {
	
	
	private SeriesTable               seriesTable;
	private OperationFilter           operationFiler;
	private List<OperationDescriptor> saved;
	private TableViewerColumn inputs, outputs;
	private IOperationErrorInformer informer;
	private IAction add;
	private IAction delete;
	private IAction clear;
	private String lastPath = null;
	
	
	private final static Logger logger = LoggerFactory.getLogger(ProcessingView.class);

	public ProcessingView() {
		this.seriesTable    = new SeriesTable();
		this.operationFiler = new OperationFilter();
	}
	
	@Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
		
		super.init(site, memento);

		final String key = memento!=null ? memento.getString(ProcessingConstants.OPERATION_IDS) : null;
		if (key!=null && !"".equals(key)) {
			List<String> ids = ListUtils.getList(key);
			this.saved = operationFiler.createDescriptors(ids);
		}
    }
    
	@Override
	public void createPartControl(Composite parent) {
		
		final Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(new GridLayout(1, false));
		GridUtils.removeMargins(content);
		
		
		OperationValidator val = new OperationValidator();
		informer = new OperationInformerImpl(seriesTable);
		val.setOperationErrorInformer(informer);
		operationFiler.setOperationErrorInformer(informer);

		seriesTable.setValidator(val);
		final OperationLabelProvider prov = new OperationLabelProvider(0);
		seriesTable.createControl(content, prov);
		seriesTable.registerSelectionProvider(getViewSite());	
		createToobarActions();
		final MenuManager rightClick = new MenuManager("#PopupMenu");
		rightClick.setRemoveAllWhenShown(true);
		//createActions(rightClick);
		rightClick.addMenuListener(new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				setDynamicMenuOptions(manager);
			}
		});
		createColumns();
		
		// Here's the data, lets show it!
		seriesTable.setMenuManager(rightClick);
		seriesTable.setInput(saved, operationFiler);
		DropTarget dt = seriesTable.getDropTarget();
		dt.setTransfer(new Transfer[] { TextTransfer.getInstance(),
				FileTransfer.getInstance(), ResourceTransfer.getInstance(),
				LocalSelectionTransfer.getTransfer() });
		dt.addDropListener(new DropTargetAdapter() {
			
			@Override
			public void drop(DropTargetEvent event) {
				Object dropData = event.data;
				if (dropData instanceof TreeSelection) {
					TreeSelection selectedNode = (TreeSelection) dropData;
					Object obj[] = selectedNode.toArray();
					for (int i = 0; i < obj.length; i++) {
						if (obj[i] instanceof IFile) {
							IFile file = (IFile) obj[i];
							readOperationsFromFile(file.getLocation().toOSString());
							return;
						}
					}
				} else if (dropData instanceof String[]) {
					for (String path : (String[])dropData){
						readOperationsFromFile(path);
						return;
					}
				}
				
			}
		});

	}
	
	private void readOperationsFromFile(String filename) {
		try {
			IPersistenceService service = (IPersistenceService)ServiceManager.getService(IPersistenceService.class);
			IOperationService os = (IOperationService)ServiceManager.getService(IOperationService.class);
			IPersistentFile pf = service.getPersistentFile(filename);
			IOperation<? extends IOperationModel, ? extends OperationData>[] operations = pf.getOperations();
			if (operations == null) return;
			List<OperationDescriptor> list = new ArrayList<OperationDescriptor>(operations.length);
			for (IOperation<? extends IOperationModel, ? extends OperationData> op : operations) list.add(new OperationDescriptor(op, os));
			
			if (operations != null) seriesTable.setInput(list, operationFiler);
		} catch (Exception e) {
			logger.error("Could not read operations from file", e);
		}
		
	}
	
	private void saveOperationsToFile(String filename, IOperation[] op) {
		try {
			
			IPersistenceService service = (IPersistenceService)ServiceManager.getService(IPersistenceService.class);
			IPersistentFile pf = service.getPersistentFile(filename);
			pf.setOperations(op);
		} catch (Exception e) {
			logger.error("Could not write operations to file", e);
		}
	}
	
	@Override
	public Object getAdapter(Class clazz) {
		
		if (clazz==IOperation.class) {
			return getOperations();
		}
		
		if (clazz == IOperationErrorInformer.class) {
			return informer;
		}
		return super.getAdapter(clazz);
	}
	
	private IOperation[] getOperations() {
		final List<ISeriesItemDescriptor> desi = seriesTable.getSeriesItems();
		if (desi==null || desi.isEmpty()) return null;
		final IOperation<? extends IOperationModel, ? extends OperationData>[] pipeline = new IOperation[desi.size()];
		for (int i = 0; i < desi.size(); i++) {
			try {
				pipeline[i] = (IOperation<? extends IOperationModel, ? extends OperationData>)desi.get(i).getSeriesObject();
			} catch (InstantiationException e) {
				e.printStackTrace();
				return null;
			}
			}
		return pipeline;
	}

	private void createColumns() {
		
		this.inputs  = seriesTable.createColumn("Input Rank",  SWT.LEFT, 0, new OperationLabelProvider(1));
		inputs.getColumn().setWidth(0);
		inputs.getColumn().setResizable(false);

		this.outputs = seriesTable.createColumn("Output Rank", SWT.LEFT, 0, new OperationLabelProvider(2));
		outputs.getColumn().setWidth(0);
		outputs.getColumn().setResizable(false);
		
	}

	private void createToobarActions() {
		add = new Action("Insert operation", Activator.getImageDescriptor("icons/clipboard-list.png")) {
			public void run() {
				seriesTable.addNew();
			}
		};


		delete = new Action("Delete selected operation", Activator.getImageDescriptor("icons/clipboard--minus.png")) {
			public void run() {
				seriesTable.delete();
			}
		};

		clear = new Action("Clear list of operations", Activator.getImageDescriptor("icons/clipboard-empty.png")) {
			public void run() {
			    boolean ok = MessageDialog.openQuestion(getViewSite().getShell(), "Confirm Clear Pipeline", "Do you want to clear the pipeline?");
			    if (!ok) return;
				seriesTable.clear();
			}
		};
		
		getViewSite().getActionBars().getToolBarManager().add(add);
		getViewSite().getActionBars().getMenuManager().add(add);
		getViewSite().getActionBars().getToolBarManager().add(delete);
		getViewSite().getActionBars().getMenuManager().add(delete);
		getViewSite().getActionBars().getToolBarManager().add(clear);
		getViewSite().getActionBars().getMenuManager().add(clear);
		getViewSite().getActionBars().getToolBarManager().add(new Separator());
		getViewSite().getActionBars().getMenuManager().add(new Separator());
		
		final IAction save = new Action("Save configured pipeline", IAction.AS_PUSH_BUTTON) {
			public void run() {
				
				IOperation[] op = getOperations();
				
				if (op == null) return;
				FileSelectionDialog dialog = new FileSelectionDialog(ProcessingView.this.getSite().getShell());
				if (lastPath != null) dialog.setPath(lastPath);
				dialog.setNewFile(true);
				dialog.setFolderSelector(false);
				
				dialog.create();
				if (dialog.open() == Dialog.CANCEL) return;
				String path = dialog.getPath();
				saveOperationsToFile(path, op);
				lastPath = path;
			}
		};
		
		final IAction load = new Action("Load configured pipeline", IAction.AS_PUSH_BUTTON) {
			public void run() {
				
				FileSelectionDialog dialog = new FileSelectionDialog(ProcessingView.this.getSite().getShell());
				dialog.setNewFile(false);
				dialog.setFolderSelector(false);
				if (lastPath != null) dialog.setPath(lastPath);
				
				dialog.create();
				if (dialog.open() == Dialog.CANCEL) return;
				String path = dialog.getPath();
				readOperationsFromFile(path);
				lastPath = path;
			}
		};
		save.setImageDescriptor(Activator.getImageDescriptor("icons/mask-import-wiz.png"));
		load.setImageDescriptor(Activator.getImageDescriptor("icons/mask-export-wiz.png"));
	
		getViewSite().getActionBars().getToolBarManager().add(save);
		getViewSite().getActionBars().getMenuManager().add(save);
		getViewSite().getActionBars().getToolBarManager().add(load);
		getViewSite().getActionBars().getMenuManager().add(load);
		
		getViewSite().getActionBars().getToolBarManager().add(new Separator());
		getViewSite().getActionBars().getMenuManager().add(new Separator());
		
		final IAction lock = new Action("Lock pipeline editing", IAction.AS_CHECK_BOX) {
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(ProcessingConstants.LOCK_PIPELINE, isChecked());
				seriesTable.setLockEditing(isChecked());
				add.setEnabled(!isChecked());
				delete.setEnabled(!isChecked());
				clear.setEnabled(!isChecked());
			}
		};
		lock.setImageDescriptor(Activator.getImageDescriptor("icons/lock.png"));

		lock.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(ProcessingConstants.LOCK_PIPELINE));
		add.setEnabled(!lock.isChecked());
		delete.setEnabled(!lock.isChecked());
		clear.setEnabled(!lock.isChecked());
		seriesTable.setLockEditing(lock.isChecked());
		
		getViewSite().getActionBars().getToolBarManager().add(lock);
		getViewSite().getActionBars().getMenuManager().add(lock);

		final IAction showRanks = new Action("Show input and output ranks", IAction.AS_CHECK_BOX) {
			public void run() {
				inputs.getColumn().setWidth(isChecked() ? 100 : 0);
				inputs.getColumn().setResizable(isChecked() ? true : false);
				outputs.getColumn().setWidth(isChecked() ? 100 : 0);
				outputs.getColumn().setResizable(isChecked() ? true : false);
			}
		};
		showRanks.setImageDescriptor(Activator.getImageDescriptor("icons/application-tile-horizontal.png"));

		getViewSite().getActionBars().getToolBarManager().add(showRanks);
		getViewSite().getActionBars().getMenuManager().add(showRanks);
	}
	
	private void setDynamicMenuOptions(IMenuManager mm) {
		
		mm.add(add);
		mm.add(delete);
		mm.add(clear);
		mm.add(new Separator());
		
		IOperation<? extends IOperationModel, ? extends OperationData> op = null;
		
		try {
			ISeriesItemDescriptor selected = seriesTable.getSelected();
			if (!(selected instanceof OperationDescriptor)) return;
			op = ((OperationDescriptor)selected).getSeriesObject();
		} catch (InstantiationException e1) {
		}
		
		final IAction saveInter = new Action("Save output", IAction.AS_CHECK_BOX) {
			public void run() {
				ISeriesItemDescriptor current = seriesTable.getSelected();
				if (current instanceof OperationDescriptor) {
					try {
						((OperationDescriptor)current).getSeriesObject().setStoreOutput(isChecked());
						seriesTable.refreshTable();
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		
		if (op != null && op.isStoreOutput()) saveInter.setChecked(true);
		
		mm.add(saveInter);
		
		final IAction passUnMod = new Action("Pass through", IAction.AS_CHECK_BOX) {
			public void run() {
				ISeriesItemDescriptor current = seriesTable.getSelected();
				if (current instanceof OperationDescriptor) {
					try {
						((OperationDescriptor)current).getSeriesObject().setPassUnmodifiedData(isChecked());
						seriesTable.refreshTable();
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		
		if (op != null && op.isPassUnmodifiedData()) passUnMod.setChecked(true);
		mm.add(passUnMod);
	}

	@Override
	public void setFocus() {
		seriesTable.setFocus();
	}

    public void saveState(IMemento memento) {
    	memento.putString(ProcessingConstants.OPERATION_IDS, createIdList(seriesTable.getSeriesItems()));
    }

	private String createIdList(Collection<ISeriesItemDescriptor> seriesItems) {
		if (seriesItems==null || seriesItems.isEmpty()) return null;
		final StringBuilder buf = new StringBuilder();
		for (Iterator<ISeriesItemDescriptor> iterator = seriesItems.iterator(); iterator.hasNext();) {
			ISeriesItemDescriptor des = iterator.next();
			if (!(des instanceof OperationDescriptor)) continue;
			OperationDescriptor  odes = (OperationDescriptor)des;
			buf.append(odes.getId());
			if(iterator.hasNext()) buf.append(",");
		}
		return buf.toString();
	}
}


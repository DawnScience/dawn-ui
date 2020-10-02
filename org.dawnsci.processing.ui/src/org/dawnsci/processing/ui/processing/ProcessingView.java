/*
 * Copy (c) 2012 Diamond Light Source Ltd.
 *
 * All s reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.processing.ui.processing;

import java.io.File;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.util.list.ListUtils;
import org.dawnsci.common.widgets.dialog.FileSelectionDialog;
import org.dawnsci.processing.ui.Activator;
import org.dawnsci.processing.ui.ProcessingEventConstants;
import org.dawnsci.processing.ui.preference.ProcessingConstants;
import org.dawnsci.processing.ui.slice.OperationInformerImpl;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.richbeans.widgets.table.ISeriesItemDescriptor;
import org.eclipse.richbeans.widgets.table.SeriesTable;
import org.eclipse.richbeans.widgets.table.event.SeriesItemEvent;
import org.eclipse.richbeans.widgets.table.event.SeriesItemListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
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
	
	public static final String ID = "org.dawnsci.processing.ui.processingView";

	private SeriesTable               seriesTable;
	private OperationFilter           operationFilter;
	private List<OperationDescriptor> saved;
	private TableViewerColumn inputs, outputs;
	private IAction add;
	private IAction delete;
	private IAction clear;
	private String lastPath = null;
	
	private final static String[] extensions = new String[]{"nxs"};
	private final static String[] files = new String[]{"Nexus files"};
	
	private final static Logger logger = LoggerFactory.getLogger(ProcessingView.class);

	private static final char DISABLE_PREFIX = '!';

	public ProcessingView() {
		this.seriesTable    = new SeriesTable();
		this.seriesTable.addSeriesEventListener(new SeriesItemListener() {
			
			@Override
			public void itemRemoved(SeriesItemEvent evt) {
				ISeriesItemDescriptor descriptor = evt.getDescriptor();
				if (descriptor instanceof OperationDescriptor) {
					OperationDescriptor operationDescriptor = (OperationDescriptor) descriptor;
					try {
						operationDescriptor.getSeriesObject().destroy();
					} catch (Exception e) {
						logger.warn("Could not get operation from descriptor", e);
					}
				}
			}

			@Override
			public void itemAdded(SeriesItemEvent evt) {
				// do nothing
			}
		});
		this.operationFilter = new OperationFilter();
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);

		final String key = memento!=null ? memento.getString(ProcessingConstants.OPERATION_IDS) : null;
		if (key!=null && !"".equals(key)) {
			List<String> ids = ListUtils.getList(key);
			int n = ids.size();
			boolean[] disabled = new boolean[n];
			for (int i = 0; i < n; i++) {
				String id = ids.get(i);
				if (id.charAt(0) == DISABLE_PREFIX) {
					disabled[i] = true;
					ids.set(i, id.substring(1));
				}
			}
			this.saved = operationFilter.createDescriptors(ids);
			for (int i = 0; i < n; i++) {
				if (disabled[i]) {
					saved.get(i).setEnabled(false);
				}
			}
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		
		final Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(new GridLayout(1, false));
		GridUtils.removeMargins(content);
		
		
		OperationValidator val = new OperationValidator();
		final OperationInformerImpl informer = new OperationInformerImpl(seriesTable);
		val.setOperationErrorInformer(informer);
		operationFilter.setOperationErrorInformer(informer);

		seriesTable.setValidator(val);
		final OperationLabelProvider prov = new OperationLabelProvider(0);
		seriesTable.createControl(content, prov);
		getViewSite().setSelectionProvider(seriesTable.getSelectionProvider());
		createToolbarActions();
		final MenuManager Click = new MenuManager("#PopupMenu");
		Click.setRemoveAllWhenShown(true);
		//createActions(Click);
		Click.addMenuListener(new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				OperationTableUtils.addMenuItems(manager, seriesTable, getViewSite().getShell());
			}
		});
		createColumns();
		
		// Here's the data, lets show it!
		seriesTable.setMenuManager(Click);
		seriesTable.setInput(saved, operationFilter);

		OperationTableUtils.setupPipelinePaneDropTarget(seriesTable, operationFilter, logger, getSite().getShell());
		
		BundleContext ctx = FrameworkUtil.getBundle(ProcessingView.class).getBundleContext();
		EventHandler ErrorHandler = new EventHandler() {
			
			@Override
			public void handleEvent(Event event) {
				OperationException e = (OperationException)event.getProperty("error");
				informer.setInErrorState(e);
			}
		};
		
		Dictionary<String, String> props = new Hashtable<>();
		props.put(EventConstants.EVENT_TOPIC, ProcessingEventConstants.ERROR);
		ctx.registerService(EventHandler.class, ErrorHandler, props);
		
		
		EventHandler initialHandler = new EventHandler() {
			
			@Override
			public void handleEvent(Event event) {
				IDataset data = (IDataset)event.getProperty("data");
				informer.setTestData(data);
			}
		};
		
		props = new Hashtable<>();
		props.put(EventConstants.EVENT_TOPIC, ProcessingEventConstants.INITIAL_UPDATE);
		ctx.registerService(EventHandler.class, initialHandler, props);
	}
	
	private void saveOperationsToFile(String filename, IOperation<?,?>[] op) {
		try {
			
			if (new File(filename).exists()) {
				MessageDialog.openInformation(getSite().getShell(), "File error", "Overwriting of processing information forbidden, please select a new file.");
				return;
			}
			
			IPersistenceService service = Activator.getService(IPersistenceService.class);
			IPersistentFile pf = service.createPersistentFile(filename);
			pf.setOperations(op);
			pf.close();
		} catch (Exception e) {
			logger.error("Could not write operations to file", e);
			MessageDialog.openInformation(getSite().getShell(), "Exception while writing operations to file", "An exception occurred while writing the operations to a file.\n" + e.getMessage());
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object getAdapter(Class clazz) {
		
		if (clazz==IOperation.class) {
			return OperationTableUtils.getOperations(logger, seriesTable.getSeriesItems());
		}
		
		return super.getAdapter(clazz);
	}
	
	private void createColumns() {
		
		this.inputs  = seriesTable.createColumn("Input Rank",  SWT.LEFT, 0, new OperationLabelProvider(1));
		inputs.getColumn().setWidth(0);
		inputs.getColumn().setResizable(false);

		this.outputs = seriesTable.createColumn("Output Rank", SWT.LEFT, 0, new OperationLabelProvider(2));
		outputs.getColumn().setWidth(0);
		outputs.getColumn().setResizable(false);
		
	}

	private void createToolbarActions() {
		add = OperationTableUtils.getAddAction(seriesTable);

		delete = OperationTableUtils.getDeleteAction(seriesTable);

		clear = OperationTableUtils.getClearAction(seriesTable, getViewSite().getShell());
		
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
				
				IOperation<?,?>[] op = OperationTableUtils.getOperations(logger, seriesTable.getSeriesItems());

				if (op == null) return;
				FileSelectionDialog dialog = new FileSelectionDialog(ProcessingView.this.getSite().getShell());
				if (lastPath != null) dialog.setPath(lastPath);
				dialog.setExtensions(extensions);
				dialog.setNewFile(true);
				dialog.setFolderSelector(false);
				
				dialog.create();
				if (dialog.open() == Dialog.CANCEL) return;
				String path = dialog.getPath();
				if (!path.endsWith(extensions[0])) { //pipeline should always be saved to .nxs
					path = path.concat("." + extensions[0]);
					logger.info("Extension added to path, file will be saved as " + path);
				}
				saveOperationsToFile(path, op);
				lastPath = path;
			}
		};
		
		final IAction load = new Action("Load configured pipeline", IAction.AS_PUSH_BUTTON) {
			public void run() {
				Shell shell = getSite().getShell();
				FileSelectionDialog dialog = new FileSelectionDialog(shell);
				dialog.setExtensions(extensions);
				dialog.setFiles(files);
				dialog.setNewFile(false);
				dialog.setFolderSelector(false);
				if (lastPath != null) dialog.setPath(lastPath);
				
				dialog.create();
				if (dialog.open() == Dialog.CANCEL) return;
				String path = dialog.getPath();
				String dataFile = OperationTableUtils.readOperationsFromFile(path, seriesTable, operationFilter, logger, shell);
				lastPath = path;
				if (dataFile != null) {
					OperationTableUtils.confirmAddFileForProcessing(shell, dataFile);
				}
			}
		};
		save.setImageDescriptor(Activator.getImageDescriptor("icons/save_edit.png"));
		load.setImageDescriptor(Activator.getImageDescriptor("icons/import_wiz.png"));
	
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
			if (!odes.isEnabled()) {
				buf.append(DISABLE_PREFIX);
			}
			buf.append(odes.getId());
			if(iterator.hasNext()) buf.append(",");
		}
		return buf.toString();
	}
}


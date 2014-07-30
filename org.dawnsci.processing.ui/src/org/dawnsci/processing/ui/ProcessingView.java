package org.dawnsci.processing.ui;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.util.list.ListUtils;
import org.dawnsci.common.widgets.table.ISeriesItemDescriptor;
import org.dawnsci.common.widgets.table.SeriesTable;
import org.dawnsci.processing.ui.preference.ProcessingConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 * A view for constructing and executing a processing pipeline.
 * 
 * IDEA initiate pipeline from secondary id that make the pipeline static unless they edit it.
 * 
 * @author fcp94556
 *
 */
public class ProcessingView extends ViewPart {
	
	
	private SeriesTable               seriesTable;
	private OperationFilter           operationFiler;
	private List<OperationDescriptor> saved;
	private TableViewerColumn inputs, outputs;

	public ProcessingView() {
		this.seriesTable    = new SeriesTable();
		this.operationFiler = new OperationFilter();
	}
	
	@Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);

		final String key = memento.getString(ProcessingConstants.OPERATION_IDS);
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

		seriesTable.setValidator(new OperationValidator());
		final OperationLabelProvider prov = new OperationLabelProvider();
		seriesTable.createControl(content, prov);
		seriesTable.registerSelectionProvider(getViewSite());		
		
		final MenuManager rightClick = new MenuManager();
		createActions(rightClick);
		createColumns(prov);
		
		// Here's the data, lets show it!
		seriesTable.setMenuManager(rightClick);
		seriesTable.setInput(saved, operationFiler);

	}

	private void createColumns(OperationLabelProvider prov) {
		
		this.inputs  = seriesTable.createColumn("Input Rank",  SWT.LEFT, 0, prov);
		inputs.getColumn().setWidth(0);
		inputs.getColumn().setResizable(false);

		this.outputs = seriesTable.createColumn("Output Rank", SWT.LEFT, 0, prov);
		outputs.getColumn().setWidth(0);
		outputs.getColumn().setResizable(false);
		
	}

	private void createActions(IContributionManager rightClick) {
		
		final IAction add = new Action("Add operation to pipeline", Activator.getImageDescriptor("icons/clipboard-list.png")) {
			public void run() {
				seriesTable.addNew();
			}
		};
		getViewSite().getActionBars().getToolBarManager().add(add);
		getViewSite().getActionBars().getMenuManager().add(add);
		rightClick.add(add);

		final IAction delete = new Action("Delete selected operation", Activator.getImageDescriptor("icons/clipboard--minus.png")) {
			public void run() {
				seriesTable.delete();
			}
		};
		getViewSite().getActionBars().getToolBarManager().add(delete);
		getViewSite().getActionBars().getMenuManager().add(delete);
		rightClick.add(delete);

		
		final IAction clear = new Action("Clear list of operations", Activator.getImageDescriptor("icons/clipboard-empty.png")) {
			public void run() {
			    boolean ok = MessageDialog.openQuestion(getViewSite().getShell(), "Confirm Clear Pipeline", "Do you want to clear the pipeline?");
			    if (!ok) return;
				seriesTable.clear();
			}
		};
		getViewSite().getActionBars().getToolBarManager().add(clear);
		getViewSite().getActionBars().getMenuManager().add(clear);
		rightClick.add(clear);

		
		getViewSite().getActionBars().getToolBarManager().add(new Separator());
		getViewSite().getActionBars().getMenuManager().add(new Separator());
		rightClick.add(new Separator());
		
		final IAction lock = new Action("Lock pipeline editing", IAction.AS_CHECK_BOX) {
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(ProcessingConstants.LOCK_PIPELINE, isChecked());
				seriesTable.setLockEditing(isChecked());
				add.setEnabled(!isChecked());
				clear.setEnabled(!isChecked());
			}
		};
		lock.setImageDescriptor(Activator.getImageDescriptor("icons/lock.png"));

		lock.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(ProcessingConstants.LOCK_PIPELINE));
		add.setEnabled(!lock.isChecked());
		clear.setEnabled(!lock.isChecked());
		seriesTable.setLockEditing(lock.isChecked());
		
		getViewSite().getActionBars().getToolBarManager().add(lock);
		getViewSite().getActionBars().getMenuManager().add(lock);
		rightClick.add(lock);

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
		rightClick.add(showRanks);

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

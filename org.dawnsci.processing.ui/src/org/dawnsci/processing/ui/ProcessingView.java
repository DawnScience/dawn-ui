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
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
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

		seriesTable.createControl(content, new LabelProvider());
		seriesTable.registerSelectionProvider(getViewSite());		
		seriesTable.setInput(saved, operationFiler);
		
		createActions();
	}

	private void createActions() {
		
		final IAction add = new Action("Add operation to pipeline", Activator.getImageDescriptor("icons/clipboard-list.png")) {
			public void run() {
				seriesTable.addNew();
			}
		};
		getViewSite().getActionBars().getToolBarManager().add(add);
		getViewSite().getActionBars().getMenuManager().add(add);


		
		final IAction clear = new Action("Clear list of operations", Activator.getImageDescriptor("icons/clipboard-empty.png")) {
			public void run() {
			    boolean ok = MessageDialog.openQuestion(getViewSite().getShell(), "Confirm Clear Pipeline", "Do you want to clear the pipeline?");
			    if (!ok) return;
				seriesTable.clear();
			}
		};
		getViewSite().getActionBars().getToolBarManager().add(clear);
		getViewSite().getActionBars().getMenuManager().add(clear);

		
		getViewSite().getActionBars().getToolBarManager().add(new Separator());
		getViewSite().getActionBars().getMenuManager().add(new Separator());
		
		final IAction lock = new Action("Lock pipeline editing", IAction.AS_CHECK_BOX) {
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(ProcessingConstants.LOCK_PIPELINE, isChecked());
				seriesTable.setLockEditing(isChecked());
			}
		};
		lock.setImageDescriptor(Activator.getImageDescriptor("icons/lock.png"));
		
		getViewSite().getActionBars().getToolBarManager().add(lock);
		getViewSite().getActionBars().getMenuManager().add(lock);

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

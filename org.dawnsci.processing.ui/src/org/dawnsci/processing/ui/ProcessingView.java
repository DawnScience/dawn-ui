package org.dawnsci.processing.ui;

import org.dawb.common.ui.util.GridUtils;
import org.dawnsci.common.widgets.table.ISeriesItemFilter;
import org.dawnsci.common.widgets.table.SeriesTable;
import org.dawnsci.processing.ui.preference.ProcessingConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
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
	
	
	private SeriesTable seriesTable;

	public ProcessingView() {
		this.seriesTable = new SeriesTable();
	}

	@Override
	public void createPartControl(Composite parent) {
		
		final Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(new GridLayout(1, false));
		GridUtils.removeMargins(content);

		seriesTable.createControl(content, new LabelProvider());
		seriesTable.registerSelectionProvider(getViewSite());		
		seriesTable.setInput(null/** TODO Save in memento last list?**/, createSeriesProvider());
		
		createActions();
	}

	private void createActions() {
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
	
	private ISeriesItemFilter createSeriesProvider() {
		return new OperationFilter();
	}
}

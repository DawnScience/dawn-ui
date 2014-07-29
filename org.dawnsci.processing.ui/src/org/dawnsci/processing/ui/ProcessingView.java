package org.dawnsci.processing.ui;

import org.dawb.common.ui.util.GridUtils;
import org.dawnsci.common.widgets.table.ISeriesItemFilter;
import org.dawnsci.common.widgets.table.SeriesTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * A view for constructing and executing a processing pipeline.
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
		
	}

	@Override
	public void setFocus() {
		seriesTable.setFocus();
	}
	
	private ISeriesItemFilter createSeriesProvider() {
		return new OperationFilter();
	}
}

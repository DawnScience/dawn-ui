package org.dawnsci.plotting.tools.finding;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.peakfinding.Peak;


/**
 * @author Dean P. Ottewell
 */
public class PeakFindingSetupWidget {

	private final Logger logger = LoggerFactory.getLogger(PeakFindingSetupWidget.class);
	
	private Composite composite;
	private TableViewer viewer;
	
	public void createControl(final Composite composite) {
		this.composite = composite;
		
		Composite resultsComposite = new Composite(composite, SWT.BORDER);
		resultsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		resultsComposite.setLayout(new GridLayout(1, false));

		viewer = new TableViewer(resultsComposite,
				SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		createColumns(viewer);

		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		// viewer.setContentProvider(createContentProvider()); //TODO: get same
		// setup as peakfindingview
		viewer.setContentProvider(new ArrayContentProvider()); // TODO: could
																// ignore for
																// now
		//viewer.setInput(peaks); TODO: how pass need data contoller
		viewer.refresh();
	}

	protected List<TableViewerColumn> createColumns(final TableViewer viewer) {
		// TODO: Viewer comparator needed to sort peaks ... wonder if generic
		// one to look as this is pretty generic cases. Sort on val really
		List<TableViewerColumn> ret = new ArrayList<TableViewerColumn>(3);

		TableViewerColumn table = new TableViewerColumn(viewer, SWT.LEFT, 0);

		// TODO: different selectiosnneed to trigger certain events..
		// TODO: lablerprivuider for peaks ans might want to know algorithm run
		// or more data
		// TODO: selection listener table.getColumn().addSelectionListener();
		// TODO: maybe we can put the peakNo in a class then extends
		// ColumnLabelProvider and have details selected through that
		table.getColumn().setText("Peak Name");
		table.getColumn().setWidth(80);
		table.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Peak peakNo = (Peak) element;
				return peakNo.getName();
			}
		});
		ret.add(table);

		table = new TableViewerColumn(viewer, SWT.LEFT, 1);
		table.getColumn().setText("x");
		table.getColumn().setWidth(80);
		table.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Peak peakNo = (Peak) element;
				return peakNo.getX().toString();
			}
		});
		ret.add(table);

		table = new TableViewerColumn(viewer, SWT.LEFT, 2);
		table.getColumn().setText("y");
		table.getColumn().setWidth(150);
		table.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Peak peakNo = (Peak) element;
				return peakNo.getY().toString();
			}
		});
		ret.add(table);

		return ret;
	}
}

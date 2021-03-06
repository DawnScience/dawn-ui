package org.dawnsci.processing.ui.savu;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

public class SavuParameterEditor extends Composite {

	private TableViewer tableViewer;
	private final SavuParameterEditorTableViewModel viewModel;
	
	public SavuParameterEditor(Composite parent, SavuParameterEditorTableViewModel viewModel, int style) {
		super(parent,style);
		setLayout(new GridLayout(1, true));
		this.viewModel = viewModel;
	}

	public void initialiseTable() {
		buildAndLayoutTable();
		updateTable();
	}
	
	private void buildAndLayoutTable() {

		final Table table = new Table(this, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setSize(1000, 600);
		table.setLayoutData(layoutData);
		
		
		tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		
		TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE, 0);
		viewerColumn.getColumn().setText("Name");
		viewerColumn.getColumn().setWidth(100);
		viewerColumn.setLabelProvider(new SavuParameterEditorLabelProvider(0));
		SavuParameterEditingSupport regionEditor = new SavuParameterEditingSupport(viewModel, tableViewer, 0);
		viewerColumn.setEditingSupport(regionEditor);
		
		TableViewerColumn viewerColumn1 = new TableViewerColumn(tableViewer, SWT.NONE, 1);
		viewerColumn1.getColumn().setText("Value");
		viewerColumn1.getColumn().setWidth(400);
		viewerColumn1.setLabelProvider(new SavuParameterEditorLabelProvider(1));
		SavuParameterEditingSupport regionEditor1 = new SavuParameterEditingSupport(viewModel,tableViewer, 1);
		viewerColumn1.setEditingSupport(regionEditor1);

		TableViewerColumn viewerColumn2 = new TableViewerColumn(tableViewer, SWT.NONE, 2);
		viewerColumn2.getColumn().setText("Description");
		viewerColumn2.getColumn().setWidth(400);
		viewerColumn2.setLabelProvider(new SavuParameterEditorLabelProvider(2));
		SavuParameterEditingSupport regionEditor2 = new SavuParameterEditingSupport(viewModel,tableViewer, 2);
		viewerColumn2.setEditingSupport(regionEditor2);	

	}

	public void updateTable() {
		tableViewer.setInput(viewModel.getValues());
		//tableViewer.getTable().pack();
	}
}

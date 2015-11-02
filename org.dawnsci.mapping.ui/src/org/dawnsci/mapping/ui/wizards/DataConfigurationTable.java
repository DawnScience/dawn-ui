package org.dawnsci.mapping.ui.wizards;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class DataConfigurationTable {

	private TableViewer          tableViewer;
	private TableViewerColumn map; 
	
	
	public void createControl(Composite parent) {
		
		tableViewer = new TableViewer(parent, SWT.FULL_SELECTION);
		
		final TableViewerColumn dim   = new TableViewerColumn(tableViewer, SWT.LEFT, 0);
		dim.getColumn().setText("Dim");
		dim.getColumn().setWidth(100);
		dim.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Dimension dim = (Dimension)element;
			  return dim.getDimensionWithSize();
			}
		});
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.getTable().setHeaderVisible(true);
		
		map = new TableViewerColumn(tableViewer, SWT.CENTER, 1);
		map.getColumn().setText("map");
		map.getColumn().setWidth(150);
		map.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Dimension dim = (Dimension)element;
			  return dim.getDescription() == null ? "" : dim.getDescription();
			}
		});

		final TableViewerColumn axis   = new TableViewerColumn(tableViewer, SWT.CENTER, 2);
		axis.getColumn().setText("axes");
		axis.getColumn().setWidth(200);
		axis.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Dimension dim = (Dimension)element;
			  return dim.getAxis() == null ? "" : dim.getAxis();
			}
		});
		
		axis.setEditingSupport(new AxisEditSupport(tableViewer));
		
		
	}
	
	public void setLayout(Object layoutData) {
		tableViewer.getTable().setLayoutData(layoutData);
	}
	
	public void setInput(String[] options,Dimension[] dims) {
		
		map.setEditingSupport(new DimensionEditSupport(tableViewer,options,dims));
		tableViewer.setInput(dims);
		tableViewer.getTable().getParent().layout();
	}
	
	public void clearAll() {
		tableViewer.getTable().clearAll();
	}
	
}

/*
 * Copyright (c) 2016-2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.fileviewer.table;

import java.io.File;

import org.dawnsci.fileviewer.FileViewer;
import org.dawnsci.fileviewer.Utils.SortDirection;
import org.dawnsci.fileviewer.Utils.SortType;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;

/**
 * Custom comparator based off the Eclipse TableViewer sorter snippet example
 * {@link http://git.eclipse.org/c/platform/eclipse.platform.ui.git/tree/examples/org.eclipse.jface.snippets/Eclipse%20JFace%20Snippets/org/eclipse/jface/snippets/viewers/Snippet040TableViewerSorting.java}
 * 
 * @author Baha El Kassaby
 *
 */
public class FileTableViewerComparator {

	private SortDirection direction = SortDirection.NONE;
	private TableViewerColumn column;
	private ColumnViewer viewer;
	private int index;
	private FileViewer fileViewer;
	private FileTableExplorer tableExplorer;

	public FileTableViewerComparator(FileTableExplorer tableExplorer, FileViewer fileViewer, TableViewerColumn column,
			int idx) {
		this.column = column;
		this.fileViewer = fileViewer;
		this.tableExplorer = tableExplorer;
		this.viewer = tableExplorer.getTableViewer();
		SelectionAdapter selectionAdapter = createSelectionAdapter();
		this.column.getColumn().addSelectionListener(selectionAdapter);
		this.index = idx;
	}

	private SelectionAdapter createSelectionAdapter() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SortDirection tdirection = FileTableViewerComparator.this.direction;
				if (tdirection == SortDirection.ASC || tdirection == SortDirection.NONE) {
					setSorter(FileTableViewerComparator.this, SortDirection.DESC);
				} else if (tdirection == SortDirection.DESC) {
					setSorter(FileTableViewerComparator.this, SortDirection.ASC);
				}
			}
		};
	}

	private void setSorter(FileTableViewerComparator sorter, SortDirection direction) {
		Table columnParent = column.getColumn().getParent();
		// Re sort the file[] array
		try {
			tableExplorer.setSortType(SortType.values()[index]);
			File dir = fileViewer.getCurrentDirectory();
			tableExplorer.workerUpdate(dir, true, tableExplorer.getSortType(), direction);
		} finally {
			if (direction == SortDirection.NONE) {
				columnParent.setSortColumn(null);
				columnParent.setSortDirection(SWT.NONE);
				viewer.setComparator(null);
			} else {
				columnParent.setSortColumn(column.getColumn());
				sorter.direction = direction;
				columnParent.setSortDirection(direction == SortDirection.ASC ? SWT.DOWN : SWT.UP);
				viewer.refresh();
			}
		}
	}

}
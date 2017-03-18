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

	public static final int ASC = 1;
	public static final int NONE = 0;
	public static final int DESC = -1;

	private int direction = 0;
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
				int tdirection = FileTableViewerComparator.this.direction;
				if (tdirection == ASC || tdirection == NONE) {
					setSorter(FileTableViewerComparator.this, DESC);
				} else if (tdirection == DESC) {
					setSorter(FileTableViewerComparator.this, ASC);
				}
			}
		};
	}

	private void setSorter(FileTableViewerComparator sorter, int direction) {
		Table columnParent = column.getColumn().getParent();
		// Re sort the file[] array
		try {
			tableExplorer.setSortType(getSortTypeByIdx(index));
			File dir = fileViewer.getCurrentDirectory();
			tableExplorer.workerUpdate(dir, true, tableExplorer.getSortType(), direction);
		} finally {
			if (direction == NONE) {
				columnParent.setSortColumn(null);
				columnParent.setSortDirection(SWT.NONE);
				viewer.setComparator(null);
			} else {
				columnParent.setSortColumn(column.getColumn());
				sorter.direction = direction;
				columnParent.setSortDirection(direction == ASC ? SWT.DOWN : SWT.UP);
				viewer.refresh();
			}
		}
	}

	private SortType getSortTypeByIdx(int i) {
		if (i == 0)
			return SortType.NAME;
		else if (i == 1)
			return SortType.SIZE;
		else if (i == 2)
			return SortType.TYPE;
		else if (i == 3)
			return SortType.DATE;
		else if (i == 4)
			return SortType.SCAN;
		return null;
	}
}
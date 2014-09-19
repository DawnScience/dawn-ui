/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.fitting;

import org.dawb.common.util.list.SortNatural;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

class FittingViewerComparator extends ViewerComparator {

	private int propertyIndex;
	private static final int DESCENDING = 1;
	private int direction = DESCENDING;
	private SortNatural<String> comparator;

	public FittingViewerComparator() {
		this.propertyIndex = 0;
		direction = DESCENDING;
		this.comparator = new SortNatural<String>(false);
	}

	public int getDirection() {
		return direction == 1 ? SWT.DOWN : SWT.UP;
	}

	public void setColumn(int column) {
		if (column == this.propertyIndex) {
			// Same column as last sort; toggle the direction
			direction = 1 - direction;
		} else {
			// New column; do an ascending sort
			this.propertyIndex = column;
			direction = DESCENDING;
		}
	}

	@Override
	public int compare(Viewer v, Object e1, Object e2) {
		
		final TableViewer viewer = (TableViewer)v;
		ColumnLabelProvider  labelProv = (ColumnLabelProvider)viewer.getLabelProvider(propertyIndex);
		
		// Search on what the user actually sees!
		String val1 = labelProv.getText(e1);
		String val2 = labelProv.getText(e2);
		
		int rc = comparator.compare(val1, val2);
		
		// If descending order, flip the direction
		if (direction == DESCENDING) {
			rc = -rc;
		}
		return rc;
	}
}

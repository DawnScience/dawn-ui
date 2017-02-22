/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.fitting;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

public class PeakColumnComparitor extends ViewerComparator {
	private int propertyIndex;
	private static final int DESCENDING = 1;
	private int direction = DESCENDING;

	public PeakColumnComparitor() {
		this.propertyIndex = 0;
		direction = DESCENDING;
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
	public int compare(Viewer viewer, Object e1, Object e2) {

		if (!(e1 instanceof FittedFunction)) return 0;
		if (!(e2 instanceof FittedFunction)) return 0;

		FittedFunction f1 = (FittedFunction) e1;
		FittedFunction f2 = (FittedFunction) e2;
		int rc = 0;
		
		Double val1 = null;
		Double val2 = null;

		switch (propertyIndex) {
		case 0:
			rc = f1.getDataTrace().getName().compareTo(f2.getDataTrace().getName());
			break;
		case 1:
			rc = f1.getPeakName().compareTo(f2.getPeakName());
			break;
		case 2:
			val1 = f1.getPosition();
			val2 = f2.getPosition();
			rc = val1.compareTo(val2);
			break;
		case 3:
			val1 = f1.getDataValue();
			val2 = f2.getDataValue();
			rc = val1.compareTo(val2);
			break;
		case 4:
			val1 = f1.getPeakValue();
			val2 = f2.getPeakValue();
			rc = val1.compareTo(val2);
			break;
		case 5:
			val1 = f1.getFWHM();
			val2 = f2.getFWHM();
			rc = val1.compareTo(val2);
			break;
		case 6:
			val1 = f1.getArea();
			val2 = f2.getArea();
			rc = val1.compareTo(val2);
			break;
		case 7:
			f1.getPeakType().compareTo(f2.getPeakType());
			break;
		case 8:
			rc = 0;
			break;
		default:
			rc = 0;
		}
		// If descending order, flip the direction
		if (direction == DESCENDING) {
			rc = -rc;
		}
		return rc;
	}
}

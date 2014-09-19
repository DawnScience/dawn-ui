/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.breadcrumb.navigation.table;

import java.util.Comparator;

import org.dawb.common.util.list.SortNatural;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;

/**
 * Compares data by string for table row objects using the string value of the label provider.
 * @author Matthew Gerring
 *
 */
public abstract class AbstractTableColumnComparator implements Comparator<Object> {

	protected DirectionalIndexedColumnEnum    column;
	protected AbstractLazyLabelProvider       labelProvider;
	protected SortNatural<String>             sortNatural;
	protected IProgressMonitor                monitor;
	
	public AbstractTableColumnComparator(DirectionalIndexedColumnEnum visitColumn, AbstractLazyLabelProvider prov, IProgressMonitor monitor) {
		this.column        = visitColumn;
		this.labelProvider = prov;
		this.monitor       = monitor;
		this.sortNatural   = new SortNatural<String>(false);
	}
	
	@Override
	public int compare(Object o1, Object o2) {
		
		monitor.worked(1);
		if (column.getDirection()==SWT.DOWN) {
			Object tmp1 = o1;
			Object tmp2 = o2;
			o1 = tmp2;  o2 = tmp1;
		}
        
        if (isSpecial(o1, o2)) {
        	return compareSpecial(o1, o2);
        }
        String t1 = labelProvider.getCompareText(o1, column.getIndex());
        String t2 = labelProvider.getCompareText(o2, column.getIndex());
        return sortNatural.compare(t1, t2);
 	}

	protected abstract boolean isSpecial(Object o1, Object o2);
	
	protected abstract int compareSpecial(Object o1, Object o2);

}

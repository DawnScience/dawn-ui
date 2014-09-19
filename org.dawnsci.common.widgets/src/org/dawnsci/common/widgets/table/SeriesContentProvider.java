/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.table;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * A content provider that returns the current series plus an additional
 * item on the end which is used to add more items to the series.
 * 
 * @author fcp94556
 *
 */
public class SeriesContentProvider implements IStructuredContentProvider {

	private List<ISeriesItemDescriptor> input;
	private boolean lockEditing;
	private Viewer viewer;

	@Override
	public void dispose() {
		input = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
		this.input = (List<ISeriesItemDescriptor>)newInput;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		
		final List<ISeriesItemDescriptor> copy = input!=null && input.size()>0
				                          ? new ArrayList<ISeriesItemDescriptor>(input)
				                          : new ArrayList<ISeriesItemDescriptor>();
		if (!lockEditing) copy.add(ISeriesItemDescriptor.NEW);
		
		return copy.toArray(new ISeriesItemDescriptor[copy.size()]);
	}

	public void setLockEditing(boolean checked) {
		this.lockEditing=checked;
	}

	public List<ISeriesItemDescriptor> getSeriesItems() {
		return new ArrayList<ISeriesItemDescriptor>(input);
	}

	public boolean delete(ISeriesItemDescriptor selected) {
		if (input==null) return false;
		boolean ok = input.remove(selected);
		if (ok) viewer.setInput(input);
		return ok;
	}

}

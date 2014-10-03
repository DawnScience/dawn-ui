/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.slicing.component;

import org.dawnsci.common.widgets.celleditor.SpinnerCellEditor;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset;
import org.eclipse.dawnsci.slicing.api.system.DimsData;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpanEditingSupport extends EditingSupport {
	
	private static final Logger logger = LoggerFactory.getLogger(SpanEditingSupport.class);

	private SliceSystemImpl system;

	public SpanEditingSupport(final SliceSystemImpl system, ColumnViewer viewer) {
		super(viewer);
		this.system = system;
	}

	@Override
	protected CellEditor getCellEditor(final Object element) {
		try {
			final  SpinnerCellEditor ret = new SpinnerCellEditor(((TableViewer)getViewer()).getTable(), SWT.BORDER);
			final DimsData data = (DimsData)element;
			int dimension = data.getDimension();
			ret.setMinimum(1);
			final int max = (int)Math.round(getMaxSliceLength(system.getData().getLazySet(), dimension));
			ret.setMaximum(max);
			ret.getSpinner().setToolTipText("The maximum value of a slice of dimension '"+(dimension+1)+"' is '"+max+"',\nbased on available memory.");
			
			ret.getSpinner().addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
			        data.setSliceSpan(ret.getSpinner().getSelection());
			        TableViewer tviewer = (TableViewer)getViewer();
			       
			        tviewer.update(data, new String[]{"Slice"});
			        if (system.synchronizeSliceData(data)) system.slice(false);
				}				
			});

		    return ret;
		} catch (Exception ne) {
			logger.error("Cannot set bounds of spinner, invalid data!", ne);
			return null;
		}
	}
	
	/**
	 * Gets the maximum size of a slice of a dataset in a given dimension
	 * which should normally fit in memory. Note that it might be possible
	 * to get more in memory, this is a conservative estimate and seems to
	 * almost always work at the size returned; providing Xmx is less than
	 * the physical memory.
	 * 
	 * To get more in memory increase -Xmx setting or use an expression
	 * which calls a rolling function (like rmean) instead of slicing directly
	 * to memory.
	 * 
	 * @param lazySet
	 * @param dimension
	 * @return maximum size of dimension that can be sliced.
	 */
	public static int getMaxSliceLength(ILazyDataset lazySet, int dimension) {
		// size in bytes of each item
		final double size = AbstractDataset.getItemsize(AbstractDataset.getDTypeFromClass(lazySet.elementClass()), lazySet.getElementsPerItem());
		
		// Max in bytes takes into account our minimum requirement
		final double max  = Math.max(Runtime.getRuntime().totalMemory(), Runtime.getRuntime().maxMemory());
		
        // Firstly if the whole dataset it likely to fit in memory, then we allow it.
		// Space specified in bytes per item available
		final double space = max/lazySet.getSize();

		// If we have room for this whole dataset, then fine
		int[] shape = lazySet.getShape();
		if (space >= size)
			return shape[dimension];
		
		// Otherwise estimate what we can fit in, conservatively.
		// First get size of one slice, see it that fits, if not, still return 1
		double sizeOneSlice = size; // in bytes
		for (int dim = 0; dim < shape.length; dim++) {
			if (dim == dimension)
				continue;
			sizeOneSlice *= shape[dim];
		}
		double avail = max / sizeOneSlice;
		if (avail < 1)
			return 1;

		// We fudge this to leave some room
		return (int) Math.floor(avail/4d);
	}

	@Override
	protected boolean canEdit(Object element) {
		final DimsData data = (DimsData)element;
		return system.isAdvanced() && data.getPlotAxis().isAdvanced();
	}

	@Override
	protected Object getValue(Object element) {
		final DimsData data = (DimsData)element;
		return data.getSliceSpan();
	}

	@Override
	protected void setValue(Object element, Object value) {
		final DimsData data = (DimsData)element;
        data.setSliceSpan((Integer)value);
        system.update(data, false);
	}

}

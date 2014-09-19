/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.slicing.tools.hyper;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

/**
 * Display a 3D dataset across two plots with ROI slicing
 */
public class HyperView extends ViewPart {
	
	public static final String ID = "org.dawnsci.slicing.tools.hyper.hyperView";
	private HyperComponent hyperWindow;
	
	@Override
	public void createPartControl(Composite parent) {
		hyperWindow = new HyperComponent(this);
		hyperWindow.createControl(parent);
	}
	
	public void setData(ILazyDataset lazy, List<IDataset> daxes, Slice[] slices, int[] order, HyperType hyperType) {
		
		switch (hyperType) {
		case Box_Axis:
			hyperWindow.setData(lazy, daxes, slices, order);
			break;
		case Line_Line:
			hyperWindow.setData(lazy, daxes, slices, order,new ArpesMainImageReducer(),new ArpesSideImageReducer());
			break;
		case Line_Axis:
			hyperWindow.setData(lazy, daxes, slices, order,new TraceLineReducer(),new ImageTrapeziumBaselineReducer());
			break;
		}
		
	}
	
	@Override
	public void setFocus() {
		hyperWindow.setFocus();
		
	}
	
	@Override
    public Object getAdapter(@SuppressWarnings("rawtypes") final Class clazz) {
		return hyperWindow.getAdapter(clazz);
	}

}

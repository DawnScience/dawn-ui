/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.workbench.ui.data;

import java.util.Arrays;

import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.dawnsci.slicing.api.data.ITransferableDataObject;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

class DataTableFilter extends ViewerFilter {

	private String    searchString;
	private IMetadata meta;

	public void setSearchText(String s) {
		if (s==null) s="";
		this.searchString = ".*" + s.toLowerCase() + ".*";
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return isElementVisible(element);
	}
	
	public boolean isElementVisible(Object element) {
		
		if (searchString == null || searchString.length() == 0) {
			return true;
		}

		final String dsName = ((ITransferableDataObject)element).getName();
		if (dsName==null || "".equals(dsName)) return true;

		final String name   = dsName.toLowerCase();


		if (name.matches(searchString)) {
			return true;
		}

		// Check size so that we can filter on image size too
		if (meta!=null) {
			final int[] shape = meta.getDataShapes().get(dsName);
			if (shape!=null) {
				final String dStr = Arrays.toString(shape);
				if (dStr.matches(searchString)) {
					return true;
				}
			}
		}

		return false;

	}

	public void setMetaData(IMetadata metaData) {
		this.meta = metaData;
	}

}

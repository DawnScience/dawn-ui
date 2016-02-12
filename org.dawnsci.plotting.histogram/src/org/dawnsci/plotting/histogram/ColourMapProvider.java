/*-
 * Copyright 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.histogram;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for the colour map combo box
 * 
 * @author Baha El Kassaby
 *
 */
public class ColourMapProvider implements IStructuredContentProvider{

	protected ComboViewer colourMapViewer;

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.colourMapViewer = (ComboViewer) viewer;

		if (newInput != oldInput){
			colourMapViewer.setSelection(new StructuredSelection(newInput), true);
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		Object[] elements = (Object[]) inputElement;
		String[] colourList = new String[elements.length];
		for (int i = 0; i < elements.length; i++) {
			colourList[i] = elements[i].toString();
		}
		return colourList;
	}

}
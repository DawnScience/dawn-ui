/*-
 * Copyright 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.histogram;

import java.util.List;

import org.dawnsci.plotting.histogram.functions.ColourCategoryContribution;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content Provider class for the category of colour map combo box
 * 
 * @author Baha El Kassaby
 *
 */
public class HistoCategoryProvider implements IStructuredContentProvider{

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		ExtensionPointManager manager = ExtensionPointManager.getManager();
		List<ColourCategoryContribution> cContrib = manager.getColourCategoryContributions();
		String[] names = new String[cContrib.size()];
		for (int i = 0; i < names.length; i++) {
			names[i] = cContrib.get(i).getName();
		}
		return names;
	}
}

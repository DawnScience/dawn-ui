/*-
 * Copyright (c) 2018 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.actions;

import java.net.MalformedURLException;
import java.net.URL;

import org.dawnsci.common.widgets.gda.function.FunctionTreeViewer;
import org.dawnsci.common.widgets.gda.function.IFunctionViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

public class RefreshTreeAction extends Action {

	private FunctionTreeViewer viewer;
	
	public RefreshTreeAction(IFunctionViewer viewer) {
		super();
		if (!(viewer instanceof FunctionTreeViewer)) {
			throw new UnsupportedOperationException(
					"viewer must be a FunctionTreeViewer");
		}
		this.viewer = (FunctionTreeViewer) viewer;

		setText("Refresh");
		try {
			setImageDescriptor(ImageDescriptor.createFromURL(new URL("platform:/plugin/org.dawnsci.common.widgets.gda/icons/arrow-circle-double.png")));
		} catch (MalformedURLException mue) {
			
		}
		setToolTipText("Update the list of functions");
	}
	
	@Override
	public void run() {
		viewer.refresh();
	}
}

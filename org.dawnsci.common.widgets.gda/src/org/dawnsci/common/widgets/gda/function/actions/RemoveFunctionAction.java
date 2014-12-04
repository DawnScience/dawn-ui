/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.actions;

import org.dawnsci.common.widgets.gda.function.FunctionTreeViewer;
import org.dawnsci.common.widgets.gda.function.IFunctionViewer;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModelElement;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class RemoveFunctionAction extends Action {

	private FunctionTreeViewer viewer;

	public RemoveFunctionAction(IFunctionViewer viewer) {
		super();
		if (!(viewer instanceof FunctionTreeViewer)) {
			throw new UnsupportedOperationException(
					"viewer must be a FunctionTreeViewer");
		}
		this.viewer = (FunctionTreeViewer) viewer;

		setText("Remove this Function");
		ISharedImages sharedImages = PlatformUI.getWorkbench()
				.getSharedImages();
		setImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		setDisabledImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_DELETE_DISABLED));

		setToolTipText("Update initial parameters to fitted parameters for this function");
	}

	@Override
	public void run() {
		
		FunctionModelElement model = viewer.getSelectedFunctionModel();
		if (model != null) {
			model.deleteFunction();
			viewer.refresh(model.getParentOperator());
		}
	}
}

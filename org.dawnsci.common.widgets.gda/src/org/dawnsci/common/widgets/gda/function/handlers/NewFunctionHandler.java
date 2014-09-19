/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.handlers;

import org.dawnsci.common.widgets.gda.function.FunctionTreeViewer;
import org.dawnsci.common.widgets.gda.function.IFunctionViewer;
import org.dawnsci.common.widgets.gda.function.internal.model.AddNewFunctionModel;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModelElement;
import org.dawnsci.common.widgets.gda.function.internal.model.OperatorModel;
import org.dawnsci.common.widgets.gda.function.internal.model.SetFunctionModel;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class NewFunctionHandler extends BaseHandler {
	public NewFunctionHandler(IFunctionViewer viewer) {
		super(viewer, true);
	}

	@Override
	protected boolean isSelectionValid(FunctionModelElement model) {
		// Add is always enabled, we try to guess best place to do the Add
		return true;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		FunctionModelElement model = viewer.getSelectedFunctionModel();
		if (model instanceof AddNewFunctionModel
				|| model instanceof SetFunctionModel) {
			editModel(model);
		} else {
			while (model != null) {
				if (model instanceof OperatorModel) {
					OperatorModel operatorModel = (OperatorModel) model;
					FunctionModelElement[] children = operatorModel
							.getChildren();
					for (FunctionModelElement functionModelElement : children) {
						if (functionModelElement instanceof AddNewFunctionModel
								|| functionModelElement instanceof SetFunctionModel) {
							editModel(functionModelElement);
							return null;
						}
					}
				}
				model = model.getParentModel();
			}

			// nothing is selected, or there are no operators between selection
			// and root of the tree, so operate on the root (composite function)
			FunctionModelElement[] children = viewer.getModelRoot()
					.getChildren();
			FunctionModelElement functionModelElement = children[children.length - 1];
			if (functionModelElement instanceof AddNewFunctionModel
					|| functionModelElement instanceof SetFunctionModel) {
				editModel(functionModelElement);
				return null;
			}
		}
		return null;
	}

	/**
	 * Perform the Edit on the model
	 *
	 * @param functionModelElement
	 *            The AddNewFunctionModel or SetFunctionModel that is being
	 *            triggered.
	 */
	protected void editModel(FunctionModelElement functionModelElement) {
		viewer.getTreeViewer().editElement(functionModelElement,
				FunctionTreeViewer.COLUMN.FUNCTION.COLUMN_INDEX);
	}
}

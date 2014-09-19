/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.handlers;

import org.dawnsci.common.widgets.gda.function.IFunctionViewer;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModel;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModelElement;
import org.dawnsci.common.widgets.gda.function.internal.model.OperatorModel;
import org.dawnsci.common.widgets.gda.function.internal.model.ParameterModel;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

public class CopyHandler extends BaseHandler {

	public CopyHandler(IFunctionViewer viewer) {
		super(viewer, true);
	}

	@Override
	protected boolean isSelectionValid(FunctionModelElement model) {
		return model instanceof FunctionModel || model instanceof OperatorModel
				|| model instanceof ParameterModel;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		FunctionModelElement model = viewer.getSelectedFunctionModel();
		if (model instanceof FunctionModel || model instanceof OperatorModel
				|| model instanceof ParameterModel) {

			StructuredSelection selection = new StructuredSelection(model);
			LocalSelectionTransfer localSelectionTransfer = LocalSelectionTransfer
					.getTransfer();
			localSelectionTransfer.setSelection(selection);
			Object[] data = new Object[] { model.toString(), selection };
			Transfer[] dataTypes = new Transfer[] { TextTransfer.getInstance(),
					LocalSelectionTransfer.getTransfer() };
			Clipboard cb = new Clipboard(Display.getDefault());
			try {
				cb.setContents(data, dataTypes);
			} finally {
				cb.dispose();
			}
		}

		return null;
	}
}

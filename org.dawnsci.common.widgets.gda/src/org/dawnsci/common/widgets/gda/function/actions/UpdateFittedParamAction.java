/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.actions;

import org.dawnsci.common.widgets.gda.Activator;
import org.dawnsci.common.widgets.gda.function.FunctionTreeViewer;
import org.dawnsci.common.widgets.gda.function.IFunctionViewer;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModelElement;
import org.dawnsci.common.widgets.gda.function.internal.model.ParameterModel;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;
import org.eclipse.jface.action.Action;

public class UpdateFittedParamAction extends Action {

	private FunctionTreeViewer viewer;

	public UpdateFittedParamAction(IFunctionViewer viewer) {
		super("Update selected parameters", Activator
				.getImageDescriptor("icons/copy.gif"));
		if (!(viewer instanceof FunctionTreeViewer)) {
			throw new UnsupportedOperationException(
					"viewer must be a FunctionTreeViewer");
		}
		this.viewer = (FunctionTreeViewer) viewer;
		setToolTipText("Update initial parameters to fitted parameters for this function");
	}

	@Override
	public void run() {
		FunctionModelElement model = viewer.getSelectedFunctionModel();
		if (model instanceof ParameterModel) {
			ParameterModel parameterModel = (ParameterModel) model;
			IParameter fittedParameter = parameterModel.getFittedParameter();
			if (fittedParameter != null) {
				parameterModel.setParameterValue(fittedParameter.getValue());
				viewer.refresh(parameterModel.getParameter());
			}
		}
	}
}

/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.gda.function.detail;

import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class FunctionDetailPane implements IFunctionDetailPane {

	private Label label;

	@Override
	public Control createControl(Composite parent) {
		label = new Label(parent, SWT.NONE);
		return label;
	}

	@Override
	public void display(IDisplayModelSelection displayModel) {
		Object element = displayModel.getElement();
		if (element instanceof IFunction) {
			IFunction func = (IFunction) element;
			StringBuilder toShow = new StringBuilder();
			toShow.append(func.getName());
			toShow.append(" ");
			toShow.append(func.getDescription());
			label.setText(toShow.toString());
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}
}

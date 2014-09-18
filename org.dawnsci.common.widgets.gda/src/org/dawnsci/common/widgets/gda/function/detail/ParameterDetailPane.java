package org.dawnsci.common.widgets.gda.function.detail;

import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class ParameterDetailPane implements IFunctionDetailPane {

	private Label label;

	@Override
	public Control createControl(Composite parent) {
		label = new Label(parent, SWT.NONE);
		return label;
	}

	@Override
	public void display(IDisplayModelSelection displayModel) {
		Object element = displayModel.getElement();
		if (element instanceof IParameter) {
			IParameter parameter = (IParameter) element;
			label.setText(parameter.toString());
		}
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}
}

package org.dawnsci.common.widgets.gda.function.detail;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;

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

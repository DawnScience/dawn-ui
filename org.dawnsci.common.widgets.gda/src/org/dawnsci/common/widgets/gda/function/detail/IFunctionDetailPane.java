package org.dawnsci.common.widgets.gda.function.detail;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface IFunctionDetailPane {
	public Control createControl(Composite parent);
	public void display(IDisplayModelSelection displayModel);
	public void dispose();
}

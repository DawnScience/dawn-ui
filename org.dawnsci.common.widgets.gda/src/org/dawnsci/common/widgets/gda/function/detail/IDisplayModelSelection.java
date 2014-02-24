package org.dawnsci.common.widgets.gda.function.detail;

import org.dawnsci.common.widgets.gda.function.FunctionFittingWidget;

public interface IDisplayModelSelection {

	Object getElement();

	void refreshElement();

	FunctionFittingWidget getFunctionWidget();

}

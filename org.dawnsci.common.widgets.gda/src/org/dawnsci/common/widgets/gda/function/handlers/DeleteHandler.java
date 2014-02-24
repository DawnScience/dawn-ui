package org.dawnsci.common.widgets.gda.function.handlers;

import org.dawnsci.common.widgets.gda.function.IFunctionViewer;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModel;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModelElement;
import org.dawnsci.common.widgets.gda.function.internal.model.OperatorModel;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class DeleteHandler extends BaseHandler {

	public DeleteHandler(IFunctionViewer viewer) {
		super(viewer, true);
	}

	@Override
	protected boolean isSelectionValid(FunctionModelElement model) {
		return model instanceof FunctionModel || model instanceof OperatorModel;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		FunctionModelElement model = viewer.getSelectedFunctionModel();
		if (model != null && isSelectionValid(model)) {
			model.deleteFunction();
			viewer.refresh(model.getParentOperator());
		}

		return null;
	}
}

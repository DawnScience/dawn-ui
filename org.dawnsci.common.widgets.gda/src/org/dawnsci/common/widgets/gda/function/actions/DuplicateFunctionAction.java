package org.dawnsci.common.widgets.gda.function.actions;

import org.dawnsci.common.widgets.gda.function.FunctionTreeViewer;
import org.dawnsci.common.widgets.gda.function.IFunctionViewer;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModel;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModelElement;
import org.dawnsci.common.widgets.gda.function.internal.model.OperatorModel;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DuplicateFunctionAction extends Action {
	private static final Logger logger = LoggerFactory
			.getLogger(DuplicateFunctionAction.class);

	private FunctionTreeViewer viewer;

	public DuplicateFunctionAction(IFunctionViewer viewer) {
		super();
		if (!(viewer instanceof FunctionTreeViewer)) {
			throw new UnsupportedOperationException(
					"viewer must be a FunctionTreeViewer");
		}
		this.viewer = (FunctionTreeViewer) viewer;

		setText("Duplicate selected function");
		ISharedImages sharedImages = PlatformUI.getWorkbench()
				.getSharedImages();
		setImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setDisabledImageDescriptor(sharedImages
				.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));

		setToolTipText("Update initial parameters to fitted parameters for this function");
	}

	@Override
	public void run() {
		FunctionModelElement model = viewer.getSelectedFunctionModel();
		if (model instanceof FunctionModel || model instanceof OperatorModel) {
			IFunction copy;
			try {
				copy = model.getFunction().copy();
			} catch (Exception e) {
				logger.error(
						"Failed to create copy of function when trying to insert duplicate",
						e);
				return;
			}
			OperatorModel parentModel;
			if (model instanceof FunctionModel) {
				parentModel = ((FunctionModel) model).getParentModel();
			} else { // model instanceof OperatorModel
				parentModel = ((OperatorModel) model).getParentModel();
			}
			if (parentModel == null) {
				viewer.getModelRoot().addFunction(copy);
				viewer.refresh();
			} else {
				parentModel.addFunction(copy);
			}
			viewer.refresh();
		}

	}
}

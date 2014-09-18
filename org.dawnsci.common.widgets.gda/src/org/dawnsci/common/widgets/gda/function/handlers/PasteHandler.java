package org.dawnsci.common.widgets.gda.function.handlers;

import org.dawnsci.common.widgets.gda.function.IFunctionViewer;
import org.dawnsci.common.widgets.gda.function.internal.model.AddNewFunctionModel;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModel;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModelElement;
import org.dawnsci.common.widgets.gda.function.internal.model.OperatorModel;
import org.dawnsci.common.widgets.gda.function.internal.model.ParameterModel;
import org.dawnsci.common.widgets.gda.function.internal.model.SetFunctionModel;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasteHandler extends BaseHandler {
	private static final Logger logger = LoggerFactory
			.getLogger(PasteHandler.class);

	public PasteHandler(IFunctionViewer viewer) {
		super(viewer, true);
	}

	@Override
	protected boolean isSelectionValid(FunctionModelElement model) {
		Clipboard cb = new Clipboard(Display.getDefault());
		try {
			Object contents = cb.getContents(LocalSelectionTransfer
					.getTransfer());
			if (contents instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) contents;
				Object element = selection.getFirstElement();
				if (element instanceof ParameterModel) {
					if (model instanceof ParameterModel) {
						return true;
					}
				} else if (element instanceof FunctionModel
						|| element instanceof OperatorModel) {
					if (model instanceof SetFunctionModel) {
						return true;
					} else if (model instanceof AddNewFunctionModel) {
						return true;
					}
				}
			}

			return false;
		} finally {
			cb.dispose();
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Clipboard cb = new Clipboard(Display.getDefault());
		try {
			Object contents = cb.getContents(LocalSelectionTransfer
					.getTransfer());
			if (contents instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) contents;
				Object element = selection.getFirstElement();
				if (element instanceof ParameterModel) {
					ParameterModel parameterModel = (ParameterModel) element;
					FunctionModelElement model = viewer
							.getSelectedFunctionModel();
					if (model instanceof ParameterModel) {
						ParameterModel destinationParameterModel = (ParameterModel) model;
						destinationParameterModel.copyFrom(parameterModel);
						viewer.refresh(destinationParameterModel.getParameter());
					}
				} else if (element instanceof FunctionModel
						|| element instanceof OperatorModel) {
					FunctionModelElement functionModelElement = (FunctionModelElement) element;
					FunctionModelElement model = viewer
							.getSelectedFunctionModel();

					if (model instanceof SetFunctionModel) {
						SetFunctionModel setFunctionModel = (SetFunctionModel) model;
						IFunction toPaste = getToPaste(functionModelElement);
						if (toPaste != null) {
							setFunctionModel.run(toPaste);
							viewer.refresh(setFunctionModel.getParentOperator());
						}
					} else if (model instanceof AddNewFunctionModel) {
						AddNewFunctionModel addNewFunctionModel = (AddNewFunctionModel) model;
						IFunction toPaste = getToPaste(functionModelElement);
						if (toPaste != null) {
							addNewFunctionModel.run(toPaste);
							viewer.refresh(addNewFunctionModel
									.getParentOperator());
						}
					}
				}
			}

		} finally {
			cb.dispose();
		}
		return null;
	}

	private IFunction getToPaste(FunctionModelElement functionModelElement) {
		IFunction toPaste = null;
		try {
			toPaste = functionModelElement.getFunction().copy();
		} catch (Exception e) {
			logger.error("Failed to create a copy", e);
		}
		return toPaste;
	}
}

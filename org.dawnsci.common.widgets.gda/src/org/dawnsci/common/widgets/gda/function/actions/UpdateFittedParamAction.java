package org.dawnsci.common.widgets.gda.function.actions;

import org.dawnsci.common.widgets.gda.Activator;
import org.dawnsci.common.widgets.gda.function.FunctionTreeViewer;
import org.dawnsci.common.widgets.gda.function.IFunctionViewer;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModelElement;
import org.dawnsci.common.widgets.gda.function.internal.model.ParameterModel;
import org.eclipse.jface.action.Action;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IParameter;

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

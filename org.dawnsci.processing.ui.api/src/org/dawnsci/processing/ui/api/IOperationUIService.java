package org.dawnsci.processing.ui.api;

import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;

public interface IOperationUIService {

	public IOperationSetupWizardPage getWizardPage(IOperation<? extends IOperationModel, ? extends OperationData> operation);
	
}

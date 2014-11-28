package org.dawnsci.processing.ui.slice;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;

public interface IOperationInputData {

	public IDataset getInputData();
	
	public IOperation<? extends IOperationModel, ? extends OperationData> getCurrentOperation();
	
}

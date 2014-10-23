package org.dawnsci.processing.ui.slice;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;

public interface IOperationErrorInformer {

	public void setInErrorState(OperationException e);
	
	public OperationException getInErrorState();
	
	public IDataset getTestData();
	
	public void setTestData(IDataset test);
	
}

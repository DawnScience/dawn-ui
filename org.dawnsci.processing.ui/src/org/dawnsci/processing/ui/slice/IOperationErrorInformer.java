package org.dawnsci.processing.ui.slice;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;

/**
 * Interface to allow the processing view to know if the UI pipeline is in an error state,
 * or get the first slice to determine the rank of suitable operations
 * 
 *
 */
public interface IOperationErrorInformer {

	/**
	 * Set the exception to appear in processing view
	 * Causes series table refresh
	 * 
	 * @param e
	 */
	public void setInErrorState(OperationException e);
	
	/**
	 * Get the Exception, maybe null
	 * @return
	 */
	public OperationException getInErrorState();
	
	/**
	 * Get the test data, may be null
	 * @return
	 */
	public IDataset getTestData();
	
	/**
	 * Set the test data, causes series table to refresh
	 * @param test
	 */
	public void setTestData(IDataset test);
	
}

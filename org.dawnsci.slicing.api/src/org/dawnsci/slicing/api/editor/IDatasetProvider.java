package org.dawnsci.slicing.api.editor;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

public interface IDatasetProvider {

	
	/**
	 * A dataset which can be used without loading the data
	 * @param name
	 * @param monitor
	 * @return lazy dataset
	 */
	public ILazyDataset getLazyDataset(String name, IMonitor monitor);

	/**
	 * Return dataset for name
	 * @param name
	 * @param monitor
	 * @return dataset
	 */
	public IDataset getDataset(final String name, final IMonitor monitor);

}

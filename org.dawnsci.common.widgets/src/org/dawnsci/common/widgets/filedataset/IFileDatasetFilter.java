package org.dawnsci.common.widgets.filedataset;

import org.eclipse.january.dataset.ILazyDataset;

@FunctionalInterface
public interface IFileDatasetFilter {
	/**
	 * Only method provided by this functional interface
	 * @param dataset Input lazy dataset 
	 * @return true if dataset obeys the conditions defined in this method, false otherwise
	 */
	boolean accept(ILazyDataset dataset);
}

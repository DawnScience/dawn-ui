package org.dawnsci.plotting.api.tool;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

/**
 * Interface used to add extra dataset to a tool
 * @author wqk87977
 *
 */
public interface IAuxiliaryToolDataset {

	/**
	 * Adds dataset to the tool
	 * @param data
	 */
	void addDataset(IDataset data);

	/**
	 * removes dataset
	 * @param data
	 */
	void removeDataset(IDataset data);
}

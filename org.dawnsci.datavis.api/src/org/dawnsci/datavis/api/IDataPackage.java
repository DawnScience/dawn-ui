package org.dawnsci.datavis.api;

import java.util.List;

import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;

public interface IDataPackage {

	public boolean isSelected();
	
	public String getFilePath();
	
	public String getName();
	
	public SliceND getSlice();
	
	public ILazyDataset getLazyDataset();

	public IDataset getLabelValue();

	public int[] getOmitDimensions();

	/**
	 * Get derived data of given class (can return list of subclasses or interface implementations)
	 * @param <T> generic type parameter
	 * @param clazz class
	 * @return derived data (can be null)
	 */
	public <T> List<T> getDerivedData(Class<T> clazz);

	/**
	 * Add derived data to store so it can be retrieved by its class, superclasses or interfaces
	 * @param derived
	 */
	public void addDerivedData(List<?> derived);
}

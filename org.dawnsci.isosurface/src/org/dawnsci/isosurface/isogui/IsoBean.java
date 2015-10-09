package org.dawnsci.isosurface.isogui;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.isosurface.Activator;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.alg.Surface;
import org.dawnsci.isosurface.tool.IsosurfaceJob;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IIsosurfaceTrace;

public class IsoBean
{
	private List<IsoItem> items;	
	
	public IsoBean()
	{
		items = new ArrayList<IsoItem>();
	}
	
	
	/**
	 * Clear the list of items
	 */
	public void clear()
	{
		items.clear();
	}
	
	/**
	 * Get the list of items
	 * @return ItemList
	 */
	public List<IsoItem> getItems()
	{
		return this.items;
	}
	
	/**
	 * Set the list of items
	 * @param newItems - The new list of items
	 */
	public void setItems(List<IsoItem> newItems)
	{
		this.items = newItems;
	}
	
	/**
	 * Add a new Item to the list
	 * @param newItem - The new item
	 */
	public void addItem(IsoItem newItem)
	{
		items.add(newItem);
	}
	
	public IsoItem getItem(int index)
	{
		return this.items.get(index);
	}
	
}

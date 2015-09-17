package org.dawnsci.isosurface.testingBeans;

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

public class testingBean
{
	
	private List<testItem> items;
	// private IsosurfaceJob job;
	private IPlottingSystem system;
	private ILazyDataset slice;
	
	public testingBean()
	{
		items = new ArrayList<testItem>();
	}
	
	public void clear()
	{
		items.clear();
	}
	
	public List<testItem> getItems()
	{
		return this.items;
	}
	
	public void setItems(List<testItem> newItems)
	{
		// !! this is a hack and bad
		for (testItem I : newItems)
		{
			if (I.getJob() == null)
				I.setJob(new IsosurfaceJob("Computing isosurface", system,
						slice));
		}
		this.items = newItems;
	}
	
	public void addItem(testItem newItem)
	{
		newItem.setJob(new IsosurfaceJob("Computing isosurface", system, slice));
		items.add(newItem);
	}
	
	public void setJob(ILazyDataset slice, IPlottingSystem system)
	{
		this.slice = slice;
		this.system = system;
	}
	
}

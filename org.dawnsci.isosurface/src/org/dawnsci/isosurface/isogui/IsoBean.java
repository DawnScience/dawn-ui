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
//	private IPlottingSystem system;
//	private ILazyDataset slice;
	
	public IsoBean()
	{
		items = new ArrayList<IsoItem>();
	}
		
	public void clear()
	{
		items.clear();
	}
	
	public List<IsoItem> getItems()
	{
		return this.items;
	}
	
	public void setItems(List<IsoItem> newItems)
	{
		this.items = newItems;
	}
	
	public void addItem(IsoItem newItem)
	{
		items.add(newItem);
	}
	
	
}

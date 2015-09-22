package org.dawnsci.isosurface.testingBeans;

import org.dawnsci.isosurface.Activator;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.alg.Surface;
import org.dawnsci.isosurface.tool.IsosurfaceJob;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IIsosurfaceTrace;
import org.eclipse.swt.graphics.RGB;

public class testItem
{
	
	private String name;
	private double value;
	private double opacity;
	private int x = 20, y = 20, z = 1;
	private RGB colour = new RGB(0,0,0);
	
	private IsosurfaceJob job;
	private IOperation<MarchingCubesModel, Surface> generator;
		
	public testItem()
	{
		this("testName");
	}
	
	public testItem(String name)
	{
		this.name = name;
		
		final IOperationService service = (IOperationService) Activator
				.getService(IOperationService.class);
		try
		{
			this.generator = (IOperation<MarchingCubesModel, Surface>) service
					.create("org.dawnsci.isosurface.marchingCubes");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	
	}
	
	public void update()
	{	
		
		MarchingCubesModel model = this.generator.getModel();
		int[] boxSize = new int[] {x, y, z};
		model.setBoxSize(boxSize);
		job.compute(generator);
		
	}
	
	public void destroy()
	{
		int x = 0;
		System.out.println("destroyed");
	}
	
	public void setJob(IsosurfaceJob job)
	{
		this.job = job;
	}
	
	public IsosurfaceJob getJob()
	{
		return this.job;
	}
	
	/*
	 * get - sets
	 */
	
	public String getName()
	{
		return this.name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public double getValue()
	{
		return this.value;
	}
	
	public void setValue(double newValue)
	{
		if (this.value != newValue)
		{
			MarchingCubesModel model = this.generator.getModel();
			model.setIsovalue(newValue);
			
			this.value = newValue;
			update();
		}
	}
	
	
	public int getX()
	{
		return this.x;
	}
	
	public void setX(int newSize)
	{
		this.x = newSize;
		update();
	}
	
	public int getY()
	{
		return this.y;
	}
	
	public void setY(int newSize)
	{
		this.y = newSize;
		update();
	}
	
	public int getZ()
	{
		return this.z;
	}
	
	public void setZ(int newSize)
	{
		this.z = newSize;
		update();
	}
	
	public RGB getColour()
	{
		return this.colour;
	}
	
	public void setColour(RGB newColour)
	{
		MarchingCubesModel model = this.generator.getModel();
		model.setColour(newColour.red, newColour.green, newColour.blue);
		this.colour = newColour;
		update();
	}
	
	public void setIsoSurfaceScaleValue(int newValue)
	{
		MarchingCubesModel model = this.generator.getModel();
		model.setIsovalue(newValue);
		
		this.value = newValue;
		update();
	}
	public double getIsoSurfaceScaleValue()
	{
		return this.value;
	}
	
	public void setOpacity(double newValue)
	{
		MarchingCubesModel model = this.generator.getModel();
		model.setOpacity(newValue);
		
		this.opacity = newValue;
		update();
	}
	public double getOpacity()
	{
		return this.opacity;
	}

	
}

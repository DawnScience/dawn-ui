package org.dawnsci.isosurface.IsoGUI;

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
import org.eclipse.swt.widgets.Display;

public class IsoItem
{
	
	private String name;
	private double value = 0;
	private double opacity = 0.5;
	private int x = 20, y = 20, z = 1;
	private RGB colour = new RGB(0,0,0);
	
	private IsosurfaceJob job;
	private IOperation<MarchingCubesModel, Surface> generator;
		
	public IsoItem()
	{
		this("New Surface");
	}
	
	public IsoItem(String name)
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
	
	private void update()
	{	
		
		MarchingCubesModel model = this.generator.getModel();
		int[] boxSize = new int[] {x, y, z};
		model.setBoxSize(boxSize);
		model.setOpacity(opacity);
		model.setIsovalue(value);
		
		job.compute(generator); // !! look into making void
		
	}
	
	public void destroy()
	{
		// removes the trace and the isosurface from the javafx class
		job.destroy();
	}
	
	public void setInfo(IsosurfaceJob job, double startingValue, int[] startingBoxSize, double startingOpacity)
	{
		this.job = job;
		this.value = startingValue;
		this.x = startingBoxSize[0];
		this.y = startingBoxSize[1];
		this.z = startingBoxSize[2];
		this.opacity = startingOpacity;
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
		if (newSize != this.x)
		{
			this.x = newSize;
			update();
		}
	}
	
	public int getY()
	{
		return this.y;
	}
	
	public void setY(int newSize)
	{
		if (newSize != this.y)
		{
			this.y = newSize;
			update();
		}
	}
	
	public int getZ()
	{
		return this.z;
	}
	
	public void setZ(int newSize)
	{
		if (newSize != this.z)
		{
			this.z = newSize;
			update();
		}
	}
	
	public RGB getColour()
	{
		return this.colour;
	}
	
	public void setColour(RGB newColour)
	{
		if (newColour != this.colour)
		{
			MarchingCubesModel model = this.generator.getModel();
			model.setColour(newColour.red, newColour.green, newColour.blue);
			this.colour = newColour;
			update();
		}
	}
	
	public void setIsoSurfaceScaleValue(int newValue)
	{
		if (newValue != this.value)
		{
			MarchingCubesModel model = this.generator.getModel();
			model.setIsovalue(newValue);
			this.value = newValue;
			update();
		}
	}
	public double getIsoSurfaceScaleValue()
	{
		return this.value;
	}
	
	public void setOpacity(double newValue)
	{
		if (newValue != opacity)
		{
			MarchingCubesModel model = this.generator.getModel();
			model.setOpacity(newValue);
			this.opacity = newValue;
			update();
		}
	}
	public double getOpacity()
	{
		return this.opacity;
	}

	
}

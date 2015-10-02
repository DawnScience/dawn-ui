package org.dawnsci.isosurface.isogui;

import java.awt.Color;

import org.dawnsci.isosurface.Activator;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.alg.Surface;
import org.dawnsci.isosurface.tool.IsosurfaceJob;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.swt.graphics.RGB;

public class IsoItem
{
	// the values used within the GUI
	private String name;
	private double value = 0;
	private double opacity = 0.5;
	private int x = 20, y = 20, z = 1;
	private RGB colour = new RGB(255,215,0);
	
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
	
	/**
	 * Update the isosurfaces. 
	 * Redeclares the surface data (cube size, value, etc.) and redraws the surface
	 * 
	 */
	private void update()
	{	
		
		MarchingCubesModel model = this.generator.getModel();
		int[] boxSize = new int[] {x, y, z};
		model.setBoxSize(boxSize);
		model.setOpacity(opacity);
		model.setIsovalue(value);
		model.setColour(colour.red, colour.green, colour.blue);
		
		job.compute(generator);
		
	}
	
	/**
	 * Destroy the item. Removes the trace and the surface from javafx.
	 */
	public void destroy()
	{
		// removes the trace and the isosurface from the javafx class
		job.destroy();
	}
	
	/**
	 * Declare the information required for the item.
	 * @param job - The job used to compute the surface.
	 * @param startingValue - The starting IsoValue
	 * @param startingBoxSize - The starting Box Size. int[3]
	 * @param startingOpacity - The starting opacity (transparency).
	 * @param startingColour - The starting colour.
	 */
	public void setInfo(IsosurfaceJob job, double startingValue, int[] startingBoxSize, double startingOpacity, Color startingColour)
	{
		
		this.job = job;				
		this.value = startingValue; 
		this.x = startingBoxSize[0];
		this.y = startingBoxSize[1];
		this.z = startingBoxSize[2];
		this.opacity = startingOpacity;
		this.colour = new RGB(startingColour.getRed(),startingColour.getGreen(), startingColour.getBlue());
	}
	
	/**
	 * Get the job
	 * @return IsosurfaceJob - The job
	 */
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

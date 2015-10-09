package org.dawnsci.isosurface.isogui;

import java.awt.Color;
import java.util.UUID;

import org.dawnsci.isosurface.Activator;
import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.alg.Surface;
import org.dawnsci.isosurface.tool.IsosurfaceJob;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.swt.graphics.RGB;

public class IsoItem implements Cloneable
{

	// the values used within the GUI
	private String name;
	private double value = 0;
	private double opacity = 0.5;
	private int x = 20, y = 20, z = 1;
	private RGB colour = new RGB(255,215,0);
	
	private String traceKey;
	
	public IsoItem()
	{
		this("New Surface");
		this.traceKey = UUID.randomUUID().toString(); // we will replace this with displayname on iIsosurfaceTrace
	}
	
	public IsoItem(String name)
	{
		this.name = name;		
	}
	
	
	/**
	 * Declare the information required for the item.
	 * @param job - The job used to compute the surface.
	 * @param startingValue - The starting IsoValue
	 * @param startingBoxSize - The starting Box Size. int[3]
	 * @param startingOpacity - The starting opacity (transparency).
	 * @param startingColour - The starting colour.
	 */
	public void setInfo(double startingValue, int[] startingBoxSize, double startingOpacity, Color startingColour)
	{		
		this.value = startingValue; 
		this.x = startingBoxSize[0];
		this.y = startingBoxSize[1];
		this.z = startingBoxSize[2];
		this.opacity = startingOpacity;
		this.colour = new RGB(startingColour.getRed(),startingColour.getGreen(), startingColour.getBlue());
	}
	
	public Object clone()
	{
		try 
		{
			return super.clone();
		} 
		catch (CloneNotSupportedException e) 
		{
			e.printStackTrace();
			return null;
		}
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
			this.value = newValue;
	}
	
	
	public int getX()
	{
		return this.x;
	}
	
	public void setX(int newSize)
	{

			this.x = newSize;

	}
	
	public int getY()
	{
		return this.y;
	}
	
	public void setY(int newSize)
	{

			this.y = newSize;

	}
	
	public int getZ()
	{
		return this.z;
	}
	
	public void setZ(int newSize)
	{

			this.z = newSize;

	}
	
	public RGB getColour()
	{
		return this.colour;
	}
	
	public void setColour(RGB newColour)
	{
		this.colour = newColour;

	}
	
	public void setIsoSurfaceScaleValue(int newValue)
	{
		this.value = newValue;
	}
	public double getIsoSurfaceScaleValue()
	{
		return this.value;
	}
	
	public void setOpacity(double newValue)
	{
		this.opacity = newValue;
	}
	public double getOpacity()
	{
		return this.opacity;
	}
	
	public String getTraceKey()
	{
		return this.traceKey;
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((colour == null) ? 0 : colour.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		long temp;
		temp = Double.doubleToLongBits(opacity);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + z;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IsoItem other = (IsoItem) obj;
		if (colour == null) {
			if (other.colour != null)
				return false;
		} else if (!colour.equals(other.colour))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (Double.doubleToLongBits(opacity) != Double
				.doubleToLongBits(other.opacity))
			return false;
		if (Double.doubleToLongBits(value) != Double
				.doubleToLongBits(other.value))
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		if (z != other.z)
			return false;
		return true;
	}

	
}

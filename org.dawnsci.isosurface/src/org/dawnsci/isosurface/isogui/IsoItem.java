package org.dawnsci.isosurface.isogui;

import java.awt.Color;
import java.util.UUID;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.richbeans.api.generator.RichbeansAnnotations.MaximumValue;
import org.eclipse.richbeans.api.generator.RichbeansAnnotations.MinimumValue;
import org.eclipse.richbeans.api.generator.RichbeansAnnotations.UiHidden;
import org.eclipse.swt.graphics.RGB;

public class IsoItem implements IIsoItem
{
	private Type type = Type.ISO_SURFACE;
	private double value = 0;
	private int opacity = 50;
	private int resolution = 20;
	private RGB colour = new RGB(255,215,0);
	
	private String traceKey = UUID.randomUUID().toString();
	
	public IsoItem(){}
	
	public IsoItem(Type type, double startingValue, int resolution, int startingOpacity, Color startingColour)
	{
		this.type = type;
		this.value = startingValue; 
		this.resolution = resolution;
		this.opacity = startingOpacity;
		this.colour = new RGB(startingColour.getRed(),startingColour.getGreen(), startingColour.getBlue());
	}
	
	@Override
	public Type getRenderType() {
		return type;
	}
	
	@Override
	public double getValue()
	{
		return this.value;
	}
	@Override
	public void setValue(double newValue)
	{
		this.value = newValue;
	}
	@Override
	public int getResolution() {
		return resolution;
	}
	@MinimumValue("1")
	@Override
	public void setResolution(int resolution) {
		this.resolution = resolution;
	}
	@MinimumValue("0")
	@MaximumValue("1")
	@Override
	public void setOpacity(int newValue)
	{
		this.opacity = newValue;
	}
	@Override
	public int getOpacity()
	{
		return this.opacity;
	}
	@Override
	public RGB getColour()
	{
		return this.colour;
	}
	@Override
	public void setColour(RGB newColour)
	{
		this.colour = newColour;
	}
	@Override
	@UiHidden
	public String getTraceKey()
	{
		return this.traceKey;
	}
	
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!IIsoItem.class.isAssignableFrom(obj.getClass()))
			return false;
		IIsoItem other = (IIsoItem) obj;
		if (colour == null) {
			if (other.getColour() != null)
				return false;
		} else if (!colour.equals(other.getColour()))
			return false;
		if (opacity != other.getOpacity())
			return false;
		if (resolution != other.getResolution())
			return false;
		if (traceKey == null) {
			if (other.getTraceKey() != null)
				return false;
		} else if (!traceKey.equals(other.getTraceKey()))
			return false;
		if (type != other.getRenderType())
			return false;
		if (Double.doubleToLongBits(value) != Double.doubleToLongBits(other.getValue()))
			return false;
		return true;
	}

	
}

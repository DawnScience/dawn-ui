package org.dawnsci.plotting.javafx.axis.objects;

import javafx.geometry.Point3D;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class AxisLineOld extends Cylinder
{
	private Translate offset;
	private Rotate rotate;
	
	AxisLineOld(double radius, double height, Rotate rotate, Point3D offset)
	{
		super(radius, height);
		this.rotate = rotate;
		this.offset = new Translate(offset.getX(), offset.getY(), offset.getZ());;
		
		this.getTransforms().addAll(this.rotate, this.offset);
		
	}
	
	
	public void setRadiusExtended(double newRadius)
	{
		this.setRadius(newRadius);
	}
	
	public void setHeightExtended(double newHeight)
	{
		this.setHeight(newHeight);
	}
	
	
	public void editRotate(Rotate newRotate)
	{
		this.rotate.setAngle(newRotate.getAngle());
		this.rotate.setAxis(newRotate.getAxis());
	}
	
	public void editOffset(Translate newMajorOffset)
	{
		this.offset.setX(newMajorOffset.getX());
		this.offset.setY(newMajorOffset.getY());
		this.offset.setZ(newMajorOffset.getZ());
	}
	
}

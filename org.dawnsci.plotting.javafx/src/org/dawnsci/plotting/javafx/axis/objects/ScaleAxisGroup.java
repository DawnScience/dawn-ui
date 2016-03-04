package org.dawnsci.plotting.javafx.axis.objects;

import org.dawnsci.plotting.javafx.tools.Vector3DUtil;

import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Translate;

public class ScaleAxisGroup extends Group
{
	
	public ScaleAxisGroup(Point3D maxLength, double axisThickness)
	{
		super();
		
		this.getChildren().add(createScaleBar(new Point3D(1,0,0), maxLength.getX(), axisThickness));
		this.getChildren().add(createScaleBar(new Point3D(0,1,0), maxLength.getY(), axisThickness));
		this.getChildren().add(createScaleBar(new Point3D(0,0,1), maxLength.getZ(), axisThickness));
		
	}
	
	private Cylinder createScaleBar(Point3D direction, double length, double thickness)
	{
		// generate the cylinder
		// default position is centered on (0,0,0) in direction (0,1,0)
		Cylinder tempBox = new Cylinder(thickness,length * 1.05d);
		
		// rotate the axis to face the right direction
		// in this case the axis
		tempBox.getTransforms().add(Vector3DUtil.alignVector(new Point3D(0,1,0), direction));
		tempBox.getTransforms().add(new Translate(0,(length * 1.05d)/2,0));
		
		// create the material to colour the axis
		PhongMaterial mat = new PhongMaterial();
		mat.setDiffuseColor (new Color(direction.getX(), direction.getY(), direction.getZ(), 1));
		mat.setSpecularColor(new Color(direction.getX(), direction.getY(), direction.getZ(), 1));
		
		// set the material -> ie colour the axis
		tempBox.setMaterial(mat);
		
		return tempBox;
		
	}
	
	// the event from scene
	public void setAxisEventListener(EventHandler<MouseEvent> eventHandler)
	{
		for (Node n: this.getChildren())
		{
			if (n instanceof Cylinder)
			{
				n.setCursor(Cursor.OPEN_HAND);
				n.setOnMouseDragged(eventHandler);
			}
		}
	}
	
	public void flipVisibility()
	{
		this.setVisible(!this.isVisible());
	}
	
	
}

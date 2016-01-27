package org.dawnsci.plotting.javafx.axis.objects;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Translate;

public class AxesGroup extends Group{
	
	private Grid axisGrid;
	private Cylinder ScaleAxis;

	private Point2D axisLimitMin = new Point2D(0, 0);
	private Point2D axisLimitMax = new Point2D(200,200);
	
	public AxesGroup(
			Point3D planeNormal,
			Point2D tickSeparationXY, 
			Point3D axisLength,
			double majorAxisThickness,
			EventHandler<MouseEvent> scaleEventHandler)
	{
		
		double textSize = (tickSeparationXY.getX() / 50) * 15;
		
		axisGrid = new Grid(planeNormal, tickSeparationXY, axisLength, majorAxisThickness, textSize);
		
		ScaleAxis = createScaleBar(planeNormal, axisLength.getZ(), majorAxisThickness);
		ScaleAxis.setCursor(Cursor.OPEN_HAND);
		ScaleAxis.setOnMouseDragged(scaleEventHandler);
		
		AmbientLight ambientAxisLight = new AmbientLight(DefaultObjectProperties.LINE_COLOUR);
		ambientAxisLight.getScope().add(axisGrid);
		axisGrid.getChildren().add(ambientAxisLight);
		
		
		this.getChildren().addAll(axisGrid); //, ScaleAxis);
	}
	
	private Cylinder createScaleBar(Point3D direction, double length, double thickness)
	{
		// generate the cylinder
		// default position is centered on (0,0,0) in direction (0,1,0)
		Cylinder tempBox = new Cylinder(thickness,length * 1.05d);
		
		// rotate the axis to face the right direction
		// in this case the axis
		tempBox.getTransforms().add(Vector3DUtil.rotateVector(new Point3D(0,1,0), direction));
		tempBox.getTransforms().add(new Translate(0,(length * 1.05d)/2,0));
		
		// create the material to colour the axis
		PhongMaterial mat = new PhongMaterial();
		mat.setDiffuseColor (new Color(direction.getX(), direction.getY(), direction.getZ(), 1));
		mat.setSpecularColor(new Color(direction.getX(), direction.getY(), direction.getZ(), 1));
		
		// set the material -> ie colour the axis
		tempBox.setMaterial(mat);
		
		return tempBox;
		
	}
	
	public void updateScale(Point3D newMax)
	{
		axisGrid.updateGridMaxLength(newMax);
		axisGrid.reDeclareLabels(axisLimitMin, axisLimitMax);
	}
	
	public void setAxisMaxLimit(Point2D newMax)
	{
		this.axisLimitMax = newMax;
	}
	
	public void setAxisMinLimit(Point2D newMin)
	{
		this.axisLimitMin = newMin;
	}
	
	public void setTickSeperation(Point2D newSeparation)
	{
		double textSize = (newSeparation.getX() / 50) * 15;
		axisGrid.setTextSize(textSize);
		
		axisGrid.setTickSeperationXY(newSeparation);
		
		axisGrid.resetTicks();
	}
	
	
	
}

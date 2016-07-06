package org.dawnsci.plotting.javafx.axis.objects;

import java.util.List;

import org.eclipse.january.dataset.IDataset;

import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.transform.Translate;


/** 
 * @author uij85458
 * 
 * A predefined function to give a basic XYZ axis grid
 * 
 */
public class SceneObjectGroup extends Group
{ 
	private TickLoop xAxisGroup;
	private TickLoop yAxisGroup;
	private TickLoop zAxisGroup;
	
	private BoundingBox boundingBox;
		
	public SceneObjectGroup()
	{
		super();
	}

	/*
	 * public functions
	 */

	/**
	 * Add the default axes to the scene.
	 * <p>
	 * Default values:
	 * <br><tab>Three axis aligned planes
	 * </p>
	 * 
	 * @param axisLength - the length of each axis (X Y and Z)
	 * @param tickSeperation_XYZ - the separation of each axis
	 */
	
	public void setAxes(
			Point3D axisLength,
			List<IDataset> axesData)
	{
		boolean xVis = true, yVis = true, zVis = true;
		
		if (xAxisGroup != null)
		{
			xVis = xAxisGroup.isVisible();
			xAxisGroup.getChildren().clear();
		}
		
		if (yAxisGroup != null)
		{
			yVis = yAxisGroup.isVisible();
			yAxisGroup.getChildren().clear();
		}
		
		if (zAxisGroup != null)
		{
			zVis = zAxisGroup.isVisible();
			zAxisGroup.getChildren().clear();
		}
		
		xAxisGroup = new TickLoop(
							axesData.get(0), 
							new Point3D(
									axisLength.getX(), 
									axisLength.getY(), 
									axisLength.getZ()), 
							10, 
							new Point3D(1, 0, 0),
							new Point3D(0, 1, 0));
		xAxisGroup.setVisible(xVis);
		
		yAxisGroup = new TickLoop(
							axesData.get(1), 
							new Point3D(
									axisLength.getY(), 
									axisLength.getZ(), 
									axisLength.getX()),
							10, 
							new Point3D(0, 1, 0),
							new Point3D(0, 0, 1));
		yAxisGroup.setVisible(yVis);
		
		zAxisGroup = new TickLoop(
							axesData.get(2), 
							new Point3D(
									axisLength.getX(), 
									axisLength.getZ(), 
									-axisLength.getY()),
							10, 
							new Point3D(1, 0, 0),
							new Point3D(0, 0, 1));
		zAxisGroup.setVisible(zVis);
		
		this.getChildren().addAll(xAxisGroup, yAxisGroup, zAxisGroup);
	}
	
	/**
	 * Add a bounding box to the scene.
	 * <br> Only one bounding box is able to be active at a time.
	 * <br> Any active boundingBoxes will be removed.
	 * @param size - The size of the bounding box
	 */
	public void setBoundingBox(Point3D size)
	{
		boolean bbVis = true;
		
		if (boundingBox != null && this.getChildren().contains(boundingBox))
		{
			bbVis = boundingBox.isVisible();
			this.getChildren().remove(boundingBox);
		}
		
		boundingBox = new BoundingBox(size);
		AmbientLight ambientAxisLight = new AmbientLight(Color.WHITE);
		ambientAxisLight.getTransforms().add(new Translate(100,100,100));
		ambientAxisLight.getScope().add(boundingBox);
		boundingBox.getChildren().add(ambientAxisLight);
		boundingBox.setColour(Color.BLACK);
		
		boundingBox.setVisible(bbVis);
		
		this.getChildren().add(boundingBox);
	}
	
	/**
	 * currently does nothing - reorganising the code
	 */
	public void setScalers()
	{
		
	}
	
	/*
	 * visibility stuff
	 */
	public void setAxisVisibility(boolean visible)
	{
		xAxisGroup.setVisible(visible);
		yAxisGroup.setVisible(visible);
		zAxisGroup.setVisible(visible);
	}
	
	public void setBoundingBoxVisibility(boolean visible)
	{
		this.boundingBox.setVisible(visible);
	}
		
}	


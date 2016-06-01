package org.dawnsci.plotting.javafx.axis.objects;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

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

	private AxesGroup xyAxisGroup;
	private AxesGroup yzAxisGroup;
	private AxesGroup zxAxisGroup;
	
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
		if (xyAxisGroup != null)
			xyAxisGroup.getChildren().clear();
		if (yzAxisGroup != null)
			yzAxisGroup.getChildren().clear();
		if (zxAxisGroup != null)
			zxAxisGroup.getChildren().clear();
				
		xyAxisGroup = new AxesGroup(
				new Point3D(0, 0, 1),
				axesData.get(0),
				new Point3D(axisLength.getX(), axisLength.getY(), axisLength.getZ()));
		this.getChildren().add(xyAxisGroup);
		
		yzAxisGroup = new AxesGroup(
				new Point3D(1, 0, 0),
				axesData.get(1),
				new Point3D(axisLength.getY(), axisLength.getZ(), axisLength.getX()));
		this.getChildren().add(yzAxisGroup);
		
		zxAxisGroup = new AxesGroup(
				new Point3D(0, 1, 0),
				axesData.get(2),
				new Point3D(axisLength.getZ(), axisLength.getX(), axisLength.getY()));
		this.getChildren().add(zxAxisGroup);
		
		
	}
	
	/**
	 * Add a bounding box to the scene.
	 * <br> Only one bounding box is able to be active at a time.
	 * <br> Any active boundingBoxes will be removed.
	 * @param size - The size of the bounding box
	 */
	public void setBoundingBox(Point3D size)
	{
		if (boundingBox != null && this.getChildren().contains(boundingBox))
		{
			this.getChildren().remove(boundingBox);
		}
		
		boundingBox = new BoundingBox(size);
		AmbientLight ambientAxisLight = new AmbientLight(Color.WHITE);
		ambientAxisLight.getTransforms().add(new Translate(100,100,100));
		ambientAxisLight.getScope().add(boundingBox);
		boundingBox.getChildren().add(ambientAxisLight);
		boundingBox.setColour(Color.BLACK);
		
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
	public void setGridXVisible(boolean visible)
	{
		yzAxisGroup.setVisible(visible);
	}
	public void setGridYVisible(boolean visible)
	{
		zxAxisGroup.setVisible(visible);
	}
	public void setGridZVisible(boolean visible)
	{
		xyAxisGroup.setVisible(visible);
	}
	public void setGridAllVisible(boolean visible)
	{
		setGridXVisible(visible);
		setGridYVisible(visible);
		setGridZVisible(visible);
	}
	public void setAllVisible (boolean visible)
	{
		setGridXVisible(visible);
		setGridYVisible(visible);
		setGridZVisible(visible);
	}
		
	public void setBoundingBoxVisibility(boolean visible)
	{
		this.boundingBox.setVisible(visible);
	}
		
}	


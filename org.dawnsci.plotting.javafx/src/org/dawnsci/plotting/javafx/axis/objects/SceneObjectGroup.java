package org.dawnsci.plotting.javafx.axis.objects;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
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
	
	private AxesGroup yzAxisGroup;
	private AxesGroup zxAxisGroup;
	private AxesGroup xyAxisGroup;
	
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
	 * <br><tab>Tick values range: 0-100
	 * <br><tab>Three axis aligned planes
	 * </p>
	 * 
	 * @param axisLength - the length of each axis (X Y and Z)
	 * @param tickSeperation_XYZ - the separation of each axis
	 */
	
	public void addAxes(
			Point3D axisLength,
			Point3D tickSeperation_XYZ)
	{
		yzAxisGroup = new AxesGroup(
				new Point3D(1, 0, 0), 
				new Point2D(tickSeperation_XYZ.getY(), tickSeperation_XYZ.getZ()),  
				new Point3D(axisLength.getY(), axisLength.getZ(), axisLength.getX()),
				new Point2D(0, 0),
				new Point2D(100, 100));
		this.getChildren().add(yzAxisGroup);
		
		zxAxisGroup = new AxesGroup(
				new Point3D(0, 1, 0), 
				new Point2D(tickSeperation_XYZ.getZ(), tickSeperation_XYZ.getX()),  
				new Point3D(axisLength.getZ(), axisLength.getX(), axisLength.getY()),
				new Point2D(0, 0),
				new Point2D(100, 100));
		this.getChildren().add(zxAxisGroup);
		
		xyAxisGroup = new AxesGroup(
				new Point3D(0, 0, 1), 
				new Point2D(tickSeperation_XYZ.getX(), tickSeperation_XYZ.getY()),  
				new Point3D(axisLength.getX(), axisLength.getY(), axisLength.getZ()),
				new Point2D(0, 0),
				new Point2D(100, 100));
		this.getChildren().add(xyAxisGroup);
	}
	/**
	 * Add a bounding box to the scene.
	 * <br> Only one bounding box is able to be active at a time.
	 * <br> Any active boundingBoxes will be removed.
	 * @param size - The size of the bounding box
	 */
	public void addBoundingBox(Point3D size)
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
	public void addScalers()
	{
		
	}
	
	public void checkScale(Point3D newMaxLengthXYZ, double zoom)
	{
		// yz
		yzAxisGroup.updateScale(new Point3D(newMaxLengthXYZ.getY(), newMaxLengthXYZ.getZ(), newMaxLengthXYZ.getX()));
		// zx
		zxAxisGroup.updateScale(new Point3D(newMaxLengthXYZ.getZ(), newMaxLengthXYZ.getX(), newMaxLengthXYZ.getY()));
		// xy
		xyAxisGroup.updateScale(new Point3D(newMaxLengthXYZ.getX(), newMaxLengthXYZ.getY(), newMaxLengthXYZ.getZ()));
	}
	
	/**
	 * sets the new tick separations of each of the axes
	 * @param newTickSpeperation - the x, y, and z tick separations
	 */
	public void SetTickSeparationXYZ(Point3D newTickSpeperation)
	{
		yzAxisGroup.setTickSeperation(new Point2D(newTickSpeperation.getY(), newTickSpeperation.getZ()));
		     
		zxAxisGroup.setTickSeperation(new Point2D(newTickSpeperation.getZ(), newTickSpeperation.getX()));
		       
		xyAxisGroup.setTickSeperation(new Point2D(newTickSpeperation.getX(), newTickSpeperation.getY()));
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
		
	public void setAxisLimitMin(Point3D MinLimit)
	{		
		yzAxisGroup.setAxisMinLimit(new Point2D(MinLimit.getY(), MinLimit.getZ()));
		zxAxisGroup.setAxisMinLimit(new Point2D(MinLimit.getZ(), MinLimit.getX()));
		xyAxisGroup.setAxisMinLimit(new Point2D(MinLimit.getX(), MinLimit.getY()));
	}
	
	public void setAxisLimitMax(Point3D MaxLimit)
	{
		yzAxisGroup.setAxisMaxLimit(new Point2D(MaxLimit.getY(), MaxLimit.getZ()));
		zxAxisGroup.setAxisMaxLimit(new Point2D(MaxLimit.getZ(), MaxLimit.getX()));
		xyAxisGroup.setAxisMaxLimit(new Point2D(MaxLimit.getX(), MaxLimit.getY()));
	}
	
}	


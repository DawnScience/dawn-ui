package org.dawnsci.plotting.javafx.axis.objects;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
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
	
	private EventHandler<MouseEvent> scaleEventHandler;
	
	public SceneObjectGroup(EventHandler<MouseEvent> scaleEventHandler)
	{
		super();
		this.scaleEventHandler = scaleEventHandler;
	}
	/*
	 * public functions
	 */
	
	/**
	 * Create the XYZ axes.
	 * Not declared upon initialisation as DAWN initialises Javafx prior to the plug in being used.
	 * 
	 * @param maxLength
	 * @param axisThickness
	 * @param tickSeperationXYZ
	 */
	public void createAxes(
			Point3D maxLength, 
			double axisThickness, 
			Point3D tickSeperationXYZ)
	{
		yzAxisGroup = new AxesGroup(
				new Point3D(1, 0, 0), 
				new Point2D(tickSeperationXYZ.getY(), tickSeperationXYZ.getZ()),  
				new Point3D(maxLength.getY(), maxLength.getZ(), maxLength.getX()),
				axisThickness,
				scaleEventHandler);
		this.getChildren().add(yzAxisGroup);
		
		zxAxisGroup = new AxesGroup(
				new Point3D(0, 1, 0), 
				new Point2D(tickSeperationXYZ.getZ(), tickSeperationXYZ.getX()),  
				new Point3D(maxLength.getZ(), maxLength.getX(), maxLength.getY()),
				axisThickness,
				scaleEventHandler);
		this.getChildren().add(zxAxisGroup);
			
		xyAxisGroup = new AxesGroup(
				new Point3D(0, 0, 1), 
				new Point2D(tickSeperationXYZ.getX(), tickSeperationXYZ.getY()),  
				new Point3D(maxLength.getX(), maxLength.getY(), maxLength.getZ()),
				axisThickness,
				scaleEventHandler);
		this.getChildren().add(xyAxisGroup);
	}
	
	public void createBoundingBox(Point3D size)
	{
		boundingBox = new BoundingBox(size);
		
		AmbientLight ambientAxisLight = new AmbientLight(Color.WHITE);
		ambientAxisLight.getTransforms().add(new Translate(100,100,100));
		ambientAxisLight.getScope().add(boundingBox);
		boundingBox.getChildren().add(ambientAxisLight);
		
		boundingBox.setColour(Color.ORANGE);
		
		this.getChildren().add(boundingBox);
	}
	
	
	public void checkScale(Point3D newMaxLengthXYZ, double zoom)
	{
		// yz
		yzAxisGroup.updateScale(new Point3D(newMaxLengthXYZ.getY(), newMaxLengthXYZ.getZ(), newMaxLengthXYZ.getX()));
//		// zx
		zxAxisGroup.updateScale(new Point3D(newMaxLengthXYZ.getZ(), newMaxLengthXYZ.getX(), newMaxLengthXYZ.getY()));
		// xy
		xyAxisGroup.updateScale(new Point3D(newMaxLengthXYZ.getX(), newMaxLengthXYZ.getY(), newMaxLengthXYZ.getZ()));
	}
	
	/**
	 * The 
 tick separation with no zooming effect.
	 * @param newTickSpeperation
	 */
	public void SetTickSeparationXYZ(Point3D newTickSpeperation)
	{
		yzAxisGroup.setTickSeperation(new Point2D(newTickSpeperation.getY(), newTickSpeperation.getZ()));
//		     
		zxAxisGroup.setTickSeperation(new Point2D(newTickSpeperation.getZ(), newTickSpeperation.getX()));
		       
		xyAxisGroup.setTickSeperation(new Point2D(newTickSpeperation.getX(), newTickSpeperation.getY()));
	}
	
	
	
	
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
	
	public void flipXGridVisible()
	{
		yzAxisGroup.setVisible(!yzAxisGroup.isVisible());
	}
	public void flipYGridVisible()
	{
		zxAxisGroup.setVisible(!zxAxisGroup.isVisible());
	}
	public void flipZGridVisible()
	{
		xyAxisGroup.setVisible(!xyAxisGroup.isVisible());
	}
	
	public void flipAll()
	{
		flipXGridVisible();
		flipYGridVisible();
		flipZGridVisible();
	}
	
	public void flipBoundingBoxVisibility()
	{
		this.boundingBox.setVisible(!this.boundingBox.isVisible());
	}
		
	public void setAxisLimitMin(Point3D MinLimit)
	{		
		yzAxisGroup.setAxisMinLimit(new Point2D(MinLimit.getY(), MinLimit.getZ()));
		zxAxisGroup.setAxisMinLimit(new Point2D(MinLimit.getZ(), MinLimit.getX()));
		xyAxisGroup.setAxisMinLimit(new Point2D(MinLimit.getX(), MinLimit.getY()));
	}
	
	public void setAxisLimitMax(Point3D MaxLimit)
	{
		yzAxisGroup.setAxisMinLimit(new Point2D(MaxLimit.getY(), MaxLimit.getZ()));
		zxAxisGroup.setAxisMinLimit(new Point2D(MaxLimit.getZ(), MaxLimit.getX()));
		xyAxisGroup.setAxisMinLimit(new Point2D(MaxLimit.getX(), MaxLimit.getY()));
	}
	
	
	
	
}	


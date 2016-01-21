package org.dawnsci.plotting.javafx.axis.objects;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;


/** 
 * @author uij85458
 * 
 * A predefined function to give a basic XYZ axis grid
 * 
 */
public class CombinedAxisGroup extends Group
{ 
	private Point3D origin;
	private Point3D Max;
	
	private AxesGroup yzAxisGroup;
	private AxesGroup zxAxisGroup;
	private AxesGroup xyAxisGroup;
			
	
	public CombinedAxisGroup(
			Point3D origin, 
			Point3D maxLength, 
			double axisThickness, 
			Point3D tickSeperationXYZ, 
			EventHandler<MouseEvent> scaleEventHandler)
	{
		
				
		yzAxisGroup = new AxesGroup(
							new Point3D(1, 0, 0), 
							new Point2D(tickSeperationXYZ.getY(), tickSeperationXYZ.getZ()),  
							new Point2D(maxLength.getY(), maxLength.getZ()),
							maxLength.getX(),
							axisThickness,
							scaleEventHandler);
		zxAxisGroup = new AxesGroup(
							new Point3D(0, 1, 0), 
							new Point2D(tickSeperationXYZ.getZ(), tickSeperationXYZ.getX()),  
							new Point2D(maxLength.getZ(), maxLength.getX()),
							maxLength.getY(),
							axisThickness,
							scaleEventHandler);
		xyAxisGroup = new AxesGroup(
							new Point3D(0, 0, 1), 
							new Point2D(tickSeperationXYZ.getX(), tickSeperationXYZ.getY()),  
							new Point2D(maxLength.getX(), maxLength.getY()),
							maxLength.getZ(),
							axisThickness,
							scaleEventHandler);
			
		this.getChildren().addAll(yzAxisGroup, zxAxisGroup, xyAxisGroup);
		
		// create axis grids
		// yz plane
//		this.getChildren().add(
//				yzAxisGrid = createBasicAxisGrid(
//						new Point3D(1,0,0), 
//						new Point2D(tickSeperationXYZ.getY(), tickSeperationXYZ.getZ()),  
//						new Point2D(maxLength.getY(), maxLength.getZ()), 
//						axisThickness/10));
//		
//		// zx plane
//		this.getChildren().add(
//				zxAxisGrid = createBasicAxisGrid(
//						new Point3D(0,1,0), 
//						new Point2D(tickSeperationXYZ.getZ(), tickSeperationXYZ.getX()),
//						new Point2D(maxLength.getZ(),maxLength.getX()), 
//						axisThickness/10));
//	
//		// xy plane
//		this.getChildren().add(
//				xyAxisGrid = createBasicAxisGrid(
//						new Point3D(0,0,1),
//						new Point2D(tickSeperationXYZ.getX(), tickSeperationXYZ.getY()), 
//						new Point2D(maxLength.getX(), maxLength.getY()), 
//						axisThickness/10));
	}
	/*
	 * public functions
	 */
	
	public void checkScale(Point3D newMaxLengthXYZ)
	{
		// yz
		yzAxisGroup.updateScale(new Point2D(newMaxLengthXYZ.getY(), newMaxLengthXYZ.getZ()));
		// zx
		zxAxisGroup.updateScale(new Point2D(newMaxLengthXYZ.getZ(), newMaxLengthXYZ.getX()));
		// xy
		xyAxisGroup.updateScale(new Point2D(newMaxLengthXYZ.getX(), newMaxLengthXYZ.getY()));
	}
	
	public void SetTickSeparationXYZ(Point3D newTickSpeperation)
	{
		yzAxisGroup.setTickSeperation(new Point2D(newTickSpeperation.getY(), newTickSpeperation.getZ()));
		       
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












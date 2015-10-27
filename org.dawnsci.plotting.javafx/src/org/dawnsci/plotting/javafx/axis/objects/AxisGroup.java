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
import javafx.scene.transform.Translate;


/** 
 * @author uij85458
 * 
 * A predefined function to give a basic XYZ axis grid
 * 
 */
public class AxisGroup extends Group
{ 
	private Point3D origin;
	private Point3D Max;
	
	private AxisGrid xAxisGrid;
	private AxisGrid yAxisGrid;
	private AxisGrid zAxisGrid;
	
	private Point3D axisLimitMin = new Point3D(0, 0, 0);
	private Point3D axisLimitMax = new Point3D(100, 100, 100);
	
	public AxisGroup(Point3D origin, Point3D maxLength, double axisThickness, Point3D tickSeperationXYZ)
	{

		// create axis grids
		// yz plane
		this.getChildren().add(
				xAxisGrid = createBasicAxisGrid(
						new Point3D(1,0,0), 
						new Point2D(tickSeperationXYZ.getY(), tickSeperationXYZ.getZ()),  
						new Point2D(maxLength.getY(), maxLength.getZ()), 
						axisThickness/10)
						);
		
		// zx plane
		this.getChildren().add(
				yAxisGrid = createBasicAxisGrid(
						new Point3D(0,1,0), 
						new Point2D(tickSeperationXYZ.getZ(), tickSeperationXYZ.getX()),
						new Point2D(maxLength.getZ(),maxLength.getX()), 
						axisThickness/10)
						);

		// xy plane
		this.getChildren().add(
				zAxisGrid = createBasicAxisGrid(
						new Point3D(0,0,1),
						new Point2D(tickSeperationXYZ.getX(), tickSeperationXYZ.getY()), 
						new Point2D(maxLength.getX(), maxLength.getY()), 
						axisThickness/10)				
						);
	}

	private Cylinder createScaleAxis(Point3D direction, double length, double thickness)
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

	private AxisGrid createBasicAxisGrid(Point3D planeVectorXYZ, Point2D tickSeperationXY, Point2D axisLengthXY, double thickness)
	{
		AxisGrid tempGrid = new AxisGrid(planeVectorXYZ, tickSeperationXY, axisLengthXY, thickness);
		return tempGrid;
	}
	
	private void reDeclareLabels()
	{
		xAxisGrid.reDeclareLabels(
				new Point2D(axisLimitMin.getY(), axisLimitMin.getZ()), 
				new Point2D(axisLimitMax.getY(), axisLimitMax.getZ()));	
		yAxisGrid.reDeclareLabels(
				new Point2D(axisLimitMin.getZ(), axisLimitMin.getX()), 
				new Point2D(axisLimitMax.getZ(), axisLimitMax.getX()));	
		zAxisGrid.reDeclareLabels(
				new Point2D(axisLimitMin.getX(), axisLimitMin.getY()), 
				new Point2D(axisLimitMax.getX(), axisLimitMax.getY()));	
	}
	
	/*
	 * public functions
	 */
	
	public void checkScale(Point3D newMaxLengthXYZ)
	{
		// yz
		xAxisGrid.updateGrid(new Point2D(newMaxLengthXYZ.getY(), newMaxLengthXYZ.getZ()));
		// zx
		yAxisGrid.updateGrid(new Point2D(newMaxLengthXYZ.getZ(), newMaxLengthXYZ.getX()));
		// xy
		zAxisGrid.updateGrid(new Point2D(newMaxLengthXYZ.getX(), newMaxLengthXYZ.getY()));
		
		reDeclareLabels();
	}
	
	public void setGridXVisible(boolean visible)
	{
		xAxisGrid.setVisible(visible);
	}
	public void setGridYVisible(boolean visible)
	{
		yAxisGrid.setVisible(visible);
	}
	public void setGridZVisible(boolean visible)
	{
		zAxisGrid.setVisible(visible);
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
		xAxisGrid.setVisible(!xAxisGrid.isVisible());
	}
	public void flipYGridVisible()
	{
		yAxisGrid.setVisible(!yAxisGrid.isVisible());
	}
	public void flipZGridVisible()
	{
		zAxisGrid.setVisible(!zAxisGrid.isVisible());
	}
	
	public void flipAll()
	{
		System.out.println("flip all");
		flipXGridVisible();
		flipYGridVisible();
		flipZGridVisible();
	}
	
	public AxisGrid getXAxisGrid()
	{
		return this.xAxisGrid;
	}
	public AxisGrid getYAxisGrid()
	{
		return this.yAxisGrid;
	}
	public AxisGrid getZAxisGrid()
	{
		return this.zAxisGrid;
	}

	public void setAxisData(List<IDataset> axesData) {
		// TODO Auto-generated method stub
		
	}
	
	
}	












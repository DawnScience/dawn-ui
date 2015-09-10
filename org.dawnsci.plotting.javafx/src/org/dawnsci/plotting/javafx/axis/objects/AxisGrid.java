package org.dawnsci.plotting.javafx.axis.objects;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class AxisGrid extends Group
{
	private Group xAxis;
	private Group yAxis;
	
	// tags
	final Point3D X_AXIS_DIRECTION = new Point3D(1, 0, 0);
	final Point3D Y_AXIS_DIRECTION = new Point3D(0, 1, 0);
	
	// transformations
	private Translate offset;
	private Rotate rotate;
		
	// saved details
	private Point3D planeVector;
	private double offsetW;
	private Point2D maxLengthXY;
	private Point2D tickSeperationXY;
	private double thickness;
	private Color colour;
	
	public AxisGrid(Point3D planeXYZ, Point2D tickSeperationXY, Point2D axisLength, double thickness)
	{
		
		
		this.planeVector = planeXYZ;
		this.tickSeperationXY = tickSeperationXY;
		this.maxLengthXY = axisLength;
		this.thickness = thickness;
		
		axisPlane(this.planeVector, this.tickSeperationXY, this.maxLengthXY , this.thickness);		
		
		
	}
	
	/*
	 * private
	 */
	
	// !! make this look nicer
	private void axisPlane(Point3D planeXYZ, Point2D tickSeperationXY, Point2D axisLength, double thickness)
	{
		
		this.xAxis = new Group();		
		this.yAxis = new Group();		
		
		// calculate axis colours
		final double planeVectorMagnitude = Math.sqrt(
				Math.pow(planeXYZ.getX(),2) +
				Math.pow(planeXYZ.getY(),2) +
				Math.pow(planeXYZ.getZ(),2));
		
		// declare the colour, this is pointless but i thought i'd add a little colour for debugging 
		this.colour = new Color(
				planeXYZ.getX() / planeVectorMagnitude,
				planeXYZ.getY() / planeVectorMagnitude,
				planeXYZ.getZ() / planeVectorMagnitude,
				1);
		
		this.getChildren().addAll(xAxis, yAxis);
		
		// default vector - normal to the axis plane
		// axis plane always going to be XY plane
		final Point3D defaultVector = new Point3D(0,0,1);
		// separate plane vector from W translation
		final Point3D planeVector = new Point3D(
				planeXYZ.getX(),
				planeXYZ.getY(),
				planeXYZ.getZ());
		this.rotate = new Rotate();
		this.rotate = (rotateAxisToVector(defaultVector, planeVector, new Point3D(-1, -1, -1) ));
		this.getTransforms().add(this.rotate);
		
		
		// set up the offset transformation -> will be used later
		this.offset = new Translate();
		this.offset = new Translate(0, 0, 0);
		this.getTransforms().add(this.offset);
		
		// finally - once all initialised
		// update the grid -> create the grid
		this.updateGrid(axisLength);
		
	}
	
	// creates a rotation from the plane vector and end vector
	// essentially turns the create plane into the right direction
	// i used this rotation because it allows for a more consistant grid group
	private Rotate rotateAxisToVector(Point3D startVector, Point3D endVector, Point3D rotationVector)
	{
		Point3D startVectorProjection = Vector3DUtil.getVectorPlaneProjection(rotationVector, startVector);
		Point3D endVectorProjection = Vector3DUtil.getVectorPlaneProjection(rotationVector, endVector);
		
		double angle = startVectorProjection.angle(endVectorProjection);
		
		return new Rotate(angle, startVectorProjection.crossProduct(endVectorProjection));
	}
	
	private AxisLineGroup createTickBar(Point2D length, double radius, Point3D axisDirection, Point2D offsetXY, String text)
	{
		
		AxisLineGroup returnBar = new AxisLineGroup(
				radius,
				length.getX(),
				axisDirection,
				new Point3D(offsetXY.getX() ,offsetXY.getY(), 0),
				text);
		
		return returnBar;
	}
	
	/*
	 * public 
	 */
	
	public void reDeclareLabels(Point2D labelMin, Point2D labelMax)
	{
		reDeclareLabelsSpecfic(labelMin, labelMax, xAxis);
		reDeclareLabelsSpecfic(labelMin, labelMax, yAxis);
	}
	
	// !! move
	private void reDeclareLabelsSpecfic(Point2D labelMin, Point2D labelMax, Group axis)
	{
		// create a list of only axis lines from the scene graph
		List<AxisLineGroup> axisLineList = new ArrayList<AxisLineGroup>();
		
		for (Node lineNode: axis.getChildren())
		{
			if (lineNode instanceof AxisLineGroup)
			{
				if (((AxisLineGroup)lineNode).getTextState())
					axisLineList.add((AxisLineGroup)lineNode);
			}
		}
		
		// calculate each lines label
		final int n = axisLineList.size();
		final Point2D minMaxDifference = labelMax.subtract(labelMin);
		final Point2D minMaxStep = minMaxDifference.multiply((double)1/n);
		
		int i = 0;
		for (AxisLineGroup line: axisLineList)
		{
			line.rewriteLabel(Integer.toString((int)(labelMin.getX() + (minMaxStep.getX() * i))));
			i++;
		}
	}
	
	// !! organise
	public void updateGrid(Point2D newMaxLengthXY)
	{
		xAxis.setVisible(true);
		yAxis.setVisible(true);
		
		this.maxLengthXY = newMaxLengthXY;

		// check current axis line lengths
		// x axis
		for (Node n : xAxis.getChildren())
		{
			if (n instanceof AxisLineGroup)
			{
				((AxisLineGroup)n).setHeightExtended(this.maxLengthXY.getX());
				((AxisLineGroup)n).resetOffset();
			}
		}
		// y axis
		for (Node n : yAxis.getChildren())
		{
			if (n instanceof AxisLineGroup)
			{
				((AxisLineGroup)n).setHeightExtended(this.maxLengthXY.getY());
				((AxisLineGroup)n).resetOffset();
			}
			
		}
		
		// check if a new axis line needs to be added
		
		// x axis
		int nCount = yAxis.getChildren().size();
		int excessXLineCount = (int)(( this.maxLengthXY.getX() - (nCount*this.tickSeperationXY.getX()))/ this.tickSeperationXY.getX());
		
		if (excessXLineCount > 0)
		{
			// add new line to axis grid
			for (int i = 0; i < excessXLineCount; i ++)
			{
				AxisLineGroup bar = createTickBar(
						this.maxLengthXY, 
						this.thickness, 
						Y_AXIS_DIRECTION, 
						new Point2D(tickSeperationXY.getX()*(nCount+i),0),
						Double.toString(this.tickSeperationXY.getX()*(nCount+i)));
				bar.setMaterial(new PhongMaterial(colour));
				this.yAxis.getChildren().add(bar);
			}
		}
		else if (excessXLineCount < 0)
		{
			if (this.yAxis.getChildren().size() > 0)
			{
				try
				{
					final int lowerLimit = yAxis.getChildren().size() + excessXLineCount;
					final int upperLimit = yAxis.getChildren().size();
					this.yAxis.getChildren().remove(lowerLimit, upperLimit);
				}
				finally{}
			}
		}
		
		// y axis
		nCount = this.xAxis.getChildren().size();
		int excessYLineCount = (int)(( this.maxLengthXY.getY() - (nCount*this.tickSeperationXY.getY()))/ this.tickSeperationXY.getY());
		
		if ((excessYLineCount) > 0)
		{
			// add new line to axis grid
			for (int i = 0; i < excessYLineCount; i ++)
			{	
				
				AxisLineGroup bar = createTickBar(
						this.maxLengthXY, 
						this.thickness, 
						X_AXIS_DIRECTION, 
						new Point2D(this.tickSeperationXY.getY()*(nCount+i), 0),
						Double.toString(this.tickSeperationXY.getY()*(nCount+i)));
				bar.setMaterial(new PhongMaterial(colour));
				this.xAxis.getChildren().add(bar);
			}
		}
		else if (excessYLineCount < 0)
		{
			if (this.xAxis.getChildren().size() > 0)
			{
				try
				{
					final int lowerLimit = xAxis.getChildren().size() + excessXLineCount;
					final int upperLimit = xAxis.getChildren().size();
					this.xAxis.getChildren().remove(lowerLimit, upperLimit);
				}
				finally{}
			}
		}
	}
	
//	private void removeBarsFromAxis(Group axis, int lowerLimit, int upperLimit)
//	{
//		List<AxisLineGroup> axisLineList = new ArrayList<AxisLineGroup>();
//		
//		for (Node n : axis.getChildren())
//		{
//			boolean added = false;
//			if (n instanceof AxisLineGroup)
//			{
//				for (int i = 0; i < axisLineList.size(); i ++)
//				{
//					if (axisLineList.get(i).getOffset().)
//					{
//						added = true;
//					}
//				}	
//				if (!added)
//				{
//					axisLineList.add((AxisLineGroup)n);
//				}
//			}
//		}
//		
//		
//	}
	
	
	
	
}

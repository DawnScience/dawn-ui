package org.dawnsci.plotting.javafx.axis.objects;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
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
		this.colour = Color.RED;
		
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
	
	private AxisLineGroup createTickBar(double length, Point3D axisDirection, Point2D offsetXY, String text)
	{
		
		AxisLineGroup returnBar = new AxisLineGroup(
				length,
				axisDirection,
				new Point3D(offsetXY.getX(),offsetXY.getY(), 0),
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
	
	public void setTickSeperationXY(Point2D newSeperation)
	{
		this.tickSeperationXY = newSeperation;
		
		refreshGrid();
		
	}
	
	// !! organise
	public void refreshGrid()
	{
		xAxis.getChildren().clear();
		yAxis.getChildren().clear();
		updateGrid();
	}
	
	public void updateGrid()
	{
		updateGrid(maxLengthXY);
	}
	
	public void updateGrid(Point2D newMaxLengthXY)
	{
		
		xAxis.setVisible(true);
		yAxis.setVisible(true);
		
		this.maxLengthXY = newMaxLengthXY;
		
		// check current axis line lengths
		updateLineLengths(xAxis.getChildren(), this.maxLengthXY.getX());
		updateLineLengths(yAxis.getChildren(), this.maxLengthXY.getY());
		
		// check if a new axis line needs to be added
		updateLineCount();
		
		
	}
	
	private void updateLineLengths(ObservableList<Node> aixsGroup, double length)
	{
		for (Node n : aixsGroup)
		{
			if (n instanceof AxisLineGroup)
			{
				((AxisLineGroup)n).setHeightExtended(length);
				((AxisLineGroup)n).resetOffset();
			}
		}
	}
	
	private void updateLineCount() // !! Refactor
	{
		// x axis 
		int nXCount = yAxis.getChildren().size();
		double seperationXLength = this.tickSeperationXY.getX() * (nXCount-1);
		
		
		if (this.maxLengthXY.getX() < seperationXLength)
		{
			int excessXLineCount = (int)(( this.maxLengthXY.getX() - ((nXCount)*this.tickSeperationXY.getX()))/ this.tickSeperationXY.getX());
			
			if (this.yAxis.getChildren().size() > 0)
			{
				final int lowerLimit = yAxis.getChildren().size() + excessXLineCount;
				final int upperLimit = yAxis.getChildren().size();
				this.yAxis.getChildren().remove(lowerLimit, upperLimit);
			}
		}
		else if (this.maxLengthXY.getX() > seperationXLength)
		{
			int excessXLineCount = (int)(( this.maxLengthXY.getX() - ((nXCount - 1)*this.tickSeperationXY.getX()))/ this.tickSeperationXY.getX());
			
			for (int i = 0; i < excessXLineCount; i ++)
			{
				AxisLineGroup bar = createTickBar(
						this.maxLengthXY.getY(),
						Y_AXIS_DIRECTION, 
						new Point2D(tickSeperationXY.getX()*(nXCount+i),0),
						Double.toString(this.tickSeperationXY.getX()*(nXCount+i)));
				bar.setMaterial(new PhongMaterial(colour));
				this.yAxis.getChildren().add(bar);
			}
						
		}
		
		
		
		
		int nYCount = this.xAxis.getChildren().size();
		double seperationYLength = this.tickSeperationXY.getY() * (nYCount-1);
		
		
		if (this.maxLengthXY.getY() < seperationYLength)
		{
			int excessYLineCount = (int)(( this.maxLengthXY.getY() - ((nYCount)*this.tickSeperationXY.getY()))/ this.tickSeperationXY.getY());
		
			if (this.xAxis.getChildren().size() > 0)
			{
				final int lowerLimit = xAxis.getChildren().size() + excessYLineCount;
				final int upperLimit = xAxis.getChildren().size();
				this.xAxis.getChildren().remove(lowerLimit, upperLimit);
			}
		}
		else if (this.maxLengthXY.getY() > seperationYLength)
		{
			int excessYLineCount = (int)(( this.maxLengthXY.getY() - ((nYCount - 1)*this.tickSeperationXY.getY()))/ this.tickSeperationXY.getY());
			
			for (int i = 0; i < excessYLineCount; i ++)
			{	
				
				AxisLineGroup bar = createTickBar(
						this.maxLengthXY.getX(), 
						X_AXIS_DIRECTION, 
						new Point2D(this.tickSeperationXY.getY()*(nYCount+i), 0),
						Double.toString(this.tickSeperationXY.getY()*(nYCount+i)));
				bar.setMaterial(new PhongMaterial(colour));
				this.xAxis.getChildren().add(bar);
			}
		}
		
	}
	
}

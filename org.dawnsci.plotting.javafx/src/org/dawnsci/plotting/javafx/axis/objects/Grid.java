package org.dawnsci.plotting.javafx.axis.objects;

import java.util.ArrayList;
import java.util.List;

import org.dawnsci.plotting.javafx.tools.Vector3DUtil;

import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class Grid extends Group
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
	private Point3D maxLengthXYZ;
	private Point2D tickSeperationXY;
	private double thickness;
	private Color colour;
	private double textSize;
	
	
	public Grid(Point3D planeXYZ, Point2D tickSeperationXY, Point3D axisLength, double thickness, double textSize)
	{
		this.textSize = textSize;
		this.planeVector = planeXYZ;
		this.tickSeperationXY = new Point2D(500,500);
		this.maxLengthXYZ = axisLength;
		this.thickness = thickness;
		
		Point2D maxLengthXY = new Point2D(axisLength.getX(), axisLength.getY());
		
		axisPlane(this.planeVector, this.tickSeperationXY, maxLengthXY , this.thickness);
		
		this.localToSceneTransformProperty().addListener((obs, oldT, newT) -> {
			
			Point3D angles = Vector3DUtil.extractEulerAnglersFromMatrix(newT);
			
			if (angles.getX() > 180)
			{
				offsetLabels(new Point3D(0, axisLength.getY(), 0), yAxis);
			}
			else
			{
				offsetLabels(new Point3D(0, 0, 0), yAxis);
			}
			
			
			angles = angles.subtract(new Point3D(180, 180, 180));
			if (angles.getX() > -90 && angles.getX() < 90)
			{
				offsetGrid(new Point3D(0, 0, 0));
			}
			else
			{
				offsetGrid(new Point3D(0, 0, axisLength.getZ()));
			}
        });	
		
	}
	
	/*
	 * private
	 */

	
	private void offsetGrid(Point3D offset)
	{
		this.offset.setX(offset.getX());
		this.offset.setY(offset.getY());
		this.offset.setZ(offset.getZ());
	}
	
	/**
	 * Sets the translate property of any lineGroup objects within the groups children to the newOffset
	 * @param newOffset
	 * @param axis
	 */
	private void offsetLabels(Point3D newOffset, Group axis)
	{
		for (Node n : axis.getChildren())
		{
			if (n instanceof LineGroup)
			{
				LineGroup lg = (LineGroup)n;
				lg.setTextOffset(newOffset);
			}
		}
	}
	
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
		this.updateGridMaxLength(this.maxLengthXYZ);
		
	}
	/**
	 * Creates a rotation from the plane vector and end vector
	 * essentially turns the create plane into the right direction
	 * i used this rotation because it allows for a more consistant grid group
	 * 
	 * @param startVector
	 * @param endVector
	 * @param rotationVector
	 * @return
	 */
	private Rotate rotateAxisToVector(Point3D startVector, Point3D endVector, Point3D rotationVector)
	{
		Point3D startVectorProjection = Vector3DUtil.getVectorPlaneProjection(rotationVector, startVector);
		Point3D endVectorProjection = Vector3DUtil.getVectorPlaneProjection(rotationVector, endVector);
		
		double angle = startVectorProjection.angle(endVectorProjection);
		
		return new Rotate(angle, startVectorProjection.crossProduct(endVectorProjection));
	}
	
	private LineGroup createTickBar(double length, Point3D axisDirection, Point2D offsetXY, String text)
	{
		
		LineGroup returnBar = new LineGroup(
				length,
				axisDirection,
				new Point3D(offsetXY.getX(),offsetXY.getY(), 0),
				text,
				this.textSize,
				this.rotate);
		return returnBar;
	}
	
	/*
	 * public 
	 */
	
	public void reDeclareLabels(Point2D labelMin, Point2D labelMax)
	{
		reDeclareLabelsSpecfic(labelMin.getX(), labelMax.getX(), yAxis, this.maxLengthXYZ.getX(), tickSeperationXY.getX());
		reDeclareLabelsSpecfic(labelMin.getY(), labelMax.getY(), xAxis, this.maxLengthXYZ.getY(), tickSeperationXY.getY());
	}
	
	// !! move
	private void reDeclareLabelsSpecfic(double labelMin, double labelMax, Group axis, double maxLength, double tickSeperation)
	{
		// create a list of only axis lines from the scene graph
		List<LineGroup> axisLineList = new ArrayList<LineGroup>();
		
		for (Node lineNode: axis.getChildren())
		{
			if (lineNode instanceof LineGroup)
			{
				if (((LineGroup)lineNode).getTextState())
					axisLineList.add((LineGroup)lineNode);
			}
		}
				
		int i = 0;
		for (LineGroup line: axisLineList)
		{			
			final int newValue = (int)(((((tickSeperation * i) / maxLength) * (labelMax - labelMin)) + labelMin) + 0.5f);
			line.rewriteLabel(Integer.toString(newValue));
			i++;
		}
	}
	
	public void setTickSeperationXY(Point2D newSeperation)
	{
		this.tickSeperationXY = newSeperation;
				
		refreshGrid();
	}
	
	public void setTextSize(double newTextSize)
	{
		this.textSize = newTextSize;
	}
	
	public void refreshGrid()
	{
		xAxis.getChildren().clear();
		yAxis.getChildren().clear();
		updateGrid();
	}
	
	public void updateGrid()
	{
		updateGridMaxLength(maxLengthXYZ);
	}
	
	public void updateGridMaxLength(Point3D newMaxLengthXYZ)
	{
		
		xAxis.setVisible(true);
		yAxis.setVisible(true);
		
		this.maxLengthXYZ = newMaxLengthXYZ;
		
		// check current axis line lengths
		updateLineLengths(xAxis.getChildren(), this.maxLengthXYZ.getX());
		updateLineLengths(yAxis.getChildren(), this.maxLengthXYZ.getY());
		
		// check if a new axis line needs to be added
		updateLineCount();
	}
	
	private void updateLineLengths(ObservableList<Node> aixsGroup, double length)
	{
		for (Node n : aixsGroup)
		{
			if (n instanceof LineGroup)
			{
				((LineGroup)n).setHeightExtended(length);
				((LineGroup)n).resetOffset();
			}
		}
	}
	
	private void updateLineCount() // !! Refactor
	{
		// x axis 
		int nXCount = yAxis.getChildren().size();
		double seperationXLength = this.tickSeperationXY.getX() * (nXCount-1);
		
		
		if (this.maxLengthXYZ.getX() < seperationXLength)
		{
			int excessXLineCount = (int)(( this.maxLengthXYZ.getX() - ((nXCount)*this.tickSeperationXY.getX()))/ this.tickSeperationXY.getX());
			
			if (this.yAxis.getChildren().size() > 0)
			{
				final int lowerLimit = yAxis.getChildren().size() + excessXLineCount;
				final int upperLimit = yAxis.getChildren().size();
				this.yAxis.getChildren().remove(lowerLimit, upperLimit);
			}
		}
		else if (this.maxLengthXYZ.getX() > seperationXLength)
		{
			int excessXLineCount = (int)(( this.maxLengthXYZ.getX() - ((nXCount - 1)*this.tickSeperationXY.getX()))/ this.tickSeperationXY.getX());
			
			for (int i = 0; i < excessXLineCount; i ++)
			{
				LineGroup tickLine = createTickBar(
						this.maxLengthXYZ.getY(),
						Y_AXIS_DIRECTION, 
						new Point2D(tickSeperationXY.getX()*(nXCount+i),0),
						Double.toString(this.tickSeperationXY.getX()*(nXCount+i)));
				this.yAxis.getChildren().add(tickLine);
			}		
		}
		
		
		
		
		int nYCount = this.xAxis.getChildren().size();
		double seperationYLength = this.tickSeperationXY.getY() * (nYCount-1);
		
		
		if (this.maxLengthXYZ.getY() < seperationYLength)
		{
			int excessYLineCount = (int)(( this.maxLengthXYZ.getY() - ((nYCount)*this.tickSeperationXY.getY()))/ this.tickSeperationXY.getY());
		
			if (this.xAxis.getChildren().size() > 0)
			{
				final int lowerLimit = xAxis.getChildren().size() + excessYLineCount;
				final int upperLimit = xAxis.getChildren().size();
				this.xAxis.getChildren().remove(lowerLimit, upperLimit);
			}
		}
		else if (this.maxLengthXYZ.getY() > seperationYLength)
		{
			int excessYLineCount = (int)(( this.maxLengthXYZ.getY() - ((nYCount - 1)*this.tickSeperationXY.getY()))/ this.tickSeperationXY.getY());
			
			for (int i = 0; i < excessYLineCount; i ++)
			{	
				
				LineGroup tickLine = createTickBar(
						this.maxLengthXYZ.getX(), 
						X_AXIS_DIRECTION, 
						new Point2D(this.tickSeperationXY.getY()*(nYCount+i), 0),
						null);
				this.xAxis.getChildren().add(tickLine);
			}
		}
		
	}
	
}

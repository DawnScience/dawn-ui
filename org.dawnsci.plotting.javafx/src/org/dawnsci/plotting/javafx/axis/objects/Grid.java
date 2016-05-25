package org.dawnsci.plotting.javafx.axis.objects;

import java.text.DecimalFormat;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

import org.dawnsci.plotting.javafx.tools.Vector3DUtil;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.nebula.visualization.xygraph.linearscale.Tick;
import org.eclipse.nebula.visualization.xygraph.linearscale.TickFactory;
import org.eclipse.nebula.visualization.xygraph.linearscale.TickFactory.TickFormatting;

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
	private double textSize;
	private IDataset tickLookUpTable;
	private Point3D axisLength;

	/**
	 * Initialise an axis grid. Generates a new axis plane along the "planeXYZ" plane. Using the below parameters
	 * @param planeXYZ - The axis plane
	 * @param tickSeperationXY - the separation of each tick.
	 * @param axisLength - the bounding box size of the axes [t].
	 * @param textSize - the text size.
	 *  <p>
	 * [t]: The XY are used to determine tick length, the Z is used to determine the offset of the plane during camera movement.
 	 * </p>
	 */
	public Grid(Point3D planeXYZ, IDataset tickLookUpTable, Point3D axisLength, double textSize)
	{
		// TODO add axis label
		this.textSize = textSize;
		this.planeVector = planeXYZ;
		this.tickLookUpTable = tickLookUpTable;
		this.axisLength = axisLength;
		
		Point2D maxLengthXY = new Point2D(axisLength.getX(), axisLength.getY());
		
		generateAxisPlane(this.planeVector, maxLengthXY);
		
		this.localToSceneTransformProperty().addListener((obs, oldT, newT) -> {
			
			Rotate worldRotate = Vector3DUtil.matrixToRotate(newT);
			
			Point3D zVector = new Point3D(0, 0, 1);
			try 
			{
				zVector = worldRotate.createInverse().transform(zVector);
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
						
			double zAngle = zVector.angle( new Point3D(0, 0, 1));
			double xCross = zVector.crossProduct(new Point3D(0, 0, 1)).getX();
			
			
			if (xCross < 0)
			{
				offsetLabels(new Point3D(0, axisLength.getY(), 0), xAxis);
			}
			else
			{
				offsetLabels(new Point3D(0, 0, 0), xAxis);
			}
			
			zAngle = zAngle - 180;
			if (zAngle > -90 && zAngle < 90)
			{
				setGridOffset(new Point3D(0, 0, 0));
				offsetGrid(new Point3D(0, 0, 0), xAxis);
			}
			else
			{
				setGridOffset(new Point3D(0, 0, axisLength.getZ()));
				offsetGrid(new Point3D(0,0, axisLength.getY()), xAxis);
			}
		});
		
	}
	
	/*
	 * private
	 */
	
	private void setGridOffset(Point3D offset)
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
			if (n instanceof TickGroup)
			{
				TickGroup lg = (TickGroup)n;
				lg.setTextOffset(newOffset);
			}
		}
	}
	
	// !! temp !!
	private void offsetGrid(Point3D newOffset, Group axis)
	{
		for (Node n : axis.getChildren())
		{
			if (n instanceof TickGroup)
			{
				TickGroup lg = (TickGroup)n;
				lg.gridMoved(newOffset);
			}
		}
	}
	
	private void generateAxisPlane(Point3D planeVector, Point2D axisLength)
	{
		
		this.xAxis = new Group();
		this.yAxis = new Group();
		
		generateTicks();
		
		orientatePlane(planeVector);
		
		this.offset = new Translate();
		this.getTransforms().add(this.offset);
	}
	
	private void generateTicks()
	{
		
		TickFactory tickGenerator = new TickFactory(TickFormatting.autoMode, null);
		
		// set the data set size
		List<Tick> xTickList = tickGenerator.generateTicks(0, this.axisLength.getX(), 10, false, true);
		List<Tick> yTickList = tickGenerator.generateTicks(0, this.axisLength.getY(), 10, false, true);
		
		for (Tick t : xTickList)
		{
			double value;
			if (axisLookUpTable == null)
				value = t.getPosition();
			else
				value = axisLookUpTable.getDouble((int)t.getValue());

			DecimalFormat df = new DecimalFormat("0.00");
			df.setMaximumFractionDigits(2);
			
			Point2D pos = new Point2D(
					t.getPosition() * this.axisLength.getX(), 
					0);
			
			xAxis.getChildren().add(createTick(
					this.axisLength.getY(),
					new Point3D(0, 1, 0),
					pos,
					df.format(value)));
		}
		
		for (Tick t : yTickList)
		{
			Point2D pos = new Point2D(t.getPosition()*this.axisLength.getY(), 0);
			
			yAxis.getChildren().add(createTick(
					this.axisLength.getX(),
					new Point3D(1, 0, 0),
					pos,
					null));
		}
		
		this.getChildren().addAll(xAxis, yAxis);
	}
	
	private void orientatePlane(Point3D planeVector)
	{
		// default vector - normal to the axis plane
		// axis plane always going to be XY plane at this stage
		final Point3D defaultVector = new Point3D(0,0,1);
		
		this.rotate = new Rotate();
		
		// align the axis to its new position
		// [-1,-1,-1] is the rotation vector by combining all three axis aligned axes.
		// they are aligned this way to keep the X and Y ticks correctly orientated
		this.rotate = (Vector3DUtil.alignVectorOnPlane(defaultVector, planeVector, new Point3D(-1,-1,-1)));
		this.getTransforms().add(this.rotate);
	}
	
	private TickGroup createTick(double length, Point3D axisDirection, Point2D XYPosition, String label)
	{
		TickGroup returnTick = new TickGroup(
				length,
				axisDirection,
				new Point3D(XYPosition.getX(),XYPosition.getY(), 0),
				label,
				this.textSize,
				this.rotate);
		return returnTick;
	}
	
	/*
	 * public 
	 */
	
	public void setTextSize(double newTextSize)
	{
		this.textSize = newTextSize;
	}

}

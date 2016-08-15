package org.dawnsci.plotting.javafx.axis.objects;


import java.util.List;

import org.dawnsci.plotting.javafx.tools.Vector3DUtil;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.nebula.visualization.xygraph.linearscale.Tick;
import org.eclipse.nebula.visualization.xygraph.linearscale.TickFactory;
import org.eclipse.nebula.visualization.xygraph.linearscale.TickFactory.TickFormatting;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;

/**
 * @author uij85458
 *
 */
public class TickLoop extends Group {
	
	Point3D size;
	IDataset tickLookUpTable;
	private double textSize;
	private Rotate rotate;
	
	/*
	 * Looks like this:
	 * 				Top
	 * origin->0--------->
	 * 			/\		|
	 * 	Left	|		| 	Right
	 * 			|		|
	 * 			|		\/
	 * 			<--------
	 * 				Bottom	
	 * 
	 * Depending on the camera direction 2 planes will made invisible
	 * 
	 */
	private Group top, bottom, left, right;
	
	
	
	public TickLoop(IDataset tickLookUpTable, Point3D size, double textSize, Point3D xDirection, Point3D yDirection)
	{
		super();
		this.tickLookUpTable = tickLookUpTable;
		this.size = size;
		this.textSize = textSize;
		
		generateTicks();
		
		align(xDirection, yDirection);
		
		addUpdateListener();
		
		
	}
	
	private void addUpdateListener()
	{
		this.localToSceneTransformProperty().addListener((obs, oldT, newT) -> {
			
			Rotate worldRotate = Vector3DUtil.matrixToRotate(newT);
			
			// with relation to the ticks not the camera
			Point3D yVector = new Point3D(0, 0, -1);
			try 
			{
				yVector = worldRotate.createInverse().transform(yVector);
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			
			double xAngle = yVector.angle( new Point3D(1, 0, 0));
			double yAngle = yVector.angle( new Point3D(0, 1, 0));
			
			top.setVisible(false);
			bottom.setVisible(false);
			left.setVisible(false);
			right.setVisible(false);
			
			
			labelVisibility(top, 	false);
			labelVisibility(bottom, false);
			labelVisibility(right, 	false);
			labelVisibility(left, 	false);
			
			// tick visibility
			
			if (yAngle < 90)
			{
				top.setVisible(true);
			}
			else
			{
				bottom.setVisible(true);
			}
			
			if (xAngle < 90)
			{
				left.setVisible(true);
			}
			else
			{
				right.setVisible(true);
			}
			
			//label offsets
			
			if (xAngle < 90)
			{
				offsetLabel(top, new Point3D(0, size.getX(), 0));
				offsetLabel(bottom, new Point3D(0, 0, 0));
				
			}
			else
			{
				offsetLabel(top, new Point3D(0, 0, 0));
				offsetLabel(bottom, new Point3D(0, size.getX(), 0));
			}
			
			if (yAngle < 90)
			{
				offsetLabel(left, new Point3D(0, 0, 0));
				offsetLabel(right, new Point3D(0, size.getY(), 0));
				
			}
			else
			{
				offsetLabel(left, new Point3D(0, size.getY(), 0));
				offsetLabel(right, new Point3D(0, 0, 0));
			}
			
			// label visibility
			
			if (yAngle < 45)
			{
				labelVisibility(left, true);
				labelVisibility(right, true);
			}
			
			if (yAngle > 45 && yAngle < 135)
			{
				labelVisibility(top, true);
				labelVisibility(bottom, true);
			}
			
			if (yAngle > 135 )
			{
				labelVisibility(left, true);
				labelVisibility(right, true);
			}
			
		});
	}
	
	private void offsetLabel(Group axis, Point3D newOffset)
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
	
	private void labelVisibility(Group axis, boolean visibility)
	{
		for (Node n : axis.getChildren())
		{
			if (n instanceof TickGroup)
			{
				TickGroup lg = (TickGroup)n;
				lg.setTextVisibility(visibility);
			}
		}
	}
	
	private void align(Point3D xDirection, Point3D yDirection)
	{
		Point3D planeEndDirection = xDirection.crossProduct(yDirection);
		
		this.rotate = Vector3DUtil.AlignPlaneToRotationAndDirection(
				new Point3D(0, 0, 1), new Point3D(1, 0, 0), 
				planeEndDirection, xDirection);
		
		this.getTransforms().add(this.rotate);
	}
	
	private void generateTicks()
	{

		top 	= new Group(); 
		bottom 	= new Group(); 
		left 	= new Group();
		right	= new Group();

		TickFactory tickGenerator = new TickFactory(TickFormatting.autoMode, null);
		
		List<Tick> tickList = tickGenerator.generateTicks(tickLookUpTable.min().doubleValue(),
				tickLookUpTable.max().doubleValue(), 10, false, true);
		
		Point3D maxPivot = new Point3D(0,0,0);
		
		for (Tick t : tickList) {
			
			double Zoffset = t.getPosition() * this.size.getZ();
			
			// add top
			TickGroup topTick = new TickGroup(
										this.size.getX(),
										new Point3D(1, 0, 0), 
										new Point3D(0, 0, Zoffset), 
										t.getText(),
										this.textSize,
										new Rotate());
			this.top.getChildren().add(topTick);
			
			// add right
			TickGroup rightTick = new TickGroup(
										this.size.getY(),
										new Point3D(0, 1, 0), 
										new Point3D(this.size.getX(), 0, Zoffset), 
										t.getText(),
										this.textSize,
										new Rotate());
			this.right.getChildren().add(rightTick);
			
			// add bottom
			TickGroup bottomTick = new TickGroup(
										this.size.getX(),
										new Point3D(-1, 0, 0), 
										new Point3D(this.size.getX(), this.size.getY(), Zoffset), 
										t.getText(),
										this.textSize,
										new Rotate());
			this.bottom.getChildren().add(bottomTick);
			
			// add left
			TickGroup leftTick = new TickGroup(
										this.size.getY(),
										new Point3D(0, -1, 0), 
										new Point3D(0, this.size.getY(), Zoffset), 
										t.getText(),
										this.textSize,
										new Rotate());
			this.left.getChildren().add(leftTick);
		}
		
		
		
		this.getChildren().addAll(this.top, this.right, this.bottom, this.left);
		
	}
	
}

























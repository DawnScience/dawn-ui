package org.dawnsci.plotting.javafx.axis.objects;

import org.dawnsci.plotting.javafx.objects.Line3D;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;

public class BoundingBox extends Group
{
	
	public BoundingBox(Point3D size) {
		super();
		
		addLines(size);
	}
	
	/**
	 * Manual input makes me sad
	 * @param size
	 */
	private void addLines(Point3D size)
	{
		// 000 - 100
		this.getChildren().add(new Line3D(new Point3D(0, 0, 0), new Point3D(size.getX(), 0, 0)));
		// 000 - 010
		this.getChildren().add(new Line3D(new Point3D(0, 0, 0), new Point3D(0, size.getY(), 0)));
		// 100 - 110
		this.getChildren().add(new Line3D(new Point3D(size.getX(), 0, 0), new Point3D(size.getX(), size.getY(), 0)));
		// 010 - 110
		this.getChildren().add(new Line3D(new Point3D(0, size.getY(), 0), new Point3D(size.getX(), size.getY(), 0)));
		
		// 000 - 001
		this.getChildren().add(new Line3D(new Point3D(0, 0, 0), new Point3D(0, 0, size.getZ())));
		// 100 - 101
		this.getChildren().add(new Line3D(new Point3D(size.getX(), 0, 0), new Point3D(size.getX(), 0, size.getZ())));
		// 010 - 011
		this.getChildren().add(new Line3D(new Point3D(0, size.getY(), 0), new Point3D(0, size.getY(), size.getZ())));
		// 110 - 111
		this.getChildren().add(new Line3D(new Point3D(size.getX(), size.getY(), 0), new Point3D(size.getX(), size.getY(), size.getZ())));

		// 001 - 101
		this.getChildren().add(new Line3D(new Point3D(0, 0, size.getZ()), new Point3D(size.getX(), 0, size.getZ())));
		// 001 - 011
		this.getChildren().add(new Line3D(new Point3D(0, 0, size.getZ()), new Point3D(0, size.getY(), size.getZ())));
		// 101 - 111
		this.getChildren().add(new Line3D(new Point3D(size.getX(), 0, size.getZ()), new Point3D(size.getX(), size.getY(), size.getZ())));
		// 011 - 111
		this.getChildren().add(new Line3D(new Point3D(0, size.getY(), size.getZ()), new Point3D(size.getX(), size.getY(), size.getZ())));
	}

	public void setColour(Color colour)
	{
		for (Node line : this.getChildren())
		{
			if (line instanceof Line3D)
			{
				((Line3D)line).setColour(colour);
			}
		}
	}
	
}

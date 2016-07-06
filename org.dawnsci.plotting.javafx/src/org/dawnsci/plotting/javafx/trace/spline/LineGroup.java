package org.dawnsci.plotting.javafx.trace.spline;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point3D;
import javafx.scene.Group;

import org.dawnsci.plotting.javafx.objects.Line3D;
import org.eclipse.january.dataset.IDataset;


/**
 * !!!!!!!!!<br>
 * THIS CLASS HAS NOT BEEN TESTED AND WAS MADE IN ROUGHLY 1 HOUR<br>
 * DO NOT USE UNTIL IT IS TESTED<br>
 * !!!!!!!!!<br>
 * Remove this message once tested<br>
 * @author uij85458
 *
 */
public class LineGroup extends Group
{
	
	List<Line3D> LineList;
	
	public LineGroup(IDataset points)
	{
		LineList = new ArrayList<Line3D>();
		createLineList(points);
		groupLineList();
	}
	
	private void createLineList(IDataset points)
	{
		if (points.getShape()[1] != 3)
			throw new IllegalArgumentException("points is not the right shape - is required to be (n,3)");
		
		for (int i = 1; i < points.getShape()[0]; i ++)
		{
			Point3D start = new Point3D(
									points.getDouble(i-1, 0), 
									points.getDouble(i-1, 1), 
									points.getDouble(i-1, 2));
			Point3D end = new Point3D(
									points.getDouble(i  , 0), 
									points.getDouble(i  , 1), 
									points.getDouble(i  , 2));
			
			LineList.add(new Line3D(start, end));			
		}
		
	}
	
	private void groupLineList()
	{
		this.getChildren().clear();
		for(Line3D line: LineList)
		{
			this.getChildren().add(line);
		}
	}
	
}

package org.dawnsci.plotting.javafx.axis.objects;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class Line extends MeshView
{
	private Translate offset;
	private Rotate rotate;
	private TriangleMesh mesh;
	private double height;
	private PhongMaterial mat;
	
	Line(double height, Rotate rotate, Point3D offset)
	{
		super();
		
		this.height = height;
		this.rotate = rotate;
		this.offset = new Translate(offset.getX(), offset.getY(), offset.getZ());;
		
		ArrayList<Point3D> points = new ArrayList<Point3D>();
		
		points.add(new Point3D(0, 0, 0));
		points.add(new Point3D(0, height, 0));
		
		createLine(points);
		
		this.getTransforms().addAll(this.rotate, this.offset);
		
	}
	public Line(Point3D start, Point3D end)
	{
		List<Point3D> pointList = new ArrayList<Point3D>();
		pointList.add(start);
		pointList.add(end);
		createLine(pointList);
	}
	
	public Line(Point3D start, Point3D end, Color colour)
	{
		this(start, end);
		this.setColour(colour);
	}
			
	private void createLine(List<Point3D> points)
	{
		mesh = new TriangleMesh();
		
		for (Point3D point: points)
		{
			mesh.getPoints().addAll((float)point.getX(), (float)point.getY(), (float)point.getZ());
			mesh.getPoints().addAll((float)point.getX(), (float)point.getY(), (float)point.getZ() + 0.00001f);
			
		}
		
		mesh.getTexCoords().addAll(0,0);
		
		for (int i = 2; i < points.size()*2; i +=2)
		{
			mesh.getFaces().addAll(i   ,0 ,i-2 ,0 ,i+1 ,0 );
			mesh.getFaces().addAll(i+1 ,0 ,i-2 ,0 ,i+1 ,0 );
			
			mesh.getFaces().addAll(i+1 ,0 ,i-2 ,0 ,i   ,0 );
			mesh.getFaces().addAll(i-1 ,0 ,i-2 ,0 ,i+1 ,0 );
		}
		
		this.setDrawMode(DrawMode.LINE);
		
		this.setMesh(mesh);
		this.mat = new PhongMaterial(JavaFXProperties.ColourProperties.LINE_COLOUR);
		
		this.setMaterial(mat);
		this.setCullFace(CullFace.NONE);
	}
		
	public void setColour(Color colour)
	{
		this.mat.setDiffuseColor(colour);
	}
	
	public void setHeightExtended(double newHeight)
	{
		this.height = newHeight;
		
		ArrayList<Point3D> points = new ArrayList<Point3D>();
		
		points.add(new Point3D(0, 0, 0));
		points.add(new Point3D(0, this.height, 0));
		
		createLine(points);
	}
	
	public double getHeight()
	{
		return this.height;
	}
		
	public void editRotate(Rotate newRotate)
	{
		this.rotate.setAngle(newRotate.getAngle());
		this.rotate.setAxis(newRotate.getAxis());
	}
	
	public void editOffset(Translate newMajorOffset)
	{
		this.offset.setX(newMajorOffset.getX());
		this.offset.setY(newMajorOffset.getY());
		this.offset.setZ(newMajorOffset.getZ());
	}
	
}

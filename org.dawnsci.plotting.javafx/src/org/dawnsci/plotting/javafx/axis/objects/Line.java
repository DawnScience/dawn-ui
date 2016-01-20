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
	
	Line(double height, Rotate rotate, Point3D offset)
	{
		super();
		
		this.height = height;
		
		ArrayList<Point3D> points = new ArrayList<Point3D>();
		
		points.add(new Point3D(0, 0, 0));
		points.add(new Point3D(0, height, 0));
		
		createLine(points, Color.BLACK);
		
		this.rotate = rotate;
		this.offset = new Translate(offset.getX(), offset.getY(), offset.getZ());;
		
		this.getTransforms().addAll(this.rotate, this.offset);
		
	}
		
	private void createLine(List<Point3D> points, Color colour)
	{
		mesh = new TriangleMesh();
		
		for (Point3D point: points)
		{
			mesh.getPoints().addAll((float)point.getX(), (float)point.getY(), (float)point.getZ());
			mesh.getPoints().addAll((float)point.getX(), (float)point.getY(), (float)point.getZ() + 0.0001f);
			
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
		
		PhongMaterial mat = new PhongMaterial(colour);
		mat.setDiffuseColor(colour);
		mat.setSpecularColor(colour);
		
		this.setMaterial(mat);
		this.setCullFace(CullFace.NONE);
		
		
	}
	
	public void setRadiusExtended(double newRadius)
	{
		// do nothing for now
		// this.setRadius(newRadius);
	}
	
	public void setHeightExtended(double newHeight)
	{
		this.height = newHeight;
		
		ArrayList<Point3D> points = new ArrayList<Point3D>();
		
		points.add(new Point3D(0, 0, 0));
		points.add(new Point3D(0, this.height, 0));
		
		createLine(points, Color.RED);
		
//		this.setHeight(newHeight);
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

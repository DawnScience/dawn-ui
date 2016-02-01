package org.dawnsci.plotting.javafx.testing.shell;

import org.dawnsci.plotting.javafx.axis.objects.Vector3DUtil;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

public class AxisAlignedPlane extends MeshView{
	
	public AxisAlignedPlane(
			Point2D start, 
			Point2D end,
			Image image,
			Point3D endVector)   
	{
		super();
		
		TriangleMesh mesh = new TriangleMesh();
				
		mesh.getPoints().addAll((float)start.getX(), (float)start.getY(), 	(float)0);
		mesh.getPoints().addAll((float)end.getX(), 	 (float)start.getY(), 	(float)0);
		mesh.getPoints().addAll((float)start.getX(), (float)end.getY(), 	(float)0);
		mesh.getPoints().addAll((float)end.getX(), 	 (float)end.getY(), 	(float)0);
		
		mesh.getTexCoords().addAll(0,0,1,0,0,1,1,1);
		
		mesh.getFaces().addAll(0,0,1,1,3,3);
		mesh.getFaces().addAll(0,0,2,2,3,3);
				
		this.setMesh(mesh);
		
		PhongMaterial mat = new PhongMaterial(new Color(1, 1, 1, 0.5));
		mat.setDiffuseMap(image);
		mat.setSpecularColor(new Color(1, 1, 1, 0.5));
		mat.setDiffuseColor(new Color(1, 1, 1, 0.5));
		this.setOpacity(0.5);
		this.setMaterial(mat);
		
		this.setCullFace(CullFace.NONE);		
	}
}

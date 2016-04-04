package org.dawnsci.plotting.javafx.volume;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;

import org.dawnsci.plotting.javafx.tools.Vector3DUtil;

public class TexturedPlane extends MeshView{
	
	private PhongMaterial mat;
	
	/**
	 * Generate an textured plane. Able to accept an image as a texture.
	 * @param start - Starting point of the plane
	 * @param end - the end point of the plane: (end - start) = size
	 * @param image - texture of the plane
	 * @param facingDirection - aligns the mesh, not the node, to this direction.
	 */
	public TexturedPlane(
			Point2D start, 
			Point2D end,
			Image image,
			Point3D facingDirection)   
	{
		super();
		TriangleMesh mesh = new TriangleMesh();
		
		// generate the plane points		
		mesh.getPoints().addAll((float)start.getX(), (float)start.getY(), 	(float)0);
		mesh.getPoints().addAll((float)end.getX(), 	 (float)start.getY(), 	(float)0);
		mesh.getPoints().addAll((float)start.getX(), (float)end.getY(), 	(float)0);
		mesh.getPoints().addAll((float)end.getX(), 	 (float)end.getY(), 	(float)0);
		
		// declare the indices
		mesh.getTexCoords().addAll(0,0,1,0,0,1,1,1);
		mesh.getFaces().addAll(3,3,1,1,0,0);
		mesh.getFaces().addAll(0,0,2,2,3,3);
		
		this.setMesh(mesh);
		
		// set the material - ie texture, colour, opacity
		mat = new PhongMaterial(new Color(1,1,1,1));
		mat.setDiffuseMap(image);
		mat.setSpecularColor(new Color(1,1,1,1));
		mat.setDiffuseColor( new Color(1,1,1,1));
		this.setMaterial(mat);
		
		// remove the cull face
		this.setCullFace(CullFace.NONE);		
		
		// rotate the plane to the perpendicular vector
		Rotate rotation = Vector3DUtil.alignVector(facingDirection, new Point3D(0, 0, 1));
		this.getTransforms().add(rotation);
		
	}
	
	
	/**
	 * Set the opacity of the plane
	 * @param opacity - The new opacity
	 */
	public void setOpacity_Material(double opacity)
	{
		mat.setDiffuseColor(new Color(
				mat.getDiffuseColor().getRed(),
				mat.getDiffuseColor().getGreen(),
				mat.getDiffuseColor().getBlue(),
				opacity));
	}
	
	/**
	 * Ignores Opacity
	 * @param colour - The new colour
	 */
	public void setColour(Color colour)
	{
		mat.setDiffuseColor(new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), mat.getDiffuseColor().getOpacity()));
	}
	
}

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
	 * @param image1 - texture of the plane
	 * @param facingDirection - aligns the mesh, not the node, to this direction.
	 */
	public TexturedPlane(
			Point3D volumeSize,
			Point2D imagePlaneSize,
			Image image,
			Point3D facingDirection)   
	{
		super();
		
		TriangleMesh mesh = new TriangleMesh();
		
		double offset = image.getWidth() / imagePlaneSize.getX();
		
		int layerCount = (int) offset;
		
		double x = (double)1/layerCount;
		
//		double textureOffset = (double)1/layerCount;
		
		double textureOffset = 1/offset;
		
		double zOffset = volumeSize.getZ()/layerCount;
		
		for (int z = 0; z < layerCount; z++)
		{
		
			// generate the plane points		
			mesh.getPoints().addAll((float)0, 					(float)0, 					(float)(z*zOffset));
			mesh.getPoints().addAll((float)volumeSize.getX(),	(float)0, 					(float)(z*zOffset));
			mesh.getPoints().addAll((float)0, 					(float)volumeSize.getY(), 	(float)(z*zOffset));
			mesh.getPoints().addAll((float)volumeSize.getX(), 	(float)volumeSize.getY(), 	(float)(z*zOffset));
			
			// declare the indices
			mesh.getTexCoords().addAll(generateTextureCoords( (float)textureOffset*z, (float)textureOffset) );
			mesh.getFaces().addAll(generateFaces(z*4));
			
		}
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
	
	private float[] generateTextureCoords(float start, float offset)
	{
		return new float[]{
				(start),		0,
				(start+offset),	0,
				(start),		1,
				(start+offset),	1};
	}
	
	private int[] generateFaces(int indexOffset)
	{
		return new int[]{
				indexOffset+3, indexOffset+3, indexOffset+1, indexOffset+1, indexOffset+0, indexOffset+0,
				indexOffset+0, indexOffset+0, indexOffset+2, indexOffset+2, indexOffset+3, indexOffset+3};
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
		mat.setSpecularColor(new Color(
				mat.getSpecularColor().getRed(),
				mat.getSpecularColor().getGreen(),
				mat.getSpecularColor().getBlue(),
				opacity));
	}
	
	/**
	 * Ignores Opacity
	 * @param colour - The new colour
	 */
	public void setColour(Color colour)
	{
		mat.setSpecularColor(new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), mat.getDiffuseColor().getOpacity()));
		mat.setDiffuseColor(new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), mat.getSpecularColor().getOpacity()));
	}
	
}

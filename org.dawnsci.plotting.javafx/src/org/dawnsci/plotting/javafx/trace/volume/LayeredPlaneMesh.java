package org.dawnsci.plotting.javafx.trace.volume;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;

import org.dawnsci.plotting.javafx.tools.Vector3DUtil;

public class LayeredPlaneMesh extends MeshView{
	
	private PhongMaterial mat;
	private double maxOpacity;
	
	/**
	 * Generate an textured plane. Able to accept an image as a texture.
	 * @param start - Starting point of the plane
	 * @param end - the end point of the plane: (end - start) = size
	 * @param image1 - texture of the plane
	 * @param facingDirection - aligns the mesh, not the node, to this direction.
	 */
	public LayeredPlaneMesh(
			Point3D volumeSize,
			Point2D imagePlaneSize,
			LayeredImageTexture textureData,
			Point3D facingDirection)   
	{
		super();
		TriangleMesh mesh = new TriangleMesh();
		
		
		double zOffset = volumeSize.getZ()/textureData.getStampCount();
		
		for (int z = 0; z < textureData.getStampCount(); z++)
		{
			// generate the plane points		
			mesh.getPoints().addAll((float)0, 					(float)0, 					(float)(z*zOffset));
			mesh.getPoints().addAll((float)volumeSize.getX(),	(float)0, 					(float)(z*zOffset));
			mesh.getPoints().addAll((float)0, 					(float)volumeSize.getY(), 	(float)(z*zOffset));
			mesh.getPoints().addAll((float)volumeSize.getX(), 	(float)volumeSize.getY(), 	(float)(z*zOffset));
			
			// declare the texture stamp positions
			int xStampPos = z % textureData.getWidthStampCount();
			int yStampPos = (z - xStampPos) / textureData.getHeightStampCount();
			
			// declare the texture points and faces indices
			mesh.getTexCoords().addAll(generateTextureCoords(
					(float)(xStampPos * textureData.getStampWidthWeight()), (float)textureData.getStampWidthWeight(),
					(float)(yStampPos * textureData.getStampHeightWeight()), (float)textureData.getStampHeightWeight()));
			mesh.getFaces().addAll(generateFaces(z*4));
			
		}
		this.setMesh(mesh);
		
		// set the material - ie texture, colour, opacity
		mat = new PhongMaterial(new Color(1,1,1,1));
		mat.setDiffuseMap(textureData.getTexture());
		mat.setSpecularColor(new Color(1,1,1,1));
		mat.setDiffuseColor( new Color(1,1,1,1));
		this.setMaterial(mat);
		
		// remove the cull face
		this.setCullFace(CullFace.NONE);		
		
		// rotate the plane to the perpendicular vector
		Rotate rotation = Vector3DUtil.alignVector(facingDirection, new Point3D(0, 0, 1));
		this.getTransforms().add(rotation);
				
	}
	
	private float[] generateTextureCoords(
			float xStart, float xOffset,
			float yStart, float yOffset)
	{
		
		return new float[]{
				(xStart),			(yStart),			
				(xStart+xOffset),	(yStart),
				(xStart),			(yStart+yOffset),			
				(xStart+xOffset),	(yStart+yOffset),};
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
		double newOpacity = maxOpacity * opacity;
		
		mat.setDiffuseColor(new Color(
				mat.getDiffuseColor().getRed(),
				mat.getDiffuseColor().getGreen(),
				mat.getDiffuseColor().getBlue(),
				newOpacity));
		mat.setSpecularColor(new Color(
				mat.getSpecularColor().getRed(),
				mat.getSpecularColor().getGreen(),
				mat.getSpecularColor().getBlue(),
				newOpacity));
	}
	
	public void setMaxOpacity(double opacity)
	{
		maxOpacity = opacity;
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

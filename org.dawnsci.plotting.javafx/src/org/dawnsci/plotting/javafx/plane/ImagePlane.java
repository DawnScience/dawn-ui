package org.dawnsci.plotting.javafx.plane;

import java.awt.image.BufferedImage;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;

import org.dawnsci.plotting.histogram.service.PaletteService;
import org.dawnsci.plotting.javafx.tools.Vector3DUtil;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.histogram.functions.FunctionContainer;

public class ImagePlane extends MeshView
{
	private PhongMaterial mat;
	
	private Point2D size;    
	private Point3D offsets;
	private Point3D planeNormal;
		
	/**
	 * create the image plane with a dataset
	 * @param size
	 * @param dataset
	 * @param offsets
	 * @param planeNormal
	 * @param paletteService
	 */
	public ImagePlane(
			final Point2D size, 
			final IDataset dataset, 
			final Point3D offsets,
			final Point3D planeNormal,
			final PaletteService paletteService)
	{
		super();
		
		this.size        = size;          
		this.offsets     = offsets;       
		this.planeNormal = planeNormal;   
		
		Image image = createImageFromDataset(dataset, paletteService);
		
		generatePlane(image);
	}
	
	/**
	 * create the image plane with an image
	 * @param size
	 * @param image
	 * @param offsets
	 * @param planeNormal
	 * @param paletteService
	 */
	public ImagePlane(
			final Point2D size, 
			Image image, 
			final Point3D offsets,
			final Point3D planeNormal,
			final PaletteService paletteService)
	{
		super();
		
		this.size        = size;          
		this.offsets     = offsets;       
		this.planeNormal = planeNormal;   
				
		generatePlane(image);
	}
	
	public void generatePlane(final Image image)
	{
		
		
		TriangleMesh mesh = new TriangleMesh();
		
		// generate the plane points		
		mesh.getPoints().addAll((float)0, 			(float)0, 			(float)0);
		mesh.getPoints().addAll((float)size.getX(),	(float)0, 			(float)0);
		mesh.getPoints().addAll((float)0, 			(float)size.getY(), (float)0);
		mesh.getPoints().addAll((float)size.getX(), (float)size.getY(), (float)0);
		
		// declare the indices
		mesh.getTexCoords().addAll(generateTextureCoords(0,1));
		mesh.getFaces().addAll(generateFaces(0));
		
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
		Rotate rotation = Vector3DUtil.alignVector(planeNormal, new Point3D(0, 0, 1));
		this.getTransforms().add(rotation);
	}
	

	public Image createImageFromDataset(IDataset dataset, PaletteService ps)
	{
		double minValue = dataset.min(true, true).doubleValue();
		double maxValue = dataset.max(true, true).doubleValue();
		
		FunctionContainer functionContainer = ps.getFunctionContainer("Viridis (blue-green-yellow)");
	
		BufferedImage bi = new BufferedImage(dataset.getShape()[0], dataset.getShape()[1], BufferedImage.TYPE_INT_ARGB);
	
		for (int x = 0; x < dataset.getShape()[0]; x++)
		{
			for (int y = 0; y < dataset.getShape()[1]; y++)
			{
				double drawValue = ((dataset.getDouble(x,y,0)-minValue)/(maxValue-minValue));		
				
				int argb = 255;
				
				argb = (argb << 8) + (int)(functionContainer.getRedFunc().mapToByte(drawValue));
				argb = (argb << 8) + (int)(functionContainer.getGreenFunc().mapToByte(drawValue));
				argb = (argb << 8) + (int)(functionContainer.getBlueFunc().mapToByte(drawValue));
									
				bi.setRGB(x, y, argb);
			}
		}
		
		
		return SwingFXUtils.toFXImage(bi, null); 
		
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
	
	
}

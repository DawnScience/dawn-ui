package org.dawnsci.plotting.javafx.trace.plane;

import java.awt.image.BufferedImage;
import java.util.Arrays;

import javafx.collections.ObservableFloatArray;
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
import javafx.scene.transform.Translate;

import org.dawnsci.plotting.histogram.service.PaletteService;
import org.dawnsci.plotting.javafx.tools.Vector3DUtil;
import org.eclipse.dawnsci.plotting.api.histogram.functions.FunctionContainer;
import org.eclipse.january.dataset.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;

/**
 * !!!!!!!!!<br>
 * THIS CLASS HAS NOT BEEN TESTED AND WAS MADE IN ROUGHLY 1 HOUR<br>
 * DO NOT USE UNTIL IT IS TESTED<br>
 * !!!!!!!!!<br>
 * Remove this message once tested<br>
 * @author uij85458
 *
 */
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
			final ILazyDataset lazyDataset,
			final Point3D offsets,
			final Point3D planeNormal,
			final PaletteService paletteService)
	{
		this(size, createImageFromDataset(lazyDataset, paletteService), offsets, planeNormal);
	}
	
	/**
	 * create the image plane with an image
	 * @param size
	 * @param image
	 * @param offsets
	 * @param planeNormal
	 */
	public ImagePlane(
			final Point2D size, 
			Image image, 
			final Point3D offsets,
			final Point3D planeNormal)
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
		ObservableFloatArray points = mesh.getPoints();
		points.addAll(generateVertexCoords());
		// declare the indices
		mesh.getTexCoords().addAll(generateTextureCoords());
		mesh.getFaces().addAll(generateFaces());
		
		this.setMesh(mesh);
		
		// set the material - ie texture, colour, opacity
		mat = new PhongMaterial(new Color(1,1,1,1));
		mat.setDiffuseMap(image);
		mat.setSpecularColor(new Color(1,1,1,1));
		mat.setDiffuseColor( new Color(1,1,1,1));
		this.setMaterial(mat);
		
		// remove the cull face
		this.setCullFace(CullFace.NONE);

		// translate to offset
		Translate translate = new Translate(offsets.getX(), offsets.getY(), offsets.getZ());
		this.getTransforms().add(translate);

		// lastly rotate the plane to the perpendicular vector
		Rotate rotation = Vector3DUtil.alignVector(planeNormal, new Point3D(0, 0, 1));
		this.getTransforms().add(rotation);
	}

	public static Image createImageFromDataset(ILazyDataset lazyDataset, PaletteService ps)
	{
		IDataset dataset;
		try {
			dataset = lazyDataset.getSlice();
		} catch (DatasetException e) {
			throw new IllegalArgumentException("Could not get data from lazy dataset", e);
		}
		
		double minValue = dataset.min(true, true).doubleValue();
		double maxValue = dataset.max(true, true).doubleValue();
		
		FunctionContainer functionContainer = ps.getFunctionContainer("Viridis (blue-green-yellow)");


		int[] oShape = dataset.getShape();
		if (oShape.length > 2) {
			dataset = dataset.squeezeEnds();
		}
		int[] shape = dataset.getShape();
		switch (shape.length) {
		case 0:
			shape = new int[] {1,1};
			break;
		case 1:
			if (oShape[0] > 1) {
				shape = new int[] {shape[0],1};
			} else {
				shape = new int[] {1, shape[0]};
			}
			break;
		case 2:
			break;
		default:
			throw new IllegalArgumentException("Dataset must have rank <= 2");
		}
		if (!Arrays.equals(oShape, shape)) {
			dataset.setShape(shape);
		}

		// TODO refactor to reuse exist stuff for SWT (and also cope with RGB datasets)
		// see ImageTrace
		// NB there is a SWTFXUtils.toFXImage() method
		BufferedImage bi = new BufferedImage(shape[1], shape[0], BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < shape[0]; y++)
		{
			for (int x = 0; x < shape[1]; x++)
			{
				double drawValue = ((dataset.getDouble(y, x)-minValue)/(maxValue-minValue));
				
				int argb = 255;

				argb = (argb << 8) + (int) (functionContainer.getRedFunc().mapToByte(drawValue));
				argb = (argb << 8) + (int) (functionContainer.getGreenFunc().mapToByte(drawValue));
				argb = (argb << 8) + (int) (functionContainer.getBlueFunc().mapToByte(drawValue));

				bi.setRGB(x, y, argb);
			}
		}

		return SwingFXUtils.toFXImage(bi, null);
	}

	private float[] generateVertexCoords() {
		float x = (float) size.getX();
		float y = (float) size.getY();
		return new float[] {
				0, 0, 0,
				x, 0, 0,
				0, y, 0,
				x, y, 0,
		};
	}

	private float[] generateTextureCoords() {
		return new float[] { 0, 0, 1, 0, 0, 1, 1, 1 };
	}

	private int[] generateFaces() {
		return new int[] { 0, 0, 2, 2, 3, 3, 3, 3, 1, 1, 0, 0 };
	}

	public void setOpacityMaterial(double opacity)
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

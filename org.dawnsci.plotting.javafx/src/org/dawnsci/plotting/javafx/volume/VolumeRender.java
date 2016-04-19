package org.dawnsci.plotting.javafx.volume;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;

public class VolumeRender extends Group
{
	@SuppressWarnings("unused")
	private ILazyDataset lazySlice;
	
	private double max;
	
	private Group xygroup;
	private Group zygroup;
	private Group zxgroup;
	
	/**
	 * Create a volume render from a dataset.
	 * @param size - the size of the volume
	 * @param dataset - the dataset to be rendered
	 * @param intensityValue - the intensity of each values colour
	 */
	public VolumeRender()
	{
		super();
		
		xygroup = new Group();
		zygroup = new Group();
		zxgroup = new Group();
	}
	
	/**
	 * Create a volume render from buffered image planes
	 * @param xyPlanes - list of xy images
	 * @param yzPlanes - list of yz images
	 * @param zxPlanes - list of xz images
	 * @param xyzObjectSize - the size of the volume render (int[3])
	 */
	public VolumeRender(final BufferedImage[] xyPlanes, 
						final BufferedImage[] yzPlanes,
						final BufferedImage[] zxPlanes,
						final int[] xyzObjectSize)
	{
		xygroup = createPlanesFromImageSlice(xyPlanes, new int[]{xyzObjectSize[0], xyzObjectSize[1], xyzObjectSize[2]});
		zygroup = createPlanesFromImageSlice(yzPlanes, new int[]{xyzObjectSize[1], xyzObjectSize[2], xyzObjectSize[0]});
		zxgroup = createPlanesFromImageSlice(zxPlanes, new int[]{xyzObjectSize[2], xyzObjectSize[0], xyzObjectSize[1]});
		
		initialise();
	}
	
	/*
	 * privates 
	 */
	private void initialise()
	{
		zygroup.getTransforms().addAll(
				new Rotate(120, new Point3D(1, 1, 1)));
		
		zxgroup.getTransforms().addAll(
				new Rotate(-120, new Point3D(1, 1, 1)));
		
		xygroup.setDepthTest(DepthTest.DISABLE);
		zygroup.setDepthTest(DepthTest.DISABLE);
		zxgroup.setDepthTest(DepthTest.DISABLE);
		
		this.getChildren().addAll(xygroup, zygroup, zxgroup);
	}
	
	private Group createPlanesFromDataSlice(final double valueIntensity, final int[]XYZSize, final ILazyDataset lazySlice) 
	{
		Group outputGroup = new Group();
		
		for (int z = 0; z < lazySlice.getShape()[2]; z ++)
		{
			IDataset slice = lazySlice.getSlice(
								new int[]{0,0,z}, 
								new int[]{
										lazySlice.getShape()[0],
										lazySlice.getShape()[1],
										z+1},
								new int[]{1,1,1});
			
			BufferedImage bi = new BufferedImage(slice.getShape()[0], slice.getShape()[1],BufferedImage.TYPE_INT_ARGB);
			
			for (int y = 0; y < slice.getShape()[1]; y++)
			{
				for (int x = 0; x < slice.getShape()[0]; x++)
				{
					int value = (int)(((slice.getInt(x,y,0)/max) * valueIntensity) + 0.5f);
					
					// anything with a value of 1 or less gets culled
					// javafx makes it become grey
					if (value < 2)
					{
						value = 0;
					}
					
					int argb = value;
					argb = (argb << 8) + 255;
					argb = (argb << 8) + 255;
					argb = (argb << 8) + 255;
					
					bi.setRGB(x, y, argb);
				}
			}
				
			TexturedPlane newPlane = new TexturedPlane(
					new Point2D(0, 0),
					new Point2D(XYZSize[0], XYZSize[1]),
					SwingFXUtils.toFXImage(bi, null),
					new Point3D(0, 0, 1));
			newPlane.setTranslateZ(z * ((double)XYZSize[2]/ lazySlice.getShape()[2]));
			
			outputGroup.getChildren().add(newPlane);
			
		}
		return outputGroup;
	}
	
	private Group createPlanesFromImageSlice(final BufferedImage[] imagePlanes, final int[] size)
	{
		Group outputGroup = new Group();
		
		double zOffset = size[2] / imagePlanes.length;
		
		for (int i = 0; i < imagePlanes.length; i ++)
		{
			TexturedPlane newPlane = new TexturedPlane(
					new Point2D(0, 0),
					new Point2D(size[0], size[1]),
					SwingFXUtils.toFXImage(imagePlanes[i], null),
					new Point3D(0, 0, 1));
			
			newPlane.setTranslateZ(i * zOffset);
		}
		
		return outputGroup;
	}
	
	private void setGroupColour(Color colour, Group group)
	{
		
		for (Node n : group.getChildren())
		{
			if (n instanceof TexturedPlane)
			{
				((TexturedPlane)n).setColour(colour);
			}
		}
		
	}
	private void setGroupOpacity(double opacity, Group group)
	{
		
		for (Node n : group.getChildren())
		{
			if (n instanceof TexturedPlane)
			{
				((TexturedPlane)n).setOpacity_Material(opacity);
			}
		}
		
	}
	
	
	/* 
	 * publics
	 * 
	 */

	public void compute(final int[] size, final ILazyDataset dataset, final double intensityValue)
	{
		this.max = dataset.getSlice().max(true, true).doubleValue();
		
		xygroup.getChildren().clear(); 
		zygroup.getChildren().clear(); 
		zxgroup.getChildren().clear(); 
		
		// estimate the opacity needed
		int maxDepth = Collections.min((Arrays.asList(ArrayUtils.toObject(dataset.getShape())))); // very strange way to find the max
		double valueIntensity = ((255 / maxDepth) * 25) * intensityValue;
		
		// generate the planes
		xygroup = createPlanesFromDataSlice(valueIntensity, new int[]{size[0], size[1], size[2]}, dataset);
		zygroup = createPlanesFromDataSlice(valueIntensity, new int[]{size[1], size[2], size[0]}, dataset.getTransposedView(1,2,0).getSlice());
		zxgroup = createPlanesFromDataSlice(valueIntensity, new int[]{size[2], size[0], size[1]}, dataset.getTransposedView(2,0,1).getSlice());
		
		initialise();
	}
	
	/**
	 * sets the colour of the volume. IGNORES THE OPACITY.
	 * @param colour - the new colour
	 */
	public void setColour(Color colour)
	{
		setGroupColour(colour, xygroup);
		setGroupColour(colour, zygroup);
		setGroupColour(colour, zxgroup);
	}
	
	/**
	 * sets the opacity of the volume.
	 * @param opacity - the new opacity
	 */
	public void setOpacity_Matrial(double opacity)
	{
		setGroupOpacity(opacity, xygroup);
		setGroupOpacity(opacity, zygroup);
		setGroupOpacity(opacity, zxgroup);
	}
	
}




















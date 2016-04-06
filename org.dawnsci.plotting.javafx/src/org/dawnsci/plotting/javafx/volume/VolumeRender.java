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
import org.dawnsci.plotting.histogram.service.PaletteService;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.plotting.api.histogram.functions.FunctionContainer;

public class VolumeRender extends Group
{
	@SuppressWarnings("unused")
	private ILazyDataset lazySlice;
	
	private double maxValue;
	private double minValue;

	private double maxCulling;
	private double minCulling;
	
	private double opacity;
	private double intensity;
	
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
		
//		this.getChildren().addAll(xygroup);
	}
	
	private Group createPlanesFromDataSlice(
			final int[]XYZSize, 
			final PaletteService paletteService, 
			final ILazyDataset lazySlice) 
	{
		Group outputGroup = new Group();
		
		PaletteService ps = paletteService;
		
		FunctionContainer functionContainer = ps.getFunctionContainer("Viridis (blue-green-yellow)");
				
		BufferedImage bi = new BufferedImage(lazySlice.getShape()[0]*lazySlice.getShape()[2], lazySlice.getShape()[1],BufferedImage.TYPE_INT_ARGB);
		
		for (int z = 0; z < lazySlice.getShape()[2]; z ++)
		{
			IDataset slice = lazySlice.getSlice(
								new int[]{0,0,z},
								new int[]{
										lazySlice.getShape()[0],
										lazySlice.getShape()[1],
										z+1},
								new int[]{1,1,1});
			
			for (int y = 0; y < slice.getShape()[1]; y++)
			{
				for (int x = 0; x < slice.getShape()[0]; x++)
				{
					double drawValue = ((slice.getDouble(x,y,0)-minValue)/(maxValue-minValue));					
					
					if (drawValue < 0)
						drawValue = 0;
					if (drawValue > 1)
						drawValue = 1;

					int argb = 255;
					if (slice.getDouble(x,y,0) < minCulling || slice.getDouble(x,y,0) > maxCulling)
						 argb = 0;
					
					argb = (argb << 8) + (int)(functionContainer.getRedFunc().mapToByte(drawValue)	* intensity);
					argb = (argb << 8) + (int)(functionContainer.getGreenFunc().mapToByte(drawValue)	* intensity);
					argb = (argb << 8) + (int)(functionContainer.getBlueFunc().mapToByte(drawValue)	* intensity);
										
					bi.setRGB(x + (z*slice.getShape()[0]), y, argb);
					
				}
			}
			
		}
		
		
		TexturedPlane newPlane = new TexturedPlane(
				new Point3D(XYZSize[0], XYZSize[1], XYZSize[2]),
				new Point2D(lazySlice.getShape()[0], lazySlice.getShape()[1]),
				SwingFXUtils.toFXImage(bi, null),
				new Point3D(0, 0, 1));
		
		
		double xOffset = (double)(XYZSize[0] / lazySlice.getShape()[0]) / 2 ;
		double yOffset = (double)(XYZSize[1] / lazySlice.getShape()[1]) / 2 ;
				
		newPlane.setTranslateX(-xOffset);
		newPlane.setTranslateY(-yOffset);
		
		newPlane.setOpacity_Material(opacity);
		
		outputGroup.getChildren().add(newPlane);
				
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
	/**
	 * 
	 * @param size
	 * @param dataset
	 * @param intensity
	 * @param opacity
	 * @param paletteService
	 */
	public void compute(
			final int[] size, 
			final ILazyDataset dataset, 
			final double intensity, 
			final double opacity,
			final PaletteService paletteService,
			final double[] minMaxValue,
			final double[] minMaxCulling)
	{

		this.minValue = minMaxValue[0];
		this.maxValue = minMaxValue[1];

		this.minCulling = minMaxCulling[0];
		this.maxCulling = minMaxCulling[1];
				
		this.intensity = intensity;
		this.opacity =  Math.pow(opacity, 3);
				
		xygroup.getChildren().clear(); 
		zygroup.getChildren().clear(); 
		zxgroup.getChildren().clear(); 
				
		// generate the planes
		xygroup = createPlanesFromDataSlice(new int[]{size[0], size[1], size[2]}, paletteService, dataset);
		zygroup = createPlanesFromDataSlice(new int[]{size[1], size[2], size[0]}, paletteService, dataset.getTransposedView(1,2,0).getSlice());
		zxgroup = createPlanesFromDataSlice(new int[]{size[2], size[0], size[1]}, paletteService, dataset.getTransposedView(2,0,1).getSlice());
		
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



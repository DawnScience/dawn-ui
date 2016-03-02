package org.dawnsci.plotting.javafx.axis.volume;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;

public class VolumeRender extends Group
{
	private ILazyDataset lazySlice;
	
	private double max;
	
	private Group xygroup;
	private Group zygroup;
	private Group zxgroup;
	
	
	public VolumeRender(final int[] size, final ILazyDataset dataset, final double opacityValue)
	{
		super();
		
		this.max = dataset.getSlice().max(true, true).doubleValue();
		
		xygroup = new Group();
		zygroup = new Group();
		zxgroup = new Group();
		
		// estimate the opacity needed
		
		int maxDepth = Collections.max((Arrays.asList(ArrayUtils.toObject(dataset.getShape())))); // very strange way to find the max
		double opacity = (maxDepth / 15) * opacityValue;
		
		// generate the planes
		xygroup = createPlanesFromDataSlice(opacity, new int[]{size[0], size[1], size[2]}, dataset);
		zygroup = createPlanesFromDataSlice(opacity, new int[]{size[1], size[2], size[0]}, dataset.getTransposedView(1,2,0).getSlice());
		zxgroup = createPlanesFromDataSlice(opacity, new int[]{size[2], size[0], size[1]}, dataset.getTransposedView(2,0,1).getSlice());
		
		initialise();
	}
	
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
	
	private Group createPlanesFromDataSlice(final double opacity, final int[]XYZSize, final ILazyDataset lazySlice) 
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
					
					int value = (int)(((slice.getInt(x,y,0)/max) * 5) + 0.5f);
					
					int argb = value;
					argb = (argb << 8) + 255;
					argb = (argb << 8) + 0;
					argb = (argb << 8) + 0;
					
					bi.setRGB(x, y, argb);
				}
			}
			
			double x = XYZSize[2]/ lazySlice.getShape()[2];
			
			AxisAlignedPlane newPlane = new AxisAlignedPlane(
					new Point2D(0, 0),
					new Point2D(XYZSize[0], XYZSize[1]),
					SwingFXUtils.toFXImage(bi, null),
					new Point3D(0, 0, 1));
			newPlane.setTranslateZ(z * (XYZSize[2]/ lazySlice.getShape()[2]));
			
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
			AxisAlignedPlane newPlane = new AxisAlignedPlane(
					new Point2D(0, 0),
					new Point2D(size[0], size[1]),
					SwingFXUtils.toFXImage(imagePlanes[i], null),
					new Point3D(0, 0, 1));
			
			newPlane.setTranslateZ(i * zOffset);
			
		}
		
		return outputGroup;
	}
}

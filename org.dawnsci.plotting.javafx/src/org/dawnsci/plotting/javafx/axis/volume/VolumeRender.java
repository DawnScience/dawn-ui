package org.dawnsci.plotting.javafx.axis.volume;

import java.awt.image.BufferedImage;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;

public class VolumeRender extends Group
{
	private ILazyDataset lazySlice;
	
	private Group xygroup;
	private Group zygroup;
	private Group zxgroup;
	
	
	public VolumeRender(final ILazyDataset dataset)
	{
		this(new int[]{1,1,1}, dataset);
	}
	
	public VolumeRender(final int[] shape, final ILazyDataset dataset)
	{
		super();
		
		xygroup = new Group();    
		zygroup = new Group();    
		zxgroup = new Group();    
		
		
		
		xygroup = createPlanesFromDataSlice(lazySlice, 1800);
		zygroup = createPlanesFromDataSlice(lazySlice.getTransposedView(1,2,0).getSlice(), 1800);
		zxgroup = createPlanesFromDataSlice(lazySlice.getTransposedView(2,0,1).getSlice(), 1800);
		
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
	
	private Group createPlanesFromDataSlice(final ILazyDataset lazySlice, final double isoValue) 
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

					int argb = 7;
					if (slice.getInt(x,y,0) > isoValue)
					{
						argb = (argb << 8) + 255;
						argb = (argb << 8) + 0;
						argb = (argb << 8) + 0;
						
					}
					else
					{
						argb = 0;
					}
					bi.setRGB(x, y, argb);
					
				}
			}
			
			AxisAlignedPlane newPlane = new AxisAlignedPlane(
					new Point2D(0, 0),
					new Point2D(slice.getShape()[0], slice.getShape()[1]),
					SwingFXUtils.toFXImage(bi, null),
					new Point3D(0, 0, 1));
			newPlane.setTranslateZ(z);
			
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

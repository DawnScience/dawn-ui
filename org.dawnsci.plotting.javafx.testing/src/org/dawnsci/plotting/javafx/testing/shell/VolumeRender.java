package org.dawnsci.plotting.javafx.testing.shell;

import java.awt.image.BufferedImage;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;

public class VolumeRender extends Group
{
	private Number max;
	private Number min;
	private ILazyDataset lazySlice;
	private int[] shape;
	
	private Group xygroup = new Group();
	private Group zygroup = new Group();
	private Group xzgroup = new Group();
	
	public VolumeRender(int[] shape, ILazyDataset dataset)
	{
		super();
		this.shape = shape;
		this.max = dataset.getSlice().max(true, true);
		this.min = dataset.getSlice().min(true, true);
		
		int[] step = new int[]{
				dataset.getShape()[0]/shape[0],
				dataset.getShape()[1]/shape[1],
				dataset.getShape()[2]/shape[2]};
		
		lazySlice = dataset.getSliceView(new int []{0, 0, 0}, dataset.getShape(), step);
		
		xygroup = createPlanes(lazySlice, 1800);
		zygroup = createPlanes(lazySlice.getTransposedView(1,2,0).getSlice(), 1800);
		xzgroup = createPlanes(lazySlice.getTransposedView(0,2,1).getSlice(), 1800);
		
		xygroup.setDepthTest(DepthTest.DISABLE);
		zygroup.setDepthTest(DepthTest.DISABLE);
		xzgroup.setDepthTest(DepthTest.DISABLE);
		
		this.getChildren().addAll(xygroup, zygroup, xzgroup);
		
		
	}

	private Group createPlanes(ILazyDataset lazySlice, double isoValue) {
		Group group = new Group();
		
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
			
			group.getChildren().add(newPlane);
			
			
		}
			
		return group;
		
	}
	
	
}

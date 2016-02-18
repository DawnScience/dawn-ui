package org.dawnsci.plotting.javafx.testing.shell;

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
	private Number max;
	private Number min;
	private ILazyDataset lazySlice;
//	private int[] shape;
	
	private Group xygroup = new Group();
	private Group zygroup = new Group();
	private Group xzgroup = new Group();
	
	public VolumeRender(double resolutionRatio, ILazyDataset dataset)
	{
		super();
		this.max = dataset.getSlice().max(true, true);
		this.min = dataset.getSlice().min(true, true);
		
		if (resolutionRatio > 1)
		{
			resolutionRatio = 1;
		}
		
		
		int[] step = new int[]{
				(int)((dataset.getShape()[0] * resolutionRatio) + 0.5f),
				(int)((dataset.getShape()[1] * resolutionRatio) + 0.5f),
				(int)((dataset.getShape()[2] * resolutionRatio) + 0.5f)};
		
		lazySlice = dataset.getSliceView(new int []{0, 0, 0}, dataset.getShape(), step);
		
		xygroup = createPlanes(lazySlice, 1800);
		zygroup = createPlanes(lazySlice.getTransposedView(1,2,0).getSlice(), 1800);
		xzgroup = createPlanes(lazySlice.getTransposedView(0,2,1).getSlice(), 1800);
		
		xygroup.setDepthTest(DepthTest.DISABLE);
		
		
		zygroup.setDepthTest(DepthTest.DISABLE);
		zygroup.getTransforms().addAll(
				new Rotate(90, new Point3D(0, 1, 0)));
		
		
		xzgroup.setDepthTest(DepthTest.DISABLE);
		xzgroup.getTransforms().addAll(
				new Rotate(90, new Point3D(1, 0, 0)),
				new Translate(0,0,0));
		
		
		
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

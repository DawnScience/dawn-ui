package org.dawnsci.isosurface.test;

import org.dawnsci.isosurface.Activator;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Random;
import org.eclipse.dawnsci.plotting.api.histogram.IImageService;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.HistoType;
import org.junit.Test;

public class MaxMinSpeedTest
{	
	@Test
	public void testMaxMinSpeed() throws Exception {
		
		final ILazyDataset lz = Random.lazyRand(100, 1024, 1024);
		
		int xMax = Integer.MIN_VALUE;
		int xMin = Integer.MAX_VALUE;
		
		for (int xSlice = 1; xSlice < lz.getShape()[0]; xSlice++ )
		{
			IDataset slicedImage = lz.getSlice(
					new int[] {xSlice,0,0},
					new int[] {xSlice+1,lz.getShape()[1],lz.getShape()[2]},
					new int[] {1,1,1});
			
			if (xMin > slicedImage.minPos()[0])
			{
				xMin = slicedImage.minPos()[0];
			}
			if (xMax > slicedImage.maxPos()[0])
			{
				xMax = slicedImage.maxPos()[0];
			}
			
			
		}
						
		
		// throw new Exception("Test failed! --");
		
	}
	
	@Test 
	public void testBigON1() throws Exception 
	{
		final ILazyDataset lz = Random.lazyRand(1, 1024, 1024);
		
		double testDouble = checkBigON(lz);
				
		//throw new Exception("Test failed! --" + testDouble);
		
	}
	
	@Test
	public void testBigON10() throws Exception 
	{
		final ILazyDataset lz = Random.lazyRand(10, 1024, 1024);
		
		double testDouble = checkBigON(lz);
		
		//throw new Exception("Test failed! --" + testDouble);
		
	}
	
	@Test 
	public void testBigON100() throws Exception 
	{
		final ILazyDataset lz = Random.lazyRand(100, 1024, 1024);
		
		double testDouble = checkBigON(lz);
		
		//throw new Exception("Test failed! --" + testDouble);
		
	}
	
	
	private double checkBigON(ILazyDataset lz)
	{
		double returnDouble = Integer.MIN_VALUE;
		
		for (int xSlice = 1; xSlice < lz.getShape()[0]; xSlice++ )
		{
			IDataset slicedImage = lz.getSlice(
					new int[] {xSlice,0,0},
					new int[] {xSlice+1,lz.getShape()[1],lz.getShape()[2]},
					new int[] {1,1,1});
						
			for (int x = 0; x < slicedImage.getShape()[0]; x++)
			{
				for (int y = 0; y < slicedImage.getShape()[1]; y++)
				{
					for (int z = 0; z < slicedImage.getShape()[2]; z++)
					{
						if (slicedImage.getDouble(x,y,z) > returnDouble)
						{
							returnDouble = slicedImage.getDouble(x,y,z);
						}
					}
				}
			}
		}
		
		return returnDouble;
	}
	
	private double getEstimatedIsovalue(ILazyDataset lz) {
		
		double isovalueMin = Integer.MAX_VALUE;
		double isovalueMax = Integer.MIN_VALUE;
		
		// TODO Auto-generated method stub
//		IDataset slicedImage;
		
		int[] shape = { lz.getShape()[0]/3, 0,0};
		int[] stop =  {1+lz.getShape()[0]/3, lz.getShape()[1], lz.getShape()[2]};
		
		IDataset slicedImage = lz.getSlice(
				new int[] { lz.getShape()[0]/3, 0,0}, 
				new int[] {1+lz.getShape()[0]/3, lz.getShape()[1], lz.getShape()[2]},
				new int[] {1,1,1});
		
		final IImageService service = (IImageService)Activator.getService(IImageService.class);
		double[] stats = service.getFastStatistics(new ImageServiceBean((Dataset)slicedImage, HistoType.MEAN));
		
		if(stats[0]<isovalueMin){
			isovalueMin = stats[0];
		}
		
		if(stats[1]>isovalueMax){
			isovalueMax = stats[1];
		}
		
		IDataset slicedImage1 = lz.getSlice(
				new int[] { lz.getShape()[0]/2, 0,0}, 
                new int[] {1+ lz.getShape()[0]/2, lz.getShape()[1], lz.getShape()[2]},
                new int[] {1,1,1});
		
        stats = service.getFastStatistics(new ImageServiceBean((Dataset)slicedImage1, HistoType.MEAN));
		
		if(stats[0]<isovalueMin){
			isovalueMin = stats[0];
		}
		
		if(stats[1]>isovalueMax){
			isovalueMax = stats[1];
		}
		
		IDataset slicedImage2 = lz.getSlice(
				new int[] { 2*lz.getShape()[0]/3, 0,0}, 
                new int[] {1 + 2*lz.getShape()[0]/3, lz.getShape()[1], lz.getShape()[2]},
                new int[] {1,1,1});
		
		stats = service.getFastStatistics(new ImageServiceBean((Dataset)slicedImage2, HistoType.MEAN));
		
		if(stats[0]<isovalueMin){
			isovalueMin = stats[0];
		}
		
		if(stats[1]>isovalueMax){
			isovalueMax = stats[1];
		}
		
		return (isovalueMin + isovalueMax)/2d;
		
	}
}

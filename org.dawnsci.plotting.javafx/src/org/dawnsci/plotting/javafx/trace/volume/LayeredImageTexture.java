package org.dawnsci.plotting.javafx.trace.volume;

import java.awt.image.BufferedImage;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import org.eclipse.dawnsci.plotting.api.histogram.functions.FunctionContainer;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;

public class LayeredImageTexture {
	
	private int stampCount;
	private int stampCountWidth;
	private int stampCountHeight;
	
	private int stampWidth;
	private int stampHeight;
	private Image texture;
	
	// drawing variables
	private BufferedImage bi;
	private double maxValue;      
	private double minValue;    
	private double maxCulling;   
	private double minCulling;    
	private double intensity;
	private FunctionContainer functionContainer;
	
	public LayeredImageTexture(
			ILazyDataset lazyDataset,
			double maxValue,
			double minValue,
			double maxCulling,
			double minCulling,
			double intensity,
			FunctionContainer functionContainer)
			
	{
		this.maxValue   = maxValue;       
		this.minValue   = minValue;       
		this.maxCulling = maxCulling;     
		this.minCulling = minCulling;     
		this.intensity  = intensity;      
		this.functionContainer = functionContainer;
		
		generatetextureDimensions(lazyDataset);
		createTextureImage(lazyDataset);
	}
	
	private void createTextureImage(ILazyDataset lazyDataset)
	{
		int[] shape = lazyDataset.getShape();
		bi = new BufferedImage(stampCountWidth * stampWidth, stampCountHeight * stampHeight,
				BufferedImage.TYPE_INT_ARGB);
		
		int z = 0;
		int[] start = new int[3];
		int[] stop = shape.clone();
		int[] step = new int[] {1, 1, 1};
		for (int y = 0; y < stampCountHeight && z < stampCount; y ++)
		{
			for (int x = 0; x < stampCountWidth && z < stampCount; x ++)
			{
				start[2] = z;
				stop[2] = z + 1;
				try {
					IDataset slice = lazyDataset.getSlice(start, stop, step);
					addSliceToBufferedImage(x * stampWidth, y * stampHeight, slice, z);
				} catch (DatasetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				z++;
			}
		}
		
		texture = SwingFXUtils.toFXImage(bi, null);
		
	}
	
	private void addSliceToBufferedImage(int xOffset, int yOffset, IDataset slice, int z)
	{
		
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
				argb = (argb << 8) + (int)(functionContainer.getGreenFunc().mapToByte(drawValue)* intensity);
				argb = (argb << 8) + (int)(functionContainer.getBlueFunc().mapToByte(drawValue)	* intensity);
								
				bi.setRGB(x + xOffset, y + yOffset, argb);
				
			}
		}
	}
	
	private void generatetextureDimensions(ILazyDataset lazyDataset)
	{
		stampWidth = lazyDataset.getShape()[0];
		stampHeight = lazyDataset.getShape()[1];
		
		stampCount = lazyDataset.getShape()[2];
		
		// make the texture as square as possible
		double sqrtStampCount = Math.sqrt(stampCount);
		
		int xStampCount = (int)(sqrtStampCount + 1);
		int yStampCount = (int)(sqrtStampCount);
				
		stampCountWidth = xStampCount;
		stampCountHeight = yStampCount;
	}
	
	public Image getTexture(){
		return texture;
	}
	public int getStampCount() {
		return stampCount;
	}

	public int getWidthStampCount() {
		return stampCountWidth;
	}

	public int getHeightStampCount() {
		return stampCountHeight;
	}

	public int getStampWidth() {
		return stampWidth;
	}

	public int getStampHeight() {
		return stampHeight;
	}
	public double getStampWidthWeight(){
		return 1 / (double)stampCountWidth;
	}
	public double getStampHeightWeight(){
		return 1 / (double)stampCountHeight;
	}

	
	
	
}

package org.dawnsci.plotting.api.trace;

import java.util.List;

import org.dawnsci.plotting.api.histogram.ImageServiceBean.HistoType;
import org.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.roi.IROI;

/**
 * Interface used for the plotting system to plot images.
 * 
 * In the LightWeightPlotter this is called ImageTrace.
 * 
 * @author fcp94556
 * 
 * <pre>
 *    Histogramming Explanation
 *    ---------------------------
 *    Image intensity distribution:
 * 
 *                 ++----------------------**---------------
 *                 +                      *  *              
 *                 ++                    *    *             
 *                 |                     *    *             
 *                 ++                    *     *            
 *                 *                    *       *            
 *                 +*                   *       *            
 *                 |*                  *        *            
 *                 +*                  *        *           
 *                 |                  *          *         
 *                 ++                 *          *          
 *                 |                  *           *        
 *                 ++                 *           *        
 *                 |                 *            *        
 *                 ++                *            *       
 *                                  *              *      
 *         Min Cut           Min    *              *      Max                     Max cut
 *  Red <- |   (min colour)  |    (color range, palette)  |      (max color)      | -> Blue
 *                                 *                 *  
 *                 |              *        +         *  
 * ----------------++------------**--------+----------**----+---------------**+---------------++
 * </pre>
 */
public interface IImageTrace extends IPaletteTrace, IDownsampledTrace{

		
	public enum DownsampleType {
		
		POINT(0, "Point, top left of bin"),  // select corner point of bin
		MEAN(1, "Mean value of bin"),   // mean average over bin
		MAXIMUM(2, "Maximum value of bin"), // use maximum value in bin
		MINIMUM(3, "Minimum value of bin"); // use minimum value in bin
		
		private String label;
		private int index;
		
		DownsampleType(int index, String label) {
			this.index = index;
			this.label = label;
		}
		
		public String getLabel() {
			return label;
		}
		public int getIndex() {
			return index;
		}
		public static DownsampleType forLabel(String label) {
			for (DownsampleType type : values()) {
				if (type.label.equals(label)) return type;
			}
			return null;
		}
	}

	/**
	 * Default is TOP_LEFT unlike normal plotting
	 * @return
	 */
	public ImageOrigin getImageOrigin();
	
	/**
	 * Repaints the axes and the image to the new origin.
	 * @param origin
	 */
	public void setImageOrigin(final ImageOrigin origin);
	
	
	/**
	 * Call to set image data
	 * @param image
	 * @param axes - may be null
	 * @param performAutoScale - true to rescale to new selection, otherwise keeps last axis position.
	 * @return false if could not set data
	 */
	public boolean setData(final IDataset image, List<? extends IDataset> axes, boolean performAutoScale);
	
	/**
	 * Change the axes without changing the underlying data.
	 * @param axes
	 * @param performAutoScale
	 */
	public void setAxes(List<? extends IDataset> axes, boolean performAutoScale);
	
	/**
	 * @return the axes if they were set - may be null
	 */
	public List<IDataset> getAxes();
	
	/**
	 * 
	 * @return the down-sample type being used for plotting less data
	 * than received.
	 */
	public DownsampleType getDownsampleType();
	
	/**
	 * Change the down-sample type, will also refresh the UI.
	 * @param type
	 */
	public void setDownsampleType(DownsampleType type);
	
	/**
	 * @param rehisto image when run
	 */
	public void rehistogram();
	
	/**
	 * return the HistoType being used
	 * @return
	 */
	public HistoType getHistoType();
	
	/**
	 * Sets the histo type.
	 */
	public boolean setHistoType(HistoType type);

	/**
	 * You may set the image not to redraw images during updating a number of 
	 * settings for efficiency reasons. Do this in a try{} finally{} block to 
	 * avoid it being left off.
	 * 
	 * @param b
	 */
	void setImageUpdateActive(boolean b);
	
	/**
	 * Call to redraw the image, normally the same as repaint on Figure.
	 */
	public void repaint();
	
	
	/**
	 * 
	 * @return the current downsampled AbstractDataset being used to draw the image.
	 */
	public IDataset getDownsampled();

	/**
	 * 
	 * @return the current downsampled mask or null if there is no mask.
	 */
	public IDataset getDownsampledMask();

	
	/**
	 * @return the bin side in pixels which will be used when drawing the image. 
               The bin is a square of side = the return value.
	 */
	public int getDownsampleBin();

	/**
	 * The masking dataset of there is one, normally null.
	 * false to mask the pixel, true to leave as is.
	 * 
	 * @return
	 */
	public IDataset getMask();
	
	/**
	 * The masking dataset of there is one, normally null.
	 * false to mask the pixel, true to leave as is.
	 * 
	 * If you don't send a BooleanDataset the system may attempt a cast
	 * and throw an exception.
	 * 
	 * @return
	 */
	public void setMask(IDataset bd);

	
	/**
	 * If the axis data set has been set, this method will return 
	 * a selection region in the coordinates of the axes labels rather
	 * than the indices. If no axes are set, then the original roi is
	 * returned.
	 * 
	 * @return ROI in label coordinates. This roi is not that useful after it
	 *         is created. The data processing needs rois with indices.
	 *         
	 * @throws Exception if the roi could not be transformed or the roi type
	 *         is unknown.
	 */
	public IROI getRegionInAxisCoordinates(final IROI roi) throws Exception;
	
	
	/**
	 * If the axis data set has been set, this method will return 
	 * a point in the coordinates of the axes labels rather
	 * than the indices. If no axes are set, then the original point is
	 * returned.
	 * 
	 * NOTE the double[] passed in is not the pixel coordinates point from
	 * events like a mouse click (int[]). It is the data point (indices of
	 * real data the case of the image). The return value is
	 * the data point looked up in the image custom axes. If no custom axes
	 * are set (via the setAxes(..) method) then you will simply get the 
	 * same double passed back.
	 * 
	 * @see ICoordinateSystem
	 * @param  point in index of dataset coordinates (same as ROIs on images use). 
	 * @return point in label coordinates. 
	 * 
	 * @throws Exception if the point could not be transformed or the point type
	 *         is unknown.
	 */
	public double[] getPointInAxisCoordinates(final double[] point) throws Exception;

	/**
	 * For regions over images: if the axis data set has been set, this method will return 
	 * a point in the coordinates of the image indices rather
	 * than the axes. If no axes are set, then the original point is
	 * returned. If the plot is 1D then the original values are returned.
	 * 
	 * @see ICoordinateSystem
	 * @param  point in label coordinates
	 * @return point in index of dataset coordinates (same as ROIs on images use). 
	 * 
	 * @throws Exception if the point could not be transformed or the point type
	 *         is unknown.
	 */
	public double[] getPointInImageCoordinates(final double[] axisLocation) throws Exception;
}

package org.dawnsci.plotting.api.histogram;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

/**
 * 
   Histogramming Explanation
   ---------------------------
   Image intensity distribution:

                ++----------------------**---------------
                +                      *+ *              
                ++                    *    *             
                |                     *    *             
                ++                    *     *            
                *                    *       *            
                +*                   *       *            
                |*                  *        *            
                +*                  *        *           
                |                  *          *         
                ++                 *          *          
                |                  *           *        
                ++                 *           *        
                |                 *            *        
                ++                *            *       
                                 *              *      
        Min Cut           Min    *              *      Max                     Max cut
 Red <- |   (min colour)  |    (color range, palette)  |      (max color)      | -> Blue
                                *                 *  
                |              *        +         *  
----------------++------------**---------+----------**----+---------------**+---------------++

 */
public interface IImageService {

	
	/**
	 * Get a full image data for a given data set and PaletteData
	 * @param set
	 * @return
	 */
	public ImageData getImageData(ImageServiceBean bean) throws Exception;

	/**
	 * Get a full image for a given data set and PaletteData
	 * @param set
	 * @return
	 */
	public Image getImage(ImageServiceBean bean) throws Exception;
	
	/**
	 * Call to calculate max, min to be used in the histogram. These are the
	 * max and min inside the bounds which are to be part of the main histogram.
	 * 
	 * The AbstractDataset and the histogram type must be set in the bean.
	 * If the cut 
	 * 
	 * @param bean
	 * @return
	 */
	public float[] getFastStatistics(ImageServiceBean bean);
}

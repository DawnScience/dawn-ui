package org.dawnsci.plotting.api.trace;

import java.util.List;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.roi.IROI;

/**
 * 
 * This interface is ready for use from now onwards and is to be used for
 * 3D plotting operations. Use IImageTrace normally for images.
 * 
 * 
 * @author fcp94556
 *
 */
public interface ISurfaceTrace extends IPaletteTrace, IWindowTrace {

	/**
	 * Set the data of the plot, will replot if called on an active plot.
	 * @param data
	 * @param axes
	 * @throws Exception
	 */
	public void setData(final IDataset data, final List<? extends IDataset> axes);
			
	/**
	 * 
	 * @param axesNames
	 */
	public void setAxesNames(List<String> axesNames);
		
	/**
	 * 
	 * @return the region of the window, usually a SurfacePlotROI or a RectangularROI
	 */
	public IROI getWindow();
	
	/**
	 * Set the window to be used as a SurfacePlotROI or RectangularROI
	 * @param window
	 */
	public void setWindow(IROI window);

}

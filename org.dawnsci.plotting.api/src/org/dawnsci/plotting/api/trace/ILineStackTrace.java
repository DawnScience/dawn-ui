package org.dawnsci.plotting.api.trace;

import java.util.List;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;

/**
 * Interface for plotting stack of plots in 3D.
 * 
 * For plotting a stack of plots in 1D, staggered in Z.
 * This interface is for stacks in 3D and is more limited because of the
 * features available in the 3D viewer.
 * 
 * If all the tools are required, use createPlot1D(x, manyYs, ...)
 * where manyYs is a stack of 1D data. This will all be plotted in an
 * overlayed manner but allow all tools to be used.
 * 
 * @author fcp94556
 *
 */
public interface ILineStackTrace extends IAxesTrace, IWindowTrace {

	/**
	 * For IStackTrace, calling this method will throw a RuntimeException
	 * all the time which ask you to use getStack() instead.
	 */
	@Override
	public AbstractDataset getData() throws RuntimeException;

	/**
	 * Set the data of the plot, will re-plot if called on an active plot.
	 * @param data
	 * @param axes
	 * @throws Exception
	 */
	public void setData(final List<AbstractDataset> axes, final AbstractDataset... stack);
	
	/**
	 * The stack that is being plotted.
	 * @return
	 */
	public AbstractDataset[] getStack();
			
	/**
	 * 
	 * @param axesNames
	 */
	public void setAxesNames(List<String> axesNames);
	
	
	/**
	 * 
	 * @return the region of the window, usually a LineROI
	 */
	public ROIBase getWindow();
	
	/**
	 * Set the window to be used as a LineROI. The first x value is
	 * the start of the window, the second x value is the end. the y
	 * values are ignored.
	 * 
	 * This window is used to filter down the number of lines in the stack
	 * to avoid it getting to large for the 3D plotter.
	 * 
	 * @param window
	 */
	public void setWindow(ROIBase window);

}

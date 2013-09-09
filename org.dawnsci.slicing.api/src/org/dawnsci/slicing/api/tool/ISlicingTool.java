package org.dawnsci.slicing.api.tool;

import org.dawnsci.slicing.api.system.ISliceSystem;

/**
 * A tool which integrates to the slicing system to 
 * provide difference kinds of slices. For instance 1D, 2D Surface, Hyper 3D.
 * 
 * Normally a tool is added by extending AbstractSlicingTool
 * 
 * @author fcp94556
 *
 */
public interface ISlicingTool {
	
	/**
	 * Called when the tool changes the slicing and
	 * potentially does the first slice in the new 
	 * format. It will also make any UI changes so 
	 * that the UI is set up for this slicing methodology.
	 */
	public void militarize();

	
	/**
	 * The preferred plot type which the tool is active with.
	 * 
	 * Normally one of the enum PlotType however may be a custom
	 * objects because some plots can provide their own custom UI
	 * for slicing. For instance Hyper3D does this.
	 * 
	 * @return the plot type which will be some kind of enum but 
	 *         can be a custom defined one.
	 */
	public Enum getSliceType();
	
	/**
	 * The id of this tool as set in the extension point
	 * @return
	 */
	public String getToolId();
	
	/**
	 * The id of this tool as set in the extension point
	 * @param toolId
	 */
	public void setToolId(String toolId);

	/**
	 * Called internally to ensure that there is an active slicing system available.
	 * @param system
	 */
	public void setSlicingSystem(ISliceSystem system);
	
	/**
	 * 
	 * @return system -  the Slicing System which the tool is registered with.
	 */
	public ISliceSystem getSlicingSystem();

	/**
	 * Called to dispose of tool
	 */
	public void dispose();
	
}

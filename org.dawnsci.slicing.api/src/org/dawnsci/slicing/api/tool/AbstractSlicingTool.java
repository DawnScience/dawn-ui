package org.dawnsci.slicing.api.tool;

import org.dawnsci.slicing.api.system.ISliceSystem;

/**
 * Convenience class for extending to provide a tool.
 * 
 * @author fcp94556
 *
 */
public abstract class AbstractSlicingTool implements ISlicingTool {

	protected ISliceSystem slicingSystem;
	protected String       toolId;

	/**
	 * Does nothing unless overridden.
	 */
	@Override
	public void dispose() {
		
	}

	@Override
	public String getToolId() {
		return toolId;
	}

	@Override
	public void setToolId(String toolId) {
		this.toolId = toolId;
	}

	@Override
	public ISliceSystem getSlicingSystem() {
		return slicingSystem;
	}

	@Override
	public void setSlicingSystem(ISliceSystem slicingSystem) {
		this.slicingSystem = slicingSystem;
	}
	
	
	/**
	 * Does nothing unless overridden.
	 */
	@Override
	public void demilitarize() {
		
	}

}

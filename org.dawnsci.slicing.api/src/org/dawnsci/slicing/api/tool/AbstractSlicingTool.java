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
	public void dispose() {
		
	}

	public String getToolId() {
		return toolId;
	}

	public void setToolId(String toolId) {
		this.toolId = toolId;
	}

	public ISliceSystem getSlicingSystem() {
		return slicingSystem;
	}

	public void setSlicingSystem(ISliceSystem slicingSystem) {
		this.slicingSystem = slicingSystem;
	}
}

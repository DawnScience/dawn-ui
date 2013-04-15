package org.dawnsci.plotting.api.tool;

/**
 * Used to mark views which can contain tools
 * @author fcp94556
 *
 */
public interface IToolContainer {

	/**
	 * The active tool
	 * @return
	 */
	public IToolPage getActiveTool();
	
	/**
	 * Opens the tool in a dedicated view.
	 * @param tool
	 */
	public IToolPage createToolInDedicatedView(IToolPage tool) throws Exception;
}

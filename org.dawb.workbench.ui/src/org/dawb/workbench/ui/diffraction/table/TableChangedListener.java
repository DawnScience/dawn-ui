package org.dawb.workbench.ui.diffraction.table;

import java.util.EventListener;

/**
 * Event Listener to monitor a change (adding/removing images) in the DiffCalTable
 * 
 * @author wqk87977
 *
 */
public interface TableChangedListener extends EventListener {

	/**
	 * Method that is run every time a change in the table occurs.
	 * Changes can be adding or removing image(s).
	 * @param event
	 *         ADDED or REMOVED
	 */
	public void tableChanged(TableChangedEvent event);
}
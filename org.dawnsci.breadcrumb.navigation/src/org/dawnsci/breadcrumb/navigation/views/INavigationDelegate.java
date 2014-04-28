package org.dawnsci.breadcrumb.navigation.views;

import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Provides the table control used by the visit navigation to the UI
 * 
 * This is similar to a page view in that that the content of the visit view may
 * be swapped.
 * 
 * @author fcp94556
 *
 */
public interface INavigationDelegate {

	/**
	 * Called to create the GUI
	 * @param groupLabel
	 * @param toolTip
	 * @param card
	 */
	public void createContent(String groupLabel, String toolTip, Composite card);
	
	/**
	 * Provides the selection provider!
	 * @return
	 */
	public ISelectionProvider getSelectionProvider();
	
	/**
	 * Set actions required for this UI component inactive or active.
	 * @param isActive
	 */
	public void setActionsActive(boolean isActive, IContributionManager man);

	/**
	 * 
	 * @return the control which holds this table (plus any other controls like those
	 * required for searching the table).
	 */
	public Control getControl();

	/**
	 * Sets the table enabled or disabled
	 * @param enabled
	 */
	public void setEnabled(boolean enabled);

	/**
	 * 
	 * @return true if table is visible
	 */
	public boolean isVisible();

	/**
	 * Refresh the table
	 */
	public void refresh();

	/**
	 * 
	 */
	public void dispose();

	/**
	 * clears the content of the viewer.
	 */
	public void clear();

	/**
	 * 
	 * @param cursorCode
	 */
	public void setCursor(Cursor cursor);
}

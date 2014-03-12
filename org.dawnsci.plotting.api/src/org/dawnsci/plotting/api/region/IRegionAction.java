package org.dawnsci.plotting.api.region;

import org.eclipse.jface.action.IAction;

/**
 * An action for creating a region
 * @author fcp94556
 *
 */
public interface IRegionAction extends IAction {
	
	/**
	 * The user object which will be set on the region when it
	 * is created.
	 * @param userObject
	 */
    public Object getUserObject();

	/**
	 * The user object which will be set on the region when it
	 * is created.
	 * @param userObject
	 */
    public void setUserObject(Object userObject);
}

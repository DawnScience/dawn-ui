package org.dawnsci.plotting.api.region;

/**
 * 
 * @author fcp94556
 *
 */
public interface ILockableRegion extends IRegion {

	
	public boolean isCenterMovable();

	/**
	 * Set if the sector center should be locked or not.
	 * @param isCenterMovable
	 */
	public void setCenterMovable(boolean isCenterMovable);

}

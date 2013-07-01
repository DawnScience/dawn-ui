package org.dawnsci.plotting.api.region;

/**
 * 
 * This region allows control over which parts of the region
 * can be moved. The alternative would be an API which allows one to 
 * can and set handle properties by ID. This currently is more complex
 * than is required so a more high level design is implemented.
 * 
 * If the method count here grows above 6, consider replacing ILockableRegion
 * with a more flexible and abstract API, like getting and setting handle
 * properties via abstract ids.
 * 
 * @author fcp94556
 *
 */
public interface ILockableRegion extends IRegion {

	/**
	 * This may not be implemented depending on the region.
	 * @return true if it is moveable
	 */
	public boolean isCentreMoveable();

	/**
	 * This may not be implemented depending on the region.
	 * 
	 * Set if the sector centre should be locked or not.
	 * @param isCenterMoveable
	 */
	public void setCentreMoveable(boolean isCenterMoveable);
	
	/**
	 * This may not be implemented depending on the region.
	 * For instance Ellipse implements this but Sector does not.
	 * @return
	 */
	public boolean isOuterMoveable();

	/**
	 * This may not be implemented depending on the region.
	 * For instance Ellipse implements this but Sector does not.
	 * 
	 * Set if the ellipse outer handles should be locked or not.
	 * @param isCenterMovable
	 */
	public void setOuterMoveable(boolean isOuterMoveable);
}

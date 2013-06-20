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
	 * For instance Sector implements this but Ellipse does not.
	 * @return
	 */
	public boolean isCenterMovable();

	/**
	 * This may not be implemented depending on the region.
	 * For instance Sector implements this but Ellipse does not.
	 * 
	 * Set if the sector center should be locked or not.
	 * @param isCenterMovable
	 */
	public void setCenterMovable(boolean isCenterMovable);
	
	/**
	 * This may not be implemented depending on the region.
	 * For instance Sector implements this but Ellipse does not.
	 * @return
	 */
	public boolean isOuterMovable();

	/**
	 * This may not be implemented depending on the region.
	 * For instance Ellipse implements this but Sector does not.
	 * 
	 * Set if the ellipse outer handles should be locked or not.
	 * @param isCenterMovable
	 */
	public void setOuterMovable(boolean isOuterMovable);

}

package org.dawnsci.plotting.api.region;

/**
 * An interface to provide a region from an object which may be containing it.
 * 
 * For instance children of a selection region and/or the selection region itself may be.
 * 
 * @author fcp94556
 *
 */
public interface IRegionContainer {

	public IRegion getRegion();

	public void setRegion(IRegion region);
}

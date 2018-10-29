package org.dawnsci.mapping.ui.datamodel;

/**
 * Base interface for a map object in the mapping perspective
 * <p>
 * Reflects the simple tree structure of the map data model, and that the map
 * lives in a coordinate space.
 * <p>
 * The area covered by this MapObject is described by its range
 *
 */
public interface MapObject {
	
	public boolean hasChildren();
	
	public Object[] getChildren();
	
	public double[] getRange();
	
}

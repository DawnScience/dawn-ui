package org.dawnsci.multidimensional.ui.imagecuts;

import java.util.EventListener;

import org.dawnsci.multidimensional.ui.imagecuts.CutData.CutType;

/**
 * Listener to update regions used for perpendicular cuts through an image
 *
 */
public interface CutRegionUpdateListener extends EventListener {

	public void updateRequested(double value, double delta, CutType type);

}

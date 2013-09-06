package org.dawnsci.slicing.api.system;

import java.util.EventListener;

public interface DimensionalListener extends EventListener {

	/**
	 * notify the current slice dimensions.
	 * @param evt
	 */
	void dimensionsChanged(DimensionalEvent evt);
}

package org.dawnsci.plotting.api.axis;

import java.util.EventListener;


public interface ICoordinateSystemListener extends EventListener {
	public void coordinatesChanged(CoordinateSystemEvent evt);
}

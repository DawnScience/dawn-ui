package org.dawnsci.datavis.model;

import java.util.EventListener;

public interface PlotModeChangeEventListener extends EventListener {

	public void plotModeChanged();
	
	public void plotStateEvent(PlotEventObject event);
	
}

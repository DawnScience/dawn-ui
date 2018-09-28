package org.dawnsci.january.model;

import java.util.EventListener;

public interface ISliceChangeListener extends EventListener {

	public void sliceChanged(SliceChangeEvent event);
	
	public void axisChanged(SliceChangeEvent event);
	
	public void optionsChanged(SliceChangeEvent event);
	
}

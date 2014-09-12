package org.dawnsci.processing.ui;

import java.util.EventListener;

public interface ISliceChangeListener extends EventListener {
	
	public void sliceChanged(SliceChangeEvent event);

}

package org.dawnsci.processing.ui.slice;

import java.util.EventListener;

public interface IFilesAddedListener extends EventListener {

	public void filesAdded(FileAddedEvent event);
	
}

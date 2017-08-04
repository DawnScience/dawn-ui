package org.dawnsci.datavis.view.quickfile;

import java.util.EventListener;

public interface IQuickFileWidgetListener extends EventListener {

	public void fileSelected(String directory, String name);
	
}

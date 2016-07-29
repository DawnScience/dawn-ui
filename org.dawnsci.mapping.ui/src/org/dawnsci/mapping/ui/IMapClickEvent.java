package org.dawnsci.mapping.ui;

import org.eclipse.dawnsci.plotting.api.axis.ClickEvent;

public interface IMapClickEvent {

	public ClickEvent getClickEvent();
	
	public boolean isDoubleClick();
	
	public String getFilePath();
}

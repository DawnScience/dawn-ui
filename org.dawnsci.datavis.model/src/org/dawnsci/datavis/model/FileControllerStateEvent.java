package org.dawnsci.datavis.model;

import java.util.EventObject;

public class FileControllerStateEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	private boolean selectedFileChanged;
	private boolean selectedDataChanged;

	public FileControllerStateEvent(Object source, boolean selectedFileChanged,
			boolean selectedDataChanged) {
		super(source);
		
		this.selectedDataChanged = selectedDataChanged;
		this.selectedFileChanged = selectedFileChanged;
	}
	

	public boolean isSelectedFileChanged() {
		return selectedFileChanged;
	}

	public boolean isSelectedDataChanged() {
		return selectedDataChanged;
	}

}

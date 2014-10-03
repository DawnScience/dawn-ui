package org.dawnsci.processing.ui.slice;

import java.util.EventObject;

public class FileAddedEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	private String[] paths;
	private boolean[] success;

	public FileAddedEvent(Object source, String[] paths, boolean[] success) {
		super(source);
		this.paths = paths;
		this.success = success;
	}
	
	public String[] getPaths() {
		return paths;
	}

	public boolean[] getSuccess() {
		return success;
	}

}

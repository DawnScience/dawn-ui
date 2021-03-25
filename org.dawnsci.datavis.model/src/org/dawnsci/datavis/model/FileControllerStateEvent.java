package org.dawnsci.datavis.model;

import java.util.EventObject;

public class FileControllerStateEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	private boolean selectedFileChanged;
	private boolean selectedDataChanged;
	private LoadedFile loadedFile;
	private DataOptions option;
	
	private boolean pushSelectionUpdate = false;

	public FileControllerStateEvent(Object source, boolean selectedFileChanged,
			boolean selectedDataChanged, LoadedFile f, DataOptions o) {
		super(source);
		
		this.selectedDataChanged = selectedDataChanged;
		this.selectedFileChanged = selectedFileChanged;
		this.loadedFile = f;
		this.option = o;
	}
	

	public LoadedFile getLoadedFile() {
		return loadedFile;
	}


	public DataOptions getOption() {
		return option;
	}


	public boolean isSelectedFileChanged() {
		return selectedFileChanged;
	}

	public boolean isSelectedDataChanged() {
		return selectedDataChanged;
	}
	
	public boolean isPushSelectionUpdate() {
		return pushSelectionUpdate;
	}

	public void setPushSelectionUpdate(boolean pushSelectionUpdate) {
		this.pushSelectionUpdate = pushSelectionUpdate;
	}

}

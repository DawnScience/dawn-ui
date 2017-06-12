package org.dawnsci.mapping.ui;

import java.io.Serializable;
import java.util.List;

/**
 * Holds the state of MappedDataView so it can be saved and restored
 */

public class MappedDataViewState implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<String> filesInView;

	public List<String> getFilesInView() {
		return filesInView;
	}

	public void setFilesInView(List<String> filesInView) {
		this.filesInView = filesInView;
	}

	@Override
	public String toString() {
		return "MappedDataViewState [filesInView=" + filesInView + "]";
	}
}
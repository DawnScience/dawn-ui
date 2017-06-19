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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((filesInView == null) ? 0 : filesInView.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MappedDataViewState other = (MappedDataViewState) obj;
		if (filesInView == null) {
			if (other.filesInView != null)
				return false;
		} else if (!filesInView.equals(other.filesInView))
			return false;
		return true;
	}
}
package org.dawnsci.mapping.ui.datamodel;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;

public class LiveReMappedData extends ReMappedData implements ILiveData {

	public LiveReMappedData(String name, ILazyDataset map, MappedDataBlock parent, String path) {
		super(name, map, parent, path);
	}

	@Override
	public boolean connect() {
		return false;
	}

	@Override
	public boolean disconnect() {
		return false;
	}

}

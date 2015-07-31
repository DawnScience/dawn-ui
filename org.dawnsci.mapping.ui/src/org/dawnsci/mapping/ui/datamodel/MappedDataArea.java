package org.dawnsci.mapping.ui.datamodel;

import java.util.ArrayList;
import java.util.List;

public class MappedDataArea implements MapObject {

	private List<MappedDataFile> files = new ArrayList<MappedDataFile>();
	
	public void addMappedDataFile(MappedDataFile file) {
		files.add(file);
	}

	@Override
	public String toString() {
		return "Area";
	}

	@Override
	public boolean hasChildren() {
		return true;
	}

	@Override
	public Object[] getChildren() {
		return files.toArray();
	}
	
	public MappedDataFile getDataFile(int index) {
		return files.get(index);
	}
	
}

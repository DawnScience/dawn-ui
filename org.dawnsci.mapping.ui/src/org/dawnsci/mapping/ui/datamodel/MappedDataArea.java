package org.dawnsci.mapping.ui.datamodel;

import java.util.ArrayList;
import java.util.List;

public class MappedDataArea implements MapObject {

	private List<MappedDataFile> files = new ArrayList<MappedDataFile>();
	
	public void addMappedDataFile(MappedDataFile file) {
//		files.clear();
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
	
	public boolean contains(String path) {
		for (MappedDataFile file : files) if (path.equals(file.getPath())) return true;
		return false;
	}
	
	public void removeFile(MappedDataFile file) {
		files.remove(file);
	}
	
	public MappedDataFile getDataFile(int index) {
		return files.get(index);
	}

	public boolean isInRange(MappedDataFile mdf) {
		double[] newRange = mdf.getRange();
		double[] range = getRange();
		
		if (range == null) return true;
		
		if (newRange[0] < range[0] && newRange[1] < range[1]) return false;
		if (newRange[0] > range[0] && newRange[1] > range[1]) return false;
		
		if (newRange[2] < range[2] && newRange[3] < range[3]) return false;
		if (newRange[2] > range[2] && newRange[3] > range[3]) return false;
		
		return true;
	}

	@Override
	public double[] getRange() {
		if (files.isEmpty()) return null;
		
		double[] r = files.get(0).getRange();
		
		for (int i = 1; i < files.size(); i++) {
			double[] range = files.get(i).getRange();
			r[0]  = r[0] < range[0] ? r[0] : range[0];
			r[1]  = r[1] > range[1] ? r[1] : range[1];
			r[2]  = r[2] < range[2] ? r[2] : range[2];
			r[3]  = r[3] > range[3] ? r[3] : range[3];
		}
		
		return r;
	}
	
}

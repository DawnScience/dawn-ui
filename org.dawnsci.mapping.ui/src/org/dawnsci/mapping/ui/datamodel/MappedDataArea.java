package org.dawnsci.mapping.ui.datamodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappedDataArea implements MapObject {

	private List<MappedDataFile> files = new ArrayList<MappedDataFile>();
	private static final Logger logger = LoggerFactory.getLogger(MappedDataArea.class);
	
	private String xAxisName;
	private String yAxisName;
	
	public void setXandYAxesName(String xAxisName, String yAxisName){
		this.xAxisName = xAxisName;
		this.yAxisName = yAxisName;
	}
	
	public void addMappedDataFile(MappedDataFile file) {
//		files.clear();
		if (file.getLiveDataBean() != null) {
		MappedDataFile f = null;
			Iterator<MappedDataFile> iterator = files.iterator();
			while (iterator.hasNext()) {
				MappedDataFile next = iterator.next();
				if (next.getPath().equals(file.getPath())){
					f = next;
					break;
				}
			}
			
		if (f != null) removeFile(f);
			
		}
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
	
	public MappedDataFile getParentFile(MapObject object) {
		return null;
	}
	
	public void locallyReloadLiveFile(String path) {
		
		for (MappedDataFile file : files) {
			if (path.equals(file.getPath())) {
				file.locallyReloadLiveFile();
				return;
			}
		}
		
	}
	
	
	public void removeFile(MappedDataFile file) {
		files.remove(file);
		
		Object[] children = file.getChildren();
		for (Object child : children) {
			if (child instanceof MapObject) {
				try {
					((MapObject)child).disconnect();
				} catch (Exception e) {
					logger.error("Could not disconnect remote dataset",e);
				}
			}
		}
	}
	
	public void removeFile(String filename) {
		
		MappedDataFile file = null;
		
		for (MappedDataFile f : files) {
			if (f.getPath().equals(filename)) {
				file = f;
				break;
			}
		}
		
		if (file == null) return;
		
		removeFile(file);
	}
	
	public MappedDataFile getDataFile(int index) {
		return files.get(index);
	}
	
	public int count() {
		return files.size();
	}
	
	public void clearAll() {
		Iterator<MappedDataFile> iterator = files.iterator();
		
		while (iterator.hasNext()) {
			MappedDataFile file = iterator.next();
			
			Object[] children = file.getChildren();
			for (Object child : children) {
				if (child instanceof MapObject) {
					try {
						((MapObject)child).disconnect();
					} catch (Exception e) {
						logger.error("Could not disconnect remote dataset",e);
					}
				}
			}
			iterator.remove();
		}
	}

	public boolean isInRange(MappedDataFile mdf) {
		double[] newRange = mdf.getRange();
		double[] range = getRange();
		
		if (range == null) return true;
		if (newRange == null) return true;
		return newRange[0] < range[1] &&
			   newRange[1] > range[0] &&
			   newRange[2] < range[3] &&
			   newRange[3] > range[2];
	}
	
	public List<MappedDataBlock> findSuitableParentBlocks(AbstractMapData map){
		List<MappedDataBlock> list = new ArrayList<>();
		for (MappedDataFile file : files) file.addSuitableParentBlocks(map, list);
		return list;
	}

	@Override
	public double[] getRange() {
		if (files.isEmpty()) return null;
		
		double[] r = files.get(0).getRange();
		
		for (int i = 1; i < files.size(); i++) {
			double[] range = files.get(i).getRange();
			if (range == null) continue;
			if (r == null) {
				r = range;
				continue;
			}
			r[0]  = r[0] < range[0] ? r[0] : range[0];
			r[1]  = r[1] > range[1] ? r[1] : range[1];
			r[2]  = r[2] < range[2] ? r[2] : range[2];
			r[3]  = r[3] > range[3] ? r[3] : range[3];
		}
		
		return r;
	}

	@Override
	public boolean disconnect() {
		return true;
	}
	
}

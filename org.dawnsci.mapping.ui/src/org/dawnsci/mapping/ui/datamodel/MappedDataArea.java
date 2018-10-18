package org.dawnsci.mapping.ui.datamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;

public class MappedDataArea implements MapObject {

	private List<MappedDataFile> files = new ArrayList<MappedDataFile>();
	private LiveStreamMapObject streamObject;

	public void addMappedDataFile(MappedDataFile file) {
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
		
		String currentName = null;
			
		for (MappedDataFile f : files) {
			currentName = getPlottedName(f);
			if (currentName != null) {
				break;
			}
		}

		if (currentName != null) {
			for (Object o : file.getChildren()) {
				if (currentName.equals(o.toString()) && o instanceof AbstractMapData) {
					((AbstractMapData)o).setPlotted(true);
				}
			}
		}
		 
		files.add(file);
	}
	
	private String getPlottedName(MappedDataFile f) {
		
		for (Object o : f.getChildren()) {
			if (o instanceof AbstractMapData && ((AbstractMapData) o).isPlotted()) {
				return ((AbstractMapData) o).toString();
			}
		}
		
		return null;
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
		
		Object[] children = files.toArray();
		
		if (streamObject == null) {
			return children;
		}
		
		Object[] withStream = new Object[children.length+1];
		withStream[0] = streamObject;
		System.arraycopy(children, 0, withStream, 1, children.length);
		
		return withStream;
	}
	
	public boolean contains(String path) {
		for (MappedDataFile file : files) if (path.equals(file.getPath())) return true;
		return false;
	}
	
	public boolean isEmpty() {
		return files.isEmpty();
	}
	
	public MappedDataFile getParentFile(MapObject object) {
		Iterator<MappedDataFile> iterator = files.iterator();
		 while (iterator.hasNext()) {
			 MappedDataFile mdf = iterator.next();
			 Object[] children = mdf.getChildren();
			 if (Arrays.asList(children).contains(object)) return mdf;
		 }
		
		return null;
	}
	
	public boolean locallyReloadLiveFile(String path, ILoaderService lservice) {
		
		for (MappedDataFile file : files) {
			if (path.equals(file.getPath())) {
				if (file.isDescriptionSet()) {
					file.locallyReloadLiveFile(lservice);
					return true;
				}
				
				return false;
			}
		}
		
		return false;
		
	}
	
	public List<PlottableMapObject> getPlottedObjects() {
		List<PlottableMapObject> objects = new ArrayList<>();
		
		if (streamObject != null && streamObject.isPlotted()) {
			objects.add(streamObject);
		}
		
		for (MappedDataFile f : files) {
			List<PlottableMapObject> collected = Arrays.stream(f.getChildren())
													   .filter(PlottableMapObject.class::isInstance)
													   .map(PlottableMapObject.class::cast)
													   .filter(PlottableMapObject::isPlotted)
													   .collect(Collectors.toList());
			
			objects.addAll(collected);
		}
		
		return objects;
	}
	
	
	public void removeFile(MappedDataFile file) {
		files.remove(file);
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
	
	public MappedDataFile getDataFile(String path) {
		for (MappedDataFile file : files) {
			if (file.getPath().equals(path)) return file;
		}
		
		return null;
	}
	
	public int count() {
		return files.size();
	}
	
	public void clearAll() {
		Iterator<MappedDataFile> iterator = files.iterator();
		
		while (iterator.hasNext()) {
			iterator.next();
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
	
	public void setStream(LiveStreamMapObject stream) {
		if (streamObject != null) {
			try {
				streamObject.setPlotted(false);
				streamObject.disconnect();
			} catch (Exception e) {
				//TODO
			}
			
		}
		streamObject = stream;
	}
	
}

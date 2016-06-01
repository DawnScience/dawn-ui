package org.dawnsci.mapping.ui.datamodel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappedDataFile implements MapObject{

	private String path;
	private Map<String,MappedDataBlock> fullDataMap;
	private Map<String,AbstractMapData> mapDataMap;
	private Map<String,AssociatedImage> microscopeDataMap;
	private double[] range;
	private LiveDataBean liveBean;
	
//	private final static Logger logger = LoggerFactory.getLogger(MappedDataFile.class);
	
	public MappedDataFile(String path, LiveDataBean liveBean) {
		this.path = path;
		fullDataMap = new HashMap<String,MappedDataBlock>();
		mapDataMap = new HashMap<String,AbstractMapData>();
		microscopeDataMap = new HashMap<String,AssociatedImage>();
		this.liveBean = liveBean;
	}
	
	public String getPath() {
		return path;
	}
	
//	public MappedDataBlock addFullDataBlock(String datasetName, int xdim, int ydim) {
//		
//		//TODO make use of the x and y dimensions
//		MappedDataBlock block = null;
//		try {
//			ILazyDataset lz = LocalServiceManager.getLoaderService().getData(path, null).getLazyDataset(datasetName);
//			block = new MappedDataBlock(datasetName, lz,xdim,ydim, path);
//			fullDataMap.put(datasetName, block);
//		} catch (Exception e) {
//			logger.error("Error loading mapped data block!", e);
//		}
//		
//		return block;
//	}
	
	public Map<String,MappedDataBlock> getDataBlockMap() {
		return fullDataMap;
	}
	
	public void addMapObject(String name, MapObject object) {
		
		if (object instanceof MappedDataBlock) {
			fullDataMap.put(name, (MappedDataBlock)object);
		}else if (object instanceof AbstractMapData) {
			mapDataMap.put(name, (AbstractMapData)object);
		}else if (object instanceof AssociatedImage) {
			microscopeDataMap.put(name, (AssociatedImage)object);
		}
		
		updateRange(object);
		
	}
	
	private void updateRange(MapObject object) {
		if (object == null) return;
		double[] r = object.getRange();
		if (r == null) return;
		if (range == null) {
			range = r;
			return;
		}
		
		range[0]  = r[0] < range[0] ? r[0] : range[0];
		range[1]  = r[1] > range[1] ? r[1] : range[1];
		range[2]  = r[2] < range[2] ? r[2] : range[2];
		range[3]  = r[3] > range[3] ? r[3] : range[3];
		
	}
	
//	public void addMap(String mapName, MappedDataBlock parent) {
//		try {
//			ILazyDataset lz = LocalServiceManager.getLoaderService().getData(path, null).getLazyDataset(mapName);
//			mapDataMap.put(mapName, new MappedData(mapName, lz.getSlice(), parent, path));
//		} catch (Exception e) {
//			logger.error("Error loading mapped data!", e);
//		}
//	}
	
	public AbstractMapData getMap() {
		return mapDataMap.size() > 0 ? mapDataMap.values().iterator().next() : null;
	}
	
	public AssociatedImage getAssociatedImage() {
		return microscopeDataMap.size() > 0 ? microscopeDataMap.values().iterator().next() : null;
	}
	
	public double[] getRange(){
//		if (range == null) {
			for (AbstractMapData map : mapDataMap.values()) {
				updateRange(map);
			}
//		}
		
		return range == null ? null : range.clone(); 
	}
	
//	public void addNonMapImage(String imageName) {
//		try {
//			ILazyDataset lz = LocalServiceManager.getLoaderService().getData(path, null).getLazyDataset(imageName);
//			IDataset test = lz.getSlice(new Slice(0,1),null,null).squeeze();
//			RGBDataset microrgb = new RGBDataset((Dataset)lz.getSlice(new Slice(0,1),null,null).squeeze(),
//					 (Dataset)lz.getSlice(new Slice(1,2),null,null).squeeze(),
//					 (Dataset)lz.getSlice(new Slice(2,3),null,null).squeeze());
//			microrgb.setMetadata(test.getMetadata(AxesMetadata.class).get(0));
//			microscopeDataMap.put(imageName, new AssociatedImage(imageName, microrgb,path));
//		} catch (Exception e) {
//			logger.error("Error non map image!", e);
//		}
//	}
	
	public LiveDataBean getLiveDataBean() {
		return this.liveBean;
	}

	@Override
	public String toString() {
		File f = new File(path);
		return f.getName();
	}

	@Override
	public boolean hasChildren() {
		return true;
	}
	
	public Object[] getChildren() {
		List<MapObject> mo = new ArrayList<MapObject>();
		mo.addAll(fullDataMap.values());
		mo.addAll(mapDataMap.values());
		mo.addAll(microscopeDataMap.values());
		return mo.toArray();
	}
	
}

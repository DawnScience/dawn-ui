package org.dawnsci.mapping.ui.datamodel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawnsci.mapping.ui.LocalServiceManager;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.RGBDataset;

public class MappedDataFile implements MapObject{

	private String path;
	private Map<String,MappedDataBlock> fullDataMap;
	private Map<String,MappedData> mapDataMap;
	private Map<String,AssociatedImage> microscopeDataMap;
	private int mapXDim;
	private int mapYDim;
	
	public MappedDataFile(String path) {
		this.path = path;
		fullDataMap = new HashMap<String,MappedDataBlock>();
		mapDataMap = new HashMap<String,MappedData>();
		microscopeDataMap = new HashMap<String,AssociatedImage>();
	}
	
	public MappedDataBlock addFullDataBlock(String datasetName, int xdim, int ydim) {
		mapXDim = xdim;
		mapYDim = ydim;
		MappedDataBlock block = null;
		try {
			ILazyDataset lz = LocalServiceManager.getLoaderService().getData(path, null).getLazyDataset(datasetName);
			block = new MappedDataBlock(datasetName, lz);
			fullDataMap.put(datasetName, block);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return block;
	}
	
	
	public void addMapObject(String name, MapObject object) {
		
		if (object instanceof MappedDataBlock) fullDataMap.put(name, (MappedDataBlock)object);
		else if (object instanceof MappedData) mapDataMap.put(name, (MappedData)object);
		else if (object instanceof AssociatedImage) microscopeDataMap.put(name, (AssociatedImage)object);
		
	}
	
	public void addMap(String mapName, MappedDataBlock parent) {
		try {
			ILazyDataset lz = LocalServiceManager.getLoaderService().getData(path, null).getLazyDataset(mapName);
			mapDataMap.put(mapName, new MappedData(mapName, lz.getSlice(), parent));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public MappedData getMap() {
		return mapDataMap.size() > 0 ? mapDataMap.values().iterator().next() : null;
	}
	
	public AssociatedImage getAssociatedImage() {
		return microscopeDataMap.size() > 0 ? microscopeDataMap.values().iterator().next() : null;
	}
	
	public void addNonMapImage(String imageName) {
		try {
			ILazyDataset lz = LocalServiceManager.getLoaderService().getData(path, null).getLazyDataset(imageName);
			IDataset test = lz.getSlice(new Slice(0,1),null,null).squeeze();
			RGBDataset microrgb = new RGBDataset((Dataset)lz.getSlice(new Slice(0,1),null,null).squeeze(),
					 (Dataset)lz.getSlice(new Slice(1,2),null,null).squeeze(),
					 (Dataset)lz.getSlice(new Slice(2,3),null,null).squeeze());
			microrgb.setMetadata(test.getMetadata(AxesMetadata.class).get(0));
			microscopeDataMap.put(imageName, new AssociatedImage(imageName, microrgb));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

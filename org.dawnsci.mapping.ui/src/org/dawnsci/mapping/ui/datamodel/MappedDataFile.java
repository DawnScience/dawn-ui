package org.dawnsci.mapping.ui.datamodel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.io.IRemoteDataHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MappedDataFile implements MapObject{

	private IDataHolder dataHolder;
	private String path;
	private Map<String,MappedDataBlock> fullDataMap;
	private Map<String,AbstractMapData> mapDataMap;
	private Map<String,AssociatedImage> microscopeDataMap;
	private Map<String,AssociatedImageStack> stackDataMap;
	private double[] range;
	private MappedDataFileBean descriptionBean;
	private String parentPath;
	private final Object lock = new Object();
	
	private static final Logger logger = LoggerFactory.getLogger(MappedDataFile.class);
	
	public MappedDataFile(String path, MappedDataFileBean liveBean, IDataHolder dataHolder) {
		this(path, dataHolder);
		this.descriptionBean = liveBean;
	}
	
	public MappedDataFile(String path, IDataHolder dataHolder) {
		this.dataHolder = dataHolder;
		this.path = path;
		fullDataMap = new HashMap<>();
		mapDataMap = new HashMap<>();
		microscopeDataMap = new HashMap<>();
		stackDataMap = new HashMap<>();
	}
	
	public MappedDataFile(String path, LiveDataBean bean, IDataHolder dataHolder) {
		this(path, dataHolder);
		if (bean != null) {
			descriptionBean = new MappedDataFileBean();
			descriptionBean.setLiveBean(bean);
		}
	}
	
	public String getPath() {
		return path;
	}
	
	public boolean isDescriptionSet() {
		return descriptionBean != null && !descriptionBean.isEmpty();
	}
	
	public void markFileFinished() {
		if (descriptionBean != null) descriptionBean.setLiveBean(null);
	}
	
	public boolean isFileFinished() {
		return (descriptionBean == null || descriptionBean.getLiveBean() == null);
	}
	
	public void locallyReloadLiveFile(ILoaderService lService){
		if (descriptionBean == null) return;
		markFileFinished();
		
		MappedDataFile tmp;
		try {
			tmp = MappedFileFactory.getMappedDataFile(path, descriptionBean, null,lService.getData(path, null));
		} catch (Exception e) {
			logger.error("Failed local reload");
			return;
		}
		
		
		Iterator<Entry<String, MappedDataBlock>> it = fullDataMap.entrySet().iterator();
		
		while (it.hasNext()) {
			Entry<String, MappedDataBlock> next = it.next();
			MappedDataBlock live = next.getValue();
			String key = next.getKey();
			if (tmp.fullDataMap.containsKey(key)) {
				MappedDataBlock local = tmp.fullDataMap.get(key);
				live.replaceLiveDataset(local.getLazy());
			} else {
				it.remove();
			}
		}
		
		Iterator<Entry<String, AbstractMapData>> mapIt = mapDataMap.entrySet().iterator();
		
		while (mapIt.hasNext()) {
			Entry<String, AbstractMapData> next = mapIt.next();
			AbstractMapData live = next.getValue();
			String key = next.getKey();
			if (tmp.mapDataMap.containsKey(key)) {
				AbstractMapData local = tmp.mapDataMap.get(key);
				live.replaceLiveDataset(local.getData());
			} else {
				it.remove();
			}
		}
		
	}
	
	public Map<String,MappedDataBlock> getDataBlockMap() {
		return fullDataMap;
	}
	
	public void addMapObject(String name, MapObject object) {
		if (object instanceof LockableMapObject) {
			((LockableMapObject)object).setLock(lock);
		}
		if (object instanceof MappedDataBlock) {
			fullDataMap.put(name, (MappedDataBlock)object);
		}else if (object instanceof AbstractMapData) {
			mapDataMap.put(name, (AbstractMapData)object);
		}else if (object instanceof AssociatedImageStack) {
			stackDataMap.put(name, (AssociatedImageStack)object);
		} else if (object instanceof AssociatedImage) {
			microscopeDataMap.put(name, (AssociatedImage)object);
		}
		
		updateRange(object);
		
	}
	
	public void updateRange(MapObject object) {
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
			
			if (mapDataMap.isEmpty()) {
				for (PlottableMapObject o: fullDataMap.values()) {
					updateRange(o);
				}
			}
//		}
		
		return range == null ? null : range.clone(); 
	}
	
	public LiveDataBean getLiveDataBean() {
		if (descriptionBean == null) return null;
		return this.descriptionBean.getLiveBean();
	}
	
	public void addSuitableParentBlocks(AbstractMapData map, List<MappedDataBlock> list){
		if (mapDataMap.containsValue(map)) {
			list.addAll(fullDataMap.values());
		}
	}
	

	@Override
	public String toString() {
		File f = new File(path);
		return f.getName();
	}

	@Override
	public boolean hasChildren() {
		return !isEmpty();
	}
	
	public Object[] getChildren() {
		List<MapObject> mo = new ArrayList<MapObject>();
		mo.addAll(fullDataMap.values());
		mo.addAll(mapDataMap.values());
		mo.addAll(microscopeDataMap.values());
		mo.addAll(stackDataMap.values());
		return mo.toArray();
	}
	
	public boolean isEmpty() {
		return fullDataMap.isEmpty() && mapDataMap.isEmpty() && microscopeDataMap.isEmpty() && stackDataMap.isEmpty();
	}

	public String getParentPath() {
		return parentPath;
	}

	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}

	public void update() {
		if (dataHolder instanceof IRemoteDataHolder) {
			((IRemoteDataHolder) dataHolder).update();
		}
	}
	
	public boolean hasDataPlotted() {
		
		if (mapDataMap.isEmpty() && microscopeDataMap.isEmpty() && fullDataMap.isEmpty() && stackDataMap.isEmpty()) {
			return false;
		}
		
		for (AbstractMapData d : mapDataMap.values()) {
			if (d.isPlotted()) {
				return true;
			}
		}
		
		for (AssociatedImage d : microscopeDataMap.values()) {
			if (d.isPlotted()) {
				return true;
			}
		}
		
		for (MappedDataBlock d : fullDataMap.values()) {
			if(d.isPlotted()) {
				return true;
			}
		}
		
		for (AssociatedImageStack d : stackDataMap.values()) {
			if(d.isPlotted()) {
				return true;
			}
		}
		
		return false;
	}
}

package org.dawnsci.mapping.ui.datamodel;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DataEvent;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataListener;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMapData implements PlottableMapObject{

	private String name;
	protected String path;
	protected IDatasetConnector baseMap;
	protected ILazyDataset map;
	protected MappedDataBlock oParent;
	protected MappedDataBlock parent;
	private int transparency = -1;
	private double[] range;
	
	protected boolean connected = false;
	protected boolean live;
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractMapData.class);
	
	public AbstractMapData(String name, ILazyDataset map, MappedDataBlock parent, String path) {
		this.name = name;
		this.map = map;
		this.path = path;
		this.oParent = this.parent = parent;
		range = calculateRange(map);
	}
	
	public AbstractMapData(String name, IDatasetConnector map, MappedDataBlock parent, String path) {
		this.name = name;
		this.baseMap = map;
		this.path = path;
		this.oParent = this.parent = parent;
		live = true;
	}
	
	public abstract IDataset getSpectrum(double x, double y);
	
	public MappedData makeNewMapWithParent(String name, IDataset ds) {
		return new MappedData(name, ds, parent, path);
	}
	
	public IDataset getMap(){
		try {
		if (map != null && map.getRank() != 2) {
			MapScanDimensions mapDims = parent.getMapDims();
			SliceND s = mapDims.getMapSlice(map);
			IDataset slice;
			
				slice = map.getSlice(s);
			
			slice.squeeze();
			return slice;
		}
		return DatasetUtils.sliceAndConvertLazyDataset(map);
		} catch (DatasetException e) {
			logger.error("Could not slice map");
		}
		
		return null;
	}
	
	public abstract ILazyDataset getData();
	
	
	protected abstract double[] calculateRange(ILazyDataset map);
	
	protected void setRange(double[] range) {
		this.range = range;
	}
	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Object[] getChildren() {
		return null;
	}

	public int getTransparency() {
		return transparency;
	}

	public void setTransparency(int transparency) {
		this.transparency = transparency;
	}

	public MappedDataBlock getParent() {
		return parent;
	}

	public void setParent(MappedDataBlock parent) {
		this.parent = parent;
	}

	public void resetParent() {
		parent = oParent;
	}

	@Override
	public double[] getRange() {
		return range == null ? null : range.clone();
	}
	
	public String getLongName() {
		return path + " : " + name;
	}

	public abstract void replaceLiveDataset(ILazyDataset map);
	
	public abstract void update();
	
	
	public boolean connect() {
		
		try {
			((IDatasetConnector)baseMap).connect();
			((IDatasetConnector)baseMap).addDataListener(new IDataListener() {
				
				@Override
				public void dataChangePerformed(DataEvent evt) {
					// TODO Auto-generated method stub
					
				}
			});
		} catch (Exception e) {
			logger.error("Could not connect to " + toString());
			return false;
		}
		
		if (parent.connect()) {
			connected = true;
			return true;
		}
		
		return false;
	}

	public boolean disconnect() {
		try {
			if (baseMap != null) ((IDatasetConnector)baseMap).disconnect();
		} catch (Exception e) {
			logger.error("Could not disconnect from " + toString());
			return false;
		}
		
		if (parent.disconnect()) {
			connected = false;
			return true;
		}
		
		return false;
	}
}

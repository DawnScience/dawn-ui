package org.dawnsci.mapping.ui.datamodel;

import org.eclipse.january.dataset.DataEvent;
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
	protected IDataset map;
	protected MappedDataBlock oParent;
	protected MappedDataBlock parent;
	private int transparency = -1;
	private double[] range;
	
	protected boolean connected = false;
	protected boolean live;
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractMapData.class);
	
	public AbstractMapData(String name, IDataset map, MappedDataBlock parent, String path) {
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
	
//	protected void buildCurrentSlice() {
//		xDim = parent.getxDim();
//		yDim = parent.getyDim();
//		if (map == null && baseMap == null) return;
//		
//		ILazyDataset m = map;
//		
//		if (baseMap != null) {
//			m = baseMap.getDataset();
//		}
//		
//		currentSlice = new SliceND(m.getShape());
//		for (int i = 0; i < m.getRank() ; i++) {
//			if (!(i == xDim || i == yDim)) currentSlice.setSlice(i, 0, 1, 1);
//		}
//	}
	
//	protected SliceND build(IDataset m) {
//		xDim = parent.getxDim();
//		yDim = parent.getyDim();
//		if (m == null) return null;
//		
//		SliceND s = new SliceND(m.getShape());
//		for (int i = 0; i < m.getRank() ; i++) {
//			if (!(i == xDim || i == yDim)) s.setSlice(i, 0, 1, 1);
//		}
//		
//		return s;
//	}
	
	public abstract IDataset getSpectrum(double x, double y);
	
	public MappedData makeNewMapWithParent(String name, IDataset ds) {
		return new MappedData(name, ds, parent, path);
	}
	
	public IDataset getData(){
		return map;
	}
	
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

	public abstract void replaceLiveDataset(IDataset map);
	
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
			((IDatasetConnector)baseMap).disconnect();
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

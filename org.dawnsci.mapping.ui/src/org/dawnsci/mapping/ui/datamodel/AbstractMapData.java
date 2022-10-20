package org.dawnsci.mapping.ui.datamodel;

import org.dawnsci.mapping.ui.LivePlottingUtils;
import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMapData implements LockableMapObject{

	private String name;
	private String shortName;
	private String nameSuffix = "";
	protected String path;
	protected Dataset cachedMap;
	protected ILazyDataset baseMap;
	protected MappedDataBlock oParent;
	protected MappedDataBlock parent;
	private int transparency = 255;
	private double[] range;
	
	protected boolean live;
	private boolean plotted;
	private Object lock;
	private double[] colorRange;
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractMapData.class);
	
	protected AbstractMapData(String name, ILazyDataset map, MappedDataBlock parent, String path, boolean live) {
		this.name = name;
		this.baseMap = map;
		this.path = path;
		this.oParent = this.parent = parent;
		this.live = live;
		this.shortName = MappingUtils.getShortName(name);
		if (!live) range = calculateRange(map);
	}
	
	public MappedData makeNewMapWithParent(String name, IDataset ds) {
		return new MappedData(name, ds, parent, path, false);
	}
	
	@Override
	public IDataset getMap(){
		try {

			if (cachedMap != null) return cachedMap;
			
			if (isLive()) {
				cachedMap = updateMap();
				return cachedMap;
			}

			if (baseMap.getSize() == 1) return null;

			MapScanDimensions mapDims = parent.getMapDims();
			SliceND s = mapDims.getMapSlice(baseMap);
			Dataset slice;

			slice = DatasetUtils.sliceAndConvertLazyDataset(baseMap.getSlice(s));
			
			Dataset tmp = sanitizeRank(slice, mapDims);
			
			if (mapDims.isTransposed() && tmp != null) {
				cachedMap = tmp.transpose();
			} else {
				cachedMap = tmp;
			}

			return cachedMap;
			
		} catch (DatasetException e) {
			logger.error("Could not slice map");
		}

		return null;
	}
	
	protected void buildSuffix(SliceND slice, AxesMetadata m) {
		
		try {
			ILazyDataset[] md = m.getAxes();
			if (md == null) return;

			StringBuilder builder = new StringBuilder(" ");

			builder.append("[");
			Slice[] s = slice.convertToSlice();
			int[] shape = slice.getShape();
			for (int i = 0 ; i < md.length; i++){
				
				if (md[i] == null || shape[i] != 1) {
					builder.append(s[i].toString());
					builder.append(",");
					continue;
				}
				
				
				IDataset d = md[i].getSlice();
				if (d == null || d.getSize() != 1){
					builder.append(s[i].toString());
				} else {
					d.squeeze();
					double val = d.getDouble();
					builder.append(Double.toString(val));
				}
				builder.append(",");
			}
			
			builder.setCharAt(builder.length()-1,']');
			
			nameSuffix = builder.toString();
		} catch (Exception e) {
			logger.warn("Could not build name suffix", e);
		}
		
	}
	
	
	public IDataset getMapForDims(int x, int y) {
		try {

			if (baseMap.getSize() == 1) return null;

			MapScanDimensions mapDims = parent.getMapDims();
			//clone to not change state
			mapDims = new MapScanDimensions(mapDims);
			mapDims.changeXandYdims(x, y);
			SliceND s = mapDims.getMapSlice(baseMap);
			IDataset slice;

			slice = baseMap.getSlice(s);

			slice.squeeze();
			return slice;
		} catch (DatasetException e) {
			logger.error("Could not slice map");
		}

		return null;
	}
	
	protected void buildAxesMetadata(){
		AxesMetadata ax;
		((IDynamicDataset)baseMap).refreshShape();
		try {
			ax = MetadataFactory.createMetadata(AxesMetadata.class, baseMap.getRank());
			ILazyDataset ly = parent.getYAxis()[0].getSliceView().squeezeEnds();
			ILazyDataset lx = parent.getXAxis()[parent.getMapDims().isRemappingRequired() ? 1 : 0].getSliceView().squeezeEnds();
			MapScanDimensions md = parent.getMapDims();
			int xd = md.getxDim();
			int yd = md.getyDim();
			
			//important to be this way around for when xd == yd
			ax.setAxis(yd, ly);
			ax.addAxis(xd, lx);
			
			if (md.getNonXYScanDimensions() != null) {
				for (int i : md.getNonXYScanDimensions()) {
					ILazyDataset[] axis = parent.getAxis(i);
					if (axis != null && axis[0] != null) {
						ILazyDataset sv = axis[0].getSliceView();
						sv.squeezeEnds();
						ax.addAxis(i, sv);
					}
				}
			}
			
			int[] refresh = ax.refresh(baseMap.getShape());
			((IDynamicDataset)baseMap).resize(refresh);
			baseMap.setMetadata(ax);
		} catch (MetadataException e) {
			logger.error("Could not create metadata",e);
		}

	}
	
	public boolean isReady() {
		if (!parent.isReady()) return false;
		
		if (baseMap instanceof IDynamicDataset) {
			boolean refreshShape = ((IDynamicDataset)baseMap).refreshShape();
			
			return refreshShape ? refreshShape : baseMap.getSize() > 1;
		}
		
		return true;
	}
	
	
	protected Dataset updateMap() {
		if (!live) return null;

		long startTime = System.currentTimeMillis();

		if (baseMap.getFirstMetadata(AxesMetadata.class) == null) {
			try {
				((IDynamicDataset)baseMap).refreshShape();
				if (baseMap.getSize() == 1) return null;
				buildAxesMetadata();
			} catch (Exception e) {
				return null;
			}
		}

		((IDynamicDataset)baseMap).refreshShape();
		
		if (baseMap.getSize() == 0) {
			return null;
		}
		
		AxesMetadata ax = baseMap.getFirstMetadata(AxesMetadata.class);
		if (ax == null) return null;
		int[] refresh = ax.refresh(baseMap.getShape());
		((IDynamicDataset) baseMap).resize(refresh);

		try {
			
			MapScanDimensions mapDims = oParent.getMapDims();
			mapDims.updateNonXYScanSlice(baseMap.getShape());
			SliceND mapSlice = mapDims.getMapSlice(baseMap);
			long preSlice = System.currentTimeMillis();
			Dataset slice = DatasetUtils.convertToDataset(baseMap.getSlice(mapSlice));
			logger.info("Slice of data from {} took {} ms", name, (System.currentTimeMillis()-preSlice));
			
			
			slice = sanitizeRank(slice, mapDims);
			if (slice == null) return null;
			
			if (mapDims.isTransposed()) {
				slice = slice.transpose();
			}

			slice = LivePlottingUtils.cropNanValuesFromAxes(slice,!mapDims.isRemappingRequired());
			if (slice == null) return null;
			
			double[] r = MappingUtils.getRange(slice, !mapDims.isRemappingRequired());
			setRange(r);
			

			
			logger.info("Update of data from {} took {} ms", name, (System.currentTimeMillis()-startTime));
			return slice;

		} catch (DatasetException e) {
			logger.warn("Could not slice data",e);
		} catch (Exception e) {
			logger.error("Error updating map" ,e);
		}
		
		return null;

	}
	
	protected abstract Dataset sanitizeRank(Dataset data, MapScanDimensions dims);
	
	public abstract ILazyDataset getData();
	
	
	protected abstract double[] calculateRange(ILazyDataset map);
	
	public boolean isLive() {
		return live;
	}
	
	protected void setRange(double[] range) {
		this.range = range;
	}
	
	@Override
	public String toString() {
		return shortName + nameSuffix;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Object[] getChildren() {
		return null;
	}

	@Override
	public int getTransparency() {
		return transparency;
	}

	@Override
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
	
	@Override
	public String getLongName() {
		return path + " : " + name;
	}

	public void replaceLiveDataset(ILazyDataset map) {
		live = false;
		this.cachedMap = null;
		this.baseMap = map;
		setRange(calculateRange(baseMap));
	}
	
	public void clearCachedMap() {
		cachedMap = null;
	}
	
	@Override
	public abstract void update();
	
	
	@Override
	public String getPath(){
		return path;
	}
	
	@Override
	public boolean isPlotted() {
		return this.plotted;
	}
	
	@Override
	public void setPlotted(boolean plot) {
		this.plotted = plot;
	}
	
	@Override
	public void setLock(Object lock) {
		this.lock = lock;
	}
	
	@Override
	public Object getLock() {
		return this.lock;
	}

	@Override
	public void setColorRange(double[] range) {
		this.colorRange = range;
	}

	@Override
	public double[] getColorRange() {
		return colorRange;
	}
}

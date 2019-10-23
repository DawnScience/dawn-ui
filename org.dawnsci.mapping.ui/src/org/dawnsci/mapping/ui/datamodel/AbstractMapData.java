package org.dawnsci.mapping.ui.datamodel;

import org.dawnsci.mapping.ui.LivePlottingUtils;
import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.MetadataException;
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
	protected IDataset map;
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
	
	public AbstractMapData(String name, ILazyDataset map, MappedDataBlock parent, String path, boolean live) {
		this.name = name;
		this.baseMap = map;
		this.path = path;
		this.oParent = this.parent = parent;
		this.live = live;
		this.shortName = MappingUtils.getShortName(name);
		if (!live) range = calculateRange(map);
	}
	
	@Override
	public abstract IDataset getSpectrum(double x, double y);
	
	public MappedData makeNewMapWithParent(String name, IDataset ds) {
		return new MappedData(name, ds, parent, path, false);
	}
	
	@Override
	public IDataset getMap(){
		try {

			if (map != null) return map;

			if (baseMap.getSize() == 1) return null;

			MapScanDimensions mapDims = parent.getMapDims();
			SliceND s = mapDims.getMapSlice(baseMap);
			IDataset slice;

			slice = baseMap.getSlice(s);
			
			map = make2D(slice,mapDims.getxDim(), mapDims.getyDim());

			return map;
			
		} catch (DatasetException e) {
			logger.error("Could not slice map");
		}

		return null;
	}
	
	private IDataset make2D(IDataset d, int xd, int yd) {
		
		if (d.getRank() == 2) {
			return d;
		}
		
		AxesMetadata ax = d.getFirstMetadata(AxesMetadata.class);
		
		if (ax == null) return null;
		
		ILazyDataset[] axx = ax.getAxis(xd);
		ILazyDataset[] axy = ax.getAxis(yd);
		
		int[] oShape = d.getShape();
		
		int[] shape = new int[] {oShape[yd], oShape[xd]};
		
		IDataset view = d.getSliceView();
		view.clearMetadata(null);
		view.setShape(shape);
		
		buildSuffix(new SliceND(d.getShape()), ax);
		
		try {
			AxesMetadata md = MetadataFactory.createMetadata(AxesMetadata.class, 2);
			
			for (ILazyDataset l : axx) {
				md.setAxis(1, l.getSliceView().squeezeEnds());
			}
			
			for (ILazyDataset l : axy) {
				md.setAxis(0, l.getSliceView().squeezeEnds());
			}
			
			view.setMetadata(md);
			
			return view;
			
		} catch (Exception e) {
			logger.error("Could not create axes metadata",e);
			return null;
		}
		
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
	
	
	protected IDataset updateMap() {
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
			IDataset slice = baseMap.getSlice(mapSlice);
			logger.info("Slice of data from {} took {} ms", name, (System.currentTimeMillis()-preSlice));
			
			
			slice = sanitizeRank(slice, mapDims);

			slice = LivePlottingUtils.cropNanValuesFromAxes(slice,!mapDims.isRemappingRequired());
			if (slice == null) return null;
			setRange(MappingUtils.getRange(slice, !mapDims.isRemappingRequired()));
			
			logger.info("Update of data from {} took {} ms", name, (System.currentTimeMillis()-startTime));
			return slice;

		} catch (DatasetException e) {
			logger.warn("Could not slice data",e);
		} catch (Exception e) {
			logger.error("Error updating map" ,e);
		}
		
		return null;

	}
	
	protected abstract IDataset sanitizeRank(IDataset data, MapScanDimensions dims);
	
	public abstract ILazyDataset getData();
	
	
	protected abstract double[] calculateRange(ILazyDataset map);
	
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
		this.map = null;
		this.baseMap = map;
		setRange(calculateRange(baseMap));
	}
	
	public void clearCachedMap() {
		map = null;
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

package org.dawnsci.mapping.ui.datamodel;

import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceInformation;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDynamicDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappedDataBlock implements LockableMapObject {

	private String name;
	private String shortName;
	private String path;
	ILazyDataset dataset;
	private MapScanDimensions mapDims;
	private boolean live = false;
	
	private  MappedDataFile parentFile;

	private AbstractMapData mapRepresentation;
	
	private double[] range;

	private boolean plotted;

	private Object lock;
	
	private static final Logger logger = LoggerFactory.getLogger(MappedDataBlock.class);
	
	public MappedDataBlock(String name, ILazyDataset dataset, String path, MapScanDimensions dims, MappedDataFile file, boolean live) {
		this.name = name;
		this.dataset = dataset;
		this.mapDims = dims;
		this.range = calculateRange(dataset);
		this.path = path;
		this.live = live;
		this.shortName = MappingUtils.getShortName(name);
		this.parentFile = file;
	}
	
	public int getScanRank() {
		return mapDims.getScanRank();
	}
	
	public MapScanDimensions getMapDims() {
		return mapDims;
	}
	
	public void replaceLiveDataset(ILazyDataset lz) {
		this.dataset = lz;
		range = calculateRange(lz);
		mapRepresentation = null;
		live = false;
	}
	
	@Override
	public String toString() {
		
		if (canPlot()) {
			return getMapObject().toString();
			
		}

		return shortName;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Object[] getChildren() {
		return null;
	}
	
	public ILazyDataset getSpectrum(int x, int y) {
		if (live) return getLiveSpectrum(x, y);
		
		SliceND slice = getMatchingDataSlice(x,y);
		ILazyDataset sv = dataset.getSliceView(slice);
		if (sv != null) {
			sv.setMetadata(generateSliceMetadata(x,y));
		};

		return sv;
	}
	
	protected SliceND getMatchingDataSlice(int x, int y) {
		return mapDims.getSlice(x, y, dataset.getShape());
	}
	
	public IDataset getSpectrum(int index) {
		if (live) return getLiveSpectrum(index);
		
		SliceND slice = mapDims.getSlice(index, index, dataset.getShape());
		
		try {
			
			IDataset s = dataset.getSlice(slice);
			
			if (s != null) {
				s.setMetadata(generateSliceMetadata(index,index));
			};
			
			return dataset.getSlice(slice);
		} catch (DatasetException e) {
			logger.error("Could not slice dataset",e);
		}
		return null;
	}
	
	public int[] getDataDimensions() {
		return mapDims.getDataDimensions(dataset.getRank());
	}
	
	public ILazyDataset[] getXAxis() {
		if (live) return getLiveXAxis();
		AxesMetadata md = dataset.getFirstMetadata(AxesMetadata.class);
		if (md == null) return null;
		return md.getAxis(mapDims.getxDim());
	}
	
	public ILazyDataset[] getYAxis() {
		if (live) return getLiveYAxis();
		AxesMetadata md = dataset.getFirstMetadata(AxesMetadata.class);
		if (md == null) return null;
		return md.getAxis(mapDims.getyDim());
	}
	
	public ILazyDataset[] getAxis(int dim) {
		if (live) return getLiveAxis(dim);
		AxesMetadata md = dataset.getFirstMetadata(AxesMetadata.class);
		if (md == null) return null;
		return md.getAxis(dim);
	}
	
	public boolean isRemappingRequired(){
		return mapDims.isRemappingRequired();
	}

	@Override
	public double[] getRange() {
		return range == null ? null : range.clone();
	}
	
	protected double[] calculateRange(ILazyDataset block){
		
		if (block == null) return null;
		
		IDataset[] ax = MetadataPlotUtils.getAxesAsIDatasetArray(block);
		
		int yDim = mapDims.getyDim();
		int xDim = mapDims.getxDim();
		
		return MappingUtils.calculateRangeFromAxes(new IDataset[]{ax[yDim],ax[xDim]});
	}

	@Override
	public String getPath(){
		return path;
	}
	
	public ILazyDataset getLazy() {
		return dataset;
	}
	
	@Override
	public String getLongName() {
		return path + " : " + name;
	}
	
	public boolean isTransposed(){
		return mapDims.isTransposed();
	}
	
	public int getyDim() {
		return mapDims.getyDim();
	}

	public int getxDim() {
		return mapDims.getxDim();
	}
	
	private MetadataType generateSliceMetadata(int x, int y){
		SourceInformation si = new SourceInformation(getPath(), toString(), dataset);
		SliceND slice = getMatchingDataSlice(x, y);
		SliceInformation sl = new SliceInformation(slice, slice, new SliceND(dataset.getShape()), getDataDimensions(), 1, 0);
		return new SliceFromSeriesMetadata(si,sl);
	}
	
	public IDataset getLiveSpectrum(int x, int y) {
		
		IDataset out = null;
		synchronized (getLock()) {
		
		if (dataset instanceof IDynamicDataset) {
			
			if (dataset.getFirstMetadata(AxesMetadata.class) == null) {
				try {
					((IDynamicDataset)dataset).refreshShape();
					if (dataset.getSize() == 1) return null;
					buildAxesMetadata();
				} catch (Exception e) {
					return null;
				}
			}
			
			((IDynamicDataset)dataset).refreshShape();
			AxesMetadata ax = dataset.getFirstMetadata(AxesMetadata.class);
			if (ax == null) return null;
			int[] refresh = ax.refresh(dataset.getShape());
			((IDynamicDataset) dataset).resize(refresh);
		}
		
		
		SliceND slice = mapDims.getSlice(x, y, dataset.getShape());
		
		try {
			out = dataset.getSlice(slice);
			out.squeeze();
		} catch (Exception e) {
			logger.error("Could not get data from lazy dataset", e);
			return null;
		}
		
		MetadataType sslsm = generateLiveSliceMetadata(x,y);
		out.setMetadata(sslsm);
		}
		return out;
		
	}
	
	public IDataset getLiveSpectrum(int x) {
		
		return getLiveSpectrum(x, x);
		
	}

	@Override
	public boolean isLive() {
		return live;
	}
	
	private void buildAxesMetadata() throws Exception{
		
//		AxesMetadata ax = MetadataFactory.createMetadata(AxesMetadata.class, dataset.getRank());
//		ILazyDataset[] lazyAx = axes.getAxes();
//		for (int i = 0; i < lazyAx.length; i++) {
//			ax.addAxis(i, lazyAx[i]);
//		}
//		
//		if (axes.getxAxisForRemapping() != null) {
//			ax.addAxis(mapDims.getxDim(),axes.getxAxisForRemapping());
//		}
//		
//		int[] refresh = ax.refresh(dataset.getShape());
//		((IDynamicDataset)dataset).resize(refresh);
//		dataset.addMetadata(ax);
		
	}
	
	private ILazyDataset[] getLiveXAxis() {
		
		return getLiveAxis(mapDims.getxDim());
	}
	
	private ILazyDataset[] getLiveYAxis() {
		return getLiveAxis(mapDims.getyDim());
	}
	
	private ILazyDataset[] getLiveAxis(int dim) {
		AxesMetadata md = dataset.getFirstMetadata(AxesMetadata.class);
		return md.getAxis(dim);
	}

	
	private MetadataType generateLiveSliceMetadata(int x, int y){
		return null;
//		SourceInformation si = new SourceInformation(getPath(), toString(), dataset);
//		SliceND slice = getMatchingDataSlice(x, y);
//		SliceInformation sl = new SliceInformation(slice, slice, new SliceND(dataset.getShape()), getDataDimensions(), 1, 0);
//		return new SliceFromLiveSeriesMetadata(si,sl,axes.getHost(),axes.getPort(),axes.getAxesNames(),axes.getxAxisForRemappingName());
	}

	@Override
	public IDataset getMap() {
		
		return getMapObject().getMap();
	}
	
	public AbstractMapData getMapObject() {
		
		if (!canPlot()) return null;
		
		if (mapRepresentation == null) {
			if (mapDims.isRemappingRequired()) {

				mapRepresentation = new ReMappedData(this.name, dataset.getSliceView() ,this, path, isLive());
				mapRepresentation.setLock(getLock());
				if (isLive()) mapRepresentation.update();
				range = mapRepresentation.getRange();
				return mapRepresentation;


			} else {

				mapRepresentation = new MappedData(this.name,dataset.getSliceView(),this, path, isLive());
				mapRepresentation.setLock(getLock());
				if (isLive()) mapRepresentation.update();
				range = mapRepresentation.getRange();
				return mapRepresentation;

			}
			
		}
		
		if (isLive()) mapRepresentation.update();
		range = mapRepresentation.getRange();
		return mapRepresentation;
	}

	public boolean isReady() {
		
		boolean refreshShape = false;
		
		synchronized (getLock()) {
			refreshShape = ((IDynamicDataset)dataset).refreshShape();
		}
		
		
		return refreshShape ? refreshShape : dataset.getSize() > 1;
	}
	
	@Override
	public void update() {
		if (dataset instanceof IDynamicDataset) {
			synchronized (getLock()) {
				((IDynamicDataset)dataset).refreshShape();
				AxesMetadata ax = dataset.getFirstMetadata(AxesMetadata.class);
				if (ax != null) {
					int[] refresh = ax.refresh(dataset.getShape());
					((IDynamicDataset) dataset).resize(refresh);
				}
			}
		}
	}

	@Override
	public int getTransparency() {
		if (mapRepresentation != null) {
			return mapRepresentation.getTransparency();
		}
		return -1;
	}
	
	@Override
	public void setTransparency(int val) {
		if (mapRepresentation != null) {
			mapRepresentation.setTransparency(val);
		}
	}

	@Override
	public IDataset getSpectrum(double x, double y) {
		int[] idx = MappingUtils.getIndicesFromCoOrds(getMap(), x, y);
		
		if (idx == null) return null;
		
		try {
			return getSpectrum(idx[0], idx[1]).getSlice();
		} catch (DatasetException e) {
			logger.error("Could not slice dataset",e);
		}
		return null;
	}

	public boolean canPlot() {
		if (isLive()) {
			update();
		}
		
		int[] shape = null;
		
		if (dataset instanceof IDynamicDataset) {
			shape = ((IDynamicDataset) dataset).getMaxShape();
		} else {
			shape = dataset.getShape();
		}
		
		
		return mapDims.isPointDetector(shape);
		
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
		// TODO Auto-generated method stub

	}

	@Override
	public double[] getColorRange() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public MappedDataFile getParentFile() {
		return parentFile;
	}

}
	


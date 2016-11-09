package org.dawnsci.mapping.ui.datamodel;

import org.dawnsci.mapping.ui.LivePlottingUtils;
import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromLiveSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceInformation;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.eclipse.january.metadata.MetadataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappedDataBlock implements MapObject, PlottableMapObject {

	private String name;
	private String path;
	ILazyDataset dataset;
	private MapScanDimensions mapDims;

	private AbstractMapData mapRepresentation;
	
	private double[] range;
	private boolean connected = false;

	private LiveRemoteAxes axes;
	
	private static final Logger logger = LoggerFactory.getLogger(MappedDataBlock.class);
	
	public MappedDataBlock(String name, ILazyDataset dataset, String path, MapScanDimensions dims) {
		this.name = name;
		this.dataset = dataset;
		this.mapDims = dims;
		this.range = calculateRange(dataset);
		this.path = path;
		
	}
	
	public MappedDataBlock(String name, IDatasetConnector dataset,MapScanDimensions dims, String path, LiveRemoteAxes axes, String host, int port) {
		this(name, dataset.getDataset(), path,dims);
		this.axes = axes;

	}
	
	public int getScanRank() {
		return mapDims.getScanRank();
	}
	
	public MapScanDimensions getMapDims() {
		return mapDims;
	}
	
	public void replaceLiveDataset(ILazyDataset lz) {
		disconnect();
		this.dataset = lz;
		axes = null;
		calculateRange(lz);
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
	
	public ILazyDataset getSpectrum(int x, int y) {
		if (axes != null) return getLiveSpectrum(x, y);
		
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
		if (axes != null) return getLiveSpectrum(index);
		
		SliceND slice = mapDims.getSlice(index, index, dataset.getShape());
		
		try {
			
			IDataset s = dataset.getSlice(slice);
			
			if (s != null) {
				s.setMetadata(generateSliceMetadata(index,index));
			};
			
			return dataset.getSlice(slice);
		} catch (DatasetException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public int[] getDataDimensions() {
		return mapDims.getDataDimensions(dataset.getRank());
	}
	
	public ILazyDataset[] getXAxis() {
		if (axes != null) return getLiveXAxis();
		AxesMetadata md = dataset.getFirstMetadata(AxesMetadata.class);
		if (md == null) return null;
		return md.getAxis(mapDims.getxDim());
	}
	
	public ILazyDataset[] getYAxis() {
		if (axes != null) return getLiveYAxis();
		AxesMetadata md = dataset.getFirstMetadata(AxesMetadata.class);
		if (md == null) return null;
		return md.getAxis(mapDims.getyDim());
	}
	
	public boolean isRemappingRequired(){
		return mapDims.isRemappingRequired();
	}

	@Override
	public double[] getRange() {
		return range == null ? null : range.clone();
	}
	
	protected double[] calculateRange(ILazyDataset block){
		
		if (block instanceof IDatasetConnector) return null;
		
		IDataset[] ax = MetadataPlotUtils.getAxesAsIDatasetArray(block);
		
		int yDim = mapDims.getyDim();
		int xDim = mapDims.getxDim();
		
		return MappingUtils.calculateRangeFromAxes(new IDataset[]{ax[xDim],ax[yDim]});
	}

	public String getPath(){
		return path;
	}
	
	public ILazyDataset getLazy() {
		return dataset;
	}
	
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
	
	public int getySize() {
		return dataset.getShape()[mapDims.getyDim()];
	}

	public int getxSize() {
		return dataset.getShape()[mapDims.getxDim()];
	}

	private MetadataType generateSliceMetadata(int x, int y){
		SourceInformation si = new SourceInformation(getPath(), toString(), dataset);
		SliceND slice = getMatchingDataSlice(x, y);
		SliceInformation sl = new SliceInformation(slice, slice, new SliceND(dataset.getShape()), getDataDimensions(), 1, 1);
		return new SliceFromSeriesMetadata(si,sl);
	}
	
	public ILazyDataset getLiveSpectrum(int x, int y) {
		
		((IDatasetConnector)dataset).refreshShape();
		
		axes.update();
		
		int yDim = mapDims.getyDim();
		int xDim = mapDims.getxDim();
		
		SliceND slice = mapDims.getSlice(x, y, dataset.getShape());
		
		IDataset slice2;
		try {
			slice2 = dataset.getSlice(slice);
		} catch (DatasetException e) {
			logger.error("Could not get data from lazy dataset", e);
			return null;
		}
		
		AxesMetadata ax = null;
		try {
			ax = MetadataFactory.createMetadata(AxesMetadata.class, dataset.getRank());
			ILazyDataset[] lax = axes.getAxes();
			for (int i = 0; i < dataset.getRank(); i++) {
				if (i == xDim || i == yDim || lax[i] == null) continue;
				try {
					ax.setAxis(i,lax[i].getSlice());
				} catch (DatasetException e) {
					logger.error("Could not get data from lazy dataset for axis " + i, e);
				}
			}
		} catch (MetadataException e1) {
			logger.error("Could not create axes metdata", e1);
		}
		slice2.setMetadata(ax);
		MetadataType sslsm = generateLiveSliceMetadata(x,y);
		slice2.setMetadata(sslsm);
		
		return slice2;
	}
	
	public IDataset getLiveSpectrum(int x) {
		
		((IDatasetConnector)dataset).refreshShape();
		
		axes.update();
		
		int yDim = mapDims.getyDim();
		int xDim = mapDims.getxDim();
		
		SliceND slice = new SliceND(dataset.getShape());
		slice.setSlice(xDim,x,x+1,1);
		
		IDataset slice2;
		try {
			slice2 = dataset.getSlice(slice);
		} catch (DatasetException e) {
			logger.error("Could not get data from lazy dataset", e);
			return null;
		}
		
		AxesMetadata ax = null;
		try {
			ax = MetadataFactory.createMetadata(AxesMetadata.class, dataset.getRank());
			ILazyDataset[] lax = axes.getAxes();
			for (int i = 0; i < dataset.getRank(); i++) {
				if (i == xDim || i == yDim || lax[i] == null) continue;
				try {
					ax.setAxis(i, lax[i].getSlice());
				} catch (DatasetException e) {
					logger.error("Could not get data from lazy dataset for axis " + i, e);
				}
			}
			
		} catch (MetadataException e1) {
			logger.error("Could not create axes metdata", e1);
		}
		slice2.setMetadata(ax);
		MetadataType sslsm = generateLiveSliceMetadata(x,x);
		slice2.setMetadata(sslsm);
		
		return slice2;
	}

	public boolean isLive() {
		return axes != null;
	}
	
	public boolean connect(){
		
		if (axes == null) return true;
		
		connected = true;
		
		try {
			((IDatasetConnector)dataset).connect();
		} catch (Exception e) {
			connected = false;
			logger.error("Could not connect to " + toString());
			return false;
		}
		
		if (connectAxes(true)) {
			connected = true;
			return true;
		}
		
		return false;
	}


	public boolean disconnect(){
		
		if (axes == null) return true;
		
		try {
			((IDatasetConnector)dataset).disconnect();
		} catch (Exception e) {
			logger.error("Could not disconnect from " + toString());
			return false;
		}
		
		if (connectAxes(false)) {
			connected = false;
			return true;
		}
		
		return false;
		
	}
	
	private boolean connectAxes(boolean connect){
		
		boolean success = true;
		
		success = axes.connect(connect);
		
		return success;
	}
	
	private ILazyDataset[] getLiveXAxis() {
		if (!connected) {			
			connect();
		}
		
		if (!connected) return null;
		
		axes.update();
		
		if (axes.getxAxisForRemapping() != null) return new ILazyDataset[]{axes.getxAxisForRemapping()};
		return new ILazyDataset[]{axes.getAxes()[mapDims.getxDim()]};
	}
	
	private ILazyDataset[] getLiveYAxis() {
		if (!connected) {			
			connect();
		}
		
		if (!connected) return null;
		axes.update();
		return new ILazyDataset[]{axes.getAxes()[mapDims.getyDim()]};
	}

	
	private MetadataType generateLiveSliceMetadata(int x, int y){
		SourceInformation si = new SourceInformation(getPath(), toString(), dataset);
		SliceND slice = getMatchingDataSlice(x, y);
		SliceInformation sl = new SliceInformation(slice, slice, new SliceND(dataset.getShape()), getDataDimensions(), 1, 1);
		return new SliceFromLiveSeriesMetadata(si,sl,axes.getHost(),axes.getPort(),axes.getAxesNames(),axes.getxAxisForRemappingName());
	}

	@Override
	public IDataset getMap() {
		
		if (isLive()) {
			update();
			
			IDataset d = null;
			
			if (mapDims.isRemappingRequired()) {
				d = LivePlottingUtils.getUpdatedLinearMap((IDatasetConnector)dataset, this, this.toString());
				d = MappingUtils.remapData(d, null, 0)[0];
			} else {
				d = LivePlottingUtils.getUpdatedMap((IDatasetConnector)dataset, this, this.toString());
			}

			if (d == null) return null;
			double[] range = MappingUtils.getGlobalRange(d);
			this.range = range;
			return d;
		}
		
		if (mapRepresentation == null) {
			if (mapDims.isRemappingRequired()) {
				
			} else {
				
				try {
					mapRepresentation = new MappedData(this.toString(), dataset.getSlice(mapDims.getMapSlice(dataset)).squeeze(),this, path);
					return mapRepresentation.getMap();
				} catch (DatasetException e) {
					logger.error("Could not create map representation");
				}
			}
		}
		
		
		
		
		return mapRepresentation.getMap();
	}

	@Override
	public void update() {
		if (!isLive()) return;
		if (!connected) {			
			try {
				connect();
			} catch (Exception e) {
				logger.debug("Could not connect",e);

			}
		}
		
	}

	@Override
	public int getTransparency() {
		// TODO Auto-generated method stub
		return -1;
	}

	@Override
	public IDataset getSpectrum(double x, double y) {
		int[] idx = MappingUtils.getIndicesFromCoOrds(getMap(), x, y);
		
		if (idx == null) return null;
		
		try {
			return getSpectrum(idx[0], idx[1]).getSlice();
		} catch (DatasetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
	


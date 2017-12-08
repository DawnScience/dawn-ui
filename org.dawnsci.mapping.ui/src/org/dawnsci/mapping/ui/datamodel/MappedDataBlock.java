package org.dawnsci.mapping.ui.datamodel;

import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromLiveSeriesMetadata;
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

	private LiveRemoteAxes axes;
	private boolean plotted;
	
	private static final Logger logger = LoggerFactory.getLogger(MappedDataBlock.class);
	
	public MappedDataBlock(String name, ILazyDataset dataset, String path, MapScanDimensions dims) {
		this.name = name;
		this.dataset = dataset;
		this.mapDims = dims;
		this.range = calculateRange(dataset);
		this.path = path;
		
	}
	
	public MappedDataBlock(String name, IDynamicDataset dataset,MapScanDimensions dims, String path, LiveRemoteAxes axes, String host, int port) {
		this(name, null, path,dims);
		this.axes = axes;
		this.dataset = dataset;

	}
	
	public int getScanRank() {
		return mapDims.getScanRank();
	}
	
	public MapScanDimensions getMapDims() {
		return mapDims;
	}
	
	public void replaceLiveDataset(ILazyDataset lz) {
		this.dataset = lz;
		axes = null;
		range = calculateRange(lz);
		mapRepresentation = null;
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
		SliceInformation sl = new SliceInformation(slice, slice, new SliceND(dataset.getShape()), getDataDimensions(), 1, 0);
		return new SliceFromSeriesMetadata(si,sl);
	}
	
	public IDataset getLiveSpectrum(int x, int y) {
		
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
		
		IDataset slice2;
		try {
			slice2 = dataset.getSlice(slice);
			slice2.squeeze();
		} catch (Exception e) {
			logger.error("Could not get data from lazy dataset", e);
			return null;
		}
		
		MetadataType sslsm = generateLiveSliceMetadata(x,y);
		slice2.setMetadata(sslsm);
		
		return slice2;
	}
	
	public IDataset getLiveSpectrum(int x) {
		
		return getLiveSpectrum(x, x);
		
	}

	public boolean isLive() {
		return axes != null;
	}
	
	private void buildAxesMetadata() throws Exception{
		
		AxesMetadata ax = MetadataFactory.createMetadata(AxesMetadata.class, dataset.getRank());
		ILazyDataset[] lazyAx = axes.getAxes();
		for (int i = 0; i < lazyAx.length; i++) {
			ax.addAxis(i, lazyAx[i]);
		}
		
		if (axes.getxAxisForRemapping() != null) {
			ax.addAxis(mapDims.getxDim(),axes.getxAxisForRemapping());
		}
		
		int[] refresh = ax.refresh(dataset.getShape());
		((IDynamicDataset)dataset).resize(refresh);
		dataset.addMetadata(ax);
		
	}
	
	private ILazyDataset[] getLiveXAxis() {
		
		axes.update();
		
		if (axes.getxAxisForRemapping() != null) return new ILazyDataset[]{axes.getxAxisForRemapping()};
		return new ILazyDataset[]{axes.getAxes()[mapDims.getxDim()]};
	}
	
	private ILazyDataset[] getLiveYAxis() {
		axes.update();
		return new ILazyDataset[]{axes.getAxes()[mapDims.getyDim()]};
	}

	
	private MetadataType generateLiveSliceMetadata(int x, int y){
		SourceInformation si = new SourceInformation(getPath(), toString(), dataset);
		SliceND slice = getMatchingDataSlice(x, y);
		SliceInformation sl = new SliceInformation(slice, slice, new SliceND(dataset.getShape()), getDataDimensions(), 1, 0);
		return new SliceFromLiveSeriesMetadata(si,sl,axes.getHost(),axes.getPort(),axes.getAxesNames(),axes.getxAxisForRemappingName());
	}

	@Override
	public IDataset getMap() {
		
		if (mapRepresentation == null) {
			if (mapDims.isRemappingRequired()) {

				mapRepresentation = new ReMappedData(this.toString(), dataset.getSliceView() ,this, path, isLive());
				if (isLive()) mapRepresentation.update();
				range = mapRepresentation.getRange();
				return mapRepresentation.getMap();


			} else {

				mapRepresentation = new MappedData(this.toString(),dataset.getSliceView(),this, path, isLive());
				if (isLive()) mapRepresentation.update();
				range = mapRepresentation.getRange();
				return mapRepresentation.getMap();

			}
			
		}
		
		if (isLive()) mapRepresentation.update();
		range = mapRepresentation.getRange();
		return mapRepresentation.getMap();
	}

	@Override
	public void update() {
		((IDynamicDataset)dataset).refreshShape();
	}

	@Override
	public int getTransparency() {
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

	public boolean canPlot() {
		if (isLive()) {
			update();
		}
		
		return mapDims.isPointDetector(dataset.getShape());
		
	}
	
	public boolean isPlotted() {
		return this.plotted;
	}
	
	public void setPlotted(boolean plot) {
		this.plotted = plot;
	}
}
	


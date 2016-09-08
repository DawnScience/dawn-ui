package org.dawnsci.mapping.ui.datamodel;

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

public class MappedDataBlock implements MapObject {

	private String name;
	private String path;
	ILazyDataset dataset;
	int yDim = 0;
	int xDim = 1;
	private double[] range;
	private boolean connected = false;
	
	private LiveRemoteAxes axes;
	
	private static final Logger logger = LoggerFactory.getLogger(MappedDataBlock.class);
	
	public MappedDataBlock(String name, ILazyDataset dataset, int xDim, int yDim, String path) {
		this.name = name;
		this.dataset = dataset;
		this.xDim = xDim;
		this.yDim = yDim;
		this.range = calculateRange(dataset);
		this.path = path;
	}
	
	public MappedDataBlock(String name, IDatasetConnector dataset, int xDim, int yDim, String path, LiveRemoteAxes axes, String host, int port) {
		this(name, dataset.getDataset(), xDim, yDim, path);
		this.axes = axes;

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
		SliceND slice = new SliceND(dataset.getShape());
		slice.setSlice(yDim,y,y+1,1);
		slice.setSlice(xDim,x,x+1,1);
		
	return slice;
	}
	
	public IDataset getSpectrum(int index) {
		
		SliceND slice = new SliceND(dataset.getShape());
		slice.setSlice(0,index,index+1,1);
		
		try {
			return dataset.getSlice(slice);
		} catch (DatasetException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public int[] getDataDimensions() {
		int nDims = 0;
		
		int[] shape = dataset.getShape();
		for (int i = 0; i < shape.length; i++) {
			if (i!= xDim && i != yDim && shape[i] != 1) nDims++;
		}
		
		int[] dd = new int[nDims];
		int count = 0;
		for (int i = 0; i < shape.length; i++) {
			if (i!= xDim && i != yDim && shape[i] != 1) {
				dd[count] = i;
				count++;
			}
		}
		
		return dd;
	}
	
	public ILazyDataset[] getXAxis() {
		if (axes != null) return getLiveXAxis();
		AxesMetadata md = dataset.getFirstMetadata(AxesMetadata.class);
		if (md == null) return null;
		return md.getAxis(xDim);
	}
	
	public ILazyDataset[] getYAxis() {
		if (axes != null) return getLiveYAxis();
		AxesMetadata md = dataset.getFirstMetadata(AxesMetadata.class);
		if (md == null) return null;
		return md.getAxis(yDim);
	}
	
	public boolean isRemappingRequired(){
		return xDim == yDim;
	}

	@Override
	public double[] getRange() {
		return range == null ? null : range.clone();
	}
	
	protected double[] calculateRange(ILazyDataset block){
		
		if (block instanceof IDatasetConnector) return null;
		
		IDataset[] ax = MetadataPlotUtils.getAxesFromMetadata(block);
		
		double[] range = new double[4];
		int xs = ax[xDim].getSize();
		int ys = ax[yDim].getSize();
		range[0] = ax[xDim].min().doubleValue();
		range[1] = ax[xDim].max().doubleValue();
		double dx = ((range[1]-range[0])/xs)/2;
		range[0] -= dx;
		range[1] += dx;
		
		
		range[2] = ax[yDim].min().doubleValue();
		range[3] = ax[yDim].max().doubleValue();
		double dy = ((range[3]-range[2])/ys)/2;
		range[2] -= dy;
		range[3] += dy;
		
		return range;
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
		return yDim > xDim;
	}
	
	public int getyDim() {
		return yDim;
	}

	public int getxDim() {
		return xDim;
	}
	
	public int getySize() {
		return dataset.getShape()[yDim];
	}

	public int getxSize() {
		return dataset.getShape()[xDim];
	}

	private MetadataType generateSliceMetadata(int x, int y){
		SourceInformation si = new SourceInformation(getPath(), toString(), dataset);
		SliceND slice = getMatchingDataSlice(x, y);
		SliceInformation sl = new SliceInformation(slice, slice, new SliceND(dataset.getShape()), getDataDimensions(), 1, 1);
		return new SliceFromSeriesMetadata(si,sl);
	}
	
	public ILazyDataset getLiveSpectrum(int x, int y) {
		
		((IDatasetConnector)dataset).refreshShape();
		
		for (IDatasetConnector a : axes.getAxes()) {
			if (a != null) a.refreshShape();
		}
		
		SliceND slice = new SliceND(dataset.getShape());
		slice.setSlice(yDim,y,y+1,1);
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
			for (int i = 0; i < dataset.getRank(); i++) {
				if (i == xDim || i == yDim || axes.getAxes()[i] == null) continue;
				try {
					ax.setAxis(i, axes.getAxes()[i].getDataset().getSlice());
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
		
		for (IDatasetConnector a : axes.getAxes()) {
			if (a instanceof IDatasetConnector) {
				
				try {
					if (connect) a.connect();
					else a.disconnect();
				}
				catch (Exception e) {
					logger.error("Error communicating with " + a.getDataset().getName());
					success = false;
				} 
			}
		}
		
		if (axes.getxAxisForRemapping() != null) {
			try {
				if (connect) axes.getxAxisForRemapping().connect();
				else axes.getxAxisForRemapping().disconnect();
			}
			catch (Exception e) {
				logger.error("Error communicating with " + axes.getxAxisForRemapping().getDataset().getName());
				success = false;
			} 
		}
		
		return success;
	}
	
	private ILazyDataset[] getLiveXAxis() {
		if (!connected) {			
			connect();

		}
		
		if (!connected) return null;
		
		if (axes.getxAxisForRemapping() != null) return new ILazyDataset[]{axes.getxAxisForRemapping().getDataset()};
		return new ILazyDataset[]{axes.getAxes()[xDim].getDataset()};
	}
	
	private ILazyDataset[] getLiveYAxis() {
		if (!connected) {			
			connect();
		}
		
		if (!connected) return null;
		
		return new ILazyDataset[]{axes.getAxes()[yDim].getDataset()};
	}

	
	private MetadataType generateLiveSliceMetadata(int x, int y){
		SourceInformation si = new SourceInformation(getPath(), toString(), dataset);
		SliceND slice = getMatchingDataSlice(x, y);
		SliceInformation sl = new SliceInformation(slice, slice, new SliceND(dataset.getShape()), getDataDimensions(), 1, 1);
		return new SliceFromLiveSeriesMetadata(si,sl,axes.getHost(),axes.getPort(),axes.getAxesNames());
	}
}
	


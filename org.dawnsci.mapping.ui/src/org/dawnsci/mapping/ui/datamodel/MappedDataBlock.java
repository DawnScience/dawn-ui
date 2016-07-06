package org.dawnsci.mapping.ui.datamodel;

import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceInformation;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.dataset.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataType;

public class MappedDataBlock implements MapObject {

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

	private String name;
	private String path;
	ILazyDataset dataset;
	int yDim = 0;
	int xDim = 1;
	private double[] range;
	
	public MappedDataBlock(String name, ILazyDataset dataset, int xDim, int yDim, String path) {
		this.name = name;
		this.dataset = dataset;
		this.xDim = xDim;
		this.yDim = yDim;
		this.range = calculateRange(dataset);
		this.path = path;
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
		AxesMetadata md = dataset.getFirstMetadata(AxesMetadata.class);
		if (md == null) return null;
		return md.getAxis(xDim);
	}
	
	public ILazyDataset[] getYAxis() {
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

	private MetadataType generateSliceMetadata(int x, int y){
		SourceInformation si = new SourceInformation(getPath(), toString(), dataset);
		SliceND slice = getMatchingDataSlice(x, y);
		SliceInformation sl = new SliceInformation(slice, slice, new SliceND(dataset.getShape()), getDataDimensions(), 1, 1);
		return new SliceFromSeriesMetadata(si,sl);
	}
	
}

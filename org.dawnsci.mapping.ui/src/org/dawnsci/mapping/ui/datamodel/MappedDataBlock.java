package org.dawnsci.mapping.ui.datamodel;

import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;

public class MappedDataBlock implements MapObject {

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
		
		SliceND slice = new SliceND(dataset.getShape());
		slice.setSlice(yDim,y,y+1,1);
		slice.setSlice(xDim,x,x+1,1);
		
		return dataset.getSliceView(slice);
	}
	
	public IDataset getSpectrum(int index) {
		
		SliceND slice = new SliceND(dataset.getShape());
		slice.setSlice(0,index,index+1,1);
		
		return dataset.getSlice(slice);
	}
	
	public int[] getDataDimensions() {
		int nDims = 0;
		
		int[] shape = dataset.getShape();
		for (int i = 0; i < shape.length; i++) {
			if (i!= xDim && i != yDim) nDims++;
		}
		
		int[] dd = new int[nDims];
		int count = 0;
		for (int i = 0; i < shape.length; i++) {
			if (i!= xDim && i != yDim) {
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

}

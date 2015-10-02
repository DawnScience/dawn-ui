package org.dawnsci.mapping.ui.datamodel;

import org.dawnsci.mapping.ui.MappingUtils;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.metadata.AxesMetadata;

public class MappedDataBlock implements MapObject {

	private String name;
	ILazyDataset dataset;
	int yDim = 0;
	int xDim = 1;
	private double[] range;
	
	public MappedDataBlock(String name, ILazyDataset dataset, int xDim, int yDim) {
		this.name = name;
		this.dataset = dataset;
		this.xDim = xDim;
		this.yDim = yDim;
		this.range = calculateRange(dataset);
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
		return range.clone();
	}
	
	private double[] calculateRange(ILazyDataset block){
		IDataset[] ax = MappingUtils.getAxesFromMetadata(block);
		double[] r = new double[4];
		r[0] = ax[xDim].min().doubleValue();
		r[1] = ax[xDim].max().doubleValue();
		r[2] = ax[yDim].min().doubleValue();
		r[3] = ax[yDim].max().doubleValue();
		return r;
	}

}

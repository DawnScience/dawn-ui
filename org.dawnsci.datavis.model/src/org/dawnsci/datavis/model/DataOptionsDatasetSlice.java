package org.dawnsci.datavis.model;

import org.dawnsci.january.model.NDimensions;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;

public class DataOptionsDatasetSlice extends DataOptionsDataset {

	private SliceND slice;

	public DataOptionsDatasetSlice(DataOptionsDataset option, SliceND slice) {
		super(option.getName(),option.getParent(), option.getLazyDataset());
		this.setSelected(false);
		this.slice = slice.clone();
		setPlottableObject(new PlottableObject(option.getPlottableObject().getPlotMode(),new NDimensions(option.getPlottableObject().getNDimensions())));
		getPlottableObject().getNDimensions().updateShape(slice.getShape());
	}

	@Override
	public ILazyDataset getLazyDataset() {
		return super.getLazyDataset().getSliceView(this.slice);
	}

	@Override
	public String getName() {
		return super.getName() + "[" +slice.toString() + "]";
	}

	@Override
	public DataOptionsDatasetSlice clone() {
		return new DataOptionsDatasetSlice(this, this.slice);
	}
}

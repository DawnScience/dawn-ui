package org.dawnsci.datavis.model;

import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;

public class DataOptionsSlice extends DataOptions {

	private SliceND slice;

	public DataOptionsSlice(DataOptions option, SliceND slice) {
		super(option);
		this.setSelected(false);
		this.fromFile = false;
		this.slice = slice.clone();
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
	public DataOptions clone() {
		return new DataOptionsSlice(this, this.slice);
	}
}

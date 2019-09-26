package org.dawnsci.datavis.model;

import org.dawnsci.january.model.NDimensions;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;

public class DataOptionsDataset extends DataOptions {

	private ILazyDataset dataset;
	private ILazyDataset localView;

	public DataOptionsDataset(String name, LoadedFile parent, ILazyDataset dataset) {
		super(name, parent);

		this.dataset = dataset;
	}

	@Override
	public ILazyDataset getLazyDataset() {

		if (localView == null) {
			ILazyDataset local = dataset.getSliceView();
			includeAxesMetadata(local);
			localView = local;
		}

		return localView;
	}

	@Override
	public String[] getPrimaryAxes() {
		return axes;
	}

	public void setAxes(String[] axesNames) {
		localView = null;
		super.setAxes(axesNames);
	}

	public DataOptionsDataset clone() {
		//needs to deal with plottable objects, labels etc
		DataOptionsDataset dod = new DataOptionsDataset(getName(), getParent(), dataset.getSliceView());
		dod.setAxes(this.axes);
		dod.setSelected(isSelected());
		dod.setPlottableObject(new PlottableObject(this.getPlottableObject().getPlotMode(),new NDimensions(getPlottableObject().getNDimensions())));
		dod.setLabel(getLabel());
		return dod;
	}
}

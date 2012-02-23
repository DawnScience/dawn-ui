package org.dawb.workbench.plotting.system;

import org.dawb.common.ui.plot.trace.IImageTrace;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

public class ImageTrace implements IImageTrace {

	private AbstractDataset data;

	public ImageTrace(AbstractDataset data) {
		this.data = data;
	}

	@Override
	public String getName() {
		return data.getName();
	}

	@Override
	public void setName(String name) {
		data.setName(name);
	}

	@Override
	public AbstractDataset getData() {
		return data;
	}

}

package org.dawnsci.datavis.model;

public class DataStateObject {

	private boolean checked;
	private DataOptions option;
	private PlottableObject plotObject;

	public DataStateObject(DataOptions option, boolean checked, PlottableObject plotObject) {

		this.option = option;
		this.checked = checked;
		this.plotObject = plotObject;
	}

	public boolean isChecked() {
		return checked;
	}

	public DataOptions getOption() {
		return option;
	}

	public PlottableObject getPlotObject() {
		return plotObject;
	}


}

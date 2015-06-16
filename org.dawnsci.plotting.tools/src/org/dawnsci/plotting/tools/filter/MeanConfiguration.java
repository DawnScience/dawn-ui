package org.dawnsci.plotting.tools.filter;

public class MeanConfiguration extends BoxFilterConfiguration {

	@Override
	protected String getBoxToolTip() {
		return "The box size must be in the form: XxY where X=Y";
	}

	@Override
	protected String getHistoToolTip() {
		return "The bounds will be reset after the filter is removed."
				+ "\nSpecific bounds allow the features the mean filter brings out to be visible.";
	}

	@Override
	protected String getDescription() {
		return "Moves a box over the image and set each pixel value to the mean for the box.";
	}
}

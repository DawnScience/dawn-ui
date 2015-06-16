package org.dawnsci.plotting.tools.filter;

public class FanoConfiguration extends BoxFilterConfiguration {

	@Override
	protected String getBoxToolTip() {
		return "The box size must be odd numbers in the form: XxY";
	}

	@Override
	protected String getHistoToolTip() {
		return "The bounds will be reset after the filter is removed."
				+ "\nSpecific bounds allow the features the fano factor bring out to be visible.";
	}

	@Override
	protected String getDescription() {
		return "Moves a box over the image and set each pixel value to the variance/mean for the box.";
	}
}

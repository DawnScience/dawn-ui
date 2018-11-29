package org.dawnsci.datavis.e4.addons;

public class HistogramToolbarAddon extends AbstractDatavisAddon{

	@Override
	protected String getElementID() {
		return HistogramToolbarControl.ID;
	}


	@Override
	protected String getURI() {
		return HistogramToolbarControl.CLASS_URI;
	}
}

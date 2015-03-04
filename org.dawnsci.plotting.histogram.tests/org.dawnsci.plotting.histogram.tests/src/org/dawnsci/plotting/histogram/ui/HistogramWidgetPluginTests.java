package org.dawnsci.plotting.histogram.ui;

import org.eclipse.swt.widgets.Composite;
import org.junit.Test;

public class HistogramWidgetPluginTests extends PluginTestBase{
	
	private HistogramViewer histogramWidget;


	@Test
	public void testSimpleHistogram() throws Exception {
		assert(histogramWidget != null);
		readAndDispatchForever();
		
	}


	@Override
	protected void createControl(Composite parent) throws Exception {
		histogramWidget = new HistogramViewer(parent, "Histogram Test Widget", null, null);
	}

}

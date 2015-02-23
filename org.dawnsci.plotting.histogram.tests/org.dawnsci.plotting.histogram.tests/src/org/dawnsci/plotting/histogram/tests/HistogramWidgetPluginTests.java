package org.dawnsci.plotting.histogram.tests;

import org.dawnsci.plotting.histogram.HistogramWidget;
import org.eclipse.swt.widgets.Composite;
import org.junit.Test;

public class HistogramWidgetPluginTests extends PluginTestBase{
	
	private HistogramWidget histogramWidget;


	@Test
	public void testSimpleHistogram() throws Exception {
		assert(histogramWidget != null);
		readAndDispatchForever();
		
	}


	@Override
	protected void createControl(Composite parent) throws Exception {
		histogramWidget = new HistogramWidget(parent, "Histogram Test Widget", null, null);
	}

}

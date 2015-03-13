package org.dawnsci.plotting.histogram;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawnsci.plotting.histogram.ui.PluginTestBase;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * This is a regression test to make sure that the correct version of the data
 * is used when plotting histogram and that things that should not affect the
 * data do not affect the data.
 */
@RunWith(Parameterized.class)
public class ProviderLogDataPluginTest extends PluginTestBase {

	private IPlottingSystem system;
	private IDataset imageData;
	private IDataset imageDataPristine;
	private IImageTrace trace;
	private ImageHistogramProvider provider;
	private IDataset expectedImageData;

	@Parameters
	public static Collection<Object[]> data() {
		// input, expected (null for == input)
		List<Object[]> params = new ArrayList<>();

		Dataset input, expected;

		// normal data
		input = AbstractDataset.arange(100, Dataset.FLOAT64);
		input.setShape(10, 10);
		expected = null;
		params.add(new Object[] {input, expected});

		// normal data with some negative values
		input = AbstractDataset.arange(-100, 100, 2, Dataset.FLOAT64);
		input.setShape(10, 10);
		expected = null;
		params.add(new Object[] {input, expected});

		// complex data
		input = AbstractDataset.arange(100, Dataset.COMPLEX128);
		input.setShape(10, 10);
		expected = Maths.abs(input);
		params.add(new Object[] {input, expected});

		// complex data with some negative values
		input = AbstractDataset.arange(-100, 100, 2, Dataset.COMPLEX128);
		input.setShape(10, 10);
		expected = Maths.abs(input);
		params.add(new Object[] {input, expected});

		return params;
	}

	public ProviderLogDataPluginTest(IDataset imageData,
			IDataset expectedImageData) {
		this.imageData = imageData;
		this.imageDataPristine = imageData.clone();
		if (expectedImageData == null) {
			this.expectedImageData = this.imageDataPristine;
		} else {
			this.expectedImageData = expectedImageData;
		}
	}

	// This gets run in the before of the parent
	@Override
	protected void createControl(Composite parent) throws Exception {
		system = PlottingFactory.createPlottingSystem();
		system.createPlotPart(parent, "plot", null, PlotType.IMAGE, null);
	}

	@Before
	public void createProvidersAndTrace() throws Exception {
		readAndDispatch();
		trace = system.createImageTrace("trace");
		trace.setData(imageData, null, true);

		provider = new ImageHistogramProvider();
		provider.inputChanged(mock(Viewer.class), null, trace);
		readAndDispatch();
	}

	@Test
	public void testDefault() throws Exception {
		IDataset imageData2 = provider.getImageData(trace);
		assertEquals(expectedImageData, imageData2);
	}

	@Test
	public void testLog() throws Exception {
		trace.getImageServiceBean().setLogColorScale(true);
		trace.rehistogram();
		assertEquals(expectedImageData, provider.getImageData(trace));
	}

	@Test
	public void testLogCycling() throws Exception {
		boolean log = false;
		for (int i = 0; i < 10; i++) {
			log = !log;
			trace.getImageServiceBean().setLogColorScale(log);
			trace.rehistogram();
			assertEquals(expectedImageData, provider.getImageData(trace));
		}
	}

}

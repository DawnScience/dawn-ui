package org.dawnsci.plotting.histogram.ui;

import static org.mockito.Mockito.*;

import org.dawnsci.plotting.histogram.ImageHistogramProvider;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteListener;
import org.eclipse.dawnsci.plotting.api.trace.IPaletteTrace;
import org.eclipse.january.dataset.Random;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;

public class HistogramWidgetPluginTests extends PluginTestBase {

	private HistogramViewer histogramWidget;

	@Test
	public void testSimpleHistogram() throws Exception {
		assert (histogramWidget != null);
		readAndDispatchForever();

	}

	@Override
	protected void createControl(Composite parent) throws Exception {
		histogramWidget = new HistogramViewer(parent, "Histogram Test Widget",
				null, null);
		histogramWidget.setContentProvider(new ImageHistogramProvider());
		IPaletteTrace mock = mock(IPaletteTrace.class);

		ImageServiceBean bean = new ImageServiceBean();
		bean.setImage(Random.rand(new int[] { 1000, 1000 }));
		when(mock.getImageServiceBean()).thenReturn(bean);

		// linear palette
		RGB[] rgb = new RGB[256];
		for (int i = 0; i < 256; i++) {
			rgb[i] = new RGB(i, i, i);
		}
		PaletteData paletteData = new PaletteData(rgb);
		when(mock.getPaletteData()).thenReturn(paletteData);
		
		final double max[] = new double[]{0};
		when(mock.getMin()).thenReturn(0.0);
		when(mock.getMax()).thenReturn(max[0]);
		doAnswer(new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				max[0] = (double) invocation.getArguments()[0];
				return null;
			}
		}).when(mock).setMax(any(Number.class));
//		doAnswer(new Answer() {
//
//			@Override
//			public Object answer(InvocationOnMock invocation) throws Throwable {
//				// TODO Auto-generated method stub
//				return null;
//			}
//		}).when(mock).addPaletteListener((IPaletteListener) any());
//	
//		histogramWidget.setInput(mock);
	}

}

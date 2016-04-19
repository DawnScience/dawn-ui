package org.dawnsci.volumerender.tool;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IVolumeRenderTrace;
import org.eclipse.swt.widgets.Composite;
import org.junit.Test;

public class VolumeRendererTest {
	private final String traceID = "id";
	private final double resolution = .1;
	private final double intensity = .2;
	private final double opacity = .3;
	private final IDataset data = DoubleDataset.zeros(new int[]{100, 100,100}, 0);
	private final IDataset afterSlicing = DoubleDataset.zeros(new int[]{10, 10,10}, 0);
	private final double[] minMax = new double[]{1.0,1.0};

	@SuppressWarnings("unchecked")
	@Test
	public void testRenderingVolume() throws Exception{
		IPlottingSystem<Composite> plottingSystem = mock(IPlottingSystem.class);
		IVolumeRenderTrace trace = mock(IVolumeRenderTrace.class);
		when(plottingSystem.createVolumeRenderTrace(traceID)).thenReturn(trace);
		
		VolumeRenderer volumeRenderer = new VolumeRenderer(
				plottingSystem,
				traceID, 
				resolution, 
				intensity, 
				opacity, 
				data, 
				minMax, 
				minMax
			);
		volumeRenderer.run(mock(IMonitor.class));
		
		verify(trace).setData(new int[]{100,100,100}, afterSlicing, intensity, opacity, minMax, minMax);
		verify(plottingSystem).addTrace(trace);		
	}
}

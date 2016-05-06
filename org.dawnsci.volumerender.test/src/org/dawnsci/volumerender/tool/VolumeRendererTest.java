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
	private final IDataset data = DoubleDataset.createRange(10, 8010, 1).reshape(20, 20, 20);
	private final IDataset afterSlicing = DoubleDataset
			.createFromObject(new double[]{10, 20, 210, 220, 4010, 4020, 4210, 4220})
			.reshape(2,2,2);
	private final double min = 0.2;
	private final double max = 0.8;
	private final double[] resultingRange = new double[]{852,3378};

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
				min, 
				max,
				min, 
				max,
				new int[]{1,2,3}, 
				data
			);
		volumeRenderer.run(mock(IMonitor.class));
		
		verify(trace).setData(new int[]{20,20,20}, afterSlicing, intensity, opacity, resultingRange, resultingRange);
		verify(plottingSystem).addTrace(trace);		
	}
}

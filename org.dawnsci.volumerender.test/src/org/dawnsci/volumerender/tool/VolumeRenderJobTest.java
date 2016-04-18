package org.dawnsci.volumerender.tool;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IVolumeRenderTrace;
import org.eclipse.swt.widgets.Composite;
import org.junit.Test;

public class VolumeRenderJobTest{
	private final String traceID = "id";
	private final double resolution = 10.0;
	private final double intensity = 20.0;
	private final double opacity = 30.0;
	private final IDataset data = DoubleDataset.zeros(new int[]{100, 100,100}, 0);
	private final IDataset afterSlicing = DoubleDataset.zeros(new int[]{10, 10,10}, 0);
	private final double[] minMax = new double[]{1.0,1.0};
	
	@SuppressWarnings("unchecked")
	@Test
	public void testVolumeRendering() throws Throwable{
		IPlottingSystem<Composite> plottingSystem = mock(IPlottingSystem.class);
		IVolumeRenderTrace trace = mock(IVolumeRenderTrace.class);
		when(plottingSystem.createVolumeRenderTrace(traceID)).thenReturn(trace);
		
		VolumeRenderJob volumeRenderJob = new VolumeRenderJob("name", plottingSystem);
		volumeRenderJob.compute(traceID, resolution, intensity, opacity, data, minMax, minMax);
		
		eventually(() -> {
			verify(trace).setData(new int[]{100,100,100}, afterSlicing, intensity/100, opacity/100, minMax, minMax);
			verify(plottingSystem).addTrace(trace);		
		});		
	}
	
	private void eventually(Runnable condition) throws Throwable{
		long time = System.currentTimeMillis();
		Throwable lastException = null;
		while (System.currentTimeMillis() < time + 1000){
			try{
				condition.run();
				return;
			} catch (Throwable t) {
				lastException = t;
			}
			Thread.sleep(100);
		}
		throw lastException;
	}
}

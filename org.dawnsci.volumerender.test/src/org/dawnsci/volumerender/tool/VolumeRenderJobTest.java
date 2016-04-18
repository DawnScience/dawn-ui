package org.dawnsci.volumerender.tool;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class VolumeRenderJobTest{
	@Test
	public void testVolumeRendering() throws Throwable{
		VolumeRenderer volumeRenderer = mock(VolumeRenderer.class);
		VolumeRenderJob volumeRenderJob = new VolumeRenderJob("name");
		volumeRenderJob.compute(volumeRenderer);
		
		eventually(() -> {
			verify(volumeRenderer).run();
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

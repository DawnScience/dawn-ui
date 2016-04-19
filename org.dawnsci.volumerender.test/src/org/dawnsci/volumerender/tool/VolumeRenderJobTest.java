package org.dawnsci.volumerender.tool;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.junit.Test;

public class VolumeRenderJobTest{
	@Test
	public void testVolumeRendering() throws Throwable{
		VolumeRenderer volumeRenderer = mock(VolumeRenderer.class);
		VolumeRenderJob volumeRenderJob = new VolumeRenderJob("name");
		volumeRenderJob.compute(volumeRenderer);
		
		eventually(() -> {
			verify(volumeRenderer).run(notNull(IMonitor.class));
		});		
		
		assertThat(volumeRenderJob.run(mock(IProgressMonitor.class)), is(Status.OK_STATUS));
	}
	
	@Test
	public void testLogsError() throws Throwable{
		VolumeRenderer volumeRenderer = mock(VolumeRenderer.class);
		VolumeRenderJob volumeRenderJob = new VolumeRenderJob("name");
		volumeRenderJob.compute(volumeRenderer);
		doThrow(new RuntimeException()).when(volumeRenderer).run(notNull(IMonitor.class));
		doThrow(new RuntimeException()).when(volumeRenderer).run(notNull(IMonitor.class));
				
		assertThat(volumeRenderJob.run(mock(IProgressMonitor.class)).getSeverity(), is(Status.ERROR));
	}
	
	private void eventually(CheckedFunction condition) throws Throwable {
		long time = System.currentTimeMillis();
		Throwable lastException = null;
		while (System.currentTimeMillis() < time + 1000){
			try{
				condition.apply();
				return;
			} catch (Throwable t) {
				lastException = t;
			}
			Thread.sleep(100);
		}
		throw lastException;
	}
	
	public interface CheckedFunction{
		void apply() throws Exception;
	}
}

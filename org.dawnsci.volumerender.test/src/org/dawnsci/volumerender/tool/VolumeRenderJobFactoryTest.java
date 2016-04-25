package org.dawnsci.volumerender.tool;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.junit.Test;

public class VolumeRenderJobFactoryTest {
	@SuppressWarnings("unchecked")
	@Test
	public void createsJobWithNameAndPlottingSystem(){
		String name = new VolumeRenderJobFactory<>().build(mock(IPlottingSystem.class)).getName();
		
		assertThat(name, is("Volume renderer job"));
	}
}

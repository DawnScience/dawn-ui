package org.dawnsci.volumerender.tool;

import static org.mockito.Mockito.*;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.expressions.IVariableManager;
import org.eclipse.dawnsci.slicing.api.system.DimsDataList;
import org.eclipse.dawnsci.slicing.api.system.ISliceSystem;
import org.eclipse.dawnsci.slicing.api.system.SliceSource;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VolumeRenderToolTest {
	protected Shell shell;
	private Display display;

	@Before
	public void createComposite() {
		display = new Display();
		shell = new Shell(display);
		shell.setLayout(new GridLayout());
	}

	@After
	public void disposeDisplay() {
		display.dispose();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCanCreateGuiAndFireJob() throws Exception{
		VolumeRenderJobFactory<Shell> jobFactory = mock(VolumeRenderJobFactory.class);
		VolumeRenderTool<Shell> volumeRenderTool = new VolumeRenderTool<>(jobFactory);
		ISliceSystem slicingSystem = mock(ISliceSystem.class);
		IPlottingSystem<Shell> plottingSystem = mock(IPlottingSystem.class);
		when(slicingSystem.getDimsDataList()).thenReturn(new DimsDataList(new int[]{10,10,10}));
		when(slicingSystem.<Shell>getPlottingSystem()).thenReturn(plottingSystem);
		when(slicingSystem.getData()).thenReturn(new SliceSource((IVariableManager)null, new DoubleDataset(new int[]{10,10,10}), null, null, false));
		VolumeRenderJob volumeRenderJob = mock(VolumeRenderJob.class);
		when(jobFactory.build("Volume renderer job", plottingSystem)).thenReturn(volumeRenderJob);
		
		volumeRenderTool.setSlicingSystem(slicingSystem);
		volumeRenderTool.createToolComponent(shell);		
		volumeRenderTool.militarize(false);
		volumeRenderTool.militarize(true);
		volumeRenderTool.update();
		
		verify(volumeRenderJob).compute(eq("iuhdiamd8oa"), eq(0.0), eq(0.0), eq(0.0), any(), eq(new double[]{0.0,0.0}), eq(new double[]{0.0,0.0}));
	}
}

package org.dawnsci.volumerender.tool;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;

public class VolumeRenderJobFactory<T> {
	public VolumeRenderJob build(IPlottingSystem<T> plottingSystem){
		return new VolumeRenderJob("Volume renderer job", plottingSystem);
	}
}

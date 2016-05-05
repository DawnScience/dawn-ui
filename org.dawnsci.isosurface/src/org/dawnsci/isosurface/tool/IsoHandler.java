package org.dawnsci.isosurface.tool;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.isogui.IIsoItem;
import org.dawnsci.isosurface.isogui.IsoBean;
import org.dawnsci.isosurface.isogui.Type;
import org.dawnsci.isosurface.tool.IsosurfaceJob;
import org.dawnsci.volumerender.tool.VolumeRenderJob;
import org.dawnsci.volumerender.tool.VolumeRenderer;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IIsosurfaceTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsoHandler implements PropertyChangeListener {
	private static final Logger logger = LoggerFactory.getLogger(IsoHandler.class);

	final private IsosurfaceJob surfaceJob;
	final private VolumeRenderJob volumeJob;

	private ILazyDataset lazyDataset;

	private IPlottingSystem<?> system;

	private IsoBean bean;

	public IsoHandler(IsoBean bean, IsosurfaceJob surfaceJob, VolumeRenderJob volumnJob, ILazyDataset lazyDataset, IPlottingSystem<?> system) {
		this.bean = bean;
		this.surfaceJob = surfaceJob;
		this.volumeJob = volumnJob;
		this.lazyDataset = lazyDataset;
		this.system = system;
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		deleteAnyDeadTraces();
		
		for (IIsoItem current : bean.getItems()) {

			boolean justRerender = false;
			
			// run alg
			if (justRerender) {
				if (current.getType() == Type.ISO_SURFACE){
					IIsosurfaceTrace trace = (IIsosurfaceTrace) system.getTrace(current.getTraceKey());
					trace.setMaterial(
							current.getColour().red, current.getColour().green, current.getColour().blue, 
							current.getOpacity()
						);
					trace.setData(null, null, null, null);
				}
			} else {
				if (current.getType() == Type.ISO_SURFACE){
					surfaceJob.compute(
						new MarchingCubesModel(
							lazyDataset, 
							current.getValue(),
							new int[] { current.getResolution(), current.getResolution(), current.getResolution() },
							new int[] { current.getColour().red, current.getColour().green, current.getColour().blue },
							((double)current.getOpacity())/100.0, 
							current.getTraceKey()
						)
					);
				} else {
					volumeJob.compute(
							new VolumeRenderer(
									system,
									current.getTraceKey(),
									1.0/(double)current.getResolution(),
									((double)current.getOpacity())/100.0,
									((double)current.getOpacity())/100.0,
									0, 
									current.getValue(),
									0, 
									current.getValue(),
									new int[] { current.getColour().red, current.getColour().green, current.getColour().blue },
									lazyDataset
								)
						);
				}
			}
		}

	}

	private void deleteAnyDeadTraces() {
		Stream<String> currentTraces = bean.getItems().stream().map(item -> item.getTraceKey());
		surfaceJob.destroyOthers(currentTraces);
	}

}

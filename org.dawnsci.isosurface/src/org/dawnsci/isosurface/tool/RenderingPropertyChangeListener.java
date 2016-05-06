package org.dawnsci.isosurface.tool;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.stream.Stream;

import org.dawnsci.isosurface.alg.MarchingCubesModel;
import org.dawnsci.isosurface.isogui.IIsoItem;
import org.dawnsci.isosurface.isogui.IsoBean;
import org.dawnsci.isosurface.isogui.Type;
import org.dawnsci.volumerender.tool.VolumeRenderJob;
import org.dawnsci.volumerender.tool.VolumeRenderer;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;

public class RenderingPropertyChangeListener implements PropertyChangeListener {
	final private IsosurfaceJob surfaceJob;
	final private VolumeRenderJob volumeJob;

	private IPlottingSystem<?> system;
	private IsoBean bean;
	
	private ILazyDataset lazyDataset;
	private List<? extends IDataset> axes;

	public RenderingPropertyChangeListener(IsoBean bean, IsosurfaceJob surfaceJob, VolumeRenderJob volumnJob, IPlottingSystem<?> system) {
		this.bean = bean;
		this.surfaceJob = surfaceJob;
		this.volumeJob = volumnJob;
		this.system = system;
	}
	
	public void setData(ILazyDataset lazyDataset, List<? extends IDataset> axes) {
		this.lazyDataset = lazyDataset;
		this.axes = axes;	
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		deleteAnyDeadTraces();
	
		for (IIsoItem current : bean.getItems()) {
			if (current.getRenderType() == Type.ISO_SURFACE){
				renderIsoSurface(current);
			} else if (current.getRenderType() == Type.VOLUME){
				renderVolume(current);
			}
		}
	}

	private void renderVolume(IIsoItem current) {
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
						this.lazyDataset
					)
			);
	}

	private void renderIsoSurface(IIsoItem current) {
		surfaceJob.compute(
			new MarchingCubesModel(
				this.lazyDataset, 
				this.axes,
				current.getValue(),
				new int[] { current.getResolution(), current.getResolution(), current.getResolution() },
				new int[] { current.getColour().red, current.getColour().green, current.getColour().blue },
				((double)current.getOpacity())/100.0, 
				current.getTraceKey()
			)
		);
	}

	private void deleteAnyDeadTraces() {
		Stream<String> currentTraces = bean.getItems().stream().map(item -> item.getTraceKey());
		surfaceJob.destroyOthers(currentTraces);
	}

}

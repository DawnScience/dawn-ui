package org.dawnsci.jzy3d.plotmodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.dawnsci.datavis.model.PlotModeImage;
import org.dawnsci.jzy3d.Abstract2DJZY3DTrace;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJZY3DImagePlotMode extends PlotModeImage {

	private static final Logger logger = LoggerFactory.getLogger(PlotModeSurfaceMesh.class);
	
	private AtomicReference<ITrace> atomicTrace = new AtomicReference<>();
	
	@Override
	public IDataset[] sliceForPlot(ILazyDataset lz, SliceND slice, Object[] options, IPlottingSystem<?> system) throws Exception {
		IDataset[] data = super.sliceForPlot(lz, slice, options, system);
		IDataset d = data[0];
		AxesMetadata metadata = d.getFirstMetadata(AxesMetadata.class);
		List<IDataset> ax = null;
		
		if (metadata != null) {
			ax = new ArrayList<>();
			ILazyDataset[] axes = metadata.getAxes();
			if (axes != null) {
				for (ILazyDataset a : axes) {
					ax.add(a == null ? null : a.getSlice().squeeze());
				}
				Collections.reverse(ax);
			}
		}
		
		ITrace trace = null;
		
		String name = MetadataPlotUtils.removeSquareBrackets(d.getName());
		d.setName(name);

		trace = createTrace(d.getName(), system);
		trace.setDataName(d.getName());

		long t = System.currentTimeMillis();
		setData(trace,d, ax.toArray(new IDataset[ax.size()]));
		logger.info("Tesselation time {} ms for slice {} of {}", (System.currentTimeMillis()-t), slice.toString(), lz.getName());
		atomicTrace.set(trace);
		
		return data;
	}
	
	private void setData(ITrace trace, IDataset d, IDataset[] axes) {
		if (trace instanceof Abstract2DJZY3DTrace) {
			((Abstract2DJZY3DTrace)trace).setData(d, axes);
		}
	}
	
	protected abstract ITrace createTrace(String name, IPlottingSystem<?> system);
	
	
	@Override
	public void displayData(IDataset[] data, ITrace[] update, IPlottingSystem<?> system, Object userObject) throws Exception {
		
		if (update != null) {
			for (ITrace t : update) system.removeTrace(t);
		}

		ITrace trace = atomicTrace.getAndSet(null);
		
		if (trace == null) return;
		
		trace.setUserObject(userObject);
		system.addTrace(trace);

		system.repaint();
		
	}
}

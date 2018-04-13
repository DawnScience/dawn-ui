package org.dawnsci.jzy3d.plotmodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dawnsci.jzy3d.Abstract2DJZY3DTrace;
import org.dawnsci.jzy3d.VolumeTraceImpl;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.IVolumeTrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;

public class PlotModeVolume extends AbstractJZY3DImagePlotMode {

	@Override
	public String[] getOptions() {
		return new String[] {"X","Y","Z"};
	}

	@Override
	protected ITrace createTrace(String name, IPlottingSystem<?> system) {
		return system.createTrace(name,IVolumeTrace.class);
	}

	
	@Override
	public String getName() {
		return "Volume";
	}

	@Override
	public boolean isThisMode(ITrace trace) {
		return trace instanceof IVolumeTrace;
	}

	
	@Override
	public IDataset[] sliceForPlot(ILazyDataset lz, SliceND slice, Object[] options, IPlottingSystem<?> system) throws Exception {
		IDataset[] data = sliceForPlotInner(lz, slice, options, system);
		IDataset d = data[0].getSlice();
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
//		logger.info("Tesselation time {} ms for slice {} of {}", (System.currentTimeMillis()-t), slice.toString(), lz.getName());
		atomicTrace.set(trace);
		
		return data;
	}
	
	private void setData(ITrace trace, IDataset d, IDataset[] axes) {
		if (trace instanceof VolumeTraceImpl) {
			((VolumeTraceImpl)trace).setData(d, axes,d.min(true),d.max(true));
		}
	}
	
	public IDataset[] sliceForPlotInner(ILazyDataset lz, SliceND slice, Object[] options, IPlottingSystem<?> system) throws Exception {
		long t = System.currentTimeMillis();
		Dataset data = DatasetUtils.convertToDataset(lz.getSlice(slice));
//		logger.info("Slice time {} ms for slice {} of {}", (System.currentTimeMillis()-t), slice.toString(), lz.getName());
		data.setErrors(null);
//		updateName(lz.getName(),data,slice);
		data.squeeze();
		if (data.getRank() != 3) return null;
		return new IDataset[]{data};
	}


}

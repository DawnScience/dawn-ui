package org.dawnsci.datavis.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.ISurfaceTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;

public class PlotModeSurface extends PlotModeImage {

	public String[] getOptions() {
		return options;
	}

	@Override
	public String getName() {
		return "Surface";
	}

	@Override
	public boolean isThisMode(ITrace trace) {
		return trace instanceof ISurfaceTrace;
	}
	
	
	public void displayData(IDataset[] data, ITrace[] update, IPlottingSystem<?> system, Object userObject) throws Exception {
		IDataset d = data[0];
		AxesMetadata metadata = d.getFirstMetadata(AxesMetadata.class);
		List<IDataset> ax = null;
		
		if (metadata != null) {
			ax = new ArrayList<IDataset>();
			ILazyDataset[] axes = metadata.getAxes();
			if (axes != null) {
				for (ILazyDataset a : axes) {
					ax.add(a == null ? null : a.getSlice().squeeze());
				}
				Collections.reverse(ax);
			}
		}
		
		ISurfaceTrace trace = null;
		
		String name = MetadataPlotUtils.removeSquareBrackets(d.getName());
		d.setName(name);
		
		boolean isUpdate = false;
		if (update == null) {
			trace = system.createSurfaceTrace(d.getName());
			trace.setDataName(d.getName());
		} else {
			if (update[0] instanceof ISurfaceTrace) {
				trace = (ISurfaceTrace) update[0];
				isUpdate = true;
			}
			
			for (int i = 0; i < update.length; i++) {
				if (i==0 && update[i] instanceof ISurfaceTrace) {
					continue;
				}
				system.removeTrace(update[i]);
			}
		}
		
		
		trace.setData(d, ax);
		trace.setUserObject(userObject);
		if (!isUpdate) {
			system.setPlotType(PlotType.SURFACE);
			system.addTrace(trace);
		}
		
	}
}
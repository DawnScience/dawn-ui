package org.dawnsci.datavis.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.ISurfaceMeshTrace;
import org.eclipse.dawnsci.plotting.api.trace.ISurfaceTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;

public class PlotModeSurfaceMesh extends PlotModeImage {

	public String[] getOptions() {
		return options;
	}

	@Override
	public String getName() {
		return "SurfaceMesh";
	}

	@Override
	public boolean isThisMode(ITrace trace) {
		return trace instanceof ISurfaceMeshTrace;
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
		
		ISurfaceMeshTrace trace = null;
		
		String name = MetadataPlotUtils.removeSquareBrackets(d.getName());
		d.setName(name);
		
		boolean isUpdate = false;
//		if (update == null) {
		
		if (update != null) {
			for (ITrace t : update) system.removeTrace(t);
		}
		
			trace = system.createTrace(d.getName(),ISurfaceMeshTrace.class);
			trace.setDataName(d.getName());
//		} else {
//			if (update[0] instanceof ISurfaceMeshTrace) {
//				trace = (ISurfaceMeshTrace) update[0];
//				isUpdate = true;
//			}
//			
//			for (int i = 0; i < update.length; i++) {
//				if (i==0 && update[i] instanceof ISurfaceMeshTrace) {
//					continue;
//				}
//				system.removeTrace(update[i]);
//			}
//		}
		
		
		trace.setData(d, ax.toArray(new IDataset[ax.size()]));
		trace.setUserObject(userObject);
		if (!isUpdate) {
			system.setPlotType(PlotType.SURFACE);
			system.addTrace(trace);
		}
		
		system.repaint();
		
	}
}
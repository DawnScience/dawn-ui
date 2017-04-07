package org.dawnsci.datavis.model;

import org.dawnsci.slicing.tools.hyper.IHyperTrace;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;

public class PlotModeHyper implements IPlotMode {

	private static final String[] options =  new String[]{"X", "Y", "Z"};
	private ILazyDataset view3d;
	
	@Override
	public String[] getOptions() {
		return options;
	}

	@Override
	public IDataset[] sliceForPlot(ILazyDataset lz, SliceND slice, Object[] options) throws Exception {
		
		view3d = lz.getSliceView(slice);
			
		return null;
	}

	@Override
	public void displayData(IDataset[] data, ITrace[] update, IPlottingSystem system, Object userObject)
			throws Exception {
		
		IHyperTrace t = system.createTrace("Hyper Trace", IHyperTrace.class);
		
		t.setData(view3d, new int[]{0,1,2});
		
		system.addTrace(t);

	}

	@Override
	public String getName() {
		return "Hyper3d";
	}

	@Override
	public boolean supportsMultiple() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getMinimumRank() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isThisMode(ITrace trace) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int[] getDataDimensions(Object[] currentOptions) {
		// TODO Auto-generated method stub
		return null;
	}

}

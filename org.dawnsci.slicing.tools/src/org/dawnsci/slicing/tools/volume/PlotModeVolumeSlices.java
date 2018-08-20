package org.dawnsci.slicing.tools.volume;

import org.dawnsci.datavis.api.ILazyPlotMode;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;

public class PlotModeVolumeSlices implements ILazyPlotMode {
	
	private static final String[] options =  new String[]{"X", "Y", "Z"};
	private ILazyDataset view3d;
	private Object[] currentOptions;

	@Override
	public String[] getOptions() {
		return options;
	}

	@Override
	public IDataset[] sliceForPlot(ILazyDataset lz, SliceND slice, Object[] options, IPlottingSystem<?> system)
			throws Exception {
		view3d = lz.getSliceView(slice);
		currentOptions = options.clone();
		return null;
	}

	@Override
	public void displayData(IDataset[] data, ITrace[] update, IPlottingSystem<?> system, Object userObject)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return "Volume - Orthogonal Slices";
	}

	@Override
	public boolean supportsMultiple() {
		return false;
	}

	@Override
	public int getMinimumRank() {
		return 3;
	}

	@Override
	public boolean isThisMode(ITrace trace) {
		return trace instanceof IVolumeSlicesTrace;
	}

	@Override
	public int[] getDataDimensions(Object[] currentOptions) {
		//TODO properly
		return new int[] {0,1,2};
	}

	@Override
	public void displayData(ITrace[] update, IPlottingSystem<?> system, Object userObject) throws Exception {
		IVolumeSlicesTrace t = system.createTrace("Volume Slice Trace", IVolumeSlicesTrace.class);

		t.setData(view3d);
		t.setUserObject(userObject);
		system.addTrace(t);
		
	}

}

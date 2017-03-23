package org.dawnsci.datavis.model;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;

public class PlotModeHyper implements IPlotMode {

	private static final String[] options =  new String[]{"X", "Y", "Z"};
	
	@Override
	public String[] getOptions() {
		return options;
	}

	@Override
	public IDataset[] sliceForPlot(ILazyDataset lz, SliceND slice, Object[] options) throws Exception {
		return null;
	}

	@Override
	public void displayData(IDataset[] data, ITrace[] update, IPlottingSystem system, Object userObject)
			throws Exception {

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
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

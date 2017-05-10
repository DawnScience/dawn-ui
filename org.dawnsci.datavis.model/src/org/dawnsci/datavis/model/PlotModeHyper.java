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
	private int[] order = new int[]{0,1,2};
	
	@Override
	public String[] getOptions() {
		return options;
	}

	@Override
	public IDataset[] sliceForPlot(ILazyDataset lz, SliceND slice, Object[] options) throws Exception {
		
		view3d = lz.getSliceView(slice);
		int count = 0;
		for (int j = 0; j <options.length; j++) {
			if (options[j] != null) {
				if (options[j].equals(PlotModeHyper.options[0])) {
					order[0] = count++;
				} else if (options[j].equals(PlotModeHyper.options[1])) {
					order[1] = count++;
				}else if (options[j].equals(PlotModeHyper.options[2])) {
					order[2] = count++;
				}
			}
		}
		return null;
	}

	@Override
	public void displayData(IDataset[] data, ITrace[] update, IPlottingSystem<?> system, Object userObject)
			throws Exception {
		
		IHyperTrace t = system.createTrace("Hyper Trace", IHyperTrace.class);
		
		t.setData(view3d, order);
		
		system.addTrace(t);

	}

	@Override
	public String getName() {
		return "Hyper3d";
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
		return trace instanceof IHyperTrace;
	}

	@Override
	public int[] getDataDimensions(Object[] currentOptions) {
		int[] dataDims = new int[3];
		int count = 0;
		for (int i = 0; i < currentOptions.length && count < 3; i++) {
			if (currentOptions[i] != null &&
				!currentOptions[i].toString().isEmpty() &&
				(options[0].equals(currentOptions[i].toString()) || 
						options[1].equals(currentOptions[i].toString()) || 
						options[2].equals(currentOptions[i].toString()))) {
				dataDims[count++] = i;
			}
		}
		return dataDims;
	}

}

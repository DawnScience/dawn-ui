package org.dawnsci.multidimensional.ui.hyper;

import org.dawnsci.datavis.api.ILazyPlotMode;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;

public class PlotModeHyper4D implements ILazyPlotMode {

	private static final String[] options =  new String[]{"X2", "Y2", "X1", "Y1"};
	private ILazyDataset view4d;
	private int[] order = new int[]{0,1,2,3};
	private SliceND slice;
	
	@Override
	public String[] getOptions() {
		return options;
	}

	@Override
	public IDataset[] sliceForPlot(ILazyDataset lz, SliceND slice, Object[] options, IPlottingSystem<?> system)
			throws Exception {
		view4d = lz.getSliceView();
		this.slice = slice;
		int count = 0;
		order = this.order.clone();
		for (int j = 0; j <options.length; j++) {
			if (options[j] != null) {
				if (options[j].equals(PlotModeHyper4D.options[3])) {
					order[0] = count++;
				} else if (options[j].equals(PlotModeHyper4D.options[2])) {
					order[1] = count++;
				}else if (options[j].equals(PlotModeHyper4D.options[1])) {
					order[2] = count++;
				} else if (options[j].equals(PlotModeHyper4D.options[0])) {
					order[3] = count++;
				} else {
					count++;
				}
			}
		}
	
		return null;
	}

	@Override
	public String getName() {
		return "Hyper4d";
	}

	@Override
	public boolean supportsMultiple() {
		return false;
	}

	@Override
	public int getMinimumRank() {
		return 4;
	}

	@Override
	public boolean isThisMode(ITrace trace) {
		return trace instanceof IHyper4DTrace;
	}

	@Override
	public int[] getDataDimensions(Object[] currentOptions) {
		return null;
	}

	@Override
	public void displayData(ITrace[] update, IPlottingSystem<?> system, Object userObject) throws Exception {
		if (update != null && update.length > 0 && update[0] instanceof IHyperTrace) {
			IHyper4DTrace h = (IHyper4DTrace)update[0];
			h.setData(view4d, order, slice);
			h.setUserObject(userObject);
		} else {
			IHyper4DTrace t = system.createTrace("Hyper Trace", IHyper4DTrace.class);

			t.setData(view4d, order, slice);
			t.setUserObject(userObject);
			system.addTrace(t);
		}
		
	}

	@Override
	public void displayData(IDataset[] data, ITrace[] update, IPlottingSystem<?> system, Object userObject)
			throws Exception {
	}

}

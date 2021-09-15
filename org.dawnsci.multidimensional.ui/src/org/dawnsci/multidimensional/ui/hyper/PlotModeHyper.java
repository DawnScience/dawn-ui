package org.dawnsci.multidimensional.ui.hyper;

import org.dawnsci.datavis.api.ILazyPlotMode;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;

public class PlotModeHyper implements ILazyPlotMode {

	private static final String[] options =  new String[]{"Z", "X", "Y"};
	protected ILazyDataset view3d;
	protected int[] order = new int[]{0,1,2};
	protected SliceND slice;
	
	@Override
	public String[] getOptions() {
		return options;
	}

	@Override
	public IDataset[] sliceForPlot(ILazyDataset lz, SliceND slice, Object[] options, IPlottingSystem<?> system) throws Exception {
		
		view3d = lz.getSliceView();
		this.slice = slice;
		int count = 0;
		order = this.order.clone();
		for (int j = 0; j <options.length; j++) {
			if (options[j] != null) {
				if (options[j].equals(PlotModeHyper.options[1])) {
					order[0] = count++;
				} else if (options[j].equals(PlotModeHyper.options[2])) {
					order[1] = count++;
				}else if (options[j].equals(PlotModeHyper.options[0])) {
					order[2] = count++;
				} else {
					count++;
				}
			}
		}
	
		return null;
	}

	@Override
	public void displayData(IDataset[] data, ITrace[] update, IPlottingSystem<?> system, Object userObject)
			throws Exception {
		//does nothing for lazy plot modes
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

	@Override
	public void displayData(ITrace[] update, IPlottingSystem<?> system, Object userObject) throws Exception {
		
		if (update != null && update.length > 0 && update[0] instanceof IHyperTrace) {
			IHyperTrace h = (IHyperTrace)update[0];
			h.setData(view3d, order, slice);
			h.setUserObject(userObject);
		} else {
			IHyperTrace t = system.createTrace("Hyper Trace", IHyperTrace.class);

			t.setData(view3d, order, slice);
			t.setUserObject(userObject);
			system.addTrace(t);
		}
		
	}

}

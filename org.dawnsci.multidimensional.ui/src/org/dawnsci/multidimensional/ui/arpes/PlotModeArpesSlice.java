package org.dawnsci.multidimensional.ui.arpes;

import org.dawnsci.multidimensional.ui.hyper.IHyperTrace;
import org.dawnsci.multidimensional.ui.hyper.PlotModeHyper;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlotModeArpesSlice extends PlotModeHyper {

	private static final Logger logger = LoggerFactory.getLogger(PlotModeArpesSlice.class);
	
	private static final String[] options =  new String[]{"Z", "Y", "X"};
	
	@Override
	public String[] getOptions() {
		return options;
	}
	
	@Override
	public String getName() {
		return "ARPES Slicing";
	}
	
	@Override
	public IDataset[] sliceForPlot(ILazyDataset lz, SliceND slice, Object[] options, IPlottingSystem<?> system) throws Exception {
		IDataset[] output = super.sliceForPlot(lz, slice, options, system);
		
		//if data has 3 dimensions try and slice it into memory
		//to improve responsiveness of UI
		if (view3d.getRank() == 3) {
			try {
				view3d = view3d.getSlice();
			} catch (Exception e) {
				logger.debug("Could not read data into memory, using lazy version");
			}
		}
		
		return output;
	}
		

	@Override
	public void displayData(ITrace[] update, IPlottingSystem<?> system, Object userObject) throws Exception {
		
		if (update != null && update.length > 0 && update[0] instanceof IArpesSliceTrace) {
			IHyperTrace h = (IHyperTrace)update[0];
			h.setData(view3d, order, slice);
			h.setUserObject(userObject);
		} else {
			IArpesSliceTrace t = system.createTrace("Arpes Trace", IArpesSliceTrace.class);

			t.setData(view3d, order, slice);
			t.setUserObject(userObject);
			system.addTrace(t);
		}
		
	}
	
}

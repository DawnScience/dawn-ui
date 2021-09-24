package org.dawnsci.multidimensional.ui.arpes;

import org.dawnsci.multidimensional.ui.hyper.IHyperTrace;
import org.dawnsci.multidimensional.ui.hyper.PlotModeHyper;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;

public class PlotModeArpesSlice extends PlotModeHyper {

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

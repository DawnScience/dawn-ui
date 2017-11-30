package org.dawnsci.datavis.model;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITableDataTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.IDataset;

public class PlotModeDataTable1D extends PlotModeXY {

	@Override
	public String[] getOptions() {
		return options;
	}

	@Override
	public String getName() {
		return "Text Table 1D";
	}

	@Override
	public boolean isThisMode(ITrace trace) {
		return trace instanceof ITableDataTrace;
	}

	@Override
	public boolean supportsMultiple() {
		return false;
	}
	
	@Override
	public void displayData(IDataset[] data, ITrace[] update, IPlottingSystem<?> system, Object userObject) throws Exception {
		IDataset d = data[0];
		
		d.setShape(-1, 1);
		
		ITableDataTrace trace = new TableDataTrace1D(d);

		trace.setUserObject(userObject);
		system.addTrace(trace);
		
		
	}

}

package org.dawnsci.datavis.model;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITableDataTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.IDataset;

public class PlotModeDataTable2D extends PlotModeImage {

	@Override
	public String[] getOptions() {
		return options;
	}

	@Override
	public String getName() {
		return "Text Table 2D";
	}

	@Override
	public boolean isThisMode(ITrace trace) {
		return trace instanceof ITableDataTrace;
	}
	
	@Override
	public void displayData(IDataset[] data, ITrace[] update, IPlottingSystem<?> system, Object userObject) throws Exception {
		IDataset d = data[0];
		
		
		ITableDataTrace trace = new TableDataTrace2D(d);

		trace.setUserObject(userObject);
		system.addTrace(trace);
		
		
	}

}

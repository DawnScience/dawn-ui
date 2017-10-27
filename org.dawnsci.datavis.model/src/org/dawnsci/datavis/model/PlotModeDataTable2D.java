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
		
		
		ITableDataTrace trace = new ITableDataTrace() {
			
			private Object userObject = null;
			
			@Override
			public void setName(String name) {
				
			}
			
			@Override
			public String getName() {
				return null;
			}
			
			@Override
			public void setVisible(boolean isVisible) {
				
			}
			
			@Override
			public void setUserTrace(boolean isUserTrace) {
				
			}
			
			@Override
			public void setUserObject(Object userObject) {
				this.userObject = userObject;
				
			}
			
			@Override
			public void setDataName(String name) {
				
			}
			
			@Override
			public boolean isVisible() {
				return false;
			}
			
			@Override
			public boolean isUserTrace() {
				return false;
			}
			
			@Override
			public boolean is3DTrace() {
				return false;
			}
			
			@Override
			public Object getUserObject() {
				return userObject;
			}
			
			@Override
			public int getRank() {
				return 0;
			}
			
			@Override
			public String getDataName() {
				return null;
			}
			
			@Override
			public IDataset getData() {
				return d;
			}
			
			@Override
			public void dispose() {
				
			}
		};

		trace.setUserObject(userObject);
		system.addTrace(trace);
		
		
	}

}

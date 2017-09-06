package org.dawnsci.datavis.model;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITableDataTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.IDataset;

public class PlotModeDataTable extends PlotModeImage {

	public String[] getOptions() {
		return options;
	}

	@Override
	public String getName() {
		return "Text Table";
	}

	@Override
	public boolean isThisMode(ITrace trace) {
		return trace instanceof ITableDataTrace;
	}
	
	
	public void displayData(IDataset[] data, ITrace[] update, IPlottingSystem<?> system, Object userObject) throws Exception {
		IDataset d = data[0];
		
		
		ITableDataTrace trace = new ITableDataTrace() {
			
			private Object userObject = null;
			
			@Override
			public void setName(String name) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void setVisible(boolean isVisible) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setUserTrace(boolean isUserTrace) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void setUserObject(Object userObject) {
				this.userObject = userObject;
				
			}
			
			@Override
			public void setDataName(String name) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean isVisible() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean isUserTrace() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean is3DTrace() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public Object getUserObject() {
				return userObject;
			}
			
			@Override
			public int getRank() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public String getDataName() {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public IDataset getData() {
				return d;
			}
			
			@Override
			public void dispose() {
				// TODO Auto-generated method stub
				
			}
		};

		trace.setUserObject(userObject);
		system.addTrace(trace);
		
		
	}

}

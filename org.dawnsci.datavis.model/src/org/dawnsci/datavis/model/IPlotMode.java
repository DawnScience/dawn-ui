package org.dawnsci.datavis.model;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.SliceND;

public interface IPlotMode {

	public String[] getOptions();
	
	public IDataset[] sliceForPlot(ILazyDataset lz, SliceND slice,Object[] options) throws Exception;
	
	public void displayData(IDataset[] data, ITrace[] update, IPlottingSystem<?> system, Object userObject) throws Exception;
	
	public String getName();
	
	public boolean supportsMultiple();
	
	public int getMinimumRank();
	
	public boolean isThisMode(ITrace trace);
	
	public int[] getDataDimensions(Object[] currentOptions);

	
}

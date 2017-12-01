package org.dawnsci.processing.ui;

import org.dawnsci.processing.ui.slice.ProcessingLogDisplay;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.swt.widgets.Composite;

public interface IProcessDisplayHelper {
	
	public IPlottingSystem<Composite> getDisplayPlot();
	
	public ProcessingLogDisplay getLogDisplay();
	
	public void setDisplayMode(ProcessDisplayOptions mode);
	
	public enum ProcessDisplayOptions {
		OUTPUT_ONLY, OUTPUT_LOG, OUTPUT_DISPLAY, ALL;
	}

}

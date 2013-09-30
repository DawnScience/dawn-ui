package org.dawnsci.plotting.api.filter;

import java.util.List;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.plotting.api.trace.TraceWillPlotEvent;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

/**
 * Convenience class for filtering plots based on name.
 * 
 * @author fcp94556
 *
 */
public abstract class NamedPlottingFilter extends AbstractPlottingFilter {

	protected String dataName;

	/**
	 * Only traces with this name will be filtered.
	 * @param name
	 */
	public NamedPlottingFilter(String dataName) {
		this.dataName = dataName;
	}
	
	@Override
	public void filter(IPlottingSystem system, TraceWillPlotEvent evt) {
		final ITrace trace = (ITrace)evt.getSource();
		final String dName = trace.getDataName() != null ? trace.getDataName() : trace.getName(); 
		if (!dataName.equals(dName)) return;
		super.filter(system, evt);
	}

}

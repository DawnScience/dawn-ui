package org.dawnsci.plotting.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawnsci.plotting.api.filter.IFilterDecorator;
import org.dawnsci.plotting.api.filter.IPlottingFilter;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ILineTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.plotting.api.trace.ITraceListener;
import org.dawnsci.plotting.api.trace.TraceWillPlotEvent;

/**
 * This class is not intended to be accessed directly.
 * 
 * Instead use:
 * <code>
 * IFilterDecorator dec = PlottingFactory.createFilterDecorator(IPlottingSystem)
 * dec.addFilter(myFilter extends AbstractPlottingFilter);
 * </code>
 * 
@internal
**/
class FilterDecoratorImpl implements IFilterDecorator {
	
	private IPlottingSystem        system;
	private List<IPlottingFilter>  filters;
	private boolean filterActive      = true;
	private boolean processingAllowed = true;
    private boolean isDisposed        = false;
    
    private ITraceListener listener;
    
	FilterDecoratorImpl(final IPlottingSystem system) {
		this.system  = system;
		this.filters = new ArrayList<IPlottingFilter>(3);
		
		listener = new ITraceListener.Stub() {
			@Override
			public void traceWillPlot(TraceWillPlotEvent evt) {
				if (!filterActive)       return;
				if (!processingAllowed)  return;
				if (system.isDisposed()) return;
				process(evt);
			}
		};
		system.addTraceListener(listener);
	}

	protected void process(TraceWillPlotEvent evt) {
		if (filters.isEmpty()) return;
		for (IPlottingFilter filter : filters) {
			final ITrace trace = (ITrace)evt.getSource();
			if (trace.getRank()!=filter.getRank()) {
				if (filter.getRank()>0) continue;
			}
			filter.filter(system, evt);
		}
	}

	@Override
	public void addFilter(IPlottingFilter filter) {
		if (isDisposed) throw new RuntimeException("IFilterDecorator is disposed!");
		if (filters.contains(filter))  throw new RuntimeException("The filter is already added!");
		filters.add(filter);
	}

	@Override
	public void removeFilter(IPlottingFilter filter) {
		if (isDisposed) throw new RuntimeException("IFilterDecorator is disposed!");
		try {
			processingAllowed = false;
			filter.reset();
		} finally {
			processingAllowed = true;
		}
		filters.remove(filter);
		filter.dispose();
	}

	@Override
	public void setActive(boolean isActive) {
		this.filterActive = isActive;
	}

	@Override
	public boolean isActive() {
		return filterActive;
	}

	@Override
	public void clear() {
		reset();
		filters.clear();
		system.repaint();
	}

	@Override
	public void reset() {
		try {
			processingAllowed = false;
			if (filters.size()>0) filters.get(0).reset(); // Puts data back to original data.
		} finally {
			processingAllowed = true;
		}
	}
	

	@Override
	public void apply() {
		final Collection<ITrace> traces = system.getTraces();
		
		final Collection<ITrace> existing = getExistingFilteredTraces();
		for (ITrace trace : traces) {
			if (trace instanceof ILineTrace) {
				ILineTrace lt = (ILineTrace)trace;
				lt.setData(lt.getXData(), lt.getYData());
				
			} else if (trace instanceof IImageTrace) {
				IImageTrace it = (IImageTrace)trace;
				it.setData(it.getData(), it.getAxes(), false);
			}
		}
		
		system.repaint();
		
	}



	private Collection<ITrace> getExistingFilteredTraces() {
		if (filters.isEmpty()) return null;
        final IPlottingFilter filter = filters.get(0);
		return filter.getFilteredTraces();
	}

	@Override
	public void dispose() {
		for (IPlottingFilter filter : filters) filter.dispose();
		isDisposed = true;
		clear();
		system.removeTraceListener(listener);
	}
	
}

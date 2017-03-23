package org.dawnsci.slicing.tools.hyper;

import org.eclipse.dawnsci.plotting.api.IPlottingSystemViewer;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.swt.widgets.Composite;

public class HyperPlotViewer extends IPlottingSystemViewer.Stub<Composite> {

	private HyperComponent hyper;
	
	public void createControl(final Composite parent) {
		hyper = new HyperComponent();
		hyper.createControl(parent);
	}
	
	@Override
	public boolean addTrace(ITrace trace){
		if (trace instanceof IHyperTrace) {
			//hyper.setData(lazy, daxes, slices, order);
		}
		return false;
	}
	
	
	@Override
	public ITrace createTrace(String name, Class<? extends ITrace> clazz){
		IHyperTrace t = new HyperTrace();
		return t;
	}
	
	@Override
	public boolean isTraceTypeSupported(Class<? extends ITrace> trace) {
		if (IHyperTrace.class.isAssignableFrom(trace)) {
			return true;
		}
		return false;
	}
}

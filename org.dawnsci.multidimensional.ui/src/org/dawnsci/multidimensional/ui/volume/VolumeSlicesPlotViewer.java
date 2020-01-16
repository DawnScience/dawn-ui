package org.dawnsci.multidimensional.ui.volume;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystemViewer;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class VolumeSlicesPlotViewer extends IPlottingSystemViewer.Stub<Composite>  {

	private VolumeViewer control;
	private VolumeSliceTrace trace;
	
	@Override
	public void createControl(final Composite parent) {
		
		BundleContext bundleContext =
				FrameworkUtil.
				getBundle(this.getClass()).
				getBundleContext();
		
		IPlottingService plotService = bundleContext.getService(bundleContext.getServiceReference(IPlottingService.class));
		
		control = new VolumeViewer(parent, SWT.NONE, plotService);
	}
	
	@Override
	public Composite getControl() {
		return control;
	}
	
	@Override
	public boolean addTrace(ITrace trace){
		
		if (trace instanceof VolumeSliceTrace) {
			this.trace = (VolumeSliceTrace)trace;
			control.setData(this.trace.getLazyData(), new int[] {0,1,2});
			return true;
		}
		
		return false;
	}
	
	@Override
	public  <U extends ITrace> U createTrace(String name, Class<? extends ITrace> clazz) {
		
		if (clazz == IVolumeSlicesTrace.class) {
			VolumeSliceTrace volume = new VolumeSliceTrace();
			volume.setName(name);
			return (U)volume;
		}
		return null;
	}
	
	@Override
	public boolean isTraceTypeSupported(Class<? extends ITrace> trace) {
		
		if (IVolumeSlicesTrace.class.isAssignableFrom(trace)) {
			return true;
		}
		return false;
	}
	
	@Override
	public Collection<Class<? extends ITrace>> getSupportTraceTypes() {
		List<Class<? extends ITrace>> l = new ArrayList<>();
		l.add(IVolumeSlicesTrace.class);
		return l;
	}
	
	@Override
	public void removeTrace(ITrace trace) {
		if (trace == this.trace) {
			control.reset();
		}
	}
	
	@Override
	public void clearTraces() {
		control.reset();
	}
	
	@Override
	public void reset(boolean force) {
		control.reset();
	}
	
}

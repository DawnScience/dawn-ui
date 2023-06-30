package org.dawnsci.multidimensional.ui.imagecompare;

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

public class ImageComparePlotViewer extends IPlottingSystemViewer.Stub<Composite> {
	
	private ImageCompareViewer control;

	@Override
	public void createControl(final Composite parent) {
		
		BundleContext bundleContext =
				FrameworkUtil.
				getBundle(this.getClass()).
				getBundleContext();
		
		IPlottingService plotService = bundleContext.getService(bundleContext.getServiceReference(IPlottingService.class));
		
		control = new ImageCompareViewer(parent, SWT.NONE, plotService);
	}
	
	@Override
	public boolean addTrace(ITrace trace){
		
		if (trace instanceof ImageCompareTrace) {
			control.setImages(((ImageCompareTrace)trace).getImages());
			return true;
		}
		
		return false;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public  <U extends ITrace> U createTrace(String name, Class<U> clazz) {
		
		if (clazz == IImageCompareTrace.class) {
			ImageCompareTrace t = new ImageCompareTrace();
			t.setName(name);
			return (U)t;
		}
		return null;
	}
	
	@Override
	public Composite getControl() {
		return control;
	}
	
	
	@Override
	public boolean isTraceTypeSupported(Class<? extends ITrace> trace) {
		
		return IImageCompareTrace.class.isAssignableFrom(trace);

	}
	
	@Override
	public void clearTraces() {
		//Only clear on reset...
		// not ideal but stops flashing while slicing
		
	}
	
	@Override
	public void reset(boolean force) {
		control.clear();
	}
	
	@Override
	public Collection<Class<? extends ITrace>> getSupportTraceTypes() {
		List<Class<? extends ITrace>> l = new ArrayList<>();
		l.add(IImageCompareTrace.class);
		return l;
	}

}

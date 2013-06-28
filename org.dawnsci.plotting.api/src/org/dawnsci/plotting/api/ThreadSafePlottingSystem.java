package org.dawnsci.plotting.api;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.StandardMBean;

import org.dawnsci.plotting.api.annotation.IAnnotation;
import org.dawnsci.plotting.api.axis.IAxis;
import org.dawnsci.plotting.api.axis.IPositionListener;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.region.IRegionListener;
import org.dawnsci.plotting.api.trace.ColorOption;
import org.dawnsci.plotting.api.trace.IImageStackTrace;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ILineStackTrace;
import org.dawnsci.plotting.api.trace.ILineTrace;
import org.dawnsci.plotting.api.trace.ISurfaceTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.plotting.api.trace.ITraceListener;
import org.dawnsci.plotting.api.trace.TraceEvent;
import org.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
/**
 * Will be a thread safe version of all the plotting system methods.
 * 
 * @author fcp94556
 *
 */
public class ThreadSafePlottingSystem extends StandardMBean implements IPlottingSystem, NotificationBroadcaster {

	private static final Logger logger = LoggerFactory.getLogger(ThreadSafePlottingSystem.class);
	
	private IPlottingSystem deligate;

	public ThreadSafePlottingSystem(IPlottingSystem deligate) throws Exception {
		super(IPlottingSystem.class);
		this.deligate = deligate;
	}

	@Override
	public IImageTrace createImageTrace(String traceName) {
		return (IImageTrace)call(getMethodName(Thread.currentThread().getStackTrace()), traceName);
	}

	@Override
	public ILineTrace createLineTrace(String traceName) {
		return (ILineTrace)call(getMethodName(Thread.currentThread().getStackTrace()), traceName);
	}

	@Override
	public ISurfaceTrace createSurfaceTrace(String traceName) {
		return (ISurfaceTrace)call(getMethodName(Thread.currentThread().getStackTrace()), traceName);
	}

	@Override
	public ILineStackTrace createLineStackTrace(String traceName) {
		return (ILineStackTrace)call(getMethodName(Thread.currentThread().getStackTrace()), traceName);
	}
	
	@Override
	public IImageStackTrace createImageStackTrace(String traceName) {
		return (IImageStackTrace)call(getMethodName(Thread.currentThread().getStackTrace()), traceName);
	}

	@Override
	public void addTrace(ITrace trace) {
		call(getMethodName(Thread.currentThread().getStackTrace()), trace);
	}

	@Override
	public void removeTrace(ITrace trace) {
		call(getMethodName(Thread.currentThread().getStackTrace()), trace);
	}

	@Override
	public ITrace getTrace(String name) {
		return deligate.getTrace(name);
	}

	@Override
	public Collection<ITrace> getTraces() {
		return deligate.getTraces();
	}

	@Override
	public Collection<ITrace> getTraces(Class<? extends ITrace> clazz) {
		return deligate.getTraces(clazz);
	}

	@Override
	public void addTraceListener(ITraceListener l) {
		deligate.addTraceListener(l);
	}

	@Override
	public void removeTraceListener(ITraceListener l) {
		deligate.removeTraceListener(l);
	}

	@Override
	public void renameTrace(ITrace trace, String name) throws Exception {
		call(getMethodName(Thread.currentThread().getStackTrace()), trace, name);
	}

	@Override
	public IRegion createRegion(String name, RegionType regionType) throws Exception {
		return (IRegion)call(getMethodName(Thread.currentThread().getStackTrace()), name, regionType);
	}

	@Override
	public void addRegion(IRegion region) {
		call(getMethodName(Thread.currentThread().getStackTrace()), region);
	}

	@Override
	public void removeRegion(IRegion region) {
		call(getMethodName(Thread.currentThread().getStackTrace()), region);
	}

	@Override
	public IRegion getRegion(String name) {
		return deligate.getRegion(name);
	}

	@Override
	public Collection<IRegion> getRegions(RegionType type) {
        return deligate.getRegions(type);
	}

	@Override
	public boolean addRegionListener(IRegionListener l) {
		return deligate.addRegionListener(l);
	}

	@Override
	public boolean removeRegionListener(IRegionListener l) {
		return deligate.removeRegionListener(l);
	}

	@Override
	public void clearRegions() {
		call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public Collection<IRegion> getRegions() {
		return deligate.getRegions();
	}

	@Override
	public void renameRegion(IRegion region, String name) throws Exception {
		call(getMethodName(Thread.currentThread().getStackTrace()), region, name);
	}

	@Override
	public IAxis createAxis(String title, boolean isYAxis, int side) {
		return 	(IAxis)call(getMethodName(Thread.currentThread().getStackTrace()), 
				           new Class[]{String.class, boolean.class, int.class},
				           title, isYAxis, side);
	}

	@Override
	public IAxis getSelectedYAxis() {
		return deligate.getSelectedYAxis();
	}

	@Override
	public void setSelectedYAxis(IAxis yAxis) {
		call(getMethodName(Thread.currentThread().getStackTrace()), yAxis);
	}

	@Override
	public IAxis getSelectedXAxis() {
		return deligate.getSelectedXAxis();
	}

	@Override
	public void setSelectedXAxis(IAxis xAxis) {
		call(getMethodName(Thread.currentThread().getStackTrace()), xAxis);
	}

	@Override
	public void autoscaleAxes() {
		call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public IAnnotation createAnnotation(String name) throws Exception {
		return (IAnnotation)call(getMethodName(Thread.currentThread().getStackTrace()), name);
	}

	@Override
	public void addAnnotation(IAnnotation annot) {
		call(getMethodName(Thread.currentThread().getStackTrace()), annot);
	}

	@Override
	public void removeAnnotation(IAnnotation annot) {
		call(getMethodName(Thread.currentThread().getStackTrace()), annot);
	}

	@Override
	public IAnnotation getAnnotation(String name) {
		return deligate.getAnnotation(name);
	}

	@Override
	public void clearAnnotations() {
		call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public void renameAnnotation(IAnnotation annotation, String name)
			throws Exception {
		call(getMethodName(Thread.currentThread().getStackTrace()), annotation, name);
	}

	@Override
	public void printPlotting() {
		call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public void copyPlotting() {
		call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public String savePlotting(String filename) throws Exception {
		return (String)call(getMethodName(Thread.currentThread().getStackTrace()), filename);
	}

	@Override
	public void savePlotting(String filename, String filetype) throws Exception {
		call(getMethodName(Thread.currentThread().getStackTrace()), filename, filetype);
	}

	@Override
	public String getTitle() {
		return (String)call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public void setTitle(String title) {
		call(getMethodName(Thread.currentThread().getStackTrace()), title);
	}

	@Override
	public void createPlotPart(Composite parent, 
			                   String plotName,
			                   IActionBars bars, 
			                   PlotType hint, 
			                   IWorkbenchPart part) {
		
		throw new RuntimeException("Cannot call createPlotPart, only allowed to use this from python!");
	}

	@Override
	public String getPlotName() {
		return deligate.getPlotName();
	}

	@Override
	public List<ITrace> createPlot1D(IDataset x, List<? extends IDataset> ys, IProgressMonitor monitor) {
		return deligate.createPlot1D(x, ys, monitor);
	}

	@Override
	public List<ITrace> createPlot1D(IDataset x,
			List<? extends IDataset> ys, String title, IProgressMonitor monitor) {
		return deligate.createPlot1D(x, ys, title, monitor);
	}

	@Override
	public List<ITrace> updatePlot1D(IDataset x,
			List<? extends IDataset> ys, IProgressMonitor monitor) {
		return deligate.updatePlot1D(x, ys, monitor);
	}

	@Override
	public ITrace createPlot2D(IDataset image,
			List<? extends IDataset> axes, IProgressMonitor monitor) {
		return deligate.createPlot2D(image, axes, monitor);
	}

	@Override
	public ITrace updatePlot2D(IDataset image,
			List<? extends IDataset> axes, IProgressMonitor monitor) {
		return deligate.updatePlot2D(image, axes, monitor);
	}

	@Override
	public void setPlotType(PlotType plotType) {
		call(getMethodName(Thread.currentThread().getStackTrace()), plotType);
	}

	@Override
	public void append(String dataSetName, Number xValue, Number yValue, IProgressMonitor monitor) throws Exception {
		deligate.append(dataSetName, xValue, yValue, monitor);
	}

	@Override
	public void reset() {
		deligate.reset();
	}

	@Override
	public void clear() {
		deligate.clear();
	}

	@Override
	public void dispose() {
		call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public void repaint() {
		call(getMethodName(Thread.currentThread().getStackTrace()));
	}
	
	@Override
	public void repaint(boolean autoScale) {
		call(getMethodName(Thread.currentThread().getStackTrace()), new Class[]{boolean.class}, autoScale);
	}

	@Override
	public Composite getPlotComposite() {
		return deligate.getPlotComposite();
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return deligate.getSelectionProvider();
	}

	@Override
	public IDataset getData(String dataSetName) {
		return deligate.getData(dataSetName);
	}

	@Override
	public PlotType getPlotType() {
		return deligate.getPlotType();
	}

	@Override
	public boolean is2D() {
		return deligate.is2D();
	}

	@Override
	public IActionBars getActionBars() {
		return deligate.getActionBars();
	}

	@Override
	public IPlotActionSystem getPlotActionSystem() {
		return deligate.getPlotActionSystem();
	}

	@Override
	public void setDefaultCursor(int cursorType) {
		call(getMethodName(Thread.currentThread().getStackTrace()), new Class[]{int.class}, cursorType);
	}
	
	/**
	 * Calls method in a SWT thread safe way.
	 * @param methodName
	 * @param args
	 */
	private Object call(final String methodName, final Object... args) {
		
		@SuppressWarnings("rawtypes")
		final Class[] classes = args!=null ? new Class[args.length] : null;
		if (classes!=null) {
			for (int i = 0; i < args.length; i++) classes[i]=args[i].getClass();
		}
		return call(methodName, classes, args);
	}
	
	/**
	 * Calls method in a SWT thread safe way.
	 * @param methodName
	 * @param args
	 */
	private Object call(final String methodName, @SuppressWarnings("rawtypes") final Class[] classes, final Object... args) {
		
		final List<Object> ret = new ArrayList<Object>(1);
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try {
				    Method method = deligate.getClass().getMethod(methodName, classes);
				    Object val    = method.invoke(deligate, args);
				    ret.add(val);
				} catch (Exception ne) {
					logger.error("Cannot execute "+methodName+" with "+args, ne);
				}
			}
		});
		return ret.get(0);
	}

	public static String getMethodName ( StackTraceElement ste[] ) {  
		   
	    String methodName = "";  
	    boolean flag = false;  
	   
	    for ( StackTraceElement s : ste ) {  
	   
	        if ( flag ) {  
	   
	            methodName = s.getMethodName();  
	            break;  
	        }  
	        flag = s.getMethodName().equals( "getStackTrace" );  
	    }  
	    return methodName;  
	}

	@Override
	public IAxis removeAxis(IAxis axis) {
		return (IAxis)call(getMethodName(Thread.currentThread().getStackTrace()), axis);	
	}  
	
	@SuppressWarnings("unchecked")
	@Override
	public List<IAxis> getAxes() {
		return (List<IAxis>)call(getMethodName(Thread.currentThread().getStackTrace()));	
	}
	
	@Override
	public void addPositionListener(IPositionListener l) {
		call(getMethodName(Thread.currentThread().getStackTrace()), new Class[]{IPositionListener.class}, l);
	}

	@Override
	public void removePositionListener(IPositionListener l) {
		call(getMethodName(Thread.currentThread().getStackTrace()), new Class[]{IPositionListener.class}, l);
	}

	private NotificationBroadcasterSupport generalBroadcaster;

	@Override
	public void addNotificationListener(NotificationListener listener,
			                            NotificationFilter filter, Object handback) throws IllegalArgumentException {
		
		if (generalBroadcaster == null)  generalBroadcaster = new NotificationBroadcasterSupport();		
		generalBroadcaster.addNotificationListener(listener, filter, handback);
		
	}

	@Override
	public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
		if (generalBroadcaster == null)  return;	
		generalBroadcaster.removeNotificationListener(listener);
	}

	@Override
	public MBeanNotificationInfo[] getNotificationInfo() {
		return new MBeanNotificationInfo[] {
				new MBeanNotificationInfo(
						new String[] { "plotting code 1" },   // notif. types
						Notification.class.getName(), // notif. class
						"User Notifications."         // description
				)
		};
	}

	@Override
	public void setKeepAspect(boolean b) {
		call(getMethodName(Thread.currentThread().getStackTrace()), new Class[] { boolean.class }, b);
	}

	@Override
	public void setShowIntensity(boolean b) {
		call(getMethodName(Thread.currentThread().getStackTrace()), new Class[] { boolean.class }, b);
	}

	@Override
	public void setShowLegend(boolean b) {
		call(getMethodName(Thread.currentThread().getStackTrace()), new Class[] { boolean.class }, b);
	}

	@Override
	public Object getAdapter(Class adapter) {
		return call(getMethodName(Thread.currentThread().getStackTrace()), new Class[] { adapter }, adapter);
	}


	@Override
	public boolean isDisposed() {
		return (Boolean)call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public void setColorOption(ColorOption colorOption) {
		call(getMethodName(Thread.currentThread().getStackTrace()), new Class[] { ColorOption.class }, colorOption);
	}

	@Override
	public boolean isRescale() {
		return (Boolean)call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public void setRescale(boolean rescale) {
		call(getMethodName(Thread.currentThread().getStackTrace()), new Class[] { boolean.class }, rescale);
	}

	@Override
	public void setFocus() {
		call(getMethodName(Thread.currentThread().getStackTrace()));
	}
	
	public boolean isXFirst() {
		return (Boolean)call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	/**
	 * Set if the first plot is the x-axis.
	 * @param xFirst
	 */
	public void setXFirst(boolean xFirst) {
		call(getMethodName(Thread.currentThread().getStackTrace()), new Class[]{boolean.class}, xFirst);
	}
	
	public void fireWillPlot(final TraceWillPlotEvent evt) {
		call(getMethodName(Thread.currentThread().getStackTrace()), evt);
	}
	
	/**
	 * May be used to force a trace to fire update listeners in the plotting system.
	 * @param evt
	 */
	public void fireTraceUpdated(final TraceEvent evt) {
		call(getMethodName(Thread.currentThread().getStackTrace()), evt);		
	}

	public void fireTraceAdded(final TraceEvent evt) {
		call(getMethodName(Thread.currentThread().getStackTrace()), evt);		
	}

}

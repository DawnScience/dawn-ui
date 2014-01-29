/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
import org.dawnsci.plotting.api.axis.IClickListener;
import org.dawnsci.plotting.api.axis.IPositionListener;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.region.IRegionListener;
import org.dawnsci.plotting.api.trace.ColorOption;
import org.dawnsci.plotting.api.trace.IImageStackTrace;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ILineStackTrace;
import org.dawnsci.plotting.api.trace.ILineTrace;
import org.dawnsci.plotting.api.trace.IMulti2DTrace;
import org.dawnsci.plotting.api.trace.IScatter3DTrace;
import org.dawnsci.plotting.api.trace.ISurfaceTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.plotting.api.trace.ITraceListener;
import org.dawnsci.plotting.api.trace.IVectorTrace;
import org.dawnsci.plotting.api.trace.TraceEvent;
import org.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
	
	private IPlottingSystem delegate;

	public ThreadSafePlottingSystem(IPlottingSystem delegate) throws Exception {
		super(IPlottingSystem.class);
		this.delegate = delegate;
	}

	@Override
	public IImageTrace createImageTrace(String traceName) {
		return (IImageTrace)call(getMethodName(Thread.currentThread().getStackTrace()), traceName);
	}
	
	public Control setControl(Control alternative, boolean isToolbar) {
		throw new RuntimeException("Expert method "+getMethodName(Thread.currentThread().getStackTrace())+" is not allowed in JMX mode!");
	}

	@Override
	public ILineTrace createLineTrace(String traceName) {
		return (ILineTrace)call(getMethodName(Thread.currentThread().getStackTrace()), traceName);
	}

	@Override
	public IVectorTrace createVectorTrace(String traceName) {
		return (IVectorTrace)call(getMethodName(Thread.currentThread().getStackTrace()), traceName);
	}

	@Override
	public ISurfaceTrace createSurfaceTrace(String traceName) {
		return (ISurfaceTrace)call(getMethodName(Thread.currentThread().getStackTrace()), traceName);
	}

	@Override
	public IMulti2DTrace createMulti2DTrace(String traceName) {
		return (IMulti2DTrace)call(getMethodName(Thread.currentThread().getStackTrace()), traceName);
	}

	@Override
	public ILineStackTrace createLineStackTrace(String traceName) {
		return (ILineStackTrace)call(getMethodName(Thread.currentThread().getStackTrace()), traceName);
	}

	@Override
	public ILineStackTrace createLineStackTrace(String traceName, int stackplots) {
		return (ILineStackTrace)call(getMethodName(Thread.currentThread().getStackTrace()), traceName, stackplots);
	}

	@Override
	public IScatter3DTrace createScatter3DTrace(String traceName) {
		return (IScatter3DTrace)call(getMethodName(Thread.currentThread().getStackTrace()), traceName);
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
		return delegate.getTrace(name);
	}

	@Override
	public Collection<ITrace> getTraces() {
		return delegate.getTraces();
	}

	@Override
	public Collection<ITrace> getTraces(Class<? extends ITrace> clazz) {
		return delegate.getTraces(clazz);
	}

	@Override
	public void addTraceListener(ITraceListener l) {
		delegate.addTraceListener(l);
	}

	@Override
	public void removeTraceListener(ITraceListener l) {
		delegate.removeTraceListener(l);
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
		return delegate.getRegion(name);
	}

	@Override
	public Collection<IRegion> getRegions(RegionType type) {
        return delegate.getRegions(type);
	}

	@Override
	public boolean addRegionListener(IRegionListener l) {
		return delegate.addRegionListener(l);
	}

	@Override
	public boolean removeRegionListener(IRegionListener l) {
		return delegate.removeRegionListener(l);
	}

	@Override
	public void clearRegions() {
		call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public Collection<IRegion> getRegions() {
		return delegate.getRegions();
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
		return delegate.getSelectedYAxis();
	}

	@Override
	public void setSelectedYAxis(IAxis yAxis) {
		call(getMethodName(Thread.currentThread().getStackTrace()), yAxis);
	}

	@Override
	public IAxis getSelectedXAxis() {
		return delegate.getSelectedXAxis();
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
		return delegate.getAnnotation(name);
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
		return delegate.getPlotName();
	}

	@Override
	public List<ITrace> createPlot1D(IDataset x, List<? extends IDataset> ys, IProgressMonitor monitor) {
		return delegate.createPlot1D(x, ys, monitor);
	}

	@Override
	public List<ITrace> createPlot1D(IDataset x,
			List<? extends IDataset> ys, String title, IProgressMonitor monitor) {
		return delegate.createPlot1D(x, ys, title, monitor);
	}

	@Override
	public List<ITrace> updatePlot1D(IDataset x,
			List<? extends IDataset> ys, IProgressMonitor monitor) {
		return delegate.updatePlot1D(x, ys, monitor);
	}

	@Override
	public ITrace createPlot2D(IDataset image,
			List<? extends IDataset> axes, IProgressMonitor monitor) {
		return delegate.createPlot2D(image, axes, monitor);
	}

	@Override
	public ITrace updatePlot2D(IDataset image,
			List<? extends IDataset> axes, IProgressMonitor monitor) {
		return delegate.updatePlot2D(image, axes, monitor);
	}

	@Override
	public void setPlotType(PlotType plotType) {
		call(getMethodName(Thread.currentThread().getStackTrace()), plotType);
	}

	@Override
	public void append(String dataSetName, Number xValue, Number yValue, IProgressMonitor monitor) throws Exception {
		delegate.append(dataSetName, xValue, yValue, monitor);
	}

	@Override
	public void reset() {
		delegate.reset();
	}

	@Override
	public void resetAxes() {
		delegate.resetAxes();
	}

	@Override
	public void clear() {
		delegate.clear();
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
		return delegate.getPlotComposite();
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return delegate.getSelectionProvider();
	}

	@Override
	public IDataset getData(String dataSetName) {
		return delegate.getData(dataSetName);
	}

	@Override
	public PlotType getPlotType() {
		return delegate.getPlotType();
	}

	@Override
	public boolean is2D() {
		return delegate.is2D();
	}

	@Override
	public IActionBars getActionBars() {
		return delegate.getActionBars();
	}

	@Override
	public IPlotActionSystem getPlotActionSystem() {
		return delegate.getPlotActionSystem();
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
				    Method method = delegate.getClass().getMethod(methodName, classes);
				    Object val    = method.invoke(delegate, args);
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
	public IAxis getAxis(String name) {
		return (IAxis)call(getMethodName(Thread.currentThread().getStackTrace()), name);	
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
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
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

	@Override
	public IWorkbenchPart getPart() {
		return (IWorkbenchPart)call(getMethodName(Thread.currentThread().getStackTrace()));
	}
	@Override
	public List<ITrace> createPlot1D(IDataset x, List<? extends IDataset> ys,
			List<String> dataNames, String title, IProgressMonitor monitor) {
		return (List<ITrace>)call(getMethodName(Thread.currentThread().getStackTrace()), x,ys,dataNames, title, monitor);
	}

	@Override
	public List<ITrace> updatePlot1D(IDataset x, List<? extends IDataset> ys,
			List<String> dataNames, IProgressMonitor monitor) {
		return (List<ITrace>)call(getMethodName(Thread.currentThread().getStackTrace()), x,ys,dataNames, monitor);
	}

	@Override
	public ITrace createPlot2D(IDataset image, List<? extends IDataset> axes,
			String dataName, IProgressMonitor monitor) {
		return (ITrace)call(getMethodName(Thread.currentThread().getStackTrace()), image, axes, dataName, monitor);
	}

	@Override
	public ITrace updatePlot2D(IDataset image, List<? extends IDataset> axes,
			String dataName, IProgressMonitor monitor) {
		return (ITrace)call(getMethodName(Thread.currentThread().getStackTrace()), image, axes, dataName, monitor);
	}

	@Override
	public void setEnabled(boolean enabled) {
		call(getMethodName(Thread.currentThread().getStackTrace()), new Class[]{boolean.class}, enabled);		
	}

	@Override
	public boolean isEnabled() {
		return (Boolean)call(getMethodName(Thread.currentThread().getStackTrace()));
	}
	

	@Override
	public void addClickListener(IClickListener l) {
		call(getMethodName(Thread.currentThread().getStackTrace()), new Class[]{IClickListener.class}, l);
	}

	@Override
	public void removeClickListener(IClickListener l) {
		call(getMethodName(Thread.currentThread().getStackTrace()), new Class[]{IClickListener.class}, l);
	}

}

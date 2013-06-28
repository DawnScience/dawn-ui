package org.dawnsci.plotting.jmx;

import java.util.Collection;
import java.util.List;

import javax.management.MalformedObjectNameException;

import org.dawnsci.plotting.api.IPlotActionSystem;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
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
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

/**
 * This class implements IPlottingSystem but can be used 
 * on a remote client, for instance a Jython VM on another machine.
 * 
 * The calls made to it pass over JMX and provide a complete remote 
 * access to a given plotting system. This access is designed to be
 * complete, so you are not isolated from SWT by using this interface.
 * 
 * This is a complete plotting system but it means that you have
 * to have quite a lot of things on the class path in order to work.
 * To have less things in the class path e.g. on GDA Server, one
 * can use @see JMXPlottingFactory.getXXXSystem(...)
 * 
 * @author fcp94556
 *
 */
@SuppressWarnings("unchecked")
public class JMXPlottingSystem extends JMXSystemObject implements IPlottingSystem {

	
	/**
	 * This is a complete plotting system but it means that you have
	 * to have quite a lot of things on the class path in order to work.
	 * To have less things in the class path e.g. on GDA Server, one
	 * can use @see JMXPlottingFactory.getXXXSystem(...)
	 * 
	 * The name of the plotting system as registered in the PlottingFactory.
	 * @param name
	 * @throws MalformedObjectNameException 
	 */
	public JMXPlottingSystem(final String plotName, final String hostName, final int port) throws Exception {

		super(plotName, hostName, port);
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
		return (ITrace)call(getMethodName(Thread.currentThread().getStackTrace()), name);
	}

	@Override
	public Collection<ITrace> getTraces() {
		return (Collection<ITrace>)call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public Collection<ITrace> getTraces(Class<? extends ITrace> clazz) {
		return (Collection<ITrace>)call(getMethodName(Thread.currentThread().getStackTrace()), clazz);
	}

	@Override
	public void addTraceListener(ITraceListener l) {
		call(getMethodName(Thread.currentThread().getStackTrace()),l);
	}

	@Override
	public void removeTraceListener(ITraceListener l) {
		call(getMethodName(Thread.currentThread().getStackTrace()),l);
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
		return (IRegion)call(getMethodName(Thread.currentThread().getStackTrace()),name);
	}

	@Override
	public Collection<IRegion> getRegions(RegionType type) {
		return (Collection<IRegion>)call(getMethodName(Thread.currentThread().getStackTrace()), type);
	}

	@Override
	public boolean addRegionListener(IRegionListener l) {
		return (Boolean)call(getMethodName(Thread.currentThread().getStackTrace()), l);
	}

	@Override
	public boolean removeRegionListener(IRegionListener l) {
		return (Boolean)call(getMethodName(Thread.currentThread().getStackTrace()), l);
	}

	@Override
	public void clearRegions() {
		call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public Collection<IRegion> getRegions() {
		return (Collection<IRegion>)call(getMethodName(Thread.currentThread().getStackTrace()));
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
		return (IAxis)call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public void setSelectedYAxis(IAxis yAxis) {
		call(getMethodName(Thread.currentThread().getStackTrace()), yAxis);
	}

	@Override
	public IAxis getSelectedXAxis() {
		return (IAxis)call(getMethodName(Thread.currentThread().getStackTrace()));
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
		return (IAnnotation)call(getMethodName(Thread.currentThread().getStackTrace()), name);
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
		
		throw new RuntimeException("Cannot call createPlotPart, you are talking to a remote plotting system!");
	}

	@Override
	public String getPlotName() {
		return (String)call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public List<ITrace> createPlot1D(IDataset x, List<? extends IDataset> ys, IProgressMonitor monitor) {
		return (List<ITrace>)call(getMethodName(Thread.currentThread().getStackTrace()), x,ys,monitor);
	}

	@Override
	public List<ITrace> createPlot1D(IDataset x,
			List<? extends IDataset> ys, String title, IProgressMonitor monitor) {
		return (List<ITrace>)call(getMethodName(Thread.currentThread().getStackTrace()), x,ys, title, monitor);
	}

	@Override
	public List<ITrace> updatePlot1D(IDataset x,
			List<? extends IDataset> ys, IProgressMonitor monitor) {
		return (List<ITrace>)call(getMethodName(Thread.currentThread().getStackTrace()), x,ys,monitor);
	}

	@Override
	public ITrace createPlot2D(IDataset image,
			List<? extends IDataset> axes, IProgressMonitor monitor) {
		return (ITrace)call(getMethodName(Thread.currentThread().getStackTrace()), image,axes,monitor);
	}

	@Override
	public ITrace updatePlot2D(IDataset image,
			List<? extends IDataset> axes, IProgressMonitor monitor) {
		return (ITrace)call(getMethodName(Thread.currentThread().getStackTrace()), image,axes,monitor);
	}

	@Override
	public void setPlotType(PlotType plotType) {
		call(getMethodName(Thread.currentThread().getStackTrace()), plotType);
	}

	@Override
	public void append(String dataSetName, Number xValue, Number yValue, IProgressMonitor monitor) throws Exception {
		call(getMethodName(Thread.currentThread().getStackTrace()), dataSetName,xValue,yValue,monitor);
	}

	@Override
	public void reset() {
		call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public void clear() {
		call(getMethodName(Thread.currentThread().getStackTrace()));
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
		throw new RuntimeException("Composite is not serializable!");
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		throw new RuntimeException("ISelectionProvider is not serializable!");
	}

	@Override
	public AbstractDataset getData(String dataSetName) {
		return (AbstractDataset)call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public PlotType getPlotType() {
		return (PlotType)call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public boolean is2D() {
		return (Boolean)call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public IActionBars getActionBars() {
		return (IActionBars)call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public IPlotActionSystem getPlotActionSystem() {
		return (IPlotActionSystem)call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public void setDefaultCursor(int cursorType) {
		call(getMethodName(Thread.currentThread().getStackTrace()), new Class[] { int.class }, cursorType);
	}

	@Override
	public IAxis removeAxis(IAxis axis) {
		return (IAxis) call(getMethodName(Thread.currentThread().getStackTrace()), axis);
	}

	@Override
	public List<IAxis> getAxes() {
		return (List<IAxis>) call(getMethodName(Thread.currentThread().getStackTrace()));
	}

	@Override
	public void addPositionListener(IPositionListener l) {
		call(getMethodName(Thread.currentThread().getStackTrace()), new Class[] { IPositionListener.class }, l);
	}

	@Override
	public void removePositionListener(IPositionListener l) {
		call(getMethodName(Thread.currentThread().getStackTrace()), new Class[] { IPositionListener.class }, l);
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

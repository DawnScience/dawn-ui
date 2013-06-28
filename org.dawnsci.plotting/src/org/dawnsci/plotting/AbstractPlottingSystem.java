/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.plotting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.dawb.common.services.ISystemService;
import org.dawnsci.plotting.api.IPlotActionSystem;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.PlottingSelectionProvider;
import org.dawnsci.plotting.api.annotation.IAnnotation;
import org.dawnsci.plotting.api.axis.IAxis;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.region.IRegionListener;
import org.dawnsci.plotting.api.region.RegionEvent;
import org.dawnsci.plotting.api.tool.IToolChangeListener;
import org.dawnsci.plotting.api.tool.IToolPage;
import org.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.dawnsci.plotting.api.tool.ToolChangeEvent;
import org.dawnsci.plotting.api.trace.ColorOption;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ILineTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.plotting.api.trace.ITraceListener;
import org.dawnsci.plotting.api.trace.TraceEvent;
import org.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.dawnsci.plotting.views.EmptyTool;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base class for IPlottingSystem. NOTE some methods that should be implemented
 * throw exceptions if they are called. They should be overridden.
 * Some methods that should be implemented do nothing, they should be overridden.
 * 
 * There are TODO tags added to provide information as to where these optional
 * methods to override are.
 * 
 * Some methods such as listeners are implemented for everyone.
 * 
 * The IToolPageSystem is implemented and populated by tools read from
 * extension point.
 * 
 * 
 * DO NOT CHANGE THIS CLASS TO IMPLEMENT CUSTOM METHODS PLEASE. Instead use the 
 * IPlottingSystem interface and keep your special methods in your class using the
 * plotting system.
 * 
 * @author fcp94556
 *
 */
public abstract class AbstractPlottingSystem implements IPlottingSystem, IToolPageSystem {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractPlottingSystem.class);

	protected boolean rescale = true;

	// True if first data set should be plotted as x axis
	protected boolean xfirst = true; // NOTE Currently must always be true or some tools start in a bad state.
	
	// Manager for actions
	protected PlottingActionBarManager actionBarManager;
	
	// Feedback for plotting, if needed
	protected Text      pointControls;
	
	// Color option for 1D plots, if needed.
	protected ColorOption colorOption=ColorOption.BY_DATA;

	protected String rootName;

	/**
	 * The action bars on the part using the plotting system, may be null
	 */
	protected IActionBars bars;

	public AbstractPlottingSystem() {
		this.actionBarManager = createActionBarManager();
	}

	public void setPointControls(Text pointControls) {
		this.pointControls = pointControls;
	}

 
	public void setRootName(String rootName) {
		this.rootName = rootName;
	}
	
	public String getRootName() {
		return rootName;
	}

	/**
	 * You may optionally implement this method to return plot
	 * color used for the IDataset
	 * @param object
	 * @return
	 */
	public Color get1DPlotColor(Object object) {
		return null;
	}

	
	public ColorOption getColorOption() {
		return colorOption;
	}

	@Override
	public void setColorOption(ColorOption colorOption) {
		this.colorOption = colorOption;
	}

	@Override
	public boolean isRescale() {
		return rescale;
	}

	@Override
	public void setRescale(boolean rescale) {
		final boolean oldRescale = rescale;
		this.rescale = rescale;
		firePropertyChangeListener(RESCALE_ID, oldRescale, rescale);
	}
	
	private Collection<IPropertyChangeListener> propertyListeners;
	
	public void addPropertyChangeListener(IPropertyChangeListener pcl) {
		if (propertyListeners==null) propertyListeners = new HashSet<IPropertyChangeListener>();
		propertyListeners.add(pcl);
	}
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		if (propertyListeners==null) return;
		propertyListeners.remove(listener);
	}
	
	protected void firePropertyChangeListener(String rescaleId, Object oldValue, Object newValue) {
		if (propertyListeners==null) return;
		final PropertyChangeEvent evt = new PropertyChangeEvent(this, rescaleId, oldValue, newValue);
		for (IPropertyChangeListener l : propertyListeners) {
			l.propertyChange(evt);
		}
	}


	/**
	 * Please override to provide a  PlottingActionBarManager or a class
	 * subclassing it. This class deals with Actions to avoid this
	 * class getting more complex.
	 * 
	 * @return
	 */
	protected abstract PlottingActionBarManager createActionBarManager();

	public void dispose() {

		PlottingFactory.removePlottingSystem(plotName);
		if (part!=null) {
			@SuppressWarnings("unchecked")
			final ISystemService<IPlottingSystem> service = (ISystemService<IPlottingSystem>)PlatformUI.getWorkbench().getService(ISystemService.class);
			if (service!=null) {
				service.removeSystem(part.getTitle());
				logger.debug("Plotting system for '"+part.getTitle()+"' removed.");
			}
		}

		actionBarManager.dispose();
		
		if (propertyListeners!=null) propertyListeners.clear();
		propertyListeners = null;
		
		if (traceListeners!=null) traceListeners.clear();
		traceListeners = null;
		pointControls = null;
		
		if (selectionProvider!=null) selectionProvider.clear();
		selectionProvider = null;
		
		if (currentToolIdMap!=null) currentToolIdMap.clear();
		currentToolIdMap = null;
	}

	@Override
	public PlotType getPlotType() {
		return plottingMode;
	}
	/**
	 * Override to define what should happen if the 
	 * system is notified that plot types are likely
	 * to be of a certain type.
	 * 
	 * Do not call before createPlotPart(...)
	 * 
	 * @param image
	 */
	public void setPlotType(PlotType plotType) {
		this.plottingMode = plotType;
	}

	public boolean isXFirst() {
		return xfirst;
	}

	public void setXFirst(boolean xfirst) {
		this.xfirst = xfirst;
	}

	/**
	 * Call this method to retrieve what is currently plotted.
	 * See all ITraceListener.
	 * 
	 * @return
	 */
	@Override
	public Collection<ITrace> getTraces() {
		return null; // TODO
	}

	private List<ITraceListener> traceListeners;
	
	/**
	 * Call to be notified of events which require the plot
	 * data to be sent again.
	 * 
	 * @param l
	 */
	@Override
	public void addTraceListener(final ITraceListener l) {
		if (traceListeners==null) traceListeners = new ArrayList<ITraceListener>(7);
		if (!traceListeners.contains(l)) traceListeners.add(l);
	}
	
	/**
	 * Call to be notified of events which require the plot
	 * data to be sent again.
	 * 
	 * @param l
	 */
	@Override
	public void removeTraceListener(final ITraceListener l) {
		if (traceListeners==null) return;
		traceListeners.remove(l);
	}
	
	public void fireTracesAltered(final TraceEvent evt) {
		if (traceListeners==null) return;
		for (ITraceListener l : traceListeners) {
			l.tracesUpdated(evt);
		}
	}
	protected void fireTraceCreated(final TraceEvent evt) {
		if (traceListeners==null) return;
		for (ITraceListener l : traceListeners) {
			l.traceCreated(evt);
		}
	}
	public void fireTraceUpdated(final TraceEvent evt) {
		if (traceListeners==null) return;
		for (ITraceListener l : traceListeners) {
			l.traceUpdated(evt);
		}
	}
	public void fireWillPlot(final TraceWillPlotEvent evt) {
		if (traceListeners==null) return;
		for (ITraceListener l : traceListeners) {
			try {
			    l.traceWillPlot(evt);
			} catch (Throwable usuallyIgnored) {
				// Does not stack trace when deployed in product.
				logger.trace("Cannot call traceWillPlot!", usuallyIgnored);
			}
		}
	}
	public void fireTraceAdded(final TraceEvent evt) {
		if (traceListeners==null) return;
		for (ITraceListener l : traceListeners) {
			l.traceAdded(evt);
		}
	}
	protected void fireTraceRemoved(final TraceEvent evt) {
		if (traceListeners==null) return;
		for (ITraceListener l : traceListeners) {
			l.traceRemoved(evt);
		}
	}

	protected void fireTracesCleared(final TraceEvent evt) {
		if (traceListeners==null) return;
		for (ITraceListener l : traceListeners) {
			l.tracesRemoved(evt);
		}
	}
	
	public void fireTracesPlotted(final TraceEvent evt) {
		if (traceListeners==null) return;
		for (ITraceListener l : traceListeners) {
			l.tracesAdded(evt);
		}
	}
	
	private String title;
	/**
	 * Override this method to provide an implementation of title setting.
	 * @param title
	 */
	public void setTitle(final String title) {
		this.title = title;
	}
	/**
	 * Override this method to provide an implementation of title setting.
	 * @param title
	 */
	public String getTitle() {
		return title;
	}

	@Override
	public void setShowLegend(boolean b) {
		//TODO
	}

	@Override
	public void setShowIntensity(boolean b){
		
	}

	/**
	 * Please override if you allow your plotter to create images
	 * @param size
	 * @return
	 */
	public Image getImage(Rectangle size) {
		return null;
	}
	
	@Override
	public void append( final String           dataSetName, 
			            final Number           xValue,
					    final Number           yValue,
					    final IProgressMonitor monitor) throws Exception {
		//TODO
		throw new Exception("updatePlot not implemented for "+getClass().getName());
	}
	
	@Override
	public void repaint() {
		//TODO
	}
	
	public void repaint(boolean autoscale) {
		//TODO
	}
	
	protected IWorkbenchPart part;
	
	// The plotting mode, used for updates to data
	protected PlotType plottingMode;

	protected String plotName;
	
	@Override
	public String getPlotName() {
		return plotName;
	}
	
	/**
	 * This simply assigns the part, subclasses should override this
	 * and call super.createPlotPart(...) to assign the part. Also registers the plot
	 * with the PlottingFactory.
	 */
	@Override
	public void createPlotPart(final Composite      parent,
							   final String         plotName,
							   final IActionBars    bars,
							   final PlotType       hint,
							   final IWorkbenchPart part) {

		this.plotName = plotName;
		this.plottingMode = hint;
		this.part = part;
		this.bars = bars;
		PlottingFactory.registerPlottingSystem(plotName, this);
		
		if (part!=null) {
			@SuppressWarnings("unchecked")
			final ISystemService<IPlottingSystem> service = (ISystemService<IPlottingSystem>)PlatformUI.getWorkbench().getService(ISystemService.class);
			if (service!=null) {
				service.putSystem(part.getTitle(), this);
				logger.debug("Plotting system for '"+part.getTitle()+"' registered.");
			}
		}
	}

	@Override
	public IAxis createAxis(final String title, final boolean isYAxis, int side) {
		//TODO
		throw new RuntimeException("Cannot create an axis with "+getClass().getName());
	}

	@Override
	public IAxis getSelectedYAxis(){
		//TODO
		throw new RuntimeException("Cannot have multiple axes with "+getClass().getName());
	}

	@Override
	public void setSelectedYAxis(IAxis yAxis){
		//TODO
		throw new RuntimeException("Cannot have multiple axes with "+getClass().getName());
	}

	@Override
	public IAxis getSelectedXAxis(){
		//TODO
		throw new RuntimeException("Cannot have multiple axes with "+getClass().getName());
	}

	@Override
	public void setSelectedXAxis(IAxis xAxis){
		//TODO
		throw new RuntimeException("Cannot have multiple axes with "+getClass().getName());
	}
	
	protected PlottingSelectionProvider selectionProvider;

	public ISelectionProvider getSelectionProvider() {
		if (selectionProvider==null) selectionProvider = new PlottingSelectionProvider();
		return selectionProvider;
	}
	
	private Collection<IRegionListener> regionListeners;

	protected void fireRegionCreated(RegionEvent evt) {
		if (regionListeners==null) return;
		for (IRegionListener l : regionListeners) l.regionCreated(evt);
	}

	protected void fireRegionAdded(RegionEvent evt) {
		if (regionListeners==null) return;
		for (IRegionListener l : regionListeners) l.regionAdded(evt);
	}


	protected void fireRegionRemoved(RegionEvent evt) {
		if (regionListeners==null) return;
		for (IRegionListener l : regionListeners) l.regionRemoved(evt);
	}

	@Override
	public IRegion createRegion(final String name, final RegionType regionType)  throws Exception {
		//TODO Please implement creation of region here.
		return null;
	}

	@Override
	public void addRegion(final IRegion region) {
		fireRegionAdded(new RegionEvent(region));
	}

	@Override
	public void removeRegion(final IRegion region) {
		fireRegionRemoved(new RegionEvent(region));
	}

	@Override
	public void renameRegion(final IRegion region, String name) {
		// Do nothing
	}

	@Override
	public void clearRegions() {
		//TODO
	}

	@Override
	public IRegion getRegion(final String name) {
		return null; // TODO
	}

	@Override
	public Collection<IRegion> getRegions() {
		return null; // TODO
	}

	@Override
	public boolean addRegionListener(final IRegionListener l) {
		if (regionListeners == null) regionListeners = new HashSet<IRegionListener>(7);
		if (!regionListeners.contains(l)) return regionListeners.add(l);
		return false;
	}

	@Override
	public boolean removeRegionListener(final IRegionListener l) {
		if (regionListeners == null) return true;
		return regionListeners.remove(l);
	}

	@Override
	public IAnnotation createAnnotation(final String name) throws Exception {
		return null;// TODO
	}

	@Override
	public void addAnnotation(final IAnnotation region) {
		// TODO
	}

	@Override
	public void removeAnnotation(final IAnnotation ann) {
		// TODO
	}

	@Override
	public void renameAnnotation(final IAnnotation ann, String name) {
		// Do nothing
	}

	@Override
	public IAnnotation getAnnotation(final String name) {
		return null;
	}

	@Override
	public void clearAnnotations() {
		// TODO
	}

	private Collection<IToolChangeListener> toolChangeListeners;
	private Map<ToolPageRole, String>       currentToolIdMap;

	@Override
	public IToolPage getCurrentToolPage(ToolPageRole role) {
		
		String id = null; 
		if(currentToolIdMap!=null)
			id = currentToolIdMap.get(role);
		if (id==null) {
			IToolPage toolPage = getEmptyTool(role);
			if(currentToolIdMap!=null)
				currentToolIdMap.put(role, toolPage.getToolId());
		}
		return actionBarManager.getToolPage(id);
	}

	protected void setCurrentToolPage(IToolPage page) {
		if (currentToolIdMap==null) currentToolIdMap = new HashMap<IToolPage.ToolPageRole, String>(7);
		currentToolIdMap.put(page.getToolPageRole(), page.getToolId());
	}
	
	public IToolPage getActiveTool() {
		IToolPage page=null;			
		// TODO FIXME 3D ??
		if (is2D()) {
			page = getCurrentToolPage(ToolPageRole.ROLE_2D);
		} else {
			page = getCurrentToolPage(ToolPageRole.ROLE_1D);
		}
		return page;
	}


	@Override
	public IToolPage getToolPage(String toolId) {
		return actionBarManager.getToolPage(toolId);
	}
	
	/**
	 * The tool system keeps a reference to all tools.
	 * 
	 * Calling this method removes this tool from the cache of tools
	 * (and leaves a new stub in its place). It then
	 * disposes the UI of the tool, if one has been created. The dispose()
	 * method of the tool will also be called.
	 */
	@Override
	public void disposeToolPage(String id) {
		if (isDisposed()) return;
		try {
			actionBarManager.disposeToolPage(id);
		} catch (Exception e) {
			logger.error("Cannot dispose tool page: "+id, e);
		}
	}
	
	@Override
	public void clearCachedTools() {
        
	}

	@Override
	public IToolPage createToolPage(String toolId) throws Exception {
		return getToolPage(toolId).cloneTool();
	}

	@Override
	public void addToolChangeListener(IToolChangeListener l) {
		if (toolChangeListeners == null)
			toolChangeListeners = new HashSet<IToolChangeListener>(7);
		toolChangeListeners.add(l);
	}

	@Override
	public void removeToolChangeListener(IToolChangeListener l) {
		if (toolChangeListeners == null)
			return;
		toolChangeListeners.remove(l);
	}

	protected void fireToolChangeListeners(final ToolChangeEvent evt) {
		if (toolChangeListeners == null)
			return;

		if (evt.getOldPage() != null)
			evt.getOldPage().deactivate();
		if (evt.getNewPage() != null)
			evt.getNewPage().activate();

		for (IToolChangeListener l : toolChangeListeners) {
			l.toolChanged(evt);
		}
	}

	protected EmptyTool getEmptyTool(ToolPageRole role) {

		EmptyTool emptyTool = new EmptyTool(role);
		emptyTool.setToolSystem(this);
		emptyTool.setPlottingSystem(this);
		emptyTool.setTitle("No tool");
		emptyTool.setPart(part);

		return emptyTool;
	}

	protected void clearRegionTool() {
		// TODO Implement to clear any region tool which the plotting system may
		// be adding if createRegion(...) has been called.
	}

	@Override
	public ILineTrace createLineTrace(String traceName) {
		// TODO
		return null;
	}

	@Override
	public IImageTrace createImageTrace(String traceName) {
		// TODO
		return null;
	}

	@Override
	public ITrace getTrace(String name) {
		// TODO
		return null;
	}

	@Override
	public void addTrace(ITrace trace) {
		// TODO
		fireTraceAdded(new TraceEvent(trace));
	}

	@Override
	public void removeTrace(ITrace trace) {
		// TODO
		fireTraceRemoved(new TraceEvent(trace));
	}

	@Override
	public void renameTrace(final ITrace trace, String name) {
		// Do nothing
	}	

	public IWorkbenchPart getPart() {
		return part;
	}

	@Override
	public boolean is2D() {
		final Collection<ITrace> traces = getTraces();
		if (traces!=null && !traces.isEmpty()) {
			for (ITrace iTrace : traces) {
				if (iTrace instanceof IImageTrace) return true;
			}
		}
		return plottingMode!=null ? plottingMode.is2D() : false;
	}

	@Override
	public void autoscaleAxes() {
		// TODO Does nothing
	}

	@Override
	public Collection<ITrace> getTraces(Class<? extends ITrace> clazz) {
		final Collection<ITrace> traces = getTraces();
		if (traces==null) return null;
		
		final Collection<ITrace> ret= new ArrayList<ITrace>();
		for (ITrace trace : traces) {
			if (clazz.isInstance(trace)) {
				ret.add(trace);
			}
		}
		
		return ret; // may be empty
	}

	@Override
	public IActionBars getActionBars() {
		return bars;
	}


	public void setFocus() {
		if (getPlotComposite()!=null) getPlotComposite().setFocus();
	}

	@Override
	public boolean isDisposed() {
		return getPlotComposite().isDisposed();
	}

	public boolean setToolVisible(final String toolId, final ToolPageRole role, final String viewId) throws Exception {
		return actionBarManager.setToolVisible(toolId, role, viewId);
	}
	
	/**
	 * Provides access to the plotting action system for those 
	 * that would prefer to fill their own actions into custom IContribtionManager(s)
	 * 
	 * 
	   We contain the action bars in an internal object
	   if the API user said they were null. This allows the API
	   user to say null for action bars and then use:
	   getPlotActionSystem().fillXXX() to add their own actions.
	 * 
	 * @return
	 */
	@Override
	public IPlotActionSystem getPlotActionSystem() {
		return this.actionBarManager;
	}
	
	/**
	 * Often used as:
	 * <code>
	 * IToolPageSystem system = (IToolPageSystem)iplottingSystem.getAdapter(IToolPageSystem.class);
	 * </code>
	 */
	public Object getAdapter(Class adapter) {
		if (adapter==IToolPageSystem.class) {
			return this;
		}
		return null;
	}


	@Override
	public boolean isActive(IWorkbenchPart activePart) {
		if (activePart instanceof MultiPageEditorPart) { 
			// Ensure that part is editor that is selected.			
			MultiPageEditorPart med = (MultiPageEditorPart)activePart;
			return med.getSelectedPage() == getPart();
		}
		return true;
	}

	/**
	 * Alternative widget to use for the UI area of a tool.
	 * Normally a tool would appear as a page but if this composite
	 * is set, all tools will be added/removed to this composite not
	 * views and pages in them.
	 * 
	 * The composite will have a tool toolbar at the top and then 
	 * add contents which fill and have a stack layout.
	 * 
	 * @param toolComposite
	 */
	public void setToolComposite(Composite toolComposite) {
		this.actionBarManager.setToolComposite(toolComposite);
	}
	
	/**
	 * Can be overridden to detect any custom cursor 
	 * set on the plot. This cursor can also be lost 
	 * if a zoom is done.
	 * 
	 * @return
	 */
	@Deprecated
	public Cursor getSelectedCursor() {
		
		return null;
	}
	
	/**
	 * Set the cursor using a custom icon on the plot.
	 * This may get cancelled if other tools are used!
	 * 
	 * @deprecated SWT Specific. Can still be used for code that does not mind SWT link.
	 */
	@Deprecated
	public void setSelectedCursor(Cursor des) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Please override for draw2d listeners.
	 * @deprecated draw2d Specific. Can still be used for code that does not mind draw2d link.
	 */
	@Deprecated
	public void addMouseMotionListener(MouseMotionListener mml) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Please override for draw2d listeners.
	 * @deprecated draw2d Specific. Can still be used for code that does not mind draw2d link.
	 */
	@Deprecated
	public void addMouseClickListener(MouseListener mcl) {
		throw new UnsupportedOperationException();
	}
	/**
	 * Please override for draw2d listeners.
	 * @deprecated draw2d Specific. Can still be used for code that does not mind draw2d link.
	 */
	@Deprecated
	public void removeMouseMotionListener(MouseMotionListener mml) {
		throw new UnsupportedOperationException();		
	}
	
	/**
	 * Please override for draw2d listeners.
	 * @deprecated draw2d Specific. Can still be used for code that does not mind draw2d link.
	 */
	@Deprecated
	public void removeMouseClickListener(MouseListener mcl) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setKeepAspect(boolean checked){
		
	}

    /**
     * Call this method to set a point. When the shift key is pressed the plotting system
     * draws a line from the 
     * @param object
 	 * @deprecated draw2d Specific. Can still be used for code that does not mind draw2d link.
    */
	@Deprecated
	public void setShiftPoint(Point object) {
		throw new UnsupportedOperationException();
	}
	
	@Deprecated
	public Point getShiftPoint() {
		throw new UnsupportedOperationException();
	}
}

package org.dawb.workbench.plotting.system.swtxy;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.csstudio.swt.xygraph.figures.Annotation;
import org.csstudio.swt.xygraph.figures.Axis;
import org.csstudio.swt.xygraph.figures.PlotArea;
import org.csstudio.swt.xygraph.figures.Trace;
import org.csstudio.swt.xygraph.undo.ZoomType;
import org.dawb.common.services.ImageServiceBean.ImageOrigin;
import org.dawb.common.ui.plot.axis.IAxis;
import org.dawb.common.ui.plot.axis.ICoordinateSystem;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.dawb.workbench.plotting.system.swtxy.selection.AbstractSelectionRegion;
import org.dawb.workbench.plotting.system.swtxy.selection.SelectionRegionFactory;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.PaletteData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegionArea extends PlotArea {

	private static final Logger logger = LoggerFactory.getLogger(RegionArea.class);
	
	protected ISelectionProvider selectionProvider;
	private final Map<String,AbstractSelectionRegion>     regions;
	private final Map<String,ImageTrace> imageTraces;
	
	private Collection<IRegionListener>     regionListeners;
	private Collection<ITraceListener>      imageTraceListeners;

	public RegionArea(XYRegionGraph xyGraph) {
		super(xyGraph);
		this.regions     = new LinkedHashMap<String,AbstractSelectionRegion>();
		this.imageTraces = new LinkedHashMap<String,ImageTrace>();	
	}

	public void setStatusLineManager(final IStatusLineManager statusLine) {
		
		if (statusLine==null) return;
		
		final NumberFormat format = new DecimalFormat("#0.0000#");
		addMouseMotionListener(new MouseMotionListener.Stub() {
			@Override
			public void mouseMoved(MouseEvent me) {
				double x = getRegionGraph().primaryXAxis.getPositionValue(me.x, false);
				double y = getRegionGraph().primaryYAxis.getPositionValue(me.y, false);
				statusLine.setMessage(format.format(x)+", "+format.format(y));
			}
		});
	}


	public void addRegion(final AbstractSelectionRegion region) {
		addRegion(region, true);
	}

	void addRegion(final AbstractSelectionRegion region, boolean fireListeners) {
		regions.put(region.getName(), region);
		region.setXyGraph(xyGraph);
		region.createContents(this);
		region.setSelectionProvider(selectionProvider);
		if (fireListeners) fireRegionAdded(new RegionEvent(region));
		clearRegionTool();
		revalidate();
	}

	public boolean removeRegion(final AbstractSelectionRegion region) {
		if (region==null) return false;
	    final AbstractSelectionRegion gone = regions.remove(region.getName());
		if (gone!=null){
			gone.remove(); // Clears up children (you can live without this
			fireRegionRemoved(new RegionEvent(gone));
			revalidate();
		}
		return gone!=null;
	}
	
	public void renameRegion(final AbstractSelectionRegion region, String name) {
	    regions.remove(region.getName());
	    region.setName(name);
	    regions.put(name, region);
	}
	
	public void clearRegions() {
		clearRegionsInternal();
		revalidate();
	}
	
    protected void clearRegionsInternal() {
		clearRegionTool();
		if (regions==null) return;
		
		final Collection<String> deleted = new HashSet<String>(5);
		for (AbstractSelectionRegion region : regions.values()) {
			if (!region.isUserRegion()) continue;
			deleted.add(region.getName());
			region.remove();
		}
		regions.keySet().removeAll(deleted);
		fireRegionsRemoved(new RegionEvent(this));

	}
	

	public ImageTrace createImageTrace(String name, Axis xAxis, Axis yAxis) {

        if (imageTraces.containsKey(name)) throw new RuntimeException("There is an image called '"+name+"' already plotted!");
        
		final ImageTrace trace = new ImageTrace(name, xAxis, yAxis);
		
		fireImageTraceCreated(new TraceEvent(trace));
		
		return trace;
	}

	/**Add a trace to the plot area.
	 * @param trace the trace to be added.
	 */
	public void addImageTrace(final ImageTrace trace){
		imageTraces.put(trace.getName(), trace);
		add(trace);
		
        toFront();		
		revalidate();
		
		fireImageTraceAdded(new TraceEvent(trace));
	}
	
	void toFront() {
		for (Annotation a : getAnnotationList()) {
			a.toFront();
		}
		// Move all regions to front again
		if (getRegionMap()!=null) for (String name : getRegionMap().keySet()) {
			try {
				getRegionMap().get(name).toFront();
			} catch (Exception ne) {
				continue;
			}
		}
	}
	
	public boolean removeImageTrace(final ImageTrace trace){
	    final ImageTrace gone = imageTraces.remove(trace.getName());
		if (gone!=null){
			trace.remove();
			fireImageTraceRemoved(new TraceEvent(trace));
			revalidate();
		}
		return gone!=null;
	}

	public void clearImageTraces() {
		if (imageTraces==null) return;
		for (ImageTrace trace : imageTraces.values()) {
			trace.remove();
			fireImageTraceRemoved(new TraceEvent(trace));
		}
		imageTraces.clear();
		revalidate();
	}

	
	@Override
	protected void layout() {
	    final Rectangle clientArea = getClientArea();
		for(ImageTrace trace : imageTraces.values()){
			if(trace != null && trace.isVisible())
				//Shrink will make the trace has no intersection with axes,
				//which will make it only repaints the trace area.
				trace.setBounds(clientArea);//.getCopy().shrink(1, 1));				
		}		
        super.layout();
	}
		
    RegionMouseListener regionListener;
    
    /**
     * Has to be set when plotting system is created.
     */
	RegionCreationLayer               regionLayer;

	/**
	 * Create region of interest
	 * @param name
	 * @param xAxis
	 * @param yAxis
	 * @param regionType
	 * @param startingWithMouseEvent
	 * @return region
	 * @throws Exception
	 */
	public AbstractSelectionRegion createRegion(String name, IAxis x, IAxis y, RegionType regionType, boolean startingWithMouseEvent) throws Exception {

		if (getRegionMap()!=null) {
			if (getRegionMap().containsKey(name)) throw new Exception("The region '"+name+"' already exists.");
		}
		
		ICoordinateSystem       coords  = new RegionCoordinateSystem(getImageTrace(), x, y);
		AbstractSelectionRegion region  = SelectionRegionFactory.createSelectionRegion(name, coords, regionType);
		if (startingWithMouseEvent) {
			xyGraph.setZoomType(ZoomType.NONE);
		    
		    // Mouse listener for region bounds
		    regionListener = new RegionMouseListener(regionLayer, this, region, region.getMinimumMousePresses(), region.getMaximumMousePresses());
		    regionLayer.setMouseListenerActive(regionListener, true);
		}

		fireRegionCreated(new RegionEvent(region));
        return region;
	}

	public void setRegionLayer(RegionCreationLayer regionLayer) {
		this.regionLayer = regionLayer;
	}

	public void disposeRegion(AbstractSelectionRegion region) {
		removeRegion(region);
		setCursor(null);
		clearRegionTool();
	}
	
	public void setZoomType(final ZoomType zoomType) {
		clearRegionTool();
        super.setZoomType(zoomType);
	}
	
	protected void clearRegionTool() {
		if (regionListener!=null) {
		    regionLayer.setMouseListenerActive(regionListener, false);
		    regionListener = null;
		    setCursor(null);
		}
	}

	/**
	 * 
	 * @param l
	 */
	public boolean addRegionListener(final IRegionListener l) {
		if (regionListeners == null) regionListeners = new HashSet<IRegionListener>(7);
		return regionListeners.add(l);
	}
	
	/**
	 * 
	 * @param l
	 */
	public boolean removeRegionListener(final IRegionListener l) {
		if (regionListeners == null) return true;
		return regionListeners.remove(l);
	}

	protected void fireRegionCreated(RegionEvent evt) {
		if (regionListeners==null) return;
		for (IRegionListener l : regionListeners) {
			try {
				l.regionCreated(evt);
			} catch (Throwable ne) {
				logger.error("Notifying of region creation", ne);
				continue;
			}
		}
	}
	

	protected void fireRegionAdded(RegionEvent evt) {
		if (regionListeners==null) return;
		for (IRegionListener l : regionListeners) {
			try {
				l.regionAdded(evt);
			} catch (Throwable ne) {
				logger.error("Notifying of region add", ne);
				continue;
			}
		}
	}
	
	protected void fireRegionRemoved(RegionEvent evt) {
		if (regionListeners==null) return;
		for (IRegionListener l : regionListeners) {
			try {
				l.regionRemoved(evt);
			} catch (Throwable ne) {
				logger.error("Notifying of region removal", ne);
				continue;
			}
		}
	}
	protected void fireRegionsRemoved(RegionEvent evt) {
		if (regionListeners==null) return;
		for (IRegionListener l : regionListeners) {
			try {
			    l.regionsRemoved(evt);
			} catch (Throwable ne) {
				logger.error("Notifying of region removal", ne);
				continue;
			}
		}
	}
	
	/**
	 * 
	 * @param l
	 */
	public boolean addImageTraceListener(final ITraceListener l) {
		if (imageTraceListeners == null) imageTraceListeners = new HashSet<ITraceListener>(7);
		return imageTraceListeners.add(l);
	}
	
	/**
	 * 
	 * @param l
	 */
	public boolean removeImageTraceListener(final ITraceListener l) {
		if (imageTraceListeners == null) return true;
		return imageTraceListeners.remove(l);
	}

	
	protected void fireImageTraceCreated(TraceEvent evt) {
		if (imageTraceListeners==null) return;
		for (ITraceListener l : imageTraceListeners) l.traceCreated(evt);
	}
	

	protected void fireImageTraceAdded(TraceEvent evt) {
		if (imageTraceListeners==null) return;
		for (ITraceListener l : imageTraceListeners) l.traceAdded(evt);
	}
	
	protected void fireImageTraceRemoved(TraceEvent evt) {
		if (imageTraceListeners==null) return;
		for (ITraceListener l : imageTraceListeners) l.traceRemoved(evt);
	}

	
	public Map<String, AbstractSelectionRegion> getRegionMap() {
		return regions;
	}
	public List<AbstractSelectionRegion> getRegions() {
		final Collection<AbstractSelectionRegion> vals = regions.values();
		return new ArrayList<AbstractSelectionRegion>(vals);
	}
	
//	private Image rawImage;
	
//	@Override
//	protected void paintClientArea(final Graphics graphics) {
	
// TODO
//		if (rawImage==null) {
//			rawImage = new Image(Display.getCurrent(), "C:/tmp/ESRF_Pilatus_Data.png");
//		}
//		
//		final Rectangle bounds = getBounds();
//		final Image scaled = new Image(Display.getCurrent(),
//				rawImage.getImageData().scaledTo(bounds.width,bounds.height));
//		graphics.drawImage(scaled, new Point(0,0));
//
//		super.paintClientArea(graphics);
//
//	}

	public Collection<String> getRegionNames() {
		return regions.keySet();
	}


	public void setSelectionProvider(ISelectionProvider provider) {
		this.selectionProvider = provider;
	}


	public AbstractSelectionRegion getRegion(String name) {
		if (regions==null) return null;
		return regions.get(name);
	}

	protected Map<String,ImageTrace> getImageTraces() {
		return this.imageTraces;
	}

  
	/**
	 * Must call in UI thread safe way.
	 */
	public void clearTraces() {
		
		final List<Trace> traceList = getTraceList();
		if (traceList!=null) {
			for (Trace trace : traceList) {
				remove(trace);
				if (trace instanceof LineTrace) ((LineTrace)trace).dispose();
			}
			traceList.clear();
	    }
		
		if (imageTraces!=null) {
			final Collection<ImageTrace> its = new HashSet<ImageTrace>(imageTraces.values());
			for (ImageTrace trace : its) {
				final ImageTrace gone = imageTraces.remove(trace.getName());
				if (gone!=null){
					trace.remove();
					fireImageTraceRemoved(new TraceEvent(trace));
				}
			}

			imageTraces.clear();

		}

	}


	public void setPaletteData(PaletteData data) {
		if (imageTraces!=null) for (ImageTrace trace : imageTraces.values()) {
			trace.setPaletteData(data);
		}
	}


	public void setImageOrigin(ImageOrigin origin) {
		if (imageTraces!=null) for (ImageTrace trace : imageTraces.values()) {
			trace.setImageOrigin(origin);
		}
	}


	public ImageTrace getImageTrace() {
		if (imageTraces!=null && imageTraces.size()>0) return imageTraces.values().iterator().next();
		return null;
	}


	public void dispose() {
		
		clearTraces();
		clearRegionsInternal();
		if (regionListeners!=null)     regionListeners.clear();
		if (imageTraceListeners!=null) imageTraceListeners.clear();
		if (regions!=null)             regions.clear();
		if (imageTraces!=null)         imageTraces.clear();
	}

	/**
	 * Call to find out of any of the current regions are user editable.
	 * @return
	 */
	public boolean hasUserRegions() {
		if (getRegionMap()==null || getRegionMap().isEmpty()) return false;
		for (String regionName : getRegionMap().keySet()) {
			if (getRegionMap().get(regionName).isUserRegion()) return true;
		}
		return false;
	}

	XYRegionGraph getRegionGraph() {
		return (XYRegionGraph)xyGraph;
	}

}

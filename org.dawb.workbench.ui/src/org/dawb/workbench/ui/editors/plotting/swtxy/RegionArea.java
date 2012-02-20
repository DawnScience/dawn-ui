package org.dawb.workbench.ui.editors.plotting.swtxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.csstudio.swt.xygraph.figures.Axis;
import org.csstudio.swt.xygraph.figures.PlotArea;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.eclipse.draw2d.Graphics;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Image;

public class RegionArea extends PlotArea {

	protected ISelectionProvider selectionProvider;
	private final Map<String,Region> regions;
	
	public RegionArea(XYRegionGraph xyGraph) {
		super(xyGraph);
		this.regions = new LinkedHashMap<String,Region>();	
	}
		

	public void addRegion(final Region region){
		regions.put(region.getName(), region);
		region.setXyGraph(xyGraph);
		region.createContents(this);
		region.setSelectionProvider(selectionProvider);
		fireRegionAdded(new RegionEvent(region));
		revalidate();
	}


	public boolean removeRegion(final Region region){
	    final Region gone = regions.remove(region.getName());
		if (gone!=null){
			region.remove();
			fireRegionRemoved(new RegionEvent(region));
			revalidate();
		}
		return gone!=null;
	}
	
	private Collection<IRegionListener> regionListeners;
	

	public Region createRegion(String name, Axis x, Axis y, RegionType regionType) throws Exception {

		if (getRegionMap()!=null) {
			if (getRegionMap().containsKey(name)) throw new Exception("The region '"+name+"' already exists.");
		}
		Region region = null;
		if (regionType==RegionType.LINE) {

			region = new LineSelection(name, x, y);

		} else if (regionType==RegionType.BOX) {

			region = new BoxSelection(name, x, y);

		} else {
			throw new NullPointerException("Cannot deal with "+regionType+" regions yet - sorry!");
		}	
		
		fireRegionCreated(new RegionEvent(region));
        return region;
	}
	
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
	
	public Map<String, Region> getRegionMap() {
		return regions;
	}
	public List<Region> getRegions() {
		final Collection<Region> vals = regions.values();
		return new ArrayList<Region>(vals);
	}
	
	private Image rawImage;
	
	@Override
	protected void paintClientArea(final Graphics graphics) {
	
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
		super.paintClientArea(graphics);

	}


	public Collection<String> getRegionNames() {
		return regions.keySet();
	}


	public void setSelectionProvider(ISelectionProvider provider) {
		this.selectionProvider = provider;
	}


	public Region getRegion(String name) {
		if (regions==null) return null;
		return regions.get(name);
	}


}

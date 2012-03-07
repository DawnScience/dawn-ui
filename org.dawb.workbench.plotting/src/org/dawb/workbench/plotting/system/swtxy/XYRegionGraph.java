package org.dawb.workbench.plotting.system.swtxy;

import java.util.List;

import org.csstudio.swt.xygraph.figures.Axis;
import org.csstudio.swt.xygraph.figures.PlotArea;
import org.csstudio.swt.xygraph.figures.XYGraph;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.eclipse.jface.viewers.ISelectionProvider;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

/**
 * This is an XYGraph which supports regions of interest.
 * @author fcp94556
 *
 */
public class XYRegionGraph extends XYGraph {
	
	protected PlotArea createPlotArea() {
        return new RegionArea(this);
	}

	public void addRegion(final Region region) {
		getRegionArea().addRegion(region);
	}
	public void removeRegion(final Region region) {
		getRegionArea().removeRegion(region);
	}
	public void setSelectionProvider(final ISelectionProvider provider) {
		getRegionArea().setSelectionProvider(provider);
	}

	public Region createRegion(String name, Axis xAxis, Axis yAxis, RegionType regionType, boolean startingWithMouseEvent) throws Exception {
		return getRegionArea().createRegion(name, xAxis, yAxis, regionType, startingWithMouseEvent);
	}
	public void disposeRegion(final Region region) {
		getRegionArea().disposeRegion(region);
	}


	public ImageTrace createImageTrace(String name, Axis xAxis, Axis yAxis, AbstractDataset image) {
		RegionArea ra = (RegionArea)getPlotArea();
		return ra.createImageTrace(name, xAxis, yAxis, image);
	}
	public void addImageTrace(final ImageTrace trace) {
		getRegionArea().addImageTrace(trace);
	}
	public void removeImageTrace(final ImageTrace trace) {
		getRegionArea().removeImageTrace(trace);
	}

	public boolean addRegionListener(IRegionListener l) {
		return getRegionArea().addRegionListener(l);
	}
	
	public boolean removeRegionListener(IRegionListener l) {
		return getRegionArea().removeRegionListener(l);
	}

	public Region getRegion(String name) {
		return getRegionArea().getRegion(name);
	}

	public void clearRegions() {
		getRegionArea().clearRegions();
	}
	public List<Region> getRegions() {
		return getRegionArea().getRegions();
	}

	public void clearRegionTool() {
		getRegionArea().clearRegionTool();
	}

	public void clearImageTraces() {
		getRegionArea().clearImageTraces();
	}
	
	protected RegionArea getRegionArea() {
		return (RegionArea)getPlotArea();
	}

	public void performAutoScale(){

		if (getRegionArea().getImageTraces()!=null && getRegionArea().getImageTraces().size()>0) {
			
			for (ImageTrace trace : getRegionArea().getImageTraces().values()) {
				trace.performAutoscale();
			}
			
			
		} else {
			super.performAutoScale();
		}
		
	}
}

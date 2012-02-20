package org.dawb.workbench.ui.editors.plotting.swtxy;

import org.csstudio.swt.xygraph.figures.Axis;
import org.csstudio.swt.xygraph.figures.PlotArea;
import org.csstudio.swt.xygraph.figures.XYGraph;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.eclipse.jface.viewers.ISelectionProvider;

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
		((RegionArea)getPlotArea()).addRegion(region);
	}
	public void removeRegion(final Region region) {
		((RegionArea)getPlotArea()).removeRegion(region);
	}
	public void setSelectionProvider(final ISelectionProvider provider) {
		((RegionArea)getPlotArea()).setSelectionProvider(provider);
	}

	public Region createRegion(String name, Axis xAxis, Axis yAxis, RegionType regionType, boolean startingWithMouseEvent) throws Exception {
		return ((RegionArea)getPlotArea()).createRegion(name, xAxis, yAxis, regionType, startingWithMouseEvent);
	}
	public void disposeRegion(final Region region) {
		((RegionArea)getPlotArea()).disposeRegion(region);
	}

	public boolean addRegionListener(IRegionListener l) {
		return ((RegionArea)getPlotArea()).addRegionListener(l);
	}
	
	public boolean removeRegionListener(IRegionListener l) {
		return ((RegionArea)getPlotArea()).removeRegionListener(l);
	}

	public Region getRegion(String name) {
		return ((RegionArea)getPlotArea()).getRegion(name);
	}

}

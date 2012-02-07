package org.dawb.workbench.ui.editors.plotting.swtxy;

import org.csstudio.swt.xygraph.figures.PlotArea;
import org.csstudio.swt.xygraph.figures.XYGraph;

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
}

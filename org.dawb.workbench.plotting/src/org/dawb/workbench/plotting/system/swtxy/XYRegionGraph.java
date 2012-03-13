package org.dawb.workbench.plotting.system.swtxy;

import java.util.Collection;
import java.util.List;

import org.csstudio.swt.xygraph.figures.Axis;
import org.csstudio.swt.xygraph.figures.Legend;
import org.csstudio.swt.xygraph.figures.PlotArea;
import org.csstudio.swt.xygraph.figures.XYGraph;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.trace.IImageTrace.ImageOrigin;
import org.dawb.workbench.plotting.Activator;
import org.dawb.workbench.plotting.preference.PlottingConstants;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.PaletteData;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

/**
 * This is an XYGraph which supports regions of interest.
 * @author fcp94556
 *
 */
public class XYRegionGraph extends XYGraph {
	
	public XYRegionGraph() {
		super();
		
		try {
		    this.showLegend = Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.XY_SHOWLEGEND);
		} catch (NullPointerException ne) {
			this.showLegend = true;
		}
	}
	
	@Override
	protected PlotArea createPlotArea(XYGraph xyGraph) {
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


	public ImageTrace createImageTrace(String name, Axis xAxis, Axis yAxis) {
		RegionArea ra = (RegionArea)getPlotArea();
		return ra.createImageTrace(name, xAxis, yAxis);
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
	
	public void setDefaultShowLegend(boolean showLeg) {
		this.showLegend = showLeg;
	}
	
	public void setShowLegend(boolean showLeg) {
		super.setShowLegend(showLeg);
		Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.XY_SHOWLEGEND, showLeg);
	}
	
	/**
	 * @return the showLegend
	 */
	public boolean isShowLegend() {
		return showLegend;
	}


	/**
	 * Call from UI thread only!
	 */
	public void clearTraces() {
		
		if (super.getLegendMap()!=null) {
			final Collection<Legend> legends = super.getLegendMap().values();
			for (Legend legend : legends) {
				legend.getTraceList().clear();
			}
			//super.getLegendMap().clear();
		}
		
		primaryXAxis.clear();
		primaryYAxis.clear();
		getRegionArea().clearTraces();
		
		revalidate();

	}

	public void setPaletteData(PaletteData data) {
		getRegionArea().setPaletteData(data);
	}

	public void setImageOrigin(ImageOrigin origin) {
		getRegionArea().setImageOrigin(origin);
	}
}

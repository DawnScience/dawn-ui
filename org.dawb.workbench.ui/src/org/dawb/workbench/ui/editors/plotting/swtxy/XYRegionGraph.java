package org.dawb.workbench.ui.editors.plotting.swtxy;

import org.csstudio.swt.xygraph.figures.PlotArea;
import org.csstudio.swt.xygraph.figures.XYGraph;

/**
 * This is an XYGraph which supports regions of interest.
 * @author fcp94556
 *
 */
public class XYRegionGraph extends XYGraph {

	private LineSelectionFigure lineSelection;
	private BoxSelectionFigure  boxSelection;
	
	protected PlotArea createPlotArea() {
        return new RegionArea(this);
	}
	
	public void setROIType(final ROIType roiType) {
		
        // TODO Get ROIType.NONE when pressing button twice!
		
		if (lineSelection!=null) lineSelection.setVisible(false);
		if (boxSelection!=null)  boxSelection.setVisible(false);

		switch (roiType) {
		
		case LINE:
			if (lineSelection==null) {
				lineSelection = new LineSelectionFigure();
				getPlotArea().add(lineSelection);
			}
			((RegionArea)getPlotArea()).setSelectionFigure(lineSelection);
			
			if (lineSelection.isActive()) lineSelection.setVisible(true);
			break;
			
		case BOX:
			if (boxSelection==null) {
				boxSelection = new BoxSelectionFigure();
				getPlotArea().add(boxSelection);
			}
			((RegionArea)getPlotArea()).setSelectionFigure(boxSelection);
			
			if (boxSelection.isActive()) boxSelection.setVisible(true);
			break;
				
		case NONE:
			if (lineSelection!=null) {
				getPlotArea().remove(lineSelection);
				lineSelection = null;
			}
			if (boxSelection!=null) {
				getPlotArea().remove(boxSelection);
				boxSelection = null;
			}
		
		}
		
		revalidate();
	}
}

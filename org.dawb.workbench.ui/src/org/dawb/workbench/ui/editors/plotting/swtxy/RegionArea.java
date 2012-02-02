package org.dawb.workbench.ui.editors.plotting.swtxy;

import org.csstudio.swt.xygraph.figures.PlotArea;
import org.eclipse.draw2d.geometry.Rectangle;

public class RegionArea extends PlotArea {

	public RegionArea(XYRegionGraph xyGraph) {
		super(xyGraph);
	}
	
	protected FixedBoundsShape selectionFigure;
	

	@Override
	protected void layout() {
		if (selectionFigure!=null) {
		    final Rectangle ca = getClientArea();
		    selectionFigure.setFixedBounds(new Rectangle(ca.x, ca.y, ca.width+100, ca.height+50));
		    if (!selectionFigure.isActive()) {
		    	selectionFigure.createActiveListener(this);
		    }
		}
		super.layout();
	}


	public FixedBoundsShape getSelectionFigure() {
		return selectionFigure;
	}


	public void setSelectionFigure(FixedBoundsShape selectionFigure) {
		this.selectionFigure = selectionFigure;
	}
	
}

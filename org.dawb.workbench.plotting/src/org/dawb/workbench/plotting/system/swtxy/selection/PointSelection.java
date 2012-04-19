package org.dawb.workbench.plotting.system.swtxy.selection;

import org.csstudio.swt.xygraph.figures.Axis;
import org.dawb.common.ui.plot.region.RegionBounds;
import org.dawb.workbench.plotting.system.swtxy.IMobileFigure;
import org.dawb.workbench.plotting.system.swtxy.translate.FigureTranslator;
import org.dawb.workbench.plotting.system.swtxy.util.Draw2DUtils;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.StructuredSelection;

import uk.ac.diamond.scisoft.analysis.roi.LinearROI;

public class PointSelection extends AbstractSelectionRegion {

	private SelectionHandle  point;
	private FigureTranslator mover;
	
	public PointSelection(String name, Axis xAxis, Axis yAxis) {
		super(name, xAxis, yAxis);
		setRegionColor(RegionType.POINT.getDefaultColor());
		setLineWidth(7);
		setAlpha(120);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.POINT;
	}

	@Override
	protected void updateConnectionBounds() {
		
	}

	@Override
	public void paintBeforeAdded(Graphics g, 
			                     PointList clicks,
			                     Rectangle parentBounds) {
		
		if (clicks.size()<1) return;
		final Point pnt    = clicks.getLastPoint();
		final int   offset = getLineWidth()/2; // int maths ok here
        g.setForegroundColor(getRegionColor());
        g.fillRectangle(pnt.x-offset, pnt.y-offset, getLineWidth(), getLineWidth());
	}

	@Override
	public void createContents(Figure parent) {
		this.point = new RectangularHandle(getxAxis(), getyAxis(), getRegionColor(), parent, getLineWidth(), 100d, 100d);
		parent.add(point);
		mover = new FigureTranslator(getXyGraph(), point);	
		mover.addTranslationListener(createRegionNotifier());
		setMobile(isMobile());
		point.setShowPosition(false);
		setRegionObjects(point);
	}
	
	@Override
	public void setMobile(final boolean mobile) {
		getBean().setMobile(mobile);
		if (mover!=null && point!=null) {
			mover.setActive(mobile);
			if (mobile) point.setCursor(Draw2DUtils.getRoiControlPointCursor()) ;
			else 	    point.setCursor(null) ; 
		}
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (point!=null) point.setVisible(visible);
		getBean().setVisible(visible);
	}

	@Override
	public void setLocalBounds(PointList clicks, Rectangle parentBounds) {
		if (clicks.size()<1) return;
		final Point last = clicks.getLastPoint();
		point.setSelectionPoint(last);
		updateRegionBounds();
	}

	@Override
	protected void fireRoiSelection() {
		if (getSelectionProvider()!=null) {
			final LinearROI roi = new LinearROI(point.getRealValue());
			getSelectionProvider().setSelection(new StructuredSelection(roi));
		}
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-point.png";
	}


	@Override
	protected RegionBounds createRegionBounds(boolean recordResult) {
		
		if (point == null) return getRegionBounds();
		
		final RegionBounds bounds = new RegionBounds();
		bounds.addPoint(point.getRealValue());
		if (recordResult) this.regionBounds = bounds;
		
		return bounds;
	}

	@Override
	protected void updateRegionBounds(RegionBounds bounds) {
		
		if (!bounds.isPoints()) throw new RuntimeException("Expected points bounds for free draw!");
		
		if (point==null) return;
		
        point.setRealValue(bounds.getPoints().iterator().next());
        
        updateConnectionBounds();
    }


}

package org.dawb.workbench.plotting.system.swtxy.selection;

import org.csstudio.swt.xygraph.figures.Axis;
import org.dawb.common.ui.plot.region.RegionBounds;
import org.dawb.workbench.plotting.system.swtxy.translate.FigureTranslator;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.StructuredSelection;

import uk.ac.diamond.scisoft.analysis.roi.LinearROI;

public class PointSelection extends AbstractSelectionRegion {

	private SelectionHandle point;
	private static final int SIZE = 5;
	
	public PointSelection(String name, Axis xAxis, Axis yAxis) {
		super(name, xAxis, yAxis);
		setRegionColor(ColorConstants.yellow);
		setLineWidth(1);
		setAlpha(100);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.POINT;
	}

	@Override
	protected void updateConnectionBounds() {
		
		final Point pnt    = point.getLocation();
		final int   offset = SIZE/2; // int maths ok here
        point.setBounds(new Rectangle(pnt.x-offset, pnt.y-offset, SIZE, SIZE));
	}

	@Override
	public void paintBeforeAdded(Graphics g, 
			                     PointList clicks,
			                     Rectangle parentBounds) {
		
		final Point pnt    = clicks.getLastPoint();
		final int   offset = SIZE/2; // int maths ok here
        g.setForegroundColor(getRegionColor());
        g.fillRectangle(pnt.x-offset, pnt.y-offset, SIZE, SIZE);
	}

	@Override
	public void createContents(Figure parent) {
		this.point = new RectangularHandle(getxAxis(), getyAxis(), getRegionColor(), parent, SIZE, 100d, 100d);
		parent.add(point);
		FigureTranslator mover = new FigureTranslator(getXyGraph(), point);	
		mover.addTranslationListener(createRegionNotifier());

		setRegionObjects(point);
	}

	@Override
	public void setLocalBounds(PointList clicks, Rectangle parentBounds) {
		final Point last = clicks.getLastPoint();
		point.setLocation(last);
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
        point.setRealValue(bounds.getPoints().iterator().next());
        
        updateConnectionBounds();
    }


}

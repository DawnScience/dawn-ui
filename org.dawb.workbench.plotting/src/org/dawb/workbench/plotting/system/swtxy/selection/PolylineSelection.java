package org.dawb.workbench.plotting.system.swtxy.selection;

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.plot.axis.ICoordinateSystem;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegionContainer;
import org.dawb.workbench.plotting.system.swtxy.translate.FigureTranslator;
import org.dawb.workbench.plotting.system.swtxy.util.Draw2DUtils;
import org.dawb.workbench.plotting.system.swtxy.util.RotatablePolylineShape;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import uk.ac.diamond.scisoft.analysis.roi.PointROI;
import uk.ac.diamond.scisoft.analysis.roi.PolylineROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;

public class PolylineSelection extends AbstractSelectionRegion {

	DecoratedPolyline pline;

	public PolylineSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(ColorConstants.cyan);
		setAlpha(80);
		setLineWidth(2);
	}

	@Override
	public void createContents(Figure parent) {
		pline = new DecoratedPolyline(parent);
		pline.setCursor(Draw2DUtils.getRoiMoveCursor());

		parent.add(pline);
		sync(getBean());
		updateROI();
		if (roi == null)
			createROI(true);
	}

	@Override
	public boolean containsPoint(double x, double y) {
		final int[] pix = coords.getValuePosition(x,y);
		return pline.containsPoint(pix[0], pix[1]);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.POLYLINE;
	}

	@Override
	protected void updateConnectionBounds() {
		if (pline != null) {
			Rectangle b = pline.updateFromHandles();
			pline.setBounds(b);
		}
	}

	@Override
	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
		g.setLineStyle(SWT.LINE_DOT);
		g.setLineWidth(2);
		g.setForegroundColor(getRegionColor());
		g.setAlpha(getAlpha());
		g.drawPolyline(clicks);
	}

	@Override
	public void setLocalBounds(PointList clicks, Rectangle parentBounds) {
		if (pline != null) {
			pline.setPoints(clicks);
			setRegionColor(getRegionColor());
			setOpaque(false);
			setAlpha(getAlpha());
			updateConnectionBounds();
			createROI(true);
			fireROIChanged(getROI());
		}
	}

	@Override
	protected String getCursorPath() {
		return "icons/Cursor-polyline.png";
	}

	@Override
	protected ROIBase createROI(boolean recordResult) {
		final PointList pl = pline.getPoints();
		final PolylineROI proi = new PolylineROI();
		for (int i = 0, imax = pl.size(); i < imax; i++) {
			Point p = pl.getPoint(i);
			proi.insertPoint(i, coords.getPositionValue(p.x(),p.y()));
		}
		if (recordResult)
			roi = proi;

		return proi;
	}

	@Override
	protected void updateROI(ROIBase roi) {
		if (roi instanceof PolylineROI) {
			if (pline == null)
				return;

			pline.updateFromROI((PolylineROI) roi);

			updateConnectionBounds();
		}
	}

	@Override
	public int getMaximumMousePresses() {
		return 0; // signifies unlimited presses
	}

	class DecoratedPolyline extends RotatablePolylineShape implements IRegionContainer {
		List<IFigure> handles;
		private Figure parent;
		private static final int SIDE = 8;

		public DecoratedPolyline(Figure parent) {
			super();
			handles = new ArrayList<IFigure>();
			this.parent = parent;
		}

		@Override
		public void setPoints(PointList points) {
			super.setPoints(points);
			FigureListener listener = new FigureListener() {
				@Override
				public void figureMoved(IFigure source) {
					parent.repaint();
				}
			};

			FigureTranslator mover;
			final Point p = new Point();
			for (int i = 0, imax = points.size(); i < imax; i++) {
				points.getPoint(p, i);
				RectangularHandle h = new RectangularHandle(coords, getRegionColor(), this, SIDE, p.preciseX(), p.preciseY());
				parent.add(h);
				mover = new FigureTranslator(getXyGraph(), h);
				mover.addTranslationListener(createRegionNotifier());
				h.addFigureListener(listener);
				handles.add(h);
			}

			addFigureListener(listener);
			mover = new FigureTranslator(getXyGraph(), parent, this, handles);
			mover.addTranslationListener(createRegionNotifier());
			setRegionObjects(this, handles);
		}

		private Rectangle updateFromHandles() {
			int i = 0;
			Rectangle b = null;
			for (IFigure f : handles) { // this is called first so update points
				if (f instanceof SelectionHandle) {
					SelectionHandle h = (SelectionHandle) f;
					setPoint(h.getSelectionPoint(), i++);
					if (b == null) {
						b = new Rectangle(h.getBounds());
					} else {
						b.union(h.getBounds());
					}
				}
			}
			return b;
		}

		/**
		 * Update according to ROI
		 * @param sroi
		 */
		public void updateFromROI(PolylineROI proi) {
			final PointList pl = getPoints();
			final int imax = handles.size();
			if (imax != proi.getNumberOfPoints())
				return;

			for (int i = 0; i < imax; i++) {
				PointROI p = proi.getPoint(i);
				int[] pnt  = coords.getValuePosition(p.getPoint());
				Point np = new Point(pnt[0], pnt[1]);
				pl.setPoint(np, i);
				SelectionHandle h = (SelectionHandle) handles.get(i);
				h.setSelectionPoint(np);
			}
		}

		@Override
		public IRegion getRegion() {
			return PolylineSelection.this;
		}

		@Override
		public void setRegion(IRegion region) {
		}
	}
}

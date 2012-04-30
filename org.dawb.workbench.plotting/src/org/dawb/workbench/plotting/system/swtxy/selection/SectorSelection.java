package org.dawb.workbench.plotting.system.swtxy.selection;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.swt.xygraph.figures.Axis;
import org.dawb.workbench.plotting.system.swtxy.translate.FigureTranslator;
import org.dawb.workbench.plotting.system.swtxy.util.Draw2DUtils;
import org.dawb.workbench.plotting.system.swtxy.util.Sector;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;

import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

public class SectorSelection extends AbstractSelectionRegion {

	DecoratedSector sector;

	public SectorSelection(String name, Axis xAxis, Axis yAxis) {
		super(name, xAxis, yAxis);
		setRegionColor(ColorConstants.cyan);
		setAlpha(80);
		setLineWidth(2);
	}

	@Override
	public void createContents(Figure parent) {
		sector = new DecoratedSector(parent);
		sector.setCursor(Draw2DUtils.getRoiMoveCursor());

		parent.add(sector);
		sync(getBean());
		updateROI();
		if (roi == null)
			createROI(true);
	}

	@Override
	public RegionType getRegionType() {
		return RegionType.SECTOR;
	}

	@Override
	protected void updateConnectionBounds() {
		if (sector != null) {
			Rectangle b = sector.getUpdatedBounds();
			if (b != null)
				sector.setBounds(b);
		}
	}

	@Override
	public void paintBeforeAdded(Graphics g, PointList clicks, Rectangle parentBounds) {
		if (clicks.size() < 2)
			return;

		g.setLineStyle(SWT.LINE_DOT);
		g.setForegroundColor(getRegionColor());
		g.setAlpha(getAlpha());

		final Point cen = clicks.getFirstPoint();
		Point inn = clicks.getPoint(1);
		Dimension rd = inn.getDifference(cen);
		final double ri = Math.hypot(rd.preciseWidth(), rd.preciseHeight());
		if (clicks.size() == 2) {
			g.drawOval((int) (cen.preciseX() - ri), (int) (cen.preciseY() - ri), (int) (2*ri), (int) (2*ri));
		} else {
			double as = Math.toDegrees(Math.atan2(-rd.preciseHeight(), rd.preciseWidth()));
			Point out = clicks.getPoint(2);
			rd = out.getDifference(cen);
			final double ro = Math.hypot(rd.preciseWidth(), rd.preciseHeight());
			double ae = Math.toDegrees(Math.atan2(-rd.preciseHeight(), rd.preciseWidth()));
			double[] a = calcAngles(as, ae);
			Sector s = new Sector(cen.preciseX(), cen.preciseY(), ri,  ro, a[0], a[1]);
			s.setLineStyle(SWT.LINE_DOT);
			s.setLineWidth(getLineWidth());
			s.paintFigure(g);
		}
	}

	private Boolean clockwise = null;
	private double[] calcAngles(double anglea, double angleb) {
		if (anglea < 0)
			anglea += 360;
		if (angleb < 0)
			angleb += 360;
		if (clockwise == null) {
			if (anglea == 0) {
				clockwise = angleb > 180;
			} else {
				clockwise = anglea > angleb;
			}
		}

		double l;
		if (clockwise) {
			if (anglea < 180) {
				if (angleb < 180) {
					l = angleb - anglea;
					if (l > 0)
						l -= 360;
				} else
					l = angleb - 360 - anglea;
			} else {
				if (angleb < 180) {
					l = angleb - anglea;
				} else {
					l = angleb - anglea;
					if (l > 0)
						l -= 360;
				}
			}
		} else {
			if (anglea < 180) {
				if (angleb < 180) {
					l = angleb - anglea;
					if (l < 0)
						l += 360;
				} else
					l = angleb - anglea;
			} else {
				if (angleb < 180)
					l = angleb - anglea + 360;
				else {
					l = angleb - anglea;
					if (l < 0)
						l += 360;
				}
			}
		}
		return new double[] {anglea, (anglea + l)};
	}

	@Override
	public void setLocalBounds(PointList clicks, Rectangle parentBounds) {
		if (sector != null) {
			sector.setPoints(clicks);
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
		return "icons/Cursor-sector.png";
	}

	@Override
	protected ROIBase createROI(boolean recordResult) {
		final Axis xa = getXAxis();
		final Axis ya = getYAxis();
		final Point c = sector.getCentre();
		final double[] r = sector.getRadii();
		final double[] a = sector.getAnglesDegrees();
		final double x = xa.getPositionValue(c.x(), false);
		final double y = ya.getPositionValue(c.y(), false);
		final SectorROI sroi = new SectorROI(x, y, xa.getPositionValue((int) (c.preciseX() + r[0]), false) - x,
				ya.getPositionValue((int) (c.preciseY() + r[1]), false) - y,
				Math.toRadians(360 - a[1]), Math.toRadians(360 - a[0]));
		if (recordResult)
			roi = sroi;

		return sroi;
	}

	@Override
	protected void updateROI(ROIBase roi) {
		if (roi instanceof SectorROI) {
			if (sector == null)
				return;

			sector.updateROI((SectorROI) roi);

			updateConnectionBounds();
		}
	}

	@Override
	public int getMaximumMousePresses() {
		return 3; // signifies unlimited presses
	}

	class DecoratedSector extends Sector {
		List<IFigure> handles;
		private Figure parent;
		private static final int SIDE = 8;

		public DecoratedSector(Figure parent) {
			super();
			handles = new ArrayList<IFigure>();
			this.parent = parent;
		}

		public void setPoints(PointList points) {
			final Point cen = points.getFirstPoint();
			Point inn = points.getPoint(1);
			Dimension rd = inn.getDifference(cen);
			final double ri = Math.hypot(rd.preciseWidth(), rd.preciseHeight());

			double as = Math.toDegrees(Math.atan2(-rd.preciseHeight(), rd.preciseWidth()));
			Point out = points.getPoint(2);
			rd = out.getDifference(cen);
			final double ro = Math.hypot(rd.preciseWidth(), rd.preciseHeight());
			double ae = Math.toDegrees(Math.atan2(-rd.preciseHeight(), rd.preciseWidth()));
			double[] a = calcAngles(as, ae);
			setCentre(cen.preciseX(), cen.preciseY());
			if (ri < ro)
				setRadii(ri,  ro);
			else
				setRadii(ro,  ri);
			setAnglesDegrees(a[0], a[1]);
			FigureListener listener = new FigureListener() {
				@Override
				public void figureMoved(IFigure source) {
					parent.repaint();
				}
			};

			FigureTranslator mover;
			RectangularHandle h = new RectangularHandle(getXAxis(), getYAxis(), getRegionColor(), this, SIDE, cen.preciseX(), cen.preciseY());
			parent.add(h);
			mover = new FigureTranslator(getXyGraph(), h);
			mover.addTranslationListener(createRegionNotifier());
			h.addFigureListener(listener);
			handles.add(h);

//			for (int i = 0, imax = points.size(); i < imax; i++) {
//				Point p = points.getPoint(i);
//				RectangularHandle h = new RectangularHandle(getXAxis(), getYAxis(), getRegionColor(), this, SIDE, p.preciseX(), p.preciseY());
//				parent.add(h);
//				mover = new FigureTranslator(getXyGraph(), h);
//				mover.addTranslationListener(createRegionNotifier());
//				h.addFigureListener(listener);
//				handles.add(h);
//			}
//
			addFigureListener(listener);
			mover = new FigureTranslator(getXyGraph(), parent, this, handles);
			mover.addTranslationListener(createRegionNotifier());
			setRegionObjects(this, handles);
		}

		private Rectangle getUpdatedBounds() {
//			int i = 0;
			Rectangle b = null;
			if (handles.size() > 0) {
				IFigure f = handles.get(0);
				if (f instanceof SelectionHandle) {
					SelectionHandle h = (SelectionHandle) f;
					Point p = h.getSelectionPoint();
					setCentre(p.preciseX(), p.preciseY());
				}
			}
//			for (IFigure f : handles) { // this is called first so update points
//				if (f instanceof SelectionHandle) {
//					SelectionHandle h = (SelectionHandle) f;
//					setPoint(h.getSelectionPoint(), i++);
//					if (b == null) {
//						b = new Rectangle(h.getBounds());
//					} else {
//						b.union(h.getBounds());
//					}
//				}
//			}
			b = getBounds();
			
			return b;
		}

		public void updateROI(SectorROI sroi) {
			final double x = sroi.getPointX();
			final double y = sroi.getPointY();
			final double[] r = sroi.getRadii();
			final double[] a = sroi.getAnglesDegrees();
			final Axis xa = getXAxis();
			final Axis ya = getYAxis();

			final double cx = xa.getValuePosition(x, false);
			final double cy = ya.getValuePosition(y, false);
			setCentre(cx, cy);
			setRadii(xa.getValuePosition(x + r[0], false) - cx, ya.getValuePosition(y + r[1], false) - cy);
			setAnglesDegrees(360-a[1], 360-a[0]);

			if (handles.size() > 0) {
				IFigure f = handles.get(0);
				if (f instanceof SelectionHandle) {
					SelectionHandle h = (SelectionHandle) f;
					h.setSelectionPoint(getCentre());
				}
			}

//			final PointList pl = getPoints();
//			final int imax = handles.size();
//			if (imax != proi.getSides())
//				return;
//
//			final Axis xa = getXAxis();
//			final Axis ya = getYAxis();
//			for (int i = 0; i < imax; i++) {
//				PointROI p = proi.getPoint(i);
//				Point np = new Point(xa.getValuePosition(p.getPointX(), false), ya.getValuePosition(p.getPointY(), false));
//				pl.setPoint(np, i);
//				SelectionHandle h = (SelectionHandle) handles.get(i);
//				h.setSelectionPoint(np);
//			}
		}

		@Override
		protected void fillShape(Graphics graphics) {
			super.fillShape(graphics);
		}

		@Override
		protected void outlineShape(Graphics graphics) {
			super.outlineShape(graphics);
			for (IFigure f : handles) {
				f.paint(graphics);
			}
		}
	}
}

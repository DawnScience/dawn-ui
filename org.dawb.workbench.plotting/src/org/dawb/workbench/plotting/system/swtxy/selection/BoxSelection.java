package org.dawb.workbench.plotting.system.swtxy.selection;

import java.util.Arrays;

import org.csstudio.swt.xygraph.figures.Axis;
import org.dawb.common.ui.plot.region.RegionBounds;
import org.dawb.workbench.plotting.system.swtxy.translate.FigureTranslator;
import org.dawb.workbench.plotting.system.swtxy.translate.TranslationEvent;
import org.dawb.workbench.plotting.system.swtxy.translate.TranslationListener;
import org.dawb.workbench.plotting.system.swtxy.util.Draw2DUtils;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;


/**
 *     p1------------p2
 *     |              |
 *     |              |
 *     p3------------p4
 *     
 * @author fcp94556
 */
class BoxSelection extends AbstractSelectionRegion {
		
	private static final int SIDE      = 8;
	
	private SelectionHandle p1, p2, p3, p4;

	private Figure connection;
	
	BoxSelection(String name, Axis xAxis, Axis yAxis) {
		super(name, xAxis, yAxis);
		setRegionColor(ColorConstants.green);	
		setAlpha(80);
	}

	public void createContents(final Figure parent) {
		
     	this.p1  = createSelectionRectangle(getRegionColor(), SIDE, 100, 100);
		this.p2  = createSelectionRectangle(getRegionColor(), SIDE, 200, 100);
		this.p3  = createSelectionRectangle(getRegionColor(), SIDE, 100, 200);
		this.p4  = createSelectionRectangle(getRegionColor(), SIDE, 200, 200);
				
		this.connection = new RegionFillFigure() {
			@Override
			public void paintFigure(Graphics gc) {
				super.paintFigure(gc);
				final Rectangle size = getRectangleFromVertices();				
				this.bounds = size;
				gc.setAlpha(getAlpha());
				gc.fillRectangle(size);
				
				BoxSelection.this.drawLabel(gc, size);
			}
		};
		connection.setCursor(Draw2DUtils.getRoiMoveCursor());
		connection.setBackgroundColor(getRegionColor());
		connection.setBounds(new Rectangle(p4.getSelectionPoint(), p1.getSelectionPoint()));
		connection.setOpaque(false);
  		
		parent.add(connection);
		parent.add(p1);
		parent.add(p2);
		parent.add(p3);
		parent.add(p4);
				
		FigureTranslator mover = new FigureTranslator(getXyGraph(), parent, connection, Arrays.asList(new IFigure[]{p1,p2,p3,p4}));
		// Add a translation listener to be notified when the mover will translate so that
		// we do not recompute point locations during the move.
		mover.addTranslationListener(new TranslationListener() {
			@Override
			public void translateBefore(TranslationEvent evt) {
				isCalculateCorners = false;
			}

			@Override
			public void translationAfter(TranslationEvent evt) {
				isCalculateCorners = true;
				updateConnectionBounds();
				fireRegionBoundsDragged(createRegionBounds(false));
			}

			@Override
			public void translationCompleted(TranslationEvent evt) {
				fireRegionBoundsChanged(createRegionBounds(true));
				fireRoiSelection();
			}

		});
		
		setRegionObjects(connection, p1, p2, p3, p4);
		sync(getBean());
        updateRegionBounds();
        if (regionBounds==null) createRegionBounds(true);

	}
	
	public void paintBeforeAdded(final Graphics gc, Point firstClick, Point dragLocation, Rectangle parentBounds) {
		gc.setLineStyle(SWT.LINE_DOT);
		final Rectangle bounds = new Rectangle(firstClick, dragLocation);
		gc.drawRectangle(bounds);
		gc.setBackgroundColor(getRegionColor());
		gc.setAlpha(getAlpha());
		gc.fillRectangle(bounds);
	}

	
	protected String getCursorPath() {
		return "icons/Cursor-box.png";
	}
	
	private boolean isCalculateCorners = true;

	private SelectionHandle createSelectionRectangle(Color color, int size, double... location) {
		
		SelectionHandle rect = new RectangularHandle(getxAxis(), getyAxis(), color, connection, size, location);
		FigureTranslator mover = new FigureTranslator(getXyGraph(), rect);	
		mover.addTranslationListener(createRegionNotifier());

		rect.addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure source) {
				if (!isCalculateCorners) return;
	
				try {
					isCalculateCorners = false;
					SelectionHandle a=null, b=null, c=null, d=null;
					if (source == p1 || source == p4) {
						a   = p1;   b   = p4;   c   = p2;   d   = p3;
	
					} else if (source == p2 || source == p3) {
						a   = p2;   b   = p3;   c   = p1;   d   = p4;
					} else {
						return;
					}
				
					Point pa   = a.getSelectionPoint();
					Point pb   = b.getSelectionPoint();
					Rectangle sa = new Rectangle(pa, pb);
					
					boolean quad1Or4 = ((pa.x<pb.x && pa.y<pb.y) || (pa.x>pb.x&&pa.y>pb.y));
					setCornerLocation(c, d, sa, quad1Or4);
 
				} finally {
					isCalculateCorners = true;
				}
			}

		});
		return rect;
	}
	
	protected void fireRoiSelection() {
		final double[] r1 = p1.getRealValue();
		final double[] r2 = p2.getRealValue();
		final double[] r4 = p4.getRealValue();
		
		// TODO Are we really going to rewrite all of the stuff that does not work?
		final RectangularROI roi = new RectangularROI(r1[0], r1[1], r2[0]-r1[0], r4[1]-r1[1], 0);
		if (getSelectionProvider()!=null) getSelectionProvider().setSelection(new StructuredSelection(roi));
	}

	private void setCornerLocation( SelectionHandle c,
			SelectionHandle d, 
									Rectangle sa, 
									boolean quad1Or4) {
		if (quad1Or4) {
			c.setSelectionPoint(new Point(sa.x+sa.width, sa.y));
			d.setSelectionPoint(new Point(sa.x,          sa.y+sa.height));
		} else {
			c.setSelectionPoint(new Point(sa.x,          sa.y));
			d.setSelectionPoint(new Point(sa.x+sa.width, sa.y+sa.height));
		}
		connection.repaint();
	}

	protected Rectangle getRectangleFromVertices() {
		final Point loc1   = p1.getSelectionPoint();
		final Point loc4   = p4.getSelectionPoint();
		Rectangle size = new Rectangle(loc1, loc4);
		return size;
	}


	public RegionBounds createRegionBounds(boolean recordResult) {
		if (p1!=null) {
			final Rectangle rect = getRectangleFromVertices();
			double[] a1 = new double[]{getxAxis().getPositionValue(rect.x, false), getyAxis().getPositionValue(rect.y, false)};
			double[] a2 = new double[]{getxAxis().getPositionValue(rect.x+rect.width, false), getyAxis().getPositionValue(rect.y+rect.height, false)};
			
			final RegionBounds bounds = new RegionBounds(a1, a2);
			if (recordResult) this.regionBounds = bounds;
			return bounds;
		}
		return super.getRegionBounds();
	}
	
	protected void updateRegionBounds(RegionBounds bounds) {
		
		if (p1!=null) p1.setRealValue(bounds.getP1());
		if (p4!=null) p4.setRealValue(bounds.getP2());
		updateConnectionBounds();
	}
	/**
	 * Sets the local in local coordinates
	 * @param bounds
	 */
	public void setLocalBounds(Point firstClick, Point dragLocation, Rectangle parentBounds) {
		if (p1!=null)   p1.setSelectionPoint(firstClick);
		if (p4!=null)   p4.setSelectionPoint(dragLocation);
		updateConnectionBounds();
		createRegionBounds(true);
		fireRegionBoundsChanged(getRegionBounds());
	}

	protected void updateConnectionBounds() {
		if (connection==null) return;
		final Rectangle size = getRectangleFromVertices();				
		connection.setBounds(size);
	}
	
	@Override
	public RegionType getRegionType() {
		return RegionType.BOX;
	}

}

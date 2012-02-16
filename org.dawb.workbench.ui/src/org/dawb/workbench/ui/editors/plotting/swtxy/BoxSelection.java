package org.dawb.workbench.ui.editors.plotting.swtxy;

import java.util.Arrays;

import org.csstudio.swt.xygraph.figures.Trace;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureListener;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Color;

import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;


/**
 *     p1------------p2
 *     |              |
 *     |              |
 *     p3------------p4
 */
public class BoxSelection extends Region {
		
	private static final int SIDE      = 8;
	
	private SelectionRectangle p1,  p2, p3, p4;

	private Figure connection;

	private Figure parent;
	
	public BoxSelection(String name, Trace trace) {
		super(name, trace);
	}

	public void createContents(final Figure parent) {
		
		this.parent = parent;
    	this.p1  = createSelectionRectangle(getRegionColor(),  new Point(100,100),  SIDE);
		this.p2  = createSelectionRectangle(getRegionColor(),  new Point(200,100),  SIDE);
		this.p3  = createSelectionRectangle(getRegionColor(),  new Point(100,200),  SIDE);
		this.p4  = createSelectionRectangle(getRegionColor(),  new Point(200,200),  SIDE);
				
		this.connection = new Figure() {
			@Override
			public void paintFigure(Graphics gc) {
				super.paintFigure(gc);
				final Rectangle size = getRectangleFromVertices();				
				this.bounds = size;
				gc.setAlpha(80);
				gc.fillRectangle(size);
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
				
		FigureMover mover = new FigureMover(getTrace().getXYGraph(), parent, connection, Arrays.asList(new IFigure[]{p1,p2,p3,p4}));
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
				final Rectangle size = getRectangleFromVertices();				
				connection.setBounds(size);
				fireRoiSelection();
			}

		});
		
		p1.setShowPosition(isShowPosition());
		p2.setShowPosition(isShowPosition());
		p3.setShowPosition(isShowPosition());
		p4.setShowPosition(isShowPosition());

	}
	
	public void remove() {
		parent.remove(connection);
		parent.remove(p1);
		parent.remove(p2);
		parent.remove(p3);
		parent.remove(p4);
	}

	public void setShowPosition(boolean showPosition) {
		if (p1!=null) p1.setShowPosition(showPosition);
		if (p2!=null) p2.setShowPosition(showPosition);
		if (p3!=null) p3.setShowPosition(showPosition);
		if (p4!=null) p4.setShowPosition(showPosition);
		super.setShowPosition(showPosition);
	}
	
	public void setTrace(Trace trace) {
		if (p1!=null) p1.setTrace(trace);
		if (p2!=null) p2.setTrace(trace);
		if (p3!=null) p3.setTrace(trace);
		if (p4!=null) p4.setTrace(trace);
		super.setTrace(trace);
	}

	public void setRegionColor(Color color) {
		if (p1!=null) p1.setColor(color);
		if (p2!=null) p2.setColor(color);
		if (p3!=null) p3.setColor(color);
		if (p4!=null) p4.setColor(color);
		if (connection!=null) connection.setBackgroundColor(color);
		super.setRegionColor(color);
	}
	
	private boolean isCalculateCorners = true;

	private SelectionRectangle createSelectionRectangle(Color color, Point location, int size) {
		
		SelectionRectangle rect = new SelectionRectangle(getTrace(), color,  location, size);
		new FigureMover(getTrace().getXYGraph(), rect);	
		
		rect.addFigureListener(new FigureListener() {
			@Override
			public void figureMoved(IFigure source) {
				if (!isCalculateCorners) return;
	
				try {
					isCalculateCorners = false;
					SelectionRectangle a=null, b=null, c=null, d=null;
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
 
					fireRoiSelection();

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

	private void setCornerLocation( SelectionRectangle c,
									SelectionRectangle d, 
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

}

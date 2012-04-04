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
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;


/**

                      ,,ggddY""""Ybbgg,,
                 ,agd""'    |         `""bg,
              ,gdP"         | 3           "Ybg,
            ,dP"            |                "Yb,
          ,dP"         _,,dd|"""Ybb,,_         "Yb,
         ,8"         ,dP"'  | |    `"Yb,         "8,
        ,8'        ,d"      | |2       "b,        `8,
       ,8'        d"        | |          "b        `8,
       d'        d'        ,gPPRg,        `b        `b
       8         8        dP'   `Yb        8         8
       8         8        8)  1  (8        8         8
       8         8        Yb     dP        8         8
       8         Y,        "8ggg8"        ,P         8
       Y,         Ya                     aP         ,P
       `8,         "Ya                 aP"         ,8'
        `8,          "Yb,_         _,dP"          ,8'
         `8a           `""YbbgggddP""'           a8'
          `Yba                                 adP'
            "Yba                             adY"
              `"Yba,                     ,adP"'
                 `"Y8ba,             ,ad8P"'
                      ``""YYbaaadPP""''
 *         
 *     
 *    1. Center 
 *    2. Inner Radius
 *    3. Outer Radius
 *    
 *    Currently the inner radius and outer radius can only be modified programmatically.
 *     
 * @author fcp94556
 */
class RingSelection extends AbstractSelectionRegion {
		
	private static final int SIDE      = 8;
	
	private SelectionRectangle center;

	private Figure    connection;
	private int       innerRad, outerRad;
	
	RingSelection(String name, Axis xAxis, Axis yAxis) {
		super(name, xAxis, yAxis);
		setRegionColor(ColorConstants.yellow);	
		setAlpha(80);
	}

	public void createContents(final Figure parent) {
		
     	this.center = new SelectionRectangle(getxAxis(), getyAxis(), getRegionColor(), new Point(100,100),  SIDE);
     	center.setCursor(null);
     	
		this.connection = new RegionFillFigure() {
			@Override
			public void paintFigure(Graphics gc) {
				super.paintFigure(gc);

				// We get a bound half the size of the 
				// two rectangles and then draw a ring
				gc.setAlpha(getAlpha());
				gc.setLineWidth(outerRad-innerRad);
				gc.setForegroundColor(getRegionColor());
				
				final Point     cen = center.getSelectionPoint();
				final Rectangle out = new Rectangle(new Point(cen.x-outerRad, cen.y-outerRad), new Point(cen.x+outerRad, cen.y+outerRad));
				final Rectangle in  = new Rectangle(new Point(cen.x-innerRad, cen.y-innerRad), new Point(cen.x+innerRad, cen.y+innerRad));
				final Point     tl  = (new Rectangle(out.getTopLeft(),     in.getTopLeft())).getCenter();
				final Point     br  = (new Rectangle(out.getBottomRight(), in.getBottomRight())).getCenter();
				final Rectangle mid = new Rectangle(tl, br);
				gc.drawOval(mid); 
				RingSelection.this.drawLabel(gc, mid);
			}
		};
		connection.setCursor(Draw2DUtils.getRoiMoveCursor());
		connection.setBackgroundColor(getRegionColor());
		connection.setBounds(new Rectangle(0,0,100,100));
		connection.setOpaque(false);
  		
		parent.add(connection);
		parent.add(center);
				
		FigureTranslator mover = new FigureTranslator(getXyGraph(), parent, connection, Arrays.asList(new IFigure[]{center, connection}));
		// Add a translation listener to be notified when the mover will translate so that
		// we do not recompute point locations during the move.
		mover.addTranslationListener(new TranslationListener() {
			@Override
			public void translateBefore(TranslationEvent evt) {
			}

			@Override
			public void translationAfter(TranslationEvent evt) {
				updateConnectionBounds();
				fireRegionBoundsDragged(createRegionBounds(false));
			}

			@Override
			public void translationCompleted(TranslationEvent evt) {
				fireRegionBoundsChanged(createRegionBounds(true));
				fireRoiSelection();
			}

		});
		
		setRegionObjects(connection, center);
		sync(getBean());
        updateRegionBounds();
        if (regionBounds==null) createRegionBounds(true);
        
	}
	
	public void paintBeforeAdded(final Graphics gc, Point cen, Point dragLocation, Rectangle parentBounds) {
		gc.setLineStyle(SWT.LINE_DOT);

		int diff = (int)Math.round(cen.getDistance(dragLocation));
		Rectangle bounds = new Rectangle(new Point(cen.x-diff, cen.y-diff), new Point(cen.x+diff, cen.y+diff));
		gc.drawOval(bounds);

		gc.setLineWidth(diff/4);
		diff = Math.round(0.875f*diff);
		bounds = new Rectangle(new Point(cen.x-diff, cen.y-diff), new Point(cen.x+diff, cen.y+diff));
		gc.setLineStyle(SWT.LINE_SOLID);
		gc.setForegroundColor(getRegionColor());
		gc.setAlpha(getAlpha());
		gc.drawOval(bounds);
	
	}

	
	protected String getCursorPath() {
		return "icons/Cursor-circle.png";
	}
	
	protected void fireRoiSelection() {
		final double[] r1 = center.getRealValue();
		//TODO SectorROI I think...
//		final RectangularROI roi = new RectangularROI(r1[0], r1[1], r2[0]-r1[0], r4[1]-r1[1], 0);
//		if (getSelectionProvider()!=null) getSelectionProvider().setSelection(new StructuredSelection(roi));
	}

	public RegionBounds createRegionBounds(boolean recordResult) {
		if (center!=null) {
			final Point     cen = center.getSelectionPoint();
			final Rectangle out = new Rectangle(new Point(cen.x+outerRad, cen.y+outerRad), new Point(cen.x-outerRad, cen.y-outerRad));
			final Rectangle in  = new Rectangle(new Point(cen.x+innerRad, cen.y+innerRad), new Point(cen.x-innerRad, cen.y-innerRad));
			
			double[] rcen = new double[]{getxAxis().getPositionValue(cen.x, false), getyAxis().getPositionValue(cen.y, false)};
			double cenY   = getyAxis().getPositionValue(cen.y, false);
			double inRad  = getyAxis().getPositionValue(in.getTop().y,  false)-cenY;
			double outRad = getyAxis().getPositionValue(out.getTop().y, false)-cenY;
			final RegionBounds bounds = new RegionBounds(rcen, Math.min(inRad, outRad) , Math.max(inRad, outRad));
			if (recordResult) this.regionBounds = bounds;
			return bounds;
		}
		return super.getRegionBounds();
	}
	
	protected void updateRegionBounds(RegionBounds bounds) {
		
		if (!bounds.isCircle()) throw new RuntimeException("Expected circular bounds for circle!");
		if (center!=null) {
			center.setRealValue(bounds.getCenter());
			int cenY = getxAxis().getValuePosition(bounds.getCenter()[1], false);
			innerRad = getxAxis().getValuePosition(bounds.getCenter()[1]+bounds.getInner(), false)-cenY;
			outerRad = getxAxis().getValuePosition(bounds.getCenter()[1]+bounds.getOuter(), false)-cenY;
		}

		updateConnectionBounds();
	}
	/**
	 * Sets the local in local coordinates
	 * @param bounds
	 */
	public void setLocalBounds(Point cen, Point dragLocation, Rectangle parentBounds) {
		
		int diff = (int)Math.round(cen.getDistance(dragLocation));
		if (center!=null)   {
			center.setSelectionPoint(cen);
			
			this.outerRad = diff;
            this.innerRad = Math.round(0.75f*diff);
		}
		
		updateConnectionBounds();
		createRegionBounds(true);
		fireRegionBoundsChanged(getRegionBounds());
	}

	protected void updateConnectionBounds() {
		if (connection==null) return;
		final Point     cen = center.getSelectionPoint();
		final Rectangle out = new Rectangle(new Point(cen.x-(2*outerRad), cen.y-(2*outerRad)), new Point(cen.x+(2*outerRad), cen.y+(2*outerRad)));
		connection.setBounds(out);
	}
	
	@Override
	public RegionType getRegionType() {
		return RegionType.RING;
	}

}

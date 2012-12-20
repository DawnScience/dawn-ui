package org.dawb.workbench.plotting.system.swtxy.selection;

import org.dawb.common.ui.plot.axis.ICoordinateSystem;
import org.dawb.common.ui.plot.region.IRegion;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

import uk.ac.diamond.scisoft.analysis.roi.GridROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;


/**
 *     A BoxSelection with grid or dots
 * 
 *     p1------------p2
 *     |  o o o o o   |
 *     |  o o o o o   |
 *     p3------------p4
 *     
 * @author fcp94556
 */
class GridSelection extends BoxSelection {
		
	private Color pointColor = ColorConstants.white;
	
	GridSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
		setRegionColor(IRegion.RegionType.GRID.getDefaultColor());	
		setAlpha(80);
	}
	
	@Override
	protected String getCursorPath() {
		return "icons/Cursor-grid.png";
	}
	
	protected Figure createRegionFillFigure() {
		return new RegionFillFigure(this) {
			@Override
			public void paintFigure(Graphics gc) {
				
				super.paintFigure(gc);
				final Rectangle size = getRectangleFromVertices();				
				this.bounds = size;
				gc.setAlpha(getAlpha());
				gc.fillRectangle(size);
				
				GridSelection.this.drawLabel(gc, size);
				
				if (getROI()!=null && getROI() instanceof GridROI) {
				    GridROI groi = (GridROI)createROI(false);
					if (groi.isMidPointOn()) {
						double[][] points = getGridPoints(groi);
						double[] xpoints = points[0];
						double[] ypoints = points[1];
                        for (int i = 0; i < Math.min(xpoints.length, ypoints.length); i++) {
                        	drawMidPoint(xpoints[i], ypoints[i], gc);
 						}
					}
				}
			}
		};
	}

	protected void drawMidPoint(double x, double y, Graphics gc) {
		
		int[] pnt = coords.getValuePosition(x, y);
		gc.pushState();
		gc.setAlpha(255);
		gc.setForegroundColor(pointColor);
		gc.setBackgroundColor(pointColor);
		gc.fillOval(pnt[0], pnt[1], 5, 5);
		gc.popState();
	}
	
	/**
	 * 
	 * @param groi
	 * @return
	 */
	protected double[][] getGridPoints(GridROI groi) {

		double[][] gridPoints = groi.getGridPoints();
		int xGrids = gridPoints[0].length;
		int yGrids = gridPoints[1].length;
		if (xGrids > 0 && yGrids > 0) {
			int numPoints = xGrids * yGrids;
			double[] xPoints = new double[numPoints];
			double[] yPoints = new double[numPoints];

			int cnt = 0;
			for (int i = 0; i < xGrids; i++) {
				for (int j = 0; j < yGrids; j++) {
					xPoints[cnt] = gridPoints[0][i];
					yPoints[cnt] = gridPoints[1][j];
					cnt++;
				}
			}
			return new double[][]{xPoints, yPoints};
		}
		return null;
	}

	@Override
	public ROIBase createROI(boolean recordResult) {
		if (p1!=null) {
			final Rectangle  rect = getRectangleFromVertices();			
			final GridROI    groi = (GridROI)getRoiFromRectangle(rect);
			
			if (getROI() != null && getROI() instanceof GridROI) {
				GridROI oldRoi = (GridROI)getROI();
				// Copy grid, preferences, etc from existing GridROI
				// This maintains spacing etc. until it is changed in setROI(...)
				// These things are determined externally by the user of the ROI
				// and we passively draw them here. TODO Consider about how to edit
				// these in the RegionComposite...
				groi.setxySpacing(oldRoi.getxSpacing(), oldRoi.getySpacing());
				groi.setGridPreferences(oldRoi.getGridPreferences());
			    groi.setGridLineOn(oldRoi.isGridLineOn());
			    groi.setMidPointOn(oldRoi.isMidPointOn());
			}
			
			if (recordResult) roi = groi;
			return groi;
		}
		return super.getROI();
	}
	
	@Override
	protected RectangularROI createROI(double ptx, double pty, double width, double height, double angle) {
		return new GridROI(ptx, pty, width, height, angle);
	}

	
	protected void updateROI(ROIBase roi) {
		if (roi instanceof GridROI) {
			GridROI groi = (GridROI) roi;
			if (p1!=null) p1.setPosition(groi.getPointRef());
			if (p4!=null) p4.setPosition(groi.getEndPoint());
			updateConnectionBounds();
		}
	}

	public Color getPointColor() {
		return pointColor;
	}

	public void setPointColor(Color pointColor) {
		this.pointColor = pointColor;
	}

}

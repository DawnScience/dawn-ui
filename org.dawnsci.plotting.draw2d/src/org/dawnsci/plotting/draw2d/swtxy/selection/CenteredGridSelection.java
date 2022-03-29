package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.eclipse.dawnsci.analysis.dataset.roi.GridROI;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.swt.SWT;

/**
 * This special grid is drawn with two thick lines crossing the midpoint
 * set in {@link GridROI}, and the rest of thin grid lines are drawn after 
 *  
 * @author Dimitrios Ladakis
 *
 */
public class CenteredGridSelection extends GridSelection {

	CenteredGridSelection(String name, ICoordinateSystem coords) {
		super(name, coords);
	}
	
	@Override
	protected void drawGridLines(GridROI groi, Graphics gc) {
		gc.pushState();
		gc.setAlpha(255);
		gc.setForegroundColor(ColorConstants.white);
		gc.setBackgroundColor(ColorConstants.lightGray);
		// Create middle set of lines (thicker)
		gc.setLineWidth(2);
		gc.setLineStyle(SWT.LINE_SOLID);
		double[]   mpt       = groi.getMidPoint();
		double[]   len       = groi.getLengths();
		double yBottom = mpt[1]-len[1]/2;
		double yTop = mpt[1]+ len[1]/2;
		double xLeft = mpt[0]-len[0]/2;
		double xRight = mpt[0]+len[0]/2;
		double[] pntx1 = coords.getPositionFromValue(mpt[0], yBottom);
		double[] pntx2 = coords.getPositionFromValue(mpt[0] , yTop);
		double[] pnty1 = coords.getPositionFromValue(xLeft, mpt[1]);
		double[] pnty2 = coords.getPositionFromValue(xRight, mpt[1]);
		gc.drawLine((int)pntx1[0] , (int)pntx1[1] , (int)pntx2[0] , (int)pntx2[1]);
		gc.drawLine((int)pnty1[0] , (int)pnty1[1] , (int)pnty2[0] , (int)pnty2[1]);
		
		// Create the rest of grid lines
		gc.setLineWidth(1);
		gc.setAlpha(125);
		
		int spacing = (int)groi.getSpacing()[0];
		if (len[0]/2 > spacing) {
			int numberOfLines = (int)((len[0]/2)/spacing);
			for(int i = 1; i <= numberOfLines; i++ ) {
				double[] pnt1 = coords.getPositionFromValue(mpt[0]-spacing*i, yBottom);
				double[] pnt2 = coords.getPositionFromValue(mpt[0]-spacing*i, yTop);
				gc.drawLine((int) pnt1[0], (int) pnt1[1], (int) pnt2[0], (int) pnt2[1]);
			}
			for(int i = 1; i <= numberOfLines; i++ ) {
				double[] pnt1 = coords.getPositionFromValue(mpt[0]+spacing*i, yBottom);
				double[] pnt2 = coords.getPositionFromValue(mpt[0]+spacing*i, yTop);
				gc.drawLine((int) pnt1[0], (int) pnt1[1], (int) pnt2[0], (int) pnt2[1]);
			}
		}
		if (len[1]/2 > spacing) {	
			int numberOfLines = (int)((len[1]/2)/spacing);
			for(int i = 1; i <= numberOfLines; i++ ) {
				double[] pnt1 = coords.getPositionFromValue(xLeft, mpt[1]-spacing*i);
				double[] pnt2 = coords.getPositionFromValue(xRight, mpt[1]-spacing*i);
				gc.drawLine((int) pnt1[0], (int) pnt1[1], (int) pnt2[0], (int) pnt2[1]);
			}
			for(int i = 1; i <= numberOfLines; i++ ) {
				double[] pnt1 = coords.getPositionFromValue(xLeft, mpt[1]+spacing*i);
				double[] pnt2 = coords.getPositionFromValue(xRight, mpt[1]+spacing*i);
				gc.drawLine((int) pnt1[0], (int) pnt1[1], (int) pnt2[0], (int) pnt2[1]);
			}
		}
		gc.popState();
	}

}

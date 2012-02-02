package org.dawb.workbench.ui.editors.plotting.swtxy;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

class SelectionRectangle extends RectangleFigure {
	
	private FigureMover mover;

	SelectionRectangle(Color colour, Rectangle initialBounds) {
		setAlpha(100);
		setBackgroundColor(colour);
		setForegroundColor(colour);
		setOpaque(false);
		setBounds(initialBounds);
		setCursor(Draw2DUtils.getRoiControlPointCursor());
		this.mover = new FigureMover(this);	
	}

	public void startMoving(MouseEvent me) {
		mover.startMoving(me);
	}
}
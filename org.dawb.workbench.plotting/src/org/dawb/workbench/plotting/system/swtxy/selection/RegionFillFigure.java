package org.dawb.workbench.plotting.system.swtxy.selection;

import java.util.Iterator;

import org.dawb.workbench.plotting.system.swtxy.translate.FigureTranslator;
import org.dawb.workbench.plotting.system.swtxy.util.Draw2DUtils;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.MouseListener;

/**
 * You do not have to use this class for the figure which fills the region and
 * is not a control point, but it can be handy if you do.
 * 
 * @author fcp94556
 *
 */
public class RegionFillFigure extends Figure {

	public void setMobile(final boolean mobile) {
		
		final FigureTranslator mover = getFigureMover();
		if (mover==null) return;
		
		mover.setActive(mobile);
		
		if (mobile) {
			setCursor(Draw2DUtils.getRoiMoveCursor());
		} else {
			setCursor(null);
		}
	}

	private FigureTranslator getFigureMover() {
		final Iterator<?> it = getListeners(MouseListener.class);
		if (it!=null && it.hasNext()) {
			MouseListener l = null;
			while((l=(MouseListener)it.next()) !=null ) {
				if (l instanceof FigureTranslator) return (FigureTranslator)l;
			}
		}
		return null;
	}
}

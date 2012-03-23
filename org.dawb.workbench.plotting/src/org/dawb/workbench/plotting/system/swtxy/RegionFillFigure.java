package org.dawb.workbench.plotting.system.swtxy;

import java.util.Iterator;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.MouseListener;

public class RegionFillFigure extends Figure {

	public void setMobile(final boolean mobile) {
		
		final FigureMover mover = getFigureMover();
		if (mover==null) return;
		
		mover.setActive(mobile);
		
		if (mobile) {
			setCursor(Draw2DUtils.getRoiMoveCursor());
		} else {
			setCursor(null);
		}
	}

	private FigureMover getFigureMover() {
		final Iterator<?> it = getListeners(MouseListener.class);
		if (it!=null && it.hasNext()) {
			MouseListener l = null;
			while((l=(MouseListener)it.next()) !=null ) {
				if (l instanceof FigureMover) return (FigureMover)l;
			}
		}
		return null;
	}
}

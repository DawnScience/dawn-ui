package org.dawnsci.plotting.draw2d.swtxy.selection;

import java.util.Iterator;

import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegionContainer;
import org.dawnsci.plotting.draw2d.swtxy.translate.FigureTranslator;
import org.dawnsci.plotting.draw2d.swtxy.util.Draw2DUtils;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.Shape;

import uk.ac.diamond.scisoft.analysis.roi.IROI;

/**
 * You do not have to use this class for the figure which fills the region and
 * is not a control point, but it can be handy if you do.
 * 
 * It also has a right click menu which will appear when the user presses right click.
 * This moves the region up or down in order and 
 * 
 * @author fcp94556
 *
 */
public abstract class RegionFillFigure<T extends IROI> extends Shape implements IRegionContainer {
	
	protected AbstractSelectionRegion<T> region;

	public RegionFillFigure(AbstractSelectionRegion<T> region) {
		this.region = region;
	}

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

	public boolean isMobile() {
		final FigureTranslator mover = getFigureMover();
		if (mover==null) return false;
		
		return mover.isActive();
	}

	protected FigureTranslator getFigureMover() {
		final Iterator<?> it = getListeners(MouseListener.class);
		if (it!=null && it.hasNext()) {
			MouseListener l = null;
			
			while(it.hasNext()) {
				l=(MouseListener)it.next();
				if (l instanceof FigureTranslator) return (FigureTranslator)l;
			}
		}
		return null;
	}

	public IRegion getRegion() {
		return region;
	}

	@SuppressWarnings("unchecked")
	public void setRegion(IRegion region) {
		if (region instanceof AbstractSelectionRegion<?>)
			this.region = (AbstractSelectionRegion<T>) region;
	}
}

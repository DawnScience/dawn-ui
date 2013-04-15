package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;

public class MouseMotionAdapter implements MouseMotionListener {

	
	private org.dawnsci.plotting.api.region.MouseMotionListener deligate;
	public MouseMotionAdapter(org.dawnsci.plotting.api.region.MouseMotionListener l) {
		this.deligate = l;
	}
	
	public int hashCode() {
		return deligate.hashCode();
	}
	public boolean equals(Object object) {
		return deligate.equals(object);
	}

	@Override
	public void mouseDragged(MouseEvent me) {
		deligate.mouseDragged(new MouseEventAdapter(me));
	}

	@Override
	public void mouseEntered(MouseEvent me) {
		deligate.mouseEntered(new MouseEventAdapter(me));
	}

	@Override
	public void mouseExited(MouseEvent me) {
		deligate.mouseExited(new MouseEventAdapter(me));
	}

	@Override
	public void mouseHover(MouseEvent me) {
		deligate.mouseHover(new MouseEventAdapter(me));
	}

	@Override
	public void mouseMoved(MouseEvent me) {
		deligate.mouseMoved(new MouseEventAdapter(me));
	}

}

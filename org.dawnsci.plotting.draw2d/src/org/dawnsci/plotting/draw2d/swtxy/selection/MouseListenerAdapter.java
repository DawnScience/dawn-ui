package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;

public class MouseListenerAdapter implements MouseListener {

	private final org.dawnsci.plotting.api.region.MouseListener deligate;
	public MouseListenerAdapter(org.dawnsci.plotting.api.region.MouseListener l) {
		deligate = l;
	}
	public int hashCode() {
		return deligate.hashCode();
	}
	public boolean equals(Object object) {
		return deligate.equals(object);
	}

	@Override
	public void mousePressed(MouseEvent me) {
		deligate.mousePressed(new MouseEventAdapter(me));
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		deligate.mouseReleased(new MouseEventAdapter(me));
	}

	@Override
	public void mouseDoubleClicked(MouseEvent me) {
		deligate.mouseDoubleClicked(new MouseEventAdapter(me));
	}
	

}

package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.dawnsci.plotting.api.region.MouseEvent;

public class MouseEventAdapter extends MouseEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7627603074904232662L;
	private final org.eclipse.draw2d.MouseEvent deligate;

	public MouseEventAdapter(org.eclipse.draw2d.MouseEvent source) {
		super(source.getSource());
		this.deligate = source;
	}

	@Override
	public int getButton() {
		return deligate.button;
	}

	@Override
	public int getX() {
		return deligate.x;
	}

	@Override
	public int getY() {
		return deligate.y;
	}

}

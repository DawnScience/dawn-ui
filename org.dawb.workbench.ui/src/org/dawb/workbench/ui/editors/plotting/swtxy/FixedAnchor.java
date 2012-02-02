package org.dawb.workbench.ui.editors.plotting.swtxy;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.geometry.Point;

class FixedAnchor extends AbstractConnectionAnchor {
	
	Point place  = new Point(1, 1);

	public FixedAnchor(Figure owner) {
		super(owner);
	}

	public Point getLocation(Point loc) {
		return ((Figure)getOwner()).getLocation();
	}
}
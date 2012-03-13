package org.dawb.workbench.plotting.system.swtxy;

import org.csstudio.swt.xygraph.figures.Axis;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * An axis which can 
 * @author fcp94556
 *
 */
public class AspectAxis extends Axis {

	private AspectAxis relativeTo;

	public AspectAxis(String title, boolean yAxis) {
		super(title, yAxis);
	}

	public void setKeepAspectWith(final AspectAxis axis) {
		this.relativeTo = axis;
	}
	
	public void setBounds(final Rectangle bounds) {
		
		if (relativeTo == null) {
			super.setBounds(bounds);
			return;
		}
		
		// Otherwise we keep aspect with it.
		
	}
}

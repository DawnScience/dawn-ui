package org.dawnsci.plotting.api.views;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * IMPORTANT do not remove this funny class it is used to 
 * give a widget with the same classloader as plugin org.dawnsci.plotting.api
 * in order to provide functionality for the squish tests!
 * @author fcp94556
 *
 */
public class RefreshButton extends Button {

	public RefreshButton(Composite parent, int style) {
		super(parent, style);
		setText("Refresh");
	}

	protected void checkSubclass () {
		// We allow this one on purpose.
	}
}

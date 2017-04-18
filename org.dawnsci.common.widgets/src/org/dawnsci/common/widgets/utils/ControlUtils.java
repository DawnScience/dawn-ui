package org.dawnsci.common.widgets.utils;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** Utility class for SWT composites
 * 
 * @author awf63395
 *
 */
public class ControlUtils {
	/**
	 * Recursively enable/disable a Control and its children (in case of a Composite)
	 * Inspired by http://stackoverflow.com/questions/2957657/disable-and-grey-out-an-eclipse-widget
	 */
	public static void recursiveSetEnabled(Control ctrl, boolean enabled) {
		if (ctrl instanceof Composite) {
			Composite comp = (Composite) ctrl;
			Control[] kids = comp.getChildren();
			for (Control c : kids)
				recursiveSetEnabled(c, enabled);
			if (kids == null || kids.length == 0)
				ctrl.setEnabled(enabled);
		} else {
			ctrl.setEnabled(enabled);
		}
	}
}

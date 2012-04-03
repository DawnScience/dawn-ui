package org.dawb.workbench.plotting.system.swtxy.util;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;

public class Draw2DUtils {

	/**
	 * Attempts to get the centre of a figure using its bounds.
	 * @param bx
	 * @return
	 */
	public static Point getCenter(Figure bx) {
		final Point   location = bx.getLocation();
		final Rectangle bounds = bx.getBounds();
		return new Point(location.x+(bounds.width/2), location.y+(bounds.height/2));
	}

	public static Cursor getRoiControlPointCursor() {
		return Display.getCurrent().getSystemCursor(SWT.CURSOR_SIZEALL);
	}
	public static Cursor getRoiMoveCursor() {
		return Display.getCurrent().getSystemCursor(SWT.CURSOR_CROSS);
	}

}

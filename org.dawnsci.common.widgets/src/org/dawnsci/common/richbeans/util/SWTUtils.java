/*-
 * Copyright © 2009 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package org.dawnsci.common.richbeans.util;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Matthew Gerring
 *
 */
public class SWTUtils {

	/**
	 * This method will centre a Shell on the primary view device.
	 * 
	 * The method will show the shell in the thread which calls the method
	 * and not return until the user closes the shell.
	 * 
	 * @param shell
	 */
	public static final void showCenteredShell(final Shell shell) {

		final Display display  = shell.getDisplay();
		final Monitor primary  = shell.getDisplay().getPrimaryMonitor();
		
		final Rectangle bounds = primary.getBounds();
		final Rectangle rect   = shell.getBounds();

		final int x = bounds.x + (bounds.width - rect.width) / 2;
		final int y = bounds.y + (bounds.height - rect.height) / 2;

		shell.setLocation(x, y);
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) shell.getDisplay().sleep();
		}

		display.dispose();
	}

}


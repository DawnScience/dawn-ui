/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.plotting.util;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;


public class ToolbarSeparator extends Figure{
	
	private final Color GRAY_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
	
	@Override
	protected void paintClientArea(Graphics graphics) {
		super.paintClientArea(graphics);
		graphics.setForegroundColor(GRAY_COLOR);
		graphics.setLineWidth(1);
		graphics.drawLine(bounds.x + bounds.width/2, bounds.y, 
				bounds.x + bounds.width/2, bounds.y + bounds.height);
	}
}

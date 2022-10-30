/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.draw2d.swtxy.selection;

import org.dawnsci.plotting.draw2d.swtxy.util.RotatablePolygonShape;
import org.dawnsci.plotting.draw2d.swtxy.util.RotatableRectangle;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Shape;
import org.eclipse.swt.graphics.Color;

public class RectangularHandle extends SelectionHandle {

	/**
	 * @param xAxis
	 * @param yAxis
	 * @param colour
	 * @param parent
	 * @param side
	 * @param params (first corner's x and y, also centre of rotation)
	 */
	public RectangularHandle(ICoordinateSystem coords, Color colour, Figure parent, int side, double... params) {
		super(coords, colour, parent, side, params);
	}

	@Override
	public Shape createHandleShape(Figure parent, int side, double[] params) {
		double angle;
		if (parent instanceof RotatablePolygonShape) {
			RotatablePolygonShape pg = (RotatablePolygonShape) parent;
			angle = pg.getAngleDegrees();
		} else {
			angle = 0;
		}
		int x = (int) Math.round(params[0]);
		int y = (int) Math.round(params[1]);
		return new RotatableRectangle(x, y, side, side, angle);
	}
}

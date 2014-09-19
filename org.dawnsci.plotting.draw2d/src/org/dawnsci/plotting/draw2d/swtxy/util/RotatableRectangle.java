/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.draw2d.swtxy.util;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;

/**
 * A Draw2D rectangle shape that allows its orientation to be set. Its location is the centre of
 * rotation (not the top-left corner of its bounding box)
 */
public class RotatableRectangle extends RotatablePolygonShape {

	private final PointList ool; // outline points
	private final PointList nol; // transformed outline points

	/**
	 * Constructor for rectangle
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param angle in degrees (positive is anti-clockwise)
	 */
	public RotatableRectangle(int x, int y, int width, int height, double angle) {
		super(4);

		opl.addPoint(0, 0);
		opl.addPoint(width, 0);
		opl.addPoint(width, height);
		opl.addPoint(0, height);
		npl.removeAllPoints();
		npl.addAll(opl);
		ool = new PointList(4);
		ool.addAll(opl);
		nol = new PointList(4);
		nol.addAll(opl);
		recalcOutline();
		affine.setTranslation(x, y);
		setAngle(angle);
	}

	public RotatableRectangle() {
		super(0);
		ool = new PointList(4);
		nol = new PointList(4);
		setAngle(0);
	}

	public void setLengths(int width, int height) {
		opl.removeAllPoints();
		opl.addPoint(0, 0);
		opl.addPoint(width, 0);
		opl.addPoint(width, height);
		opl.addPoint(0, height);
		npl.removeAllPoints();
		npl.addAll(opl);
		recalcOutline();
		refresh();
	}

	public void setOrigin(int x, int y) {
		affine.setTranslation(x, y);
		refresh();
	}

	@Override
	public void setLineWidth(int w) {
		super.setLineWidth(w);
		recalcOutline();
	}

	@Override
	public void setLineWidthFloat(float value) {
		super.setLineWidthFloat(value);
		recalcOutline();
	}

	private void recalcOutline() {
		final double lineInset = Math.max(1.0, getLineWidthFloat()) / 2.0;
		final int inset1 = (int) Math.floor(lineInset);
		final int inset2 = (int) Math.ceil(lineInset);
		final Point c = opl.getPoint(2);
		final int x = c.x() - inset2;
		final int y = c.y() - inset2;
		ool.removeAllPoints();
		ool.addPoint(inset1, inset1);
		ool.addPoint(x, inset1);
		ool.addPoint(x, y);
		ool.addPoint(inset1, y);
		nol.removeAllPoints();
		nol.addAll(ool);
	}

	/**
	 * @return centre point of rectangle
	 */
	public Point getCentre() {
		return npl.getPoint(0).getTranslated(npl.getPoint(2)).scale(0.5);
	}

	/**
	 * 
	 * @param c
	 */
	public void setCentre(Point c) {
		Dimension d = c.getDifference(getCentre());
		affine.setTranslation(affine.getTranslationX() + d.preciseWidth(), affine.getTranslationY() + d.preciseHeight());
		refresh();
	}

	/**
	 * @return list of point in outline rectangle
	 */
	public PointList getOutline() {
		return nol;
	}

	@Override
	protected void refresh() {
		recalcPoints(ool, nol, false);
		recalcPoints(opl, npl, true);
	}

	@Override
	protected void outlineShape(Graphics graphics) {
		graphics.drawPolygon(nol);
	}
}

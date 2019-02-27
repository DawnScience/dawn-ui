/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.draw2d.swtxy;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.dawnsci.plotting.api.axis.AxisEvent;
import org.eclipse.dawnsci.plotting.api.axis.CoordinateSystemEvent;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.axis.IAxisListener;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystemListener;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.draw2d.geometry.Point;

public class RegionCoordinateSystem implements ICoordinateSystem, IAxisListener {

	private IImageTrace imageTrace;
	private IAxis x,y;
	private boolean isDisposed=false;
	private double aspectRatio = 1.0;

	/**
	 * 
	 * @param imageTrace (may be null) 
	 * @param x
	 * @param y
	 */
	public RegionCoordinateSystem(IImageTrace imageTrace, IAxis x, IAxis y) {
		this.imageTrace = imageTrace;
		this.x          = x;
		this.y          = y;
		calcAspectRatio();
	}

	public IAxis getX() {
		return x;
	}

	public IAxis getY() {
		return y;
	}

	public void dispose() {
		isDisposed = true;
		if (x!=null) x.removeAxisListener(this);
		if (this.coordinateListeners!=null) {
			coordinateListeners.clear();
			coordinateListeners = null;
		}
	}

	private boolean flipCoords() {
		if (imageTrace == null) return false;
		return imageTrace.getImageServiceBean().isTransposed() ^ imageTrace.getImageOrigin().isOnLeadingDiagonal();
	}

	private double[] transformValueToPosition(double ox, double oy) {
		if (flipCoords()) {
			return new double[] {x.getPositionFromValue(ox), y.getPositionFromValue(oy)};
		}
		return new double[] {x.getPositionFromValue(oy), y.getPositionFromValue(ox)};
	}

	private double[] transformPositionToValue(double ox, double oy) {
		if (flipCoords()) {
			return new double[] {x.getValueFromPosition(ox), y.getValueFromPosition(oy)};
		}
		return new double[] {y.getValueFromPosition(oy), x.getValueFromPosition(ox)};
	}

	@Override
	public int[] getValuePosition(double... value) {
		double[] pos = getPositionFromValue(value[0], value[1]);
		return new int[] {(int) pos[0], (int) pos[1]};
	}

	private void checkDisposed() {
		if (isDisposed) throw new RuntimeException(getClass().getName() + " is disposed!");
	}

	@Override
	public double[] getPositionFromValue(double... value) {
		checkDisposed();

		return transformValueToPosition(value[0], value[1]);
	}

	private void calcAspectRatio() {
		aspectRatio = Math.abs(x.getScaling() / y.getScaling());
	}

	@Override
	public double getAspectRatio() {
		return aspectRatio;
	}

	@Override
	public double[] getPositionValue(int... position) {
		checkDisposed();

		return transformPositionToValue(position[0], position[1]);
	}

	@Override
	public double[] getValueFromPosition(double... position) {
		checkDisposed();

		return transformPositionToValue(position[0], position[1]);
	}

	private Collection<ICoordinateSystemListener> coordinateListeners;
	@Override
	public void addCoordinateSystemListener(ICoordinateSystemListener l) {
		checkDisposed();
		if (coordinateListeners==null) {
			coordinateListeners = new HashSet<ICoordinateSystemListener>();
			x.addAxisListener(this);
		}
		coordinateListeners.add(l);
	}

	@Override
	public void removeCoordinateSystemListener(ICoordinateSystemListener l) {
		checkDisposed();

		if (coordinateListeners==null) return;
		coordinateListeners.remove(l);
		if (coordinateListeners.isEmpty()) {
			x.removeAxisListener(this);
			coordinateListeners = null;
		}
	}

	private void fireCoordinateSystemListeners() {
		if (coordinateListeners==null) return;
		final CoordinateSystemEvent evt = new CoordinateSystemEvent(this);
		for (ICoordinateSystemListener l : coordinateListeners) {
			l.coordinatesChanged(evt);
		}
	}

	@Override
	public void rangeChanged(AxisEvent evt) {
		fireCoordinateSystemListeners();
	}

	@Override
	public void revalidated(AxisEvent evt) {
		calcAspectRatio();
		fireCoordinateSystemListeners();
	}

	@Override
	public boolean isXReversed() {
		if (imageTrace==null) return false;
		return !imageTrace.getImageOrigin().isOnLeft();
	}

	@Override
	public boolean isYReversed() {
		if (imageTrace==null) return false;
		return !imageTrace.getImageOrigin().isOnTop();
	}

	@Override
	public double getXAxisRotationAngleDegrees() {
		double angle = 0;

		if (imageTrace != null) {
			angle = 90 * imageTrace.getImageOrigin().ordinal();
		}
		return angle;
	}

	@Override
	public double[] getValueAxisLocation(double... values) throws Exception {
		// No need to test for reversal here, the labels and the image do not get reversed.
		if (imageTrace==null) return values;
		return imageTrace.getPointInAxisCoordinates(values);
	}

	@Override
	public double[] getAxisLocationValue(double... axisLocation) throws Exception {
		// No need to test for reversal here, the labels and the image do not get reversed.
		if (imageTrace==null) return axisLocation;
		return imageTrace.getPointInImageCoordinates(axisLocation);
	}

	@Override
	public boolean isDisposed() {
		return isDisposed;
	}
}

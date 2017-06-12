/*
 * Copyright (c) 2012, 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.preference.detector;

import java.io.Serializable;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;

import tec.units.ri.quantity.Quantities;
import tec.units.ri.unit.MetricPrefix;
import tec.units.ri.unit.Units;

public class DiffractionDetector implements Serializable{

	private static final long serialVersionUID = -1133345866155946034L;
	
	private String detectorName;
	private Quantity<Length> xPixelSize;
	private Quantity<Length> yPixelSize;
	private int numberOfPixelsX;
	private int numberOfPixelsY;

	private static final Unit<Length> MILLIMETRE = MetricPrefix.MILLI(Units.METRE);

	public int getNumberOfPixelsX() {
		return numberOfPixelsX;
	}
	public void setNumberOfPixelsX(int numberOfPixelsX) {
		this.numberOfPixelsX = numberOfPixelsX;
	}
	public int getNumberOfPixelsY() {
		return numberOfPixelsY;
	}
	public void setNumberOfPixelsY(int numberOfPixelsY) {
		this.numberOfPixelsY = numberOfPixelsY;
	}

	private String units;
	
	public String getDetectorName() {
		return detectorName;
	}
	public void setDetectorName(String name) {
		this.detectorName = name;
	}
	public Quantity<Length> getxPixelSize() {
		return xPixelSize;
	}
	public void setxPixelSize(Quantity<Length> xPixelSize) {
		this.xPixelSize = xPixelSize;
	}

	
	public Quantity<Length> getPixelSize() {
		return yPixelSize;
	}
	public void setyPixelSize(Quantity<Length> yPixelSize) {
		this.yPixelSize = yPixelSize;
	}
	
	public double getXPixelMM() {
		if (xPixelSize ==  null) return Double.NaN;
		return xPixelSize.to(MILLIMETRE).getValue().doubleValue();
	}
	
	public void setXPixelMM(double pixmm) {
		xPixelSize = Quantities.getQuantity(pixmm, MILLIMETRE);
	}
	
	public double getYPixelMM() {
		if (yPixelSize ==  null) return Double.NaN;
		return yPixelSize.to(MILLIMETRE).getValue().doubleValue();
	}
	
	public void setYPixelMM(double pixmm) {
		yPixelSize = Quantities.getQuantity(pixmm, MILLIMETRE);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((detectorName == null) ? 0 : detectorName.hashCode());
		result = prime * result + numberOfPixelsX;
		result = prime * result + numberOfPixelsY;
		result = prime * result + ((units == null) ? 0 : units.hashCode());
		result = prime * result
				+ ((xPixelSize == null) ? 0 : xPixelSize.hashCode());
		result = prime * result
				+ ((yPixelSize == null) ? 0 : yPixelSize.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DiffractionDetector other = (DiffractionDetector) obj;
		if (detectorName == null) {
			if (other.detectorName != null)
				return false;
		} else if (!detectorName.equals(other.detectorName))
			return false;
		if (numberOfPixelsX != other.numberOfPixelsX)
			return false;
		if (numberOfPixelsY != other.numberOfPixelsY)
			return false;
		if (units == null) {
			if (other.units != null)
				return false;
		} else if (!units.equals(other.units))
			return false;
		if (xPixelSize == null) {
			if (other.xPixelSize != null)
				return false;
		} else if (!xPixelSize.equals(other.xPixelSize))
			return false;
		if (yPixelSize == null) {
			if (other.yPixelSize != null)
				return false;
		} else if (!yPixelSize.equals(other.yPixelSize))
			return false;
		return true;
	}
	
}

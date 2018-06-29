/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.grid;

import java.util.Map;

import javax.measure.Dimension;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.UnitConverter;
import javax.measure.quantity.Length;

import org.eclipse.dawnsci.analysis.api.unit.UnitUtils;

import si.uom.SI;
import tec.units.indriya.AbstractUnit;
import tec.units.indriya.format.SimpleUnitFormat;
import tec.units.indriya.quantity.Quantities;
import tec.units.indriya.quantity.QuantityDimension;

public class Pixel extends AbstractUnit<Length>{

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {

		Unit<Resolution> ppmm = new PerMilliMetre();
		Quantity<Resolution> res = Quantities.getQuantity(2.0, ppmm);

		Pixel pixel = new Pixel(res, 40);
		Quantity<Length> distInPixels = Quantities.getQuantity(1000.0, pixel);

		System.out.println(distInPixels.to(pixel));
		System.out.println(distInPixels.to(SI.METRE));
		System.out.println(distInPixels.to(UnitUtils.MILLIMETRE));
		// System.out.println(distInPixels.to(NonSI.FOOT));
	}

	public static Pixel pixel() {
		return new Pixel();
	}

	private PixelConverter converter;

	public Pixel() {
		this(Quantities.getQuantity(1.0, new PerMilliMetre()), 0, "px");
	}

	public Pixel(String label) {
		this(Quantities.getQuantity(1.0, new PerMilliMetre()), 0, label);
	}

	public Pixel(Quantity<Resolution> resolution, double offset) {
		this(resolution, offset, "px");
	}

	public Pixel(Quantity<Resolution> resolution, double offset, String label) {
		this.converter = new PixelConverter(resolution, offset);
		SimpleUnitFormat.getInstance().label(this, label);
	}

	public Quantity<Resolution> getResolution() {
		return this.converter.getResolution();
	}

	public void setPixelsPerMm(double ppmm) {
		this.converter.setPixelsPerMm(ppmm);
	}

	public void setResolution(Quantity<Resolution> resolution) {
		this.converter.setResolution(resolution);
	}

	public void setOffset(double offset) {
		this.converter.setOffset(offset);
	}

	@Override
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof Pixel))
			return false;
		if (arg0 == this)
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public Map<? extends Unit<?>, Integer> getBaseUnits() {
		return null;
	}

	@Override
	public Dimension getDimension() {
		return QuantityDimension.LENGTH;
	}

	@Override
	public UnitConverter getSystemConverter() {
		return null;
	}

	@Override
	protected Unit<Length> toSystemUnit() {
		return SI.METRE;
	}
}

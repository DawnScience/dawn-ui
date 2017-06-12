/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.grid;

import tec.units.ri.spi.Measurement;

import javax.measure.UnitConverter;
import javax.measure.quantity.Length;
import javax.measure.Unit;
import javax.measure.format.UnitFormat;

public class Pixel implements Unit<Length>{

	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		
		Unit<Resolution> ppmm = new PerMilliMetre();
		Measurement<Double, Resolution> res = Measure.valueOf(2.0, ppmm);
		
		Pixel pixel = new Pixel(res, 40);
		Measure<Double, Length> distInPixels = Measure.valueOf(1000.0, pixel);
		
		System.out.println(distInPixels.to(pixel));
		System.out.println(distInPixels.to(SI.METRE));
		System.out.println(distInPixels.to(SI.MILLI(SI.METRE)));
		System.out.println(distInPixels.to(NonSI.FOOT));

	}

	public static Pixel pixel() {
		return new Pixel();
	}
	
	private PixelConverter converter;
	
	public Pixel() {
		this(Measure.valueOf(1.0, new PerMilliMetre()), 0, "px");
	}
	
	public Pixel(String label) {
		this(Measure.valueOf(1.0, new PerMilliMetre()), 0, label);
	}
	
	public Pixel(Measure<Double, Resolution> resolution, double offset) {
		this(resolution, offset, "px");
	}
	
	public Pixel(Measure<Double, Resolution> resolution, double offset, String label) {
		this.converter = new PixelConverter(resolution, offset);
		UnitFormat.getInstance().label(this, label);
	}
	
	public Measure<Double, Resolution> getResolution() {
		return this.converter.getResolution();
	}
	public void setPixelsPerMm(double ppmm) {
		this.converter.setPixelsPerMm(ppmm);
	}
	public void setResolution(Measure<Double, Resolution> resolution) {
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
	public Unit<? super Length> getStandardUnit() {
		return SI.METRE;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	@Override
	public UnitConverter toStandardUnit() {
		return converter;
	}
}

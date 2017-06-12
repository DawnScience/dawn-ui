/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.grid;

import javax.measure.Measure;
import javax.measure.ConversionException;
import javax.measure.UnitConverter;

public class PixelConverter implements UnitConverter {

	private static final long serialVersionUID = -245421741665077722L;
	private Measure<Double, Resolution> resolution;
	private double offset; //always in pixels

	public PixelConverter(Measure<Double, Resolution> resolution, double offset) {
		this.resolution = resolution;
		this.offset = offset;
	}

	public double getPixelsPerMm() {
		return resolution.doubleValue(Resolution.UNIT.divide(1000));
	}
	
	public void setPixelsPerMm(double ppmm) {
		this.resolution = Measure.valueOf(ppmm, new PerMilliMetre());
	}

	public Measure<Double, Resolution> getResolution() {
		return this.resolution;
	}
	
	public void setResolution(Measure<Double, Resolution> resolution) {
		this.resolution = resolution;
	}

	public double getOffset() {
		return offset;
	}

	public void setOffset(double offset) {
		this.offset = offset;
	}

	@Override
	public UnitConverter inverse() {
		return new UnitConverter() {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = -5608089612770287616L;

			@Override
			public boolean isLinear() {
				return false;
			}
			
			@Override
			public UnitConverter inverse() {
				return new PixelConverter(resolution, offset);
			}
			
			@Override
			public double convert(double arg0) throws ConversionException {
				return (arg0 * resolution.doubleValue(Resolution.UNIT)) + offset;
			}
		};
	}

	@Override
	public double convert(double x) throws ConversionException {
		return (x - offset) / resolution.doubleValue(Resolution.UNIT);
	}

	@Override
	public boolean isLinear() {
		return false;
	}

}

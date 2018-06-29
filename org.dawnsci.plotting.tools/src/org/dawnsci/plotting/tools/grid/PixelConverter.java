/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.grid;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.UnitConverter;

import org.eclipse.dawnsci.analysis.api.unit.UnitUtils;

import tec.units.indriya.AbstractConverter;
import tec.units.indriya.quantity.Quantities;

public class PixelConverter extends AbstractConverter {

	private static final long serialVersionUID = -245421741665077722L;
	private Quantity<Resolution> resolution;
	private double offset; //always in pixels

	public PixelConverter(Quantity<Resolution> resolution, double offset) {
		this.resolution = resolution;
		this.offset = offset;
	}

	public double getPixelsPerMm() {
		return UnitUtils.convert(resolution, Resolution.UNIT.divide(1000));
	}

	public void setPixelsPerMm(double ppmm) {
		resolution = Quantities.getQuantity(ppmm, new PerMilliMetre());
	}

	public Quantity<Resolution> getResolution() {
		return resolution;
	}

	public void setResolution(Quantity<Resolution> resolution) {
		this.resolution = resolution;
	}

	/**
	 * @return offset in pixels
	 */
	public double getOffset() {
		return offset;
	}

	/**
	 * Set offset
	 * @param offset in pixels
	 */
	public void setOffset(double offset) {
		this.offset = offset;
	}

	@Override
	public AbstractConverter inverse() {
		return new AbstractConverter() {
			private static final long serialVersionUID = -5608089612770287616L;

			@Override
			public boolean isIdentity() {
				return false;
			}

			@Override
			public boolean isLinear() {
				return false;
			}

			@Override
			public AbstractConverter inverse() {
				return new PixelConverter(resolution, offset);
			}

			@Override
			public double convert(double value) {
				return value * UnitUtils.convert(resolution, Resolution.UNIT) + offset;
			}

			@Override
			public UnitConverter concatenate(UnitConverter converter) {
				return null;
			}

			@Override
			public List<? extends UnitConverter> getConversionSteps() {
				return null;
			}

			@Override
			public int compareTo(UnitConverter o) {
				return 0;
			}

			@Override
			public BigDecimal convert(BigDecimal arg0, MathContext arg1) throws ArithmeticException {
				return null;
			}

			@Override
			public boolean equals(Object arg0) {
				return false;
			}

			@Override
			public int hashCode() {
				return 0;
			}
		};
	}

	@Override
	public double convert(double x) {
		return (x - offset) / UnitUtils.convert(resolution, Resolution.UNIT);
	}

	@Override
	public boolean isLinear() {
		return false;
	}

	@Override
	public int compareTo(UnitConverter o) {
		return 0;
	}

	@Override
	public BigDecimal convert(BigDecimal arg0, MathContext arg1) throws ArithmeticException {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PixelConverter other = (PixelConverter) obj;
		if (Double.doubleToLongBits(offset) != Double.doubleToLongBits(other.offset))
			return false;
		if (resolution == null) {
			if (other.resolution != null)
				return false;
		} else if (!resolution.equals(other.resolution))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(offset);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((resolution == null) ? 0 : resolution.hashCode());
		return result;
	}
}

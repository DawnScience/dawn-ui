/*-
 * Copyright (c) 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.tools.powderlines;

/**
 * Model of a line with associated equation of state data.
 * @author Timothy Spain, timothy.spain@diamond.ac.uk
 *
 */
public class EoSLineModel extends PowderLineModel {
	
	private double lengthRatio;
	
	/**
	 * Constructor allowing definition of the d-spacing of the line
	 * @param vaccuumDSpacing
	 * 						d-spacing at zero pressure, in Å
	 */
	public EoSLineModel(double vaccuumDSpacing) {
		super(vaccuumDSpacing);
	}
	
	@Override
	public double getDSpacing() {
		return lengthRatio*super.getDSpacing();
	}
	
	@Override
	public double getQ() {
		return super.getQ()/lengthRatio;
	}
	
	@Override
	public double get2Theta(double beamWavelength) {
		return twoThetaFromD(this.getDSpacing(), beamWavelength);
	}
	
	/**
	 * Set the length ratio by which the material is currently
	 * compressed.
	 * @param lengthRatio
	 * 					l/l₀
	 */
	public void setLengthRatio(double lengthRatio) {
		this.lengthRatio = lengthRatio;
	}
}

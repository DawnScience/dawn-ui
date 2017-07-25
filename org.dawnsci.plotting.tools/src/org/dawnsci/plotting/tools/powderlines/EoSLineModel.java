/*
 * Copyright (c) 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.powderlines;

import org.dawnsci.plotting.tools.powderlines.PowderLineTool.PowderDomains;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.Maths;

/**
 * A specialization of {@link PowderLineModel}, additionally holding equation of state data
 * @author Timothy Spain, timothy.spain@diamond.ac.uk
 *
 */
public class EoSLineModel extends PowderLineModel {

	private double bulkModulus, bulkModulus_p; 

	private double pressure;
	

	/**
	 * @return the bulk modulus of the material
	 */
	public double getBulkModulus() {
		return bulkModulus;
	}

	/**
	 * @param bulkModulus
	 * 					the bulk modulus of the material to set
	 */
	public void setBulkModulus(double bulkModulus) {
		this.bulkModulus = bulkModulus;
	}

	/**
	 * @return the pressure derivative of the bulk modulus
	 */
	public double getBulkModulus_p() {
		return bulkModulus_p;
	}

	/**
	 * @param bulkModulus_p
	 * 						the pressure derivative of the bulk modulus to set
	 */
	public void setBulkModulus_p(double bulkModulus_p) {
		this.bulkModulus_p = bulkModulus_p;
	}

	/**
	 * @return the pressure
	 */
	public double getPressure() {
		return pressure;
	}

	/**
	 * @param pressure the pressure to set
	 */
	public void setPressure(double pressure) {
		this.pressure = pressure;
	}

	@Override
	public boolean hasEoSMetadata() {
		return true;
	}
	
	@Override
	public PowderLineTool.PowderDomains getDomain() {
		return PowderDomains.EQUATION_OF_STATE;
	}
	
	@Override
	public DoubleDataset getLines(PowderLineCoord coords) {
//		double volumeRatio = this.getVolumeRatio();
		double linearRatio = solveBirchMurnaghan();
		System.err.println("Will one day apply EoS");
		DoubleDataset lines = super.getLines(coords);
		return (lines.getSize() > 0) ? (DoubleDataset) Maths.multiply(linearRatio, lines) : lines;
		
	}

	// In the below, x is the linear unit cell size as a function of pressure x = (V/V0)^(1/3)
	private double term1(double x) {
		return 3./2 * bulkModulus * (Math.pow(x, -7.) - Math.pow(x, -5.));
	}
	private double dTerm1_dx(double x) {
		return 3./2. * bulkModulus * (-7. * Math.pow(x, 8.) + 5. * Math.pow(x,  6.));
	}
	
	private double term2(double x) {
		return ( 1 + 3./4.*(bulkModulus_p - 4)*(Math.pow(x, -2.) - 1));
	}

	private double dTerm2_dx(double x) {
		return 3./4. * (bulkModulus_p - 4) * -2. * Math.pow(x, -3.);
	}
	
	private double fBirchMurnaghan(double x) {
		return  term1(x) * term2(x);
	}
	
	private double dBirchMurnaghan(double x) {
		return term1(x) * dTerm1_dx(x) + term1(x) * dTerm2_dx(x);
	}
	
	private double solveBirchMurnaghan() {
		// Solve the Birch-Murnaghan equation of state to get the linear ratio at the current pressure
		final double error = 1e-6;
		double x0, x1 = 1;
		
		do {
			x0 = x1;
			x1 = x0 - fBirchMurnaghan(x0)/dBirchMurnaghan(x0);
		} while (Math.abs(x1/x0 - 1) > error);
		
		return x1;
	}
	
}

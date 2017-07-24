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
 * @author rkl37156
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
		double volumeRatio = this.getVolumeRatio();
		double linearRatio = Math.cbrt(volumeRatio);
		System.err.println("Will one day apply EoS");
		DoubleDataset lines = super.getLines(coords);
		return (lines.getSize() > 0) ? (DoubleDataset) Maths.multiply(linearRatio, lines) : lines;
		
	}
	
	private double getVolumeRatio() {
		return 1.0;
	}
}

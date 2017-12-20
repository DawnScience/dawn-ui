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
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.scisoft.analysis.crystallography.BirchMurnaghanSolver;

/**
 * A specialization of {@link PowderLinesModel}, additionally holding equation of state data
 * @author Timothy Spain, timothy.spain@diamond.ac.uk
 *
 */
public class EoSLinesModel extends PowderLinesModel {

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
	public Composite getModelSpecificDetailsComposite(Composite parent, int style ) {
		EoSLineTool.EosDetailsComposite compo = new EoSLineTool.EosDetailsComposite(parent, style);
		compo.setPressureMultiplierMagnitude(9);
		return compo;
	}
	
//	@Override
//	public DoubleDataset convertLinePositions(DoubleDataset lines, PowderLineCoord sourceCoords, PowderLineCoord targetCoords) {
//		double lengthRatio = BirchMurnaghanSolver.birchMurnaghanLinear(pressure, bulkModulus, bulkModulus_p);
//		
//		DoubleDataset scaledLines = (lines.getSize() > 0) ? (DoubleDataset) Maths.multiply(lines, lengthRatio) : lines;
//		
//		return super.convertLinePositions(scaledLines, sourceCoords, targetCoords);
//	}

}

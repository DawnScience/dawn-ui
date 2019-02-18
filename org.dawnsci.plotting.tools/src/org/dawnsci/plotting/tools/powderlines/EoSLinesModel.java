/*-
 * Copyright (c) 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.powderlines;

import org.dawnsci.plotting.tools.powderlines.PowderLineTool.PowderDomains;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.scisoft.analysis.crystallography.BirchMurnaghanSolver;

/**
 * A specialization of {@link PowderLinesModel}, additionally holding equation of state data
 * @author Timothy Spain, timothy.spain@diamond.ac.uk
 *
 */
public class EoSLinesModel extends PowderLinesModel {

	private double bulkModulus; // in Pa
	private double bulkModulus_p; // in Pa 

	private double pressure; // in Pa
	
	private String comment;
	
	private double linearRatio;
	private double vexp_v0;
	
	public EoSLinesModel() {
		pressure = 0.0;
		vexp_v0 = 1.0;
	}
	
	/**
	 * @return the bulk modulus of the material, in Pa
	 */
	public double getBulkModulus() {
		return bulkModulus;
	}

	/**
	 * @param bulkModulus
	 * 					the bulk modulus of the material to set, in Pa
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
		updateLengthScales();
	}

//	@Override
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	@Override
	public String getDescription() {
		return this.comment;
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
		compo.setModel(this);
		compo.setTool(tool);
		compo.setPressureMultiplierMagnitude(9);
		
		compo.setModulus(bulkModulus);
		compo.setModulusDerivative(bulkModulus_p);
		compo.setPressure(this.pressure);
		
		return compo;
	}
	
	@Override
	protected PowderLineModel getLineModel(double vaccuumDSpacing) {
		return new EoSLineModel(vaccuumDSpacing);
	}
	
	// Update the length scales based on the pressure and experimental
	// unit cell ratio
	private void updateLengthScales() {
		linearRatio = BirchMurnaghanSolver.birchMurnaghanLinear(pressure, bulkModulus, bulkModulus_p);
		linearRatio *= Math.cbrt(vexp_v0);
		for (PowderLineModel lineModel: lineModels) {
			if (lineModel instanceof EoSLineModel) {
				((EoSLineModel) lineModel).setLengthRatio(linearRatio);
			}
		}
	}
	
	/**
	 * Returns the last calculated value of the linear scaling ratio. 
	 * @return Linear scale of the model.
	 */
	public double getLengthRatio() {
		return this.linearRatio;
	}
	
	/**
	 * Sets the ratio of the experimentally observed unit cell volume.
	 * to the theoretical value.
	 * @param vexpv0
	 * 				Value of V₀(exp)/V₀ to set.
	 */
	public void setVexpV0(double vexpv0) {
		this.vexp_v0 = vexpv0;
		updateLengthScales();
	}
	
	/**
	 * Returns the current value of the ratio of the experimentally
	 * observed unit cell volume.
	 * @return Value of V₀(exp)/V₀
	 */
	public double getVexpV0() {
		return vexp_v0;
	}
}

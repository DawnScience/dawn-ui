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
 * A generic class for the powder line data. Encapsulates a single
 * line, and allows retrieval of its position in any coordinate.
 * @author Timothy Spain, timothy.spain@diamond.ac.uk
 *
 */
public class PowderLineModel {

	private double dSpacing; // The fundamental value
	private double beamWavelength;
	
	/**
	 * Default constructor, d-spacing = 0
	 */
	public PowderLineModel() {
		dSpacing = 0.0;
	}
	
	/**
	 * Constructor allowing definition of the d-spacing
	 * @param dSpacing
	 * 				d-spacing value in Å
	 */
	public PowderLineModel(double dSpacing) {
		this.dSpacing = dSpacing;
	}
	
	/**
	 * Sets the wavelength of the incident beam.
	 * <p>
	 * Used in transformation from d-spacing to scattering angle.
	 * @param beamWavelength
	 * 						Incident beam wavelength in Å.
	 */
	public void setWavelength(double beamWavelength) {
		this.beamWavelength = beamWavelength;
	}
	
	/**
	 * Gets the d-spacing value
	 * @return d-spacing value in Å
	 */
	public double getDSpacing() {
		return dSpacing;
	}
	
	/**
	 * Gets the momentum transfer (Q) associated with this line.
	 * @return momentum transfer in Å⁻¹
	 */
	public double getQ() {
		return 2*Math.PI/getDSpacing();
	}
	
	/**
	 * Gets the angle to which this line scatters the defined beam.
	 * 
	 * @param beamWavelength
	 * 						wavelength of the incident beam in Å
	 * @return the scattering angle in degrees of arc.
	 */
	public double get2Theta(double beamWavelength) {
		return twoThetaFromD(getDSpacing(), beamWavelength);
	}
	
	/**
	 * Gets the position of the line in the requested coordinate.
	 * @param coord
	 * 				coordinate in which to represent the line
	 * @return Position of the line in the requested coordinate
	 */
	public double get(PowderLinesModel.PowderLineCoord coord) {
		switch (coord) {
		case ANGLE:
			return get2Theta(this.beamWavelength);
		case Q:
			return getQ();
		case D_SPACING:
		default:
			return getDSpacing();
		}
	}
	
	/**
	 * Calculates the scattering angle of a line.
	 * <p>
	 * The units of the two parameters do not matter, as long as they
	 * are both the same. They will usually both be in Å.
	 * @param dSpacing
	 * 				d-spacing of the line
	 * @param beamWavelength
	 * 						wavelength of the beam that is scattered
	 * @return Scattering angle in degrees of arc.
	 */
	protected static double twoThetaFromD(double dSpacing, double beamWavelength) {
		return Math.toDegrees(2*Math.asin(beamWavelength/2/dSpacing));
	}
}

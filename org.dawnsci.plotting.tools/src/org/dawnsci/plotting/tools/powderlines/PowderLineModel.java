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
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.Maths;

/**
 * 
 * @author Timothy Spain, timothy.spain@diamond.ac.uk 
 *
 * A model holding the data and state for the powder line tool.
 */
public class PowderLineModel {

	// Q in per Angstrom, ANGLE in degrees, D_SPACING in angstroms
	public enum PowderLineCoord {
		Q, ANGLE, D_SPACING
	}

	// hc in keV angstroms
	final private static double hc_keVAA = 12.398_419_739;
	
	
	private double wavelength; // in angstroms
	private PowderLineCoord dataCoords;
	private DoubleDataset lineLocations;
	
	public PowderLineModel() {
		// default to the typical wavelength of I15-1
		this.wavelength = hc_keVAA/76.6;
		// Probably d-spacing
		this.dataCoords = PowderLineCoord.D_SPACING;
		// and empty dataset of zeros
		this.lineLocations = DatasetFactory.zeros(0);
	}
	
	/**
	 * @return the coordinates of the line data
	 */
	public PowderLineCoord getCoords() {
		return dataCoords;
	}
	
	/**
	 * @return false, no equation of state metadata is present
	 */
	public boolean hasEoSMetadata() {
		return false;
	}
	
	/**
	 * @return the domain specific data to display with models of this type
	 */
	public PowderLineTool.PowderDomains getDomain() {
		return PowderDomains.POWDER;
	}
	
	/**
	 * @param dataCoords
	 * 				the coordinates to set for the line data
	 */
	public void setCoords(PowderLineCoord coords) {
		this.dataCoords = coords;
	}
	/**
	 * @param energy_keV
	 * 					the energy of the beam (in keV) to set
	 */
	public void setEnergy(double energy_keV) {
		this.wavelength = hc_keVAA / energy_keV;
	}
	/**
	 * @return the energy of the beam (in keV)
	 */
	public double getEnergy() {
		return hc_keVAA / this.wavelength;
	}
	/**
	 * @param wavelength
	 * 					beam wavelength in angstroms
	 */
	public void setWavelength(double wavelength){
		this.wavelength = wavelength;
	}
	/**
	 * @return beam wavelength in angstroms
	 */
	public double getWavelength() {
		return this.wavelength;
	}
	
	/**
	 * @param lines
	 * 			 the positions of the lines to be set
	 */
	public void setLines(DoubleDataset lines) {
		this.lineLocations = lines;
	}
	/**
	 * clear the current set of lines
	 */
	public void clearLines( ) {
		this.setLines(DatasetFactory.createRange(0));
	}
	/**
	 * @return the raw line data
	 */
	public DoubleDataset getLines() {
		return lineLocations;
	}
	/**
	 * Return the lines in the requested coordinates with any corrections applied 
	 * @param requestedCoords
	 * 						the coordinates in which to return the line positions
	 * @return the line positions in the requested coordinates
	 */
	public DoubleDataset getLines(PowderLineCoord requestedCoords) {
		// if the data coords are not defined (probably a there are no actual line positions), just return the raw line positions
		if (dataCoords == null) return lineLocations;
		return convertLinePositions(lineLocations, dataCoords, requestedCoords);
	}
	
	/**
	 * Convert between different coordinates of the line positions.
	 * @param lines
	 * 		line positions to convert		
	 * @param sourceCoords
	 * 					coordinates of the lines to be converted
	 * @param targetCoords
	 * 					coordinates for the lines to be converted to
	 * @return the converted values of the line positions
	 */
	public DoubleDataset convertLinePositions(DoubleDataset lines, PowderLineCoord sourceCoords, PowderLineCoord targetCoords) {
		if (targetCoords == sourceCoords || lines.getSize() == 0) {
			return lines;
		} else {
			// convert everything to D spacing
			DoubleDataset dSpaceValues;
			switch(sourceCoords) {
			case Q :
				dSpaceValues = (DoubleDataset) Maths.divide(2*Math.PI, lines);
				break;
			case ANGLE:
				dSpaceValues = (DoubleDataset) Maths.divide(this.wavelength/2, Maths.sin(Maths.multiply(Maths.toRadians(lines), 0.5)));
				break;
			case D_SPACING:
			default:
				dSpaceValues = lines;
			}
			// Convert from d spacing to the requested coords
			switch(targetCoords) {
			case Q:
				return (DoubleDataset) Maths.divide(2*Math.PI, dSpaceValues);
			case ANGLE:
				return (DoubleDataset) Maths.toDegrees(Maths.multiply(2, Maths.arcsin(Maths.divide(this.wavelength/2, dSpaceValues))));
			case D_SPACING:
			default:
				return dSpaceValues;
				
			}
		}
	}
	
	public double convertLinePositions(double line, PowderLineCoord sourceCoords, PowderLineCoord targetCoords) {
		return this.convertLinePositions((DoubleDataset) DatasetFactory.createFromObject(line, new int[]{1}), sourceCoords, targetCoords).get(0);
	}
}

/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.powderintegration;

import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;

public class PowderCorrectionModel extends AbstractOperationModel {

	boolean isApplySolidAngleCorrection = false;
	boolean isApplyPolarisationCorrection = false;
	boolean isAppyDetectorTransmissionCorrection = false;
	double polarisationFactor = 0.9;
	double polarisationAngularOffset = 0;
	double transmittedFraction = 0;
	
	public boolean isApplySolidAngleCorrection() {
		return isApplySolidAngleCorrection;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isApplyPolarisationCorrection ? 1231 : 1237);
		result = prime * result + (isApplySolidAngleCorrection ? 1231 : 1237);
		result = prime * result
				+ (isAppyDetectorTransmissionCorrection ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(polarisationAngularOffset);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(polarisationFactor);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(transmittedFraction);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	public boolean isApplyPolarisationCorrection() {
		return isApplyPolarisationCorrection;
	}
	public double getPolarisationFactor() {
		return polarisationFactor;
	}
	public double getPolarisationAngularOffset() {
		return polarisationAngularOffset;
	}
	public void setApplySolidAngleCorrection(boolean isApplySolidAngleCorrection) {
		firePropertyChange("isApplySolidAngleCorrection", this.isApplySolidAngleCorrection, this.isApplySolidAngleCorrection = isApplySolidAngleCorrection);
	}
	public void setApplyPolarisationCorrection(boolean isApplyPolarisationCorrection) {
		firePropertyChange("isApplyPolarisationCorrection", this.isApplyPolarisationCorrection, this.isApplyPolarisationCorrection = isApplyPolarisationCorrection);
	}
	public void setPolarisationFactor(double polarisationFactor) {
		firePropertyChange("polarisationFactor", this.polarisationFactor, this.polarisationFactor = polarisationFactor);
	}
	public void setPolarisationAngularOffset(double polarisationAngularOffset) {
		firePropertyChange("polarisationAngularOffset", this.polarisationAngularOffset, this.polarisationAngularOffset = polarisationAngularOffset);
	}
	
	public boolean isAppyDetectorTransmissionCorrection() {
		return isAppyDetectorTransmissionCorrection;
	}
	public void setAppyDetectorTransmissionCorrection(
			boolean isAppyDetectorTransmissionCorrection) {
		firePropertyChange("isAppyDetectorTransmissionCorrection", this.isAppyDetectorTransmissionCorrection, this.isAppyDetectorTransmissionCorrection = isAppyDetectorTransmissionCorrection);
	}
	public double getTransmittedFraction() {
		return transmittedFraction;
	}
	public void setTransmittedFraction(double transmittedFraction) {
		firePropertyChange("transmittedFraction", this.transmittedFraction, this.transmittedFraction = transmittedFraction);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PowderCorrectionModel other = (PowderCorrectionModel) obj;
		if (isApplyPolarisationCorrection != other.isApplyPolarisationCorrection)
			return false;
		if (isApplySolidAngleCorrection != other.isApplySolidAngleCorrection)
			return false;
		if (isAppyDetectorTransmissionCorrection != other.isAppyDetectorTransmissionCorrection)
			return false;
		if (Double.doubleToLongBits(polarisationAngularOffset) != Double
				.doubleToLongBits(other.polarisationAngularOffset))
			return false;
		if (Double.doubleToLongBits(polarisationFactor) != Double
				.doubleToLongBits(other.polarisationFactor))
			return false;
		if (Double.doubleToLongBits(transmittedFraction) != Double
				.doubleToLongBits(other.transmittedFraction))
			return false;
		return true;
	}
	
	@Override
	public PowderCorrectionModel clone(){
		
		PowderCorrectionModel cloned = new PowderCorrectionModel();
		cloned.isApplySolidAngleCorrection = this.isApplySolidAngleCorrection;
		cloned.isApplyPolarisationCorrection = this.isApplyPolarisationCorrection;
		cloned.isAppyDetectorTransmissionCorrection = this.isAppyDetectorTransmissionCorrection;
		cloned.polarisationFactor = this.polarisationFactor;
		cloned.polarisationAngularOffset = this.polarisationAngularOffset;
		cloned.transmittedFraction = this.transmittedFraction;
		
		return cloned;
	}
}

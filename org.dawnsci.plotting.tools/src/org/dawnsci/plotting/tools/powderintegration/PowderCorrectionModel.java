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
}

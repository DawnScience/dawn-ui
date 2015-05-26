/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.powderintegration;

import java.util.Arrays;

import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;

import uk.ac.diamond.scisoft.analysis.diffraction.powder.PixelIntegrationUtils.IntegrationMode;
import uk.ac.diamond.scisoft.analysis.roi.XAxis;

public class PowderIntegrationModel extends AbstractOperationModel {

	boolean isAzimuthal = true;
	int numberOfPrimaryBins = 1000;
	int numberOfSecondaryBins = 1000;;
	double[] radialRange = null;
	double[] azimuthalRange = null;
	IntegrationMode integrationMode = IntegrationMode.NONSPLITTING;
	XAxis axisType = XAxis.Q;
	
	public IntegrationMode getIntegrationMode() {
		return integrationMode;
	}
	public void setIntegrationMode(IntegrationMode integrationMode) {
		firePropertyChange("integrationMode", this.integrationMode, this.integrationMode = integrationMode);
	}
	public XAxis getAxisType() {
		return axisType;
	}
	
	public void setAxisType(XAxis axisType) {
		firePropertyChange("axisType", this.axisType, this.axisType = axisType);
	}
	public boolean isAzimuthal() {
		return isAzimuthal;
	}
	public int getNumberOfPrimaryBins() {
		return numberOfPrimaryBins;
	}
	public int getNumberOfSecondaryBins() {
		return numberOfSecondaryBins;
	}
	public double[] getRadialRange() {
		return radialRange;
	}
	public double[] getAzimuthalRange() {
		return azimuthalRange;
	}
	public void setAzimuthal(boolean isAzimuthal) {
		firePropertyChange("isAzimuthal", this.isAzimuthal, this.isAzimuthal = isAzimuthal);
	}
	public void setNumberOfPrimaryBins(int numberOfPrimaryBins) {
		firePropertyChange("numberOfPrimaryBins", this.numberOfPrimaryBins, this.numberOfPrimaryBins = numberOfPrimaryBins);
	}
	public void setNumberOfSecondaryBins(int numberOfSecondaryBins) {
		firePropertyChange("numberOfSecondaryBins", this.numberOfSecondaryBins, this.numberOfSecondaryBins = numberOfSecondaryBins);
	}
	public void setRadialRange(double[] radialRange) {
		firePropertyChange("radialRange", this.radialRange, this.radialRange = radialRange);
	}
	public void setAzimuthalRange(double[] azimuthalRange) {
		firePropertyChange("azimuthalRange", this.azimuthalRange, this.azimuthalRange = azimuthalRange);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((axisType == null) ? 0 : axisType.hashCode());
		result = prime * result + Arrays.hashCode(azimuthalRange);
		result = prime * result
				+ ((integrationMode == null) ? 0 : integrationMode.hashCode());
		result = prime * result + (isAzimuthal ? 1231 : 1237);
		result = prime * result + numberOfPrimaryBins;
		result = prime * result + numberOfSecondaryBins;
		result = prime * result + Arrays.hashCode(radialRange);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PowderIntegrationModel other = (PowderIntegrationModel) obj;
		if (axisType != other.axisType)
			return false;
		if (!Arrays.equals(azimuthalRange, other.azimuthalRange))
			return false;
		if (integrationMode != other.integrationMode)
			return false;
		if (isAzimuthal != other.isAzimuthal)
			return false;
		if (numberOfPrimaryBins != other.numberOfPrimaryBins)
			return false;
		if (numberOfSecondaryBins != other.numberOfSecondaryBins)
			return false;
		if (!Arrays.equals(radialRange, other.radialRange))
			return false;
		return true;
	}
	
	@Override
	public PowderIntegrationModel clone(){
		PowderIntegrationModel cloned = new PowderIntegrationModel();
		cloned.isAzimuthal = this.isAzimuthal;
		cloned.numberOfPrimaryBins = this.numberOfPrimaryBins;
		cloned.numberOfSecondaryBins = this.numberOfSecondaryBins;
		cloned.radialRange = this.radialRange;
		cloned.azimuthalRange = this.azimuthalRange;
		cloned.integrationMode = this.integrationMode;
		cloned.axisType = this.axisType;
		return cloned;
	}
}

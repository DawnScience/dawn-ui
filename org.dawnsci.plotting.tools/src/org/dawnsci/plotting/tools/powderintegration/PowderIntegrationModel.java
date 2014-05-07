package org.dawnsci.plotting.tools.powderintegration;

import org.dawb.common.ui.databinding.AbstractModelObject;

public class PowderIntegrationModel extends AbstractModelObject {

	boolean isAzimuthal;
	int numberOfPrimaryBins;
	int numberOfSecondaryBins;
	double[] radialRange;
	double[] azimuthalRange;
	
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
}

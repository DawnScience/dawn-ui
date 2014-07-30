package org.dawnsci.plotting.tools.powderintegration;

import org.dawnsci.plotting.tools.powderintegration.PowderIntegrationJob.IntegrationMode;

import uk.ac.diamond.scisoft.analysis.processing.model.AbstractOperationModel;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile.XAxis;

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
}

package org.dawnsci.dedi.configuration.preferences;

import java.io.Serializable;

import org.dawnsci.plotting.tools.preference.detector.DiffractionDetector;

/**
 * Represents a beamline configuration in the form that is directly mapped to the fields in {@link BeamlineConfigurationPreferencePage}. 
 */
public class BeamlineConfigurationBean implements Serializable {
	/**
	 * Update this when there are any serious changes to API
	 */
	private static final long serialVersionUID = -1133345866155946032L;
	
	private String name;
	private DiffractionDetector detector;
	private double beamstopDiameter;
	private double beamstopXCentre;
	private double beamstopYCentre;
	private double cameraTubeDiameter;
	private double cameraTubeXCentre;
	private double cameraTubeYCentre;
	private int clearance;
	private double maxWavelength;
	private double minWavelength;
	private double maxCameraLength;
	private double minCameraLength;
	private double cameraLengthStepSize;
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getBeamstopDiameter() {
		return beamstopDiameter;
	}

	public void setBeamstopDiameter(double beamstopDiameter) {
		this.beamstopDiameter = beamstopDiameter;
	}

	public double getBeamstopXCentre() {
		return beamstopXCentre;
	}

	public void setBeamstopXCentre(double beamstopXCentre) {
		this.beamstopXCentre = beamstopXCentre;
	}

	public double getBeamstopYCentre() {
		return beamstopYCentre;
	}

	public void setBeamstopYCentre(double beamstopYCentre) {
		this.beamstopYCentre = beamstopYCentre;
	}
	
	public int getClearance() {
		return clearance;
	}

	public void setClearance(int clearance) {
		this.clearance = clearance;
	}
	
	public double getMaxWavelength() {
		return maxWavelength;
	}


	public void setMaxWavelength(double maxWavelength) {
		this.maxWavelength = maxWavelength;
	}


	public double getMinWavelength() {
		return minWavelength;
	}


	public void setMinWavelength(double minWavelength) {
		this.minWavelength = minWavelength;
	}

	public double getMaxCameraLength() {
		return maxCameraLength;
	}


	public void setMaxCameraLength(double maxCameraLength) {
		this.maxCameraLength = maxCameraLength;
	}


	public double getMinCameraLength() {
		return minCameraLength;
	}


	public void setMinCameraLength(double minCameraLength) {
		this.minCameraLength = minCameraLength;
	}


	public DiffractionDetector getDetector() {
		return detector;
	}


	public void setDetector(DiffractionDetector detector) {
		this.detector = detector;
	}


	public double getCameraTubeDiameter() {
		return cameraTubeDiameter;
	}


	public void setCameraTubeDiameter(double cameraTubeDiameter) {
		this.cameraTubeDiameter = cameraTubeDiameter;
	}


	public double getCameraTubeXCentre() {
		return cameraTubeXCentre;
	}


	public void setCameraTubeXCentre(double cameraTubeXCentre) {
		this.cameraTubeXCentre = cameraTubeXCentre;
	}


	public double getCameraTubeYCentre() {
		return cameraTubeYCentre;
	}


	public void setCameraTubeYCentre(double cameraTubeYCentre) {
		this.cameraTubeYCentre = cameraTubeYCentre;
	}


	public double getCameraLengthStepSize() {
		return cameraLengthStepSize;
	}


	public void setCameraLengthStepSize(double cameraLengthStepSize) {
		this.cameraLengthStepSize = cameraLengthStepSize;
	}
	
	@Override
	public String toString() {
		return name;
	}
}

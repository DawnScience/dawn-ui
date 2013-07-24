package org.dawnsci.plotting.tools.diffraction;

import org.dawnsci.plotting.tools.preference.detector.DiffractionDetector;

import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;

public class DetectorBean {
	private DetectorProperties detprop;
	private DiffractionDetector det;
	
	public DetectorBean(DetectorProperties detprop, DiffractionDetector det) {
		this.detprop = detprop;
		this.det = det;
	}
	
	public DetectorProperties getDetectorProperties() {
		return detprop;
	}
	
	public DiffractionDetector getDiffractionDetector() {
		return det;
	}
}

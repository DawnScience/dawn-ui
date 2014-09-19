/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.diffraction;

import org.dawnsci.plotting.tools.preference.detector.DiffractionDetector;
import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;

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

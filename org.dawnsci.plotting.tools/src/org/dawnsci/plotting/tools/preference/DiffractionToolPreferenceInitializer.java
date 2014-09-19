/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.preference;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class DiffractionToolPreferenceInitializer extends
		AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getPlottingPreferenceStore();
		store.setDefault(DiffractionToolConstants.NUMBER_FORMAT, "#0.0###");
		store.setDefault(DiffractionToolConstants.REMEMBER_DIFFRACTION_META, "prompt");
		store.setDefault(DiffractionToolConstants.PIXEL_SIZE_X,  0.1024);
		store.setDefault(DiffractionToolConstants.PIXEL_SIZE_Y,  0.1024);
		store.setDefault(DiffractionToolConstants.DISTANCE,  200.0);
		store.setDefault(DiffractionToolConstants.DETECTOR_ROTATION_X,  0.0);
		store.setDefault(DiffractionToolConstants.DETECTOR_ROTATION_Y,  0.0);
		store.setDefault(DiffractionToolConstants.DETECTOR_ROTATION_Z,  0.0);
		store.setDefault(DiffractionToolConstants.LAMBDA,  0.9);
		store.setDefault(DiffractionToolConstants.START_OMEGA,  0.0);
		store.setDefault(DiffractionToolConstants.RANGE_OMEGA,  1.0);
		store.setDefault(DiffractionToolConstants.EXPOSURE_TIME,  1.0);
		store.setDefault(DiffractionToolConstants.BEAM_CENTRE_X,  0.0);
		store.setDefault(DiffractionToolConstants.BEAM_CENTRE_Y,  0.0);
		store.setDefault(DiffractionToolConstants.DETECTOR_YAW,  0.0);
		store.setDefault(DiffractionToolConstants.DETECTOR_PITCH,  0.0);
		store.setDefault(DiffractionToolConstants.DETECTOR_ROLL,  0.0);
	}

}

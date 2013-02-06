package org.dawnsci.plotting.preference;

import org.dawnsci.plotting.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class DiffractionToolPreferenceInitializer extends
		AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
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
	}

}

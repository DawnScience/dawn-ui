package org.dawb.workbench.plotting.tools;

import org.dawb.workbench.plotting.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class FittingPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(FittingConstants.PEAK_NUMBER_CHOICES, 10);
		store.setDefault(FittingConstants.PEAK_NUMBER,         1);
		store.setDefault(FittingConstants.FIT_SMOOTH_FACTOR,   4);
		store.setDefault(FittingConstants.SHOW_FWHM_SELECTIONS,true);
		store.setDefault(FittingConstants.SHOW_PEAK_SELECTIONS,true);
		store.setDefault(FittingConstants.SHOW_FITTING_TRACE,  true);
		store.setDefault(FittingConstants.SHOW_ANNOTATION_AT_PEAK,  true);

	}

}

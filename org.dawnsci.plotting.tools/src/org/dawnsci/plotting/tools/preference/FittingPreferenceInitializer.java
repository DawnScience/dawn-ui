package org.dawnsci.plotting.tools.preference;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import uk.ac.diamond.scisoft.analysis.fitting.FittingConstants;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;

public class FittingPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		
		IPreferenceStore store = Activator.getPlottingPreferenceStore();
		store.setDefault(FittingConstants.PEAK_NUMBER_CHOICES, 10);
		store.setDefault(FittingConstants.PEAK_NUMBER,         1);
		store.setDefault(FittingConstants.FIT_SMOOTH_FACTOR,   4);
		store.setDefault(FittingConstants.SHOW_FWHM_SELECTIONS,true);
		store.setDefault(FittingConstants.SHOW_PEAK_SELECTIONS,true);
		store.setDefault(FittingConstants.SHOW_FITTING_TRACE,  true);
		store.setDefault(FittingConstants.SHOW_ANNOTATION_AT_PEAK,  true);
		store.setDefault(FittingConstants.PEAK_TYPE,  Gaussian.class.getName());
		store.setDefault(FittingConstants.SMOOTHING, 1);
		store.setDefault(FittingConstants.QUALITY,   0.01);
		store.setDefault(FittingConstants.POLY_ORDER,   1);
		store.setDefault(FittingConstants.POLY_CHOICES,   7);
		store.setDefault(FittingConstants.SHOW_POLY_TRACE,  true);
		store.setDefault(FittingConstants.SHOW_POLY_RANGE,  true);
		store.setDefault(FittingConstants.INT_FORMAT,   "###0");
		store.setDefault(FittingConstants.REAL_FORMAT,  "##0.#####");
	}

}

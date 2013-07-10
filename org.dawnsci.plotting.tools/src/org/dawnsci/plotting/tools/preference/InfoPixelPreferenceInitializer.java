package org.dawnsci.plotting.tools.preference;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class InfoPixelPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {

		IPreferenceStore store = Activator.getPlottingPreferenceStore();
		store.setDefault(InfoPixelConstants.PIXEL_POS_FORMAT,  "###0");
		store.setDefault(InfoPixelConstants.DATA_FORMAT,       "##0.####");
		store.setDefault(InfoPixelConstants.Q_FORMAT,          "##0.####");
		store.setDefault(InfoPixelConstants.THETA_FORMAT,      "##0.###");
		store.setDefault(InfoPixelConstants.RESOLUTION_FORMAT, "##0.####");
	}
}

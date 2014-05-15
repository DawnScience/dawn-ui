package org.dawnsci.plotting.tools.preference;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class RegionEditorPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getPlottingPreferenceStore();
		store.setDefault(RegionEditorConstants.MOBILE_REGION_SETTING, true);
		store.setDefault(RegionEditorConstants.POINT_FORMAT,     "##0");
		store.setDefault(RegionEditorConstants.ANGLE_FORMAT,     "##0");
		store.setDefault(RegionEditorConstants.INTENSITY_FORMAT, "0.######E0#");
		store.setDefault(RegionEditorConstants.SUM_FORMAT,       "0.######E0#");
	}
}

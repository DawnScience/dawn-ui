package org.dawnsci.datavis.view.preference;

import org.dawnsci.datavis.view.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class DataVisPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore ps = Activator.getDefault().getPreferenceStore();
		ps.setDefault(DataVisPreferenceConstants.SIGNALS_ONLY, false);
		
	}

}

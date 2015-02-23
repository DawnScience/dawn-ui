package org.dawnsci.plotting.tools.preference;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class CrossProfileInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getLocalPreferenceStore();
		store.setDefault(CrossProfileConstants.DO_Z,    true);
		store.setDefault(CrossProfileConstants.PLUS_X,  4);
		store.setDefault(CrossProfileConstants.MINUS_X, 4);
		store.setDefault(CrossProfileConstants.PLUS_Z,  4);
		store.setDefault(CrossProfileConstants.MINUS_Z, 4);
		store.setDefault(CrossProfileConstants.Z_DIM,   0);
	}

}

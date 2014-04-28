package org.dawnsci.breadcrumb.navigation.preference;

import org.dawnsci.breadcrumb.navigation.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public PreferenceInitializer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initializeDefaultPreferences() {
		
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(NavigationConstants.SEARCH_TYPE, NavigationConstants.NONE);
		store.setDefault(NavigationConstants.SHOW_MODE_LABEL,   false); 

	}

}

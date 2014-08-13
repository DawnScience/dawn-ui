package org.dawnsci.slicing.preference;

import org.dawnsci.slicing.Activator;
import org.dawnsci.slicing.component.SliceConstants;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public PreferenceInitializer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		store.setDefault(SliceConstants.SLICE_EDITOR,     0);
		store.setDefault(SliceConstants.SHOW_HINTS,       true);

	}

}

package org.dawnsci.common.widgets.filedataset;

import org.dawnsci.common.widgets.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class FileDatasetPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		preferenceStore.setDefault(FileDatasetPreferences.FILE_DATASET_COMPOSITE_PREFS_INITIAL_FILE, System.getProperty("user.home"));
	}

}

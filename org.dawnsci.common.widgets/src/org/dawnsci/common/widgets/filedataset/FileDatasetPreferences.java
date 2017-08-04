package org.dawnsci.common.widgets.filedataset;

import java.io.File;

import org.dawnsci.common.widgets.Activator;
import org.eclipse.jface.preference.IPreferenceStore;

public class FileDatasetPreferences {

	static final String FILE_DATASET_COMPOSITE_PREFS_INITIAL_FILE = "filedatasetcomposite.prefs.initialfile";

	private FileDatasetPreferences() {
		
	}
	
	static File getInitialFileFromPreferences() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		String string = preferenceStore.getString(FILE_DATASET_COMPOSITE_PREFS_INITIAL_FILE);
		return new File(string);
	}
	
	static void setInitialFileInPreferences(final File initialFile) {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		preferenceStore.setValue(FILE_DATASET_COMPOSITE_PREFS_INITIAL_FILE, initialFile.getAbsolutePath());
	}
}

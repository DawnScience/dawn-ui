package org.dawnsci.processing.python.ui.preferences;

import org.dawnsci.processing.python.ui.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class SavuProcessingPreferenceInitializer extends
		AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore ps = Activator.getDefault().getPreferenceStore();
		ps.setDefault(SavuProcessingConstants.REMOTE_RUNNER_URI, "tcp://sci-serv5.diamond.ac.uk:61616");
		ps.setDefault(SavuProcessingConstants.FORCE_SERIES, false);
	}

}

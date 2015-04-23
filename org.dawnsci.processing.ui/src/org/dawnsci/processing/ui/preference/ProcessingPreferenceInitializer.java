package org.dawnsci.processing.ui.preference;

import org.dawnsci.processing.ui.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.dawnsci.analysis.api.processing.ExecutionType;
import org.eclipse.jface.preference.IPreferenceStore;

public class ProcessingPreferenceInitializer extends
		AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore ps = Activator.getDefault().getPreferenceStore();
		ps.setDefault(ProcessingConstants.EXECUTION_TYPE, ExecutionType.SERIES.toString());
		ps.setDefault(ProcessingConstants.POOL_SIZE, 1);
		
	}

}

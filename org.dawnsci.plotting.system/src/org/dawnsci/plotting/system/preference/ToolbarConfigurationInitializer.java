package org.dawnsci.plotting.system.preference;

import org.dawnsci.plotting.api.preferences.ToolbarConfigurationConstants;
import org.dawnsci.plotting.system.PlottingSystemActivator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class ToolbarConfigurationInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = PlottingSystemActivator.getLocalPreferenceStore();
		
		for (ToolbarConfigurationConstants constant : ToolbarConfigurationConstants.values()) {
			store.setDefault(constant.getId(),        true);
		}

	}

}

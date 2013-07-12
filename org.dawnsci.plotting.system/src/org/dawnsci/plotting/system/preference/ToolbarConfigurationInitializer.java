package org.dawnsci.plotting.system.preference;

import org.dawnsci.plotting.system.PlottingSystemActivator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class ToolbarConfigurationInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = PlottingSystemActivator.getLocalPreferenceStore();
		
		store.setDefault(ToolbarConfigurationConstants.CONFIG.getId(),        true);
		store.setDefault(ToolbarConfigurationConstants.ANNOTATION.getId(),    true);
		store.setDefault(ToolbarConfigurationConstants.TOOLS.getId(),         true);
		store.setDefault(ToolbarConfigurationConstants.AXIS.getId(),          true);
		store.setDefault(ToolbarConfigurationConstants.REGION.getId(),        true);
		store.setDefault(ToolbarConfigurationConstants.ZOOM.getId(),          true);
		store.setDefault(ToolbarConfigurationConstants.UNDO.getId(),          true);
		store.setDefault(ToolbarConfigurationConstants.EXPORT.getId(),        true);
		store.setDefault(ToolbarConfigurationConstants.HISTO.getId(),         true);
		store.setDefault(ToolbarConfigurationConstants.PALETTE.getId(),       true);
		store.setDefault(ToolbarConfigurationConstants.ORIGIN.getId(),        true);
		store.setDefault(ToolbarConfigurationConstants.MISCELLANEOUS.getId(), true);

	}

}

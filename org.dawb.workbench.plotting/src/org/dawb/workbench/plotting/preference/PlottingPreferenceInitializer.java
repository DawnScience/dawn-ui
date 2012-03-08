/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.workbench.plotting.preference;

import org.dawb.workbench.plotting.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;


public class PlottingPreferenceInitializer extends AbstractPreferenceInitializer {

		
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		//store.setDefault(PlottingConstants.PLOTTING_SYSTEM_CHOICE, "org.dawb.workbench.editors.plotting.lightWeightPlottingSystem");
        store.setDefault(PlottingConstants.PLOT_X_DATASET,          false);
		store.setDefault(PlottingConstants.XY_SHOWLEGEND,  true);
	}
}
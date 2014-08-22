/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.workbench.ui.editors.preference;

import org.dawb.workbench.ui.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;


public class EditorPreferenceInitializer extends AbstractPreferenceInitializer {

		
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(EditorConstants.IGNORE_DATASET_FILTERS, false);
		store.setDefault(EditorConstants.SHOW_XY_COLUMN,         true);
		store.setDefault(EditorConstants.SHOW_DATA_SIZE,         false);
		store.setDefault(EditorConstants.SHOW_DIMS,              false);
		store.setDefault(EditorConstants.SHOW_VARNAME,           false);
		store.setDefault(EditorConstants.SHOW_SHAPE,             true);
		store.setDefault(EditorConstants.SHOW_LOCALNAME,         true);
		store.setDefault(EditorConstants.DATA_FORMAT,            "#0.00");
		store.setDefault(EditorConstants.PLAY_SPEED,             1500);
		store.setDefault(EditorConstants.PLOTTING_SYSTEM_CHOICE, "org.dawb.workbench.editors.plotting.lightWeightPlottingSystem");
        store.setDefault(EditorConstants.HIGHLIGHT_ACTORS_CHOICE, true);
        
		store.setDefault(EditorConstants.SAVE_SEL_DATA,           true);
		store.setDefault(EditorConstants.SAVE_LOG_FORMAT,         false);
		store.setDefault(EditorConstants.SAVE_TIME_FORMAT,        false);
		store.setDefault(EditorConstants.SAVE_FORMAT_STRING,      false);
		store.setDefault(EditorConstants.RESCALE_SETTING,         true);
		store.setDefault(EditorConstants.PLOT_DATA_NAME_WIDTH,    180);
	}
}
/*
 * Copyright (c) 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.fileviewer.preferences;

import org.dawnsci.fileviewer.Activator;
import org.dawnsci.fileviewer.FileViewerConstants;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class FileViewerPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		// File viewer preferences
		store.setDefault(FileViewerConstants.SHOW_SIZE_COLUMN, true);
		store.setDefault(FileViewerConstants.SHOW_TYPE_COLUMN, true);
		store.setDefault(FileViewerConstants.SHOW_MODIFIED_COLUMN, true);
		store.setDefault(FileViewerConstants.SHOW_SCANCMD_COLUMN, false);
	}
}

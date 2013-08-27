/*-
 * Copyright (c) 2013 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.workbench.ui.preference;

import org.dawb.workbench.ui.Activator;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class is to represent global Dawn preferences.
 * It provides a root node for the other Dawn preference pages
 */
public class DataAnalysisPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {
	/**
	 * 
	 */
	public DataAnalysisPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Data Analysis Preferences (see sub pages)");
	}

	@Override
	protected void createFieldEditors() {
	}

	@Override
	public void init(IWorkbench workbench) {
	}
}

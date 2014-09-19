/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.spectrum.ui.preferences;

import org.dawnsci.spectrum.ui.Activator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class SpectrumPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	
	SpectrumNameListEditor lx;
	SpectrumNameListEditor ly;
	public static final String ID = "org.dawnsci.spectrum.ui.preferences.page";

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		
		Label instructions = new Label(parent, SWT.NONE);
		instructions.setText("Set the default names of x and y datasets.\n" +
				"Lists will be searched in order, taking the first matching name found.\n" +
				"Use * for wildcard matching.");
		
		lx = new SpectrumNameListEditor(SpectrumConstants.X_DATASETS, "X-Dataset Name", parent);
		//le.setPreferenceName(SpectrumConstants.X_DATASETS);
		lx.setPreferenceStore(getPreferenceStore());
		lx.load();
		
		ly = new SpectrumNameListEditor(SpectrumConstants.Y_DATASETS, "Y-Dataset Name", parent);
		//le.setPreferenceName(SpectrumConstants.X_DATASETS);
		ly.setPreferenceStore(getPreferenceStore());
		ly.load();
		return null;
	}
	
	@Override
	protected void performDefaults() {
		lx.loadDefault();
		ly.loadDefault();
	}
	
	@Override
	public boolean performOk(){
		lx.store();
		ly.store();
		
		return super.performOk();
	}
}

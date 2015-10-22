/*
 * Copyright (c) 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.histogram.preferences;

import java.util.Collection;

import org.dawnsci.plotting.histogram.Activator;
import org.eclipse.dawnsci.plotting.api.histogram.IPaletteService;
import org.eclipse.dawnsci.plotting.api.preferences.PlottingConstants;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class HistogramPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "org.dawnsci.plotting.histogram.colourMapPrefences";

	private Combo cmbColourMap;

	private IPaletteService pservice = (IPaletteService)PlatformUI.getWorkbench().getService(IPaletteService.class);
	private String schemeName;

	public HistogramPreferencePage() {
	}

	public HistogramPreferencePage(String title) {
		super(title);
	}

	public HistogramPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));

		Label lblColourMap = new Label(comp, SWT.LEFT);
		lblColourMap.setText("Default colour mapping:");
		cmbColourMap = new Combo(comp, SWT.RIGHT | SWT.READ_ONLY);
		// Get all information from the IPalette service
		final Collection<String> colours = pservice.getColorSchemes();
		schemeName = getColourMapChoicePreference();
		int i = 0;
		for (String colour : colours) {
			cmbColourMap.add(colour);
			if (!colour.equals(schemeName)) i++;
		}
		cmbColourMap.select(i);
		cmbColourMap.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				schemeName = cmbColourMap.getText();
			}
		});

		initializePage();

		return comp;
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getPlottingPreferenceStore());
	}

	@Override
	public boolean performOk() {
		storePreferences();
		return true;
	}

	@Override
	protected void performDefaults() {
		loadDefaultPreferences();
	}

	/**
	 * Load the resolution value
	 */
	private void initializePage() {
		cmbColourMap.select(cmbColourMap.indexOf(getColourMapChoicePreference()));
	}

	/**
	 * Load the default resolution value
	 */
	private void loadDefaultPreferences() {
		cmbColourMap.select(cmbColourMap.indexOf(getDefaultColourMapChoicePreference()));
	}

	private String getColourMapChoicePreference() {
		if (Activator.getPlottingPreferenceStore().isDefault(PlottingConstants.COLOUR_SCHEME)) {
			return Activator.getPlottingPreferenceStore().getDefaultString(PlottingConstants.COLOUR_SCHEME);
		}
		return Activator.getPlottingPreferenceStore().getString(PlottingConstants.COLOUR_SCHEME);
	}

	/**
	 * Store the resolution value
	 */
	private void storePreferences() {
		setColourMapChoicePreference(cmbColourMap.getItem(cmbColourMap.getSelectionIndex()));
	}

	private String getDefaultColourMapChoicePreference() {
		return Activator.getPlottingPreferenceStore().getDefaultString(PlottingConstants.COLOUR_SCHEME);
	}

	private void setColourMapChoicePreference(String value) {
		Activator.getPlottingPreferenceStore().setValue(PlottingConstants.COLOUR_SCHEME, value);
	}

}

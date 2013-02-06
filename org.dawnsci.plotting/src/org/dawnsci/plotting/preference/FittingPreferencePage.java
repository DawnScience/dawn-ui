/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawnsci.plotting.preference;

import org.dawnsci.common.widgets.spinner.FloatSpinner;
import org.dawnsci.plotting.Activator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


public class FittingPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "org.dawb.workbench.plotting.fittingPreferencePage";
	
	private FloatSpinner accuracy;
	private Spinner smoothing;
	private Spinner peakNumber;

	public FittingPreferencePage() {

	}

	/**
	 * @wbp.parser.constructor
	 */
	public FittingPreferencePage(String title) {
		super(title);
	}

	public FittingPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		GridData gdc = new GridData(SWT.FILL, SWT.FILL, true, true);
		comp.setLayoutData(gdc);

		Group algGroup = new Group(comp, SWT.NONE);
		algGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		algGroup.setLayout(new GridLayout(2, false));
		algGroup.setText("Algorithm Controls");


		Label accuractlab = new Label(algGroup, SWT.NONE);
		accuractlab.setText("Accuracy");
		accuractlab.setToolTipText("This sets the accuracy of the optomisation. "
				+ "The lower the number to more accurate the calculation");

		accuracy = new FloatSpinner(algGroup, SWT.NONE, 6, 5);
		accuracy.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		accuracy.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				storePreferences();
			}			
		});
		
		Label smoothingLab = new Label(algGroup, SWT.NONE);
		smoothingLab.setText("Smoothing");
		smoothingLab.setToolTipText("Smoothing over that many data points will be applied by the peak searching algorithm");

		smoothing = new Spinner(algGroup, SWT.NONE);
		smoothing.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		smoothing.setDigits(0);
		smoothing.setMinimum(0);
		smoothing.setMaximum(10000);
		smoothing.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				storePreferences();
			}			
		});
		
		
		Label peaksLab = new Label(algGroup, SWT.NONE);
		peaksLab.setText("Number of peaks");
		peaksLab.setToolTipText("The peak number to fit");

		peakNumber = new Spinner(algGroup, SWT.NONE);
		peakNumber.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		peakNumber.setDigits(0);
		peakNumber.setMinimum(1);
		peakNumber.setMaximum(1000);
		peakNumber.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				storePreferences();
			}			
		});

		
		// initialize
		initializePage();
		return comp;
	}

	private void initializePage() {
		accuracy.setDouble(getAccuracy());
		smoothing.setSelection(getSmoothing());
		peakNumber.setSelection(getPeakNumber());
	}

	@Override
	public boolean performOk() {
		storePreferences();
		return true;
	}

	@Override
	protected void performDefaults() {
		accuracy.setDouble(getDefaultAccuracy());
		smoothing.setSelection(getDefaultSmoothing());
		peakNumber.setSelection(getDefaultPeakNumber());
	}


	private void storePreferences() {
		setAccuracy(accuracy.getDouble());
		setSmoothing(smoothing.getSelection());
		setPeakNumber(peakNumber.getSelection());
	}


	private int getDefaultSmoothing() {
		return getPreferenceStore().getDefaultInt(FittingConstants.SMOOTHING);
	}
	
	private int getDefaultPeakNumber() {
		return getPreferenceStore().getDefaultInt(FittingConstants.PEAK_NUMBER);
	}

	private double getDefaultAccuracy() {
		return getPreferenceStore().getDefaultDouble(FittingConstants.QUALITY);
	}


	public int getSmoothing() {
		return getPreferenceStore().getInt(FittingConstants.SMOOTHING);
	}
	
	public int getPeakNumber() {
		return getPreferenceStore().getInt(FittingConstants.PEAK_NUMBER);
	}

	public void setSmoothing(int smooth) {
		getPreferenceStore().setValue(FittingConstants.SMOOTHING, smooth);
	}
	public void setPeakNumber(int num) {
		getPreferenceStore().setValue(FittingConstants.PEAK_NUMBER, num);
	}

	public double getAccuracy() {
		return getPreferenceStore().getDouble(FittingConstants.QUALITY);
	}

	public void setAccuracy(double accuracy) {
		getPreferenceStore().setValue(FittingConstants.QUALITY, accuracy);
	}
}

/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.tools.preference;

import java.text.DecimalFormat;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.dawnsci.common.widgets.spinner.FloatSpinner;
import org.dawnsci.plotting.tools.Activator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.FittingConstants;
import uk.ac.diamond.scisoft.analysis.fitting.FittingConstants.FIT_ALGORITHMS;

public class FittingPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	public static final String ID = "org.dawb.workbench.plotting.fittingPreferencePage";

	private static final Logger logger = LoggerFactory.getLogger(FittingPreferencePage.class);

	private FloatSpinner accuracy;
	private Spinner smoothing;
	private Spinner peakNumber;
	private Text realFormat;
	private Text accuracyValueText;
	private CCombo algorithmCombo;
	private BidiMap<Integer, FIT_ALGORITHMS> algorithmComboMap;

	public FittingPreferencePage() {
		super();
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
		setPreferenceStore(Activator.getPlottingPreferenceStore());
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
		algGroup.setText("Peak Fitting Algorithm Controls");

		Label accuractlab = new Label(algGroup, SWT.NONE);
		accuractlab.setText("Accuracy");
		accuractlab
				.setToolTipText("This sets the accuracy of the optimisation. "
						+ "The lower the number to more accurate the calculation");

		accuracy = new FloatSpinner(algGroup, SWT.BORDER, 6, 5);
		accuracy.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		accuracy.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				storePreferences();
			}
		});

		Label smoothingLab = new Label(algGroup, SWT.NONE);
		smoothingLab.setText("Smoothing");
		smoothingLab
				.setToolTipText("Smoothing over that many data points will be applied by the peak searching algorithm");

		smoothing = new Spinner(algGroup, SWT.BORDER);
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

		peakNumber = new Spinner(algGroup, SWT.BORDER);
		peakNumber.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		peakNumber.setDigits(0);
		peakNumber.setMinimum(1);
		peakNumber.setMaximum(1000);
		peakNumber.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				storePreferences();
			}
		});

		Group formatGrp = new Group(comp, SWT.NONE);
		formatGrp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		formatGrp.setLayout(new GridLayout(2, false));
		formatGrp.setText("Peak Format");

		Label realFormatLab = new Label(formatGrp, SWT.NONE);
		realFormatLab.setText("Real Format");
		realFormatLab
				.setToolTipText("Format for real numbers shown in the peak fitting table.");

		realFormat = new Text(formatGrp, SWT.BORDER);
		realFormat.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		realFormat.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				storePreferences();
			}
		});

		Group accuracyGroup = new Group(comp, SWT.NONE);
		accuracyGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		accuracyGroup.setLayout(new GridLayout(2, false));
		accuracyGroup.setText("Function Fitting Algorithm Controls");

		Label accuracyInfoLabel = new Label(accuracyGroup, SWT.NONE);
		accuracyInfoLabel.setText("Accuracy of Fitting Routine");
		accuracyValueText = new Text(accuracyGroup, SWT.BORDER);
		accuracyValueText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label algoLabel = new Label(accuracyGroup, SWT.NONE);
		algoLabel.setText("Fitting Algorithm");
		algorithmCombo = new CCombo(accuracyGroup, SWT.BORDER);
		algorithmCombo.setEditable(false);
		algorithmCombo.setListVisible(true);
		algorithmCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		algorithmComboMap = new DualHashBidiMap<Integer, FIT_ALGORITHMS>();
		for (int i = 0; i < FIT_ALGORITHMS.values().length; i++) {
			FIT_ALGORITHMS algorithm = FIT_ALGORITHMS.values()[i];
			algorithmCombo.add(algorithm.NAME);
			algorithmComboMap.put(i, algorithm);
		}

		// initialize
		initializePage();
		return comp;
	}

	private void initializePage() {
		accuracy.setDouble(getAccuracy());
		smoothing.setSelection(getSmoothing());
		peakNumber.setSelection(getPeakNumber());
		realFormat.setText(getRealFormat());
		accuracyValueText.setText(Double.toString(getFitAccuracy()));
		algorithmComboSelect(getPreferenceStore().getInt(FittingConstants.FIT_ALGORITHM));
	}

	private void algorithmComboSelect(int fitAlgorithmId) {
		FIT_ALGORITHMS algorithm = FIT_ALGORITHMS.fromId(fitAlgorithmId);
		Integer selection = algorithmComboMap.getKey(algorithm);
		if (selection != null) {
			algorithmCombo.select(selection);
		}
	}

	protected void checkState() {
		try {
			final DecimalFormat realForm = new DecimalFormat(
					realFormat.getText());
			String ok = realForm.format(1.0);
			if (ok == null || "".equals(ok))
				throw new Exception();
		} catch (Throwable ne) {
			setErrorMessage("The real format is incorrect");
			setValid(false);
			return;
		}
		setErrorMessage(null);
		setValid(true);

	}

	@Override
	public boolean performOk() {
		return storePreferences();
	}

	@Override
	protected void performDefaults() {
		accuracy.setDouble(getDefaultAccuracy());
		smoothing.setSelection(getDefaultSmoothing());
		peakNumber.setSelection(getDefaultPeakNumber());
		realFormat.setText(getDefaultRealFormat());
		accuracyValueText.setText(Double.toString(getDefaultFitAccuracy()));
		algorithmComboSelect(getPreferenceStore().getDefaultInt(FittingConstants.FIT_ALGORITHM));
		isValid();
	}

	private boolean storePreferences() {
		checkState();
		if (!isValid())
			return false;
		setAccuracy(accuracy.getDouble());
		setSmoothing(smoothing.getSelection());
		setPeakNumber(peakNumber.getSelection());
		setRealFormat(realFormat.getText());
		try {
			setFitAccuracy(Double.parseDouble(accuracyValueText.getText()));
		} catch (NumberFormatException e) {
			logger.debug("Invalid accuracy value.",e);
		}
		setFitAlgorithm(algorithmComboMap.get(algorithmCombo.getSelectionIndex()));
		return true;
	}

	@Override
	public boolean isValid() {
		try {
			Double.parseDouble(accuracyValueText.getText());
			setErrorMessage(null);
		} catch (NumberFormatException e) {
			setErrorMessage("Invalid value for accuracy of fitting routine.");
			return false;
		}
		return super.isValid();
	}

	@SuppressWarnings("unused")
	private String getDefaultIntFormat() {
		return getPreferenceStore().getDefaultString(
				FittingConstants.INT_FORMAT);
	}

	private String getDefaultRealFormat() {
		return getPreferenceStore().getDefaultString(
				FittingConstants.REAL_FORMAT);
	}

	@SuppressWarnings("unused")
	private String getIntFormat() {
		return getPreferenceStore().getString(FittingConstants.INT_FORMAT);
	}

	private String getRealFormat() {
		return getPreferenceStore().getString(FittingConstants.REAL_FORMAT);
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

	private double getDefaultFitAccuracy() {
		return getPreferenceStore().getDefaultDouble(FittingConstants.FIT_QUALITY);
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

	public void setIntFormat(String format) {
		getPreferenceStore().setValue(FittingConstants.INT_FORMAT, format);
	}

	public void setRealFormat(String format) {
		getPreferenceStore().setValue(FittingConstants.REAL_FORMAT, format);
	}

	public double getAccuracy() {
		return getPreferenceStore().getDouble(FittingConstants.QUALITY);
	}

	public double getFitAccuracy() {
		return getPreferenceStore().getDouble(FittingConstants.FIT_QUALITY);
	}

	public void setAccuracy(double accuracy) {
		getPreferenceStore().setValue(FittingConstants.QUALITY, accuracy);
	}

	public void setFitAccuracy(double accuracy) {
		getPreferenceStore().setValue(FittingConstants.FIT_QUALITY, accuracy);
	}

	private void setFitAlgorithm(FIT_ALGORITHMS algorithm) {
		if (algorithm != null) {
			getPreferenceStore().setValue(FittingConstants.FIT_ALGORITHM, algorithm.ID);
		}
	}
}

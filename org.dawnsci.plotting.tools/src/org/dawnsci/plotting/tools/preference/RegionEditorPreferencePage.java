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

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


public class RegionEditorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "org.dawb.workbench.plotting.regionEditorPreferencePage";

	private Text pointFormat;
	private Text angleFormat;
	private Text intensityFormat;
	private Text sumFormat;
	private Button regionMoveableCheckbox;
	private final String toolTipText = "Set the number format such as \'###0.#\' where\r" +
										"-\'#\' is a digit \r" +
										"-\'0\' is a digit that will be always displayed (0 if none)\r" +
										"-\'.\' is the decimal point";
	private final String sciToolTipText = "Set the number format such as \'0.##E0#\' where\r" +
										"-\'#\' is a digit \r" +
										"-\'0\' is a digit that will be always displayed (0 if none)\r" +
										"-\'.\' is the decimal point\r" +
										"-\'E\' is the scientific notation character";

	public RegionEditorPreferencePage() {

	}

	/**
	 * @wbp.parser.constructor
	 */
	public RegionEditorPreferencePage(String title) {
		super(title);
	}

	public RegionEditorPreferencePage(String title, ImageDescriptor image) {
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

		regionMoveableCheckbox = new Button(comp, SWT.CHECK);
		regionMoveableCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, false, false));
		regionMoveableCheckbox.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				storeRegionMoveable();
			}
		});
		regionMoveableCheckbox.setText("Allow regions to be moved");
		regionMoveableCheckbox.setToolTipText("Allow the regions to be moved graphically");

		Group formatGrp = new Group(comp, SWT.NONE);
		formatGrp.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		formatGrp.setLayout(new GridLayout(2, false));
		formatGrp.setText("Number Format");

		Label label = new Label(formatGrp, SWT.NONE);
		label.setText("Region Point Format");
		label.setToolTipText("Format for the region point values shown in the Region Editor tool");

		pointFormat = new Text(formatGrp, SWT.BORDER);
		pointFormat.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		pointFormat.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				storePreferences();
			}
		});
		pointFormat.setToolTipText(toolTipText);

		label = new Label(formatGrp, SWT.NONE);
		label.setText("Region Angle Format");
		label.setToolTipText("Format for the region angle values shown in the Region Editor tool");

		angleFormat = new Text(formatGrp, SWT.BORDER);
		angleFormat.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		angleFormat.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				storePreferences();
			}
		});
		angleFormat.setToolTipText(toolTipText);

		label = new Label(formatGrp, SWT.NONE);
		label.setText("Intensity Format");
		label.setToolTipText("Format for the intensity value shown in the Region Editor tool");

		intensityFormat = new Text(formatGrp, SWT.BORDER);
		intensityFormat.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		intensityFormat.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				storePreferences();
			}
		});
		intensityFormat.setToolTipText(sciToolTipText);

		label = new Label(formatGrp, SWT.NONE);
		label.setText("Sum format");
		label.setToolTipText("Format for the sum value shown in the Region Editor tool");

		sumFormat = new Text(formatGrp, SWT.BORDER);
		sumFormat.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false));
		sumFormat.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				storePreferences();
			}
		});
		sumFormat.setToolTipText(sciToolTipText);

		// initialize
		initializePage();
		return comp;
	}

	private void initializePage() {
		regionMoveableCheckbox.setSelection(getRegionMoveable());
		pointFormat.setText(getPointFormat());
		angleFormat.setText(getAngleFormat());
		intensityFormat.setText(getIntensityFormat());
		sumFormat.setText(getSumFormat());
	}
	
	protected void checkState(String format) {
		try {
			final DecimalFormat realForm = new DecimalFormat(format);
			String ok = realForm.format(1.0);
			if (ok==null || "".equals(ok)) throw new Exception();
		} catch (Throwable ne) {
			setErrorMessage("The entered format is incorrect");
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
		regionMoveableCheckbox.setSelection(getDefaultRegionMoveable());
		pointFormat.setText(getDefaultPointFormat());
		angleFormat.setText(getDefaultAngleFormat());
		intensityFormat.setText(getDefaultIntensityFormat());
		sumFormat.setText(getDefaultSumFormat());
	}

	private boolean storeRegionMoveable() {
		boolean regionMoveable = regionMoveableCheckbox.getSelection();
		if (!isValid()) return false;
		setRegionMoveable(regionMoveable);
		return true;
	}

	private boolean storePreferences() {
		String format = pointFormat.getText();
		checkState(format);
		if (!isValid()) return false;
		setPointFormat(format);

		format = angleFormat.getText();
		checkState(format);
		if (!isValid()) return false;
		setAngleFormat(format);

		format = intensityFormat.getText();
		checkState(format);
		if (!isValid()) return false;
		setIntensityFormat(format);

		format = sumFormat.getText();
		checkState(format);
		if (!isValid()) return false;
		setSumFormat(format);

		return true;
	}

	private boolean getDefaultRegionMoveable() {
		return getPreferenceStore().getDefaultBoolean(RegionEditorConstants.MOBILE_REGION_SETTING);
	}

	private boolean getRegionMoveable() {
		return getPreferenceStore().getBoolean(RegionEditorConstants.MOBILE_REGION_SETTING);
	}

	private void setRegionMoveable(boolean isMoveable) {
		getPreferenceStore().setValue(RegionEditorConstants.MOBILE_REGION_SETTING, isMoveable);
	}

	public void setPointFormat(String format) {
		getPreferenceStore().setValue(RegionEditorConstants.POINT_FORMAT, format);
	}

	private String getDefaultPointFormat() {
		return getPreferenceStore().getDefaultString(RegionEditorConstants.POINT_FORMAT);
	}

	private String getPointFormat() {
		return getPreferenceStore().getString(RegionEditorConstants.POINT_FORMAT);
	}

	public void setAngleFormat(String format) {
		getPreferenceStore().setValue(RegionEditorConstants.ANGLE_FORMAT, format);
	}

	private String getDefaultAngleFormat() {
		return getPreferenceStore().getDefaultString(RegionEditorConstants.ANGLE_FORMAT);
	}

	private String getAngleFormat() {
		return getPreferenceStore().getString(RegionEditorConstants.ANGLE_FORMAT);
	}

	private String getDefaultIntensityFormat() {
		return getPreferenceStore().getDefaultString(RegionEditorConstants.INTENSITY_FORMAT);
	}

	private String getIntensityFormat() {
		return getPreferenceStore().getString(RegionEditorConstants.INTENSITY_FORMAT);
	}

	public void setIntensityFormat(String format) {
		getPreferenceStore().setValue(RegionEditorConstants.INTENSITY_FORMAT, format);
	}

	private String getDefaultSumFormat() {
		return getPreferenceStore().getDefaultString(RegionEditorConstants.SUM_FORMAT);
	}

	private String getSumFormat() {
		return getPreferenceStore().getString(RegionEditorConstants.SUM_FORMAT);
	}

	public void setSumFormat(String format) {
		getPreferenceStore().setValue(RegionEditorConstants.SUM_FORMAT, format);
	}
}

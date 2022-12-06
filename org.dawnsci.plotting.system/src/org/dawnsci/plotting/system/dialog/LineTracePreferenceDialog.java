/*
 * Copyright (c) 2019 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.system.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawnsci.plotting.AbstractPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.ErrorBarType;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.eclipse.dawnsci.plotting.api.trace.LineTracePreferences;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

/**
 * Modification of TraceConfigPage
 */
public class LineTracePreferenceDialog extends Dialog {
	private Combo traceTypeCombo;
	private Spinner lineWidthSpinner;
	private Combo pointStyleCombo;
	private Spinner pointSizeSpinner;
	private Spinner areaAlphaSpinner;
	private Button antiAliasing;

	private Button errorBarEnabledButton;
	private Combo xErrorBarTypeCombo;
	private Combo yErrorBarTypeCombo;

	private LineTracePreferences prefs;
	private AbstractPlottingSystem<?> system;

	public LineTracePreferenceDialog(Shell parentShell, AbstractPlottingSystem<?> system) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.system = system;
		prefs = system.getLineTracePreferences();
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Line Trace Preferences");
	}

	@Override
	protected Control createDialogArea(final Composite composite) {
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Composite area = new Composite(composite, SWT.NONE);
		area.setLayout(new GridLayout(2, false));

		Composite traceCompo = new Composite(area, SWT.NONE);
		traceCompo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		traceCompo.setLayout(new GridLayout(3, false));

		final Group errorBarGroup = new Group(area, SWT.NONE);
		errorBarGroup.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));
		errorBarGroup.setLayout(new GridLayout(2, false));
		errorBarGroup.setText("Error Bar");

		GridData gd;
		GridData labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);

		final Label traceTypeLabel = new Label(traceCompo, 0);
		traceTypeLabel.setText("Trace Type: ");
		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		traceTypeLabel.setLayoutData(labelGd);

		traceTypeCombo = new Combo(traceCompo, SWT.DROP_DOWN | SWT.READ_ONLY);
		traceTypeCombo.setItems(TraceType.stringValues());
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		traceTypeCombo.setLayoutData(gd);

		final Label lineWidthLabel = new Label(traceCompo, 0);
		lineWidthLabel.setText("Line Width (pixels): ");
		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		lineWidthLabel.setLayoutData(labelGd);

		lineWidthSpinner = new Spinner(traceCompo, SWT.BORDER);
		lineWidthSpinner.setMaximum(100);
		lineWidthSpinner.setMinimum(0);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		lineWidthSpinner.setLayoutData(gd);

		final Label pointStyleLabel = new Label(traceCompo, 0);
		pointStyleLabel.setText("Point Style: ");
		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		pointStyleLabel.setLayoutData(labelGd);

		pointStyleCombo = new Combo(traceCompo, SWT.DROP_DOWN | SWT.READ_ONLY);
		pointStyleCombo.setItems(PointStyle.stringValues());
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		pointStyleCombo.setLayoutData(gd);

		final Label pointSizeLabel = new Label(traceCompo, 0);
		pointSizeLabel.setText("Point Size (pixels): ");
		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1);
		pointSizeLabel.setLayoutData(labelGd);

		pointSizeSpinner = new Spinner(traceCompo, SWT.BORDER);
		pointSizeSpinner.setMaximum(100);
		pointSizeSpinner.setMinimum(0);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		pointSizeSpinner.setLayoutData(gd);

		final Label alphaLabel = new Label(traceCompo, 0);
		alphaLabel.setText("Area Alpha: ");
		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		alphaLabel.setLayoutData(labelGd);

		areaAlphaSpinner = new Spinner(traceCompo, SWT.BORDER);
		areaAlphaSpinner.setMaximum(255);
		areaAlphaSpinner.setMinimum(0);
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		areaAlphaSpinner.setLayoutData(gd);
		areaAlphaSpinner.setToolTipText("0 for transparent, 255 for opaque");

		antiAliasing = new Button(traceCompo, SWT.CHECK);
		antiAliasing.setText("Anti Aliasing Enabled");
		antiAliasing.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 3, 1));

		// error bar settings...
		errorBarEnabledButton = new Button(errorBarGroup, SWT.CHECK);
		errorBarEnabledButton.setText("Error Bar Enabled");
		errorBarEnabledButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
		errorBarEnabledButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean enabled = errorBarEnabledButton.getSelection();
				xErrorBarTypeCombo.setEnabled(enabled);
				yErrorBarTypeCombo.setEnabled(enabled);
			}
		});

		final Label xErrorBarTypeLabel = new Label(errorBarGroup, 0);
		xErrorBarTypeLabel.setText("X Error Bar Type: ");
		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		xErrorBarTypeLabel.setLayoutData(labelGd);

		xErrorBarTypeCombo = new Combo(errorBarGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		xErrorBarTypeCombo.setItems(ErrorBarType.stringValues());
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		xErrorBarTypeCombo.setLayoutData(gd);

		final Label yErrorBarTypeLabel = new Label(errorBarGroup, 0);
		yErrorBarTypeLabel.setText("Y Error Bar Type: ");
		labelGd = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		yErrorBarTypeLabel.setLayoutData(labelGd);

		yErrorBarTypeCombo = new Combo(errorBarGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		yErrorBarTypeCombo.setItems(ErrorBarType.stringValues());
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		yErrorBarTypeCombo.setLayoutData(gd);

		initialize();

		return area;
	}

	private static final int APPLY_FIRST = 2;
	private static final int APPLY_LAST = 3;
	private static final int APPLY_ALL = 4;
	private static final int RESET_LOCAL = 5;
	private static final int RESET_GLOBAL = 6;

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Save and Close", true).setToolTipText("Save preferences for new lines");
		int n = system.getTracesByClass(ILineTrace.class).size();
		if (n > 0) {
			if (n > 1) {
				createButton(parent, APPLY_FIRST, "Apply to first", false).setToolTipText("Apply to first line");
				createButton(parent, APPLY_LAST, "Apply to last", false).setToolTipText("Apply to last line");
			}
			createButton(parent, APPLY_ALL, "Apply to all", false).setToolTipText("Apply to all lines");
		}
		createButton(parent, RESET_LOCAL, "Restore dialog", false).setToolTipText("Restore settings in dialog to those prior to any change");
		createButton(parent, RESET_GLOBAL, "Reset preferences", false).setToolTipText("Reset preferences back to the default ones");
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false).setToolTipText("Do not save preferences");
	}

	@Override
	protected void buttonPressed(int buttonId) {
		switch (buttonId) {
		case APPLY_FIRST, APPLY_LAST, APPLY_ALL:
			applyToTraces(buttonId);
			break;
		case RESET_GLOBAL:
			prefs.resetPreferences();
			/* FALLTHROUGH */
		case RESET_LOCAL:
			initialize();
			break;
		default:
			super.buttonPressed(buttonId);
			break;
		}
	}

	private void applyToTraces(int buttonId) {
		Collection<ILineTrace> traces = system.getTracesByClass(ILineTrace.class);
		if (traces.size() > 0) {
			if (buttonId == APPLY_ALL) {
				for (ILineTrace t : traces) {
					applyPreferences(t);
				}
			} else {
				List<ILineTrace> tList = traces instanceof List ? (List<ILineTrace>) traces : new ArrayList<>(traces);
				applyPreferences(tList.get(buttonId == APPLY_FIRST ? 0 : tList.size() - 1));
				
			}
		}
	}

	private void initialize() {
		traceTypeCombo.select(prefs.getOrdinal(TraceType.class));
		lineWidthSpinner.setSelection(prefs.getInteger(LineTracePreferences.LINE_WIDTH));
		pointStyleCombo.select(prefs.getOrdinal(PointStyle.class));
		pointSizeSpinner.setSelection(prefs.getInteger(LineTracePreferences.POINT_SIZE));
		areaAlphaSpinner.setSelection(prefs.getInteger(LineTracePreferences.AREA_ALPHA));
		boolean enabled = prefs.getBoolean(LineTracePreferences.ERROR_BAR_ON);
		errorBarEnabledButton.setSelection(enabled);
		xErrorBarTypeCombo.select(prefs.getOrdinal(ErrorBarType.class, LineTracePreferences.X_ERROR_BAR_TYPE));
		yErrorBarTypeCombo.select(prefs.getOrdinal(ErrorBarType.class, LineTracePreferences.Y_ERROR_BAR_TYPE));

		xErrorBarTypeCombo.setEnabled(enabled);
		yErrorBarTypeCombo.setEnabled(enabled);
	}

	private void applyPreferences(ILineTrace t) {
		t.setTraceType(TraceType.values()[traceTypeCombo.getSelectionIndex()]);
		t.setLineWidth(lineWidthSpinner.getSelection());
		t.setPointStyle(PointStyle.values()[pointStyleCombo.getSelectionIndex()]);
		t.setPointSize(pointSizeSpinner.getSelection());
		t.setAreaAlpha(areaAlphaSpinner.getSelection());
		t.setErrorBarEnabled(errorBarEnabledButton.getSelection());
		t.setXErrorBarType(ErrorBarType.values()[xErrorBarTypeCombo.getSelectionIndex()]);
		t.setYErrorBarType(ErrorBarType.values()[yErrorBarTypeCombo.getSelectionIndex()]);
	}

	@Override
	protected void okPressed() {
		prefs.set(TraceType.values()[traceTypeCombo.getSelectionIndex()]);
		prefs.set(LineTracePreferences.LINE_WIDTH, lineWidthSpinner.getSelection());
		prefs.set(PointStyle.values()[pointStyleCombo.getSelectionIndex()]);
		prefs.set(LineTracePreferences.POINT_SIZE, pointSizeSpinner.getSelection());
		prefs.set(LineTracePreferences.AREA_ALPHA, areaAlphaSpinner.getSelection());
		prefs.set(LineTracePreferences.ERROR_BAR_ON, errorBarEnabledButton.getSelection());
		prefs.set(LineTracePreferences.X_ERROR_BAR_TYPE, ErrorBarType.values()[xErrorBarTypeCombo.getSelectionIndex()]);
		prefs.set(LineTracePreferences.Y_ERROR_BAR_TYPE, ErrorBarType.values()[yErrorBarTypeCombo.getSelectionIndex()]);
		system.savePreferences();
		super.okPressed();
	}
}

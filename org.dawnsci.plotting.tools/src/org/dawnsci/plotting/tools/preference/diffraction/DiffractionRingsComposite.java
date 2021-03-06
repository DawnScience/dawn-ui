/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.preference.diffraction;

import org.eclipse.richbeans.widgets.scalebox.NumberBox;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.wrappers.SpinnerWrapper;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A class for editing one DHKL.
 * @author Matthew Gerring
 *
 */
public class DiffractionRingsComposite extends Composite {

	private TextWrapper    ringName;
	private NumberBox      dNano;
	private SpinnerWrapper h,k,l;

	public DiffractionRingsComposite(Composite par, int config) {
		super(par, config);
		
		setLayout(new GridLayout(2, false));
		
		Label label = new Label(this, SWT.NONE);
		label.setText("Name");
		
		this.ringName = new TextWrapper(this, SWT.BORDER);
		ringName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		ringName.setTextLimit(64);
		
		dNano = createRangeBox("d", 0, 1000, "nm");
		dNano.setDecimalPlaces(6);
		dNano.setName("d");
		
		h = createSpinnerBox("h", 0, 10);		
		k = createSpinnerBox("k", 0, 10);	
		l = createSpinnerBox("l", 0, 100);

	}

	private NumberBox createRangeBox(String label, double lower, double upper, String unit) {
		
		Label lbl = new Label(this, SWT.NONE);
		lbl.setText(label);

		NumberBox rb = new ScaleBox(this, SWT.NONE);
		rb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		rb.setMinimum(lower);
		rb.setMaximum(upper);
		if (unit!=null) rb.setUnit(unit);
		
		return rb;
	}
	
	private SpinnerWrapper createSpinnerBox(String label, int lower, int upper) {
		
		Label lbl = new Label(this, SWT.NONE);
		lbl.setText(label);

		SpinnerWrapper rb = new SpinnerWrapper(this, SWT.BORDER);
		rb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		rb.setMinimum(lower);
		rb.setMaximum(upper);
		return rb;
	}


	public TextWrapper getRingName() {
		return ringName;
	}

	public NumberBox getDNano() {
		return dNano;
	}

	public SpinnerWrapper getH() {
		return h;
	}

	public SpinnerWrapper getK() {
		return k;
	}

	public SpinnerWrapper getL() {
		return l;
	}
}

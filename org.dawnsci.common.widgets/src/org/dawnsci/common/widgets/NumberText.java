/*-
 * Copyright (c) 2018 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.widgets;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;

/**
 * Widget to allow entry of numbers
 * <p>
 * This differs in the notification of SelectionListeners from Text widgets.
 * Each listener's {@link SelectionListener#widgetDefaultSelected}
 * method is called on return or enter or loss of focus, and
 * {@link SelectionListener#widgetSelected} is called on
 * any valid modification
 */
public class NumberText extends Composite {

	private final Text text;
	private final Set<SelectionListener> listeners = new HashSet<>();
	private String goodText = "";

	public NumberText(Composite parent, int style) {
		super(parent, SWT.NONE);
		setLayout(new FillLayout());

		text = new Text(this, style);

		text.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				processSelection(true, e);
			}
		});
		text.addVerifyListener(NumberText::verifyDouble);

		text.addModifyListener(e -> {
			processSelection(false, e);
		});
		text.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				processSelection(true, e);
			}
		});
	}

	private static void verifyDouble(VerifyEvent e) {
		String oldText = ((Text) e.widget).getText();
		String newText = oldText.substring(0, e.start) + e.text + oldText.substring(e.end);
		newText = newText.toLowerCase();
		try {
			Double.valueOf(newText);
			e.doit = true;
		} catch (NumberFormatException ex) {
			// allow construction of positive, negative numbers
			e.doit = newText.isEmpty() || newText.equals("-") || newText.equals("+")
					// allow scientific notation
					|| newText.endsWith("e") || newText.endsWith("e-") || newText.endsWith("e+")
					// allow NaN
					|| newText.endsWith("n") || newText.endsWith("na")
					// allow Inf
					|| newText.endsWith("i") || newText.endsWith("n");
		}
	}

	private void processSelection(boolean isFinished, TypedEvent e) {
		if (getValue(text.getText()) == null) {
			if (isFinished) {
				text.setText(goodText);
			}
			return;
		}

		goodText = text.getText();
		SelectionEvent se = convertEvent(e);
		for (SelectionListener l : listeners) {
			if (isFinished) {
				l.widgetDefaultSelected(se);
			} else {
				l.widgetSelected(se);
			}
		}
	}

	private static SelectionEvent convertEvent(TypedEvent te) {
		if (te instanceof SelectionEvent) {
			return (SelectionEvent) te;
		}

		Event e = new Event();
		e.display = te.display;
		e.widget = te.widget;
		e.time = te.time;
		e.data = te.data;
		return new SelectionEvent(e);
	}

	public void addSelectionListener(SelectionListener l) {
		listeners.add(l);
	}

	public void removeSelectionListener(SelectionListener l) {
		listeners.remove(l);
	}

	@Override
	public void dispose() {
		super.dispose();
		listeners.clear();
	}

	@Override
	public void setEnabled(boolean enabled) {
		text.setEnabled(enabled);
	}

	@Override
	public void setToolTipText(String tooltip) {
		text.setToolTipText(tooltip);
	}

	/**
	 * Set value
	 * @param number
	 */
	public void setValue(Number number) {
		if (number == null) {
			goodText = "";
		} else {
			goodText = number.toString();
		}
		text.setText(goodText);
	}

	private Double getValue(String value) {
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Get value
	 * @return value or {@link Double#NaN} if text is empty
	 */
	public Number getValue() {
		Double v = getValue(goodText);
		return v == null ? Double.NaN : v;
	}

	/**
	 * Get value as double
	 * @return value or {@link Double#NaN} if text is empty
	 */
	public double getDouble() {
		Double v = getValue(goodText);
		return v == null ? Double.NaN : v;
	}
}

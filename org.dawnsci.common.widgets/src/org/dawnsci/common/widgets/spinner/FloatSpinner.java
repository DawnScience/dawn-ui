/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.widgets.spinner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Spinner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A spinner class that supports floating point numbers of fixed precision
 */
public class FloatSpinner extends Composite {
	
	private static final Logger logger = LoggerFactory.getLogger(FloatSpinner.class);

	/**
	 * prims
	 */
	private int    width;
	private int    precision;
	private int    maximumValue;
	private double factor;
	
	/**
	 * objects
	 */
	private Spinner                 spinner;
	private List<SelectionListener> listeners;
	private SelectionAdapter        sListener;
	private CLabel                  errorLabel;
	private Composite               content;

	/**
	 * Create a fixed float spinner
	 * 
	 * @param parent
	 * @param style
	 */
	public FloatSpinner(Composite parent, int style) {
		this(parent, style, 3, 1);
	}

	/**
	 * Create a fixed float spinner
	 * 
	 * @param parent
	 * @param style
	 * @param width
	 * @param precision
	 */
	public FloatSpinner(Composite parent, int style, int width, int precision) {
		super(parent, SWT.NONE);
		setLayout(new FillLayout());
		
		this.content = new Composite(this, SWT.NONE);
		content.setLayout(new StackLayout());
		
		spinner = new Spinner(content, style);
		setFormat(width, precision);
		listeners = new ArrayList<SelectionListener>();
		sListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				notifySelectionListeners(e);
			}
		};
		spinner.addSelectionListener(sListener);

		this.errorLabel = new CLabel(content, SWT.NONE);
		errorLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
		((StackLayout)content.getLayout()).topControl = spinner;
	}

	protected void notifySelectionListeners(SelectionEvent e) {
		for (SelectionListener s : listeners) {
			s.widgetSelected(e);
		}
	}

	/**
	 * Set the format and automatically set minimum and maximum allowed values
	 * 
	 * @param width
	 *            of displayed value as total number of digits
	 * @param precision
	 *            of value in decimal places
	 */
	public void setFormat(int width, int precision) {
		this.precision = precision;
		this.setWidth(width);
		maximumValue = (int) Math.pow(10, width);
		factor = Math.pow(10, precision);

		spinner.setDigits(precision);
		spinner.setMinimum(-maximumValue);
		spinner.setMaximum(maximumValue);
		spinner.setIncrement(1);
		spinner.setPageIncrement(5);
		spinner.setSelection(0);
	}

	/**
	 * @return Returns the precision.
	 */
	public int getPrecision() {
		return precision;
	}

	/**
	 * @param width
	 *            The width to set.
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return Returns the width.
	 */
	public int getWidth() {
		return width;
	}

	private Double invalidValue = null;
	/**
	 * @param value
	 */
	public void setDouble(double value) {
		
		// We round to the precision.
		double factor = Math.pow(10d, precision);
		value =	(long) (value * factor + 0.5) / factor;

		if (Double.isInfinite(value)||Double.isNaN(value)) {
			((StackLayout)content.getLayout()).topControl = errorLabel;
			errorLabel.setText(String.valueOf(value));
			content.layout();
			invalidValue = value;
			return;
		}
		((StackLayout)content.getLayout()).topControl = spinner;

		spinner.setSelection((int) (value * factor));
	}

	/**
	 * @return value
	 */
	public double getDouble() {
		if (invalidValue!=null) return invalidValue;
		return spinner.getSelection() / factor;
	}

	public double getRange() {
		double min = spinner.getMinimum() / factor;
		double max = spinner.getMaximum() / factor;
		return max-min;
	}
	
	public double getMinimum() {
		return spinner.getMinimum() / factor;
	}
	
	public double getMaximum() {
		return spinner.getMaximum() / factor;
	}
	
	/**
	 * @param listener
	 * @see Spinner#addSelectionListener(SelectionListener)
	 */
	public void addSelectionListener(SelectionListener listener) {
		listeners.add(listener);
	}

	/**
	 * @param listener
	 * @see Spinner#removeSelectionListener(SelectionListener)
	 */
	public void removeSelectionListener(SelectionListener listener) {
		listeners.remove(listener);
	}

	/**
	 * @param minimum
	 */
	public void setMinimum(double minimum) {
		spinner.setMinimum((int) (minimum * factor));
	}

	/**
	 * @param maximum
	 */
	public void setMaximum(double maximum) {
		spinner.setMaximum((int) (maximum * factor));
	}

	@Override
	public void dispose() {
		listeners = null;
		if (!spinner.isDisposed())
			spinner.removeSelectionListener(sListener);
	}
	
	public Composite getControl() {
		return spinner;
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		spinner.setEnabled(enabled);
	}

	public boolean isSpinner(Object source) {
		return source == spinner;
	}
	
	public void setIncrement(double inc) {
		spinner.setIncrement((int) (inc * factor));
	}
	
	public double getIncrement() {
		return spinner.getIncrement() / factor;
	}
	
	/**
	 * Method creates a right-click menu for a FloatSpinner with options for resetting to original value, and for setting the increment.
	 * @param increments an array of doubles representing the different increments allowed
	 * @param resetObject the object from which the resetGetter is invoked
	 * @param resetMethod the method that gets the original value
	 */
	public void createMenu(final double[] increments, final Object resetObject, final Method resetMethod) {
 		final MenuManager popupMenu = new MenuManager();
 		popupMenu.setRemoveAllWhenShown(true);
 		getControl().setMenu(popupMenu.createContextMenu(getControl()));
 		popupMenu.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				Action cutAction = new Action("Cut") {
					@Override
					public void run() {
						spinner.cut();
					}
				};
				Action copyAction = new Action("Copy") {
					@Override
					public void run() {
						spinner.copy();
					}
				};
				Action pasteAction = new Action("Paste") {
					@Override
					public void run() {
						spinner.paste();
					}
				};
				
				popupMenu.add(cutAction);
				popupMenu.add(copyAction);
				popupMenu.add(pasteAction);
				popupMenu.add(new Separator());

		 		popupMenu.add(new Action("Reset") {
		 			@Override
		 			public void run() {
		 				Object[] params = new Object[0];
		 				
						try {
							resetMethod.invoke(resetObject, params);
						} catch (IllegalArgumentException e) {
							logger.error("Can't invoke method (illegal argument).", e);
						} catch (IllegalAccessException e) {
							logger.error("Can't invoke method (illegal access).", e);
						} catch (InvocationTargetException e) {
							logger.error("Can't invoke method (invokation target).", e);
						}
		 			}
		 		});

		 		manager.update();
			}
		});
	}
	
	public void addKeyListener(KeyListener listener) {
		super.addKeyListener(listener);
        spinner.addKeyListener(listener);
	}
}

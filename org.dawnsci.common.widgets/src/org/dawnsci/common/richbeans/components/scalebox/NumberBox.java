/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components.scalebox;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dawnsci.common.richbeans.ACTIVE_MODE;
import org.dawnsci.common.richbeans.beans.BeanUI;
import org.dawnsci.common.richbeans.beans.IExpressionManager;
import org.dawnsci.common.richbeans.beans.IExpressionWidget;
import org.dawnsci.common.richbeans.beans.IFieldWidget;
import org.dawnsci.common.richbeans.components.BoundsProvider;
import org.dawnsci.common.richbeans.components.BoundsUpdater;
import org.dawnsci.common.richbeans.components.ButtonComposite;
import org.dawnsci.common.richbeans.event.BoundsEvent;
import org.dawnsci.common.richbeans.event.BoundsEvent.Mode;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.dawnsci.common.richbeans.internal.GridUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Base class for any box with a range and unit. Abstract class does not currently have abstract methods, but is not
 * designed to be used directly.
 * @deprecated Use {@link FloatDecorator} with a standard Text widget in DAWN. It is faster and easier.
 */
@Deprecated
public abstract class NumberBox extends ButtonComposite implements BoundsProvider, IFieldWidget, IExpressionWidget {

	protected StyledText expressionLabel;
	protected Label label;
	protected StyledText text;
	protected double maximum = 1000, minimum = 0;
	protected int decimalPlaces = 2;
	protected String name;
	protected String unit;
	protected BoundsProvider minProvider, maxProvider;
	protected boolean isIntegerBox = false;
	protected boolean validBounds = true;
	protected boolean doNotUseExpressions = false;
	protected boolean isEditable = true;
	protected String tooltipOveride;
	protected String minFieldName;
	protected Class<?> minClass;
	protected Mode currentBoundsMode = Mode.LEGAL;
	protected Color red, black, grey, blue;
	protected String maxFieldName;
	protected Class<?> maxClass;
	protected boolean maximumValid = true;
	protected boolean minimumValid = true;
	protected NumberFormat numberFormat;
	protected MouseTrackAdapter mouseTrackListener;
	protected FocusAdapter focusListener;
	protected ModifyListener modifyListener;
	protected VerifyKeyListener verifyListener;
	protected SelectionListener selectionListener;
	protected IExpressionManager expressionManager;
	private boolean permanentlyEnabled;
	
	private ACTIVE_MODE activeMode = ACTIVE_MODE.SET_VISIBLE_AND_ACTIVE;
	private String boundsKey;

	public NumberBox(Composite parent, int style) {
		super(parent, style);

		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		setLayout(gridLayout);

		label = new Label(this, SWT.LEFT);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		label.setVisible(false);

		text = new StyledText(this, SWT.BORDER | SWT.SINGLE);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		createTextListeners(text);

		text.setToolTipText(null); // Required to stop tip fickering on linux
		text.setStyleRange(null);
		mouseTrackListener = new MouseTrackAdapter() {
			@Override
			public void mouseEnter(MouseEvent e) {
				setupToolTip();
			}

			@Override
			public void mouseExit(MouseEvent e) {
				text.setToolTipText(null);
			}
		};
		text.addMouseTrackListener(mouseTrackListener);

		numberFormat = new DecimalFormat();
		numberFormat.setMaximumFractionDigits(decimalPlaces);
		numberFormat.setMinimumFractionDigits(decimalPlaces);
		numberFormat.setGroupingUsed(false);
		DecimalFormatSymbols dfs = ((DecimalFormat)numberFormat).getDecimalFormatSymbols();
		dfs.setInfinity(String.valueOf(Double.POSITIVE_INFINITY));
		((DecimalFormat)numberFormat).setDecimalFormatSymbols(dfs);
	}

	@Override
	public Control getControl() {
		return text;
	}

	/**
	 * @param expressionWidthHint
	 *            A hint for the width of the expression box, or -1 for no hint
	 */
	protected void createExpressionLabel(int expressionWidthHint) {
		if (expressionLabel != null)
			return;
		expressionLabel = new StyledText(this, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
		GridData gridLayout = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gridLayout.widthHint = expressionWidthHint >= 0 ? expressionWidthHint : 100;
		expressionLabel.setLayoutData(gridLayout);
		GridUtils.setVisible(expressionLabel, false);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	@Override
	public void dispose() {
		if (button != null && !button.isDisposed()) {
			button.removeSelectionListener(buttonSelection);
			button.dispose();
		}
		if (text != null && !text.isDisposed()) {
			text.removeMouseTrackListener(mouseTrackListener);
			if (focusListener != null)
				text.removeFocusListener(focusListener);
			if (modifyListener != null)
				text.removeModifyListener(modifyListener);
			if (selectionListener != null)
				text.removeSelectionListener(selectionListener);
			if (verifyListener != null)
				text.removeVerifyKeyListener(verifyListener);
			text.dispose();
		}
		if (label != null && !label.isDisposed())
			label.dispose();
		super.dispose();
	}

	protected void createTextListeners(final StyledText text) {
		// Selection and modify listener are exclusive in this context
		// No need to have both.
		createFocusListener(text);
		createModifyListener(text);
	}

	protected void createModifyListener(final StyledText text) {
		modifyListener = new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if (NumberBox.this.isOn()){
					textUpdateAndFireListeners();
				}
			}
		};
		text.addModifyListener(modifyListener);
	}

	protected void createSelectionListener(final StyledText text) {
		selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (NumberBox.this.isOn()){
					textUpdateAndFireListeners();
				}
			}
		};
		text.addSelectionListener(selectionListener);
	}

	/**
	 * Default implementation does nothing
	 * 
	 * @param text
	 */
	protected void createVerifyKeyListener(final StyledText text) {
		verifyListener = new VerifyKeyListener() {
			@Override
			public void verifyKey(VerifyEvent event) {
			}
		};
		text.addVerifyKeyListener(verifyListener);
	}

	protected void createFocusListener(final StyledText text) {
		focusListener = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				textUpdate();
			}
		};
		text.addFocusListener(focusListener);
	}

	protected void textUpdate() {
		if (text.isDisposed())
			return;
		if (!isOn())
			return;
		try {
			off();
			checkValue(text.getText());
		} finally {
			on();
		}
	}

	protected void textUpdateAndFireListeners() {
		textUpdate();
		double numericalValue = getNumericValue();
		ValueEvent evt = new ValueEvent(NumberBox.this, getFieldName());
		evt.setDoubleValue(numericalValue);
		eventDelegate.notifyValueListeners(evt);
	}

	/**
	 * Unused, just here to make bean recognize value field. The value parameter must be present in order to be accessed
	 * by bean system.
	 */
	@SuppressWarnings("unused")
	// Intentionally so
	private Object value;

	/**
	 * Returns the current value, including unit. Of the form: '%number% %unit%'
	 * 
	 * @return Object
	 */
	@Override
	public Object getValue() {
		Double val = getNumericValue();
		if (Double.isNaN(val))
			return null;
		if (isIntegerBox || getDecimalPlaces() == 0)
			return new Integer(Math.round(Math.round(val)));
		return new Double(val);
	}

	protected String formatValue(Object val){
		return numberFormat.format(val);
	}
	
	public NumberFormat getNumberFormat(){
		return numberFormat;
	}
	
	public void setNumberFormat(NumberFormat numberFormat){
		this.numberFormat = numberFormat;
	}
	
	@Override
	public void setValue(final Object value) {
		if (value != null) {
			if (value instanceof String)
				checkValue(value.toString());
			else
				checkValue(numberFormat.format(value));
		} else {
			text.setText("");
		}
	}

	protected Pattern getRegExpression() {
		String regex = getRegExpressionString();
		return Pattern.compile(regex);
	}

	protected String getRegExpressionString() {
		//We need to cope with 3., 3.0, .1, so use \\d*\\.?\\d*
		String digitExpr = "^([-+\\s]?\\d*\\.?\\d*|[-+\\s]?Infinity)";
		if (unit == null)
			return digitExpr;
		return digitExpr + "\\s*\\Q" + unit + "\\E";
	}

	protected void updateValue() {
		setupToolTip();
		checkValue(text.getText());
	}

	protected void checkValue(final String txt) {
		if (txt == null || "".equals(txt.trim()) || "-".equals(txt.trim())) {
			GridUtils.setVisible(expressionLabel, false);
			return;
		}

		// If this method is being called by a method trying to
		// set value, ensure that value is set.
		if (!txt.equals(text.getText()))
			text.setText(txt);

		if (expressionManager != null) {
			processAsExpression(txt);
		} else {
			processAsNumber(txt);
		}

	}

	private void processAsExpression(String txt) {

		Pattern pattern = getRegExpression();
		Matcher matcher = pattern.matcher(txt);
		if (matcher.matches()) {
			processAsNumber(txt);
			return;
		}

		// Remove all but expression or value (no unit etc.)
		txt = txt.trim();
		if (unit != null && txt.endsWith(unit))
			txt = txt.substring(0, txt.length() - unit.length());
		else { // Remove value if required
			pattern = Pattern.compile("(.*)\\(" + getRegExpressionString() + "\\)");
			matcher = pattern.matcher(txt);
			if (matcher.matches())
				txt = matcher.group(1).trim();
		}

		if ("".equals(txt) || txt == null || txt.equals(unit)) {
			processAsNumber(txt);
			return;
		}
		try {
			Double.parseDouble(txt);
			processAsNumber(txt);
			return;
		} catch (Throwable ignored) {
			//
		}

		// Set possible expression
		this.expressionManager.setExpression(txt);

		final int pos = text.getCaretOffset();
		if (expressionManager.isExpressionValid()) {
			text.setForeground(blue);
			text.setText(txt);
			setExpressionValue(expressionManager.getExpressionValue());
			checkBounds(expressionManager.getExpressionValue());
		}
		else {
			if (this.red == null)
				red = getDisplay().getSystemColor(SWT.COLOR_RED);
			text.setForeground(red);
			text.setText(txt);
			GridUtils.setVisible(expressionLabel, false);
		}
		text.setCaretOffset(pos);
		layout();
	}

	@Override
	public void setExpressionValue(final double numericalValue) {
		String stringValue = numberFormat.format(numericalValue);
		if (Double.isNaN(numericalValue)) {
			GridUtils.setVisible(expressionLabel, false);
			return;
		}
		if (Double.isInfinite(numericalValue))
			stringValue = "∞";
		if (!isExpressionAllowed())
			return;
		final String u = unit != null ? unit : "";
		final String value = stringValue + " " + u;
		GridUtils.setVisible(expressionLabel, true);
		expressionLabel.setText(value);
		layout();
		checkBounds(numericalValue);
	}

	private void processAsNumber(final String txt) {
		if (expressionManager != null)
			this.expressionManager.setExpression(null);
		if (expressionLabel != null)
			GridUtils.setVisible(expressionLabel, false);
		final Pattern pattern = getRegExpression();
		final Matcher matcher = pattern.matcher(txt);
		final boolean matches = matcher.matches();
		// return to the text box the string
		String extractedNumberString=null;
		if (matches)
			extractedNumberString = matcher.group(1);
		else
			extractedNumberString = txt;
		// but checkBounds using a Double object
		Double numericalValue = Double.NaN;
		try {
			numericalValue = Double.valueOf(extractedNumberString);
		} catch (NumberFormatException e) {
			text.setForeground(red);
			return;
		}
		StringBuilder buf = new StringBuilder(extractedNumberString);
		int pos = buf.toString().length() < text.getCaretOffset() ? buf.toString().length(): text.getCaretOffset();
		if (unit != null && buf.length() > 0) {
			final String unitLine = " " + unit;
			buf.append(unitLine);
		}
		text.setForeground(black);
		text.setText(buf.toString());
		text.setCaretOffset(pos);
		checkBounds(numericalValue);
		layout();
	}

	/**
	 * Can be used to re check the bounds if the box seems to be marked as out of bounds after some complex updates.
	 */
	public void checkBounds() {
		checkBounds(getNumericValue());
	}

	/**
	 * Called to update the bounds state and notify bounds listeners.
	 * 
	 * @param numericalValue
	 */
	protected void checkBounds(double numericalValue) {
		if (isDisposed() || text.isDisposed())
			return;
		BoundsEvent evt = new BoundsEvent(this);
		evt.setValue(numericalValue);
		evt.setUpper(getMaximum());
		evt.setLower(getMinimum());
		validBounds = true;
		if (!isValidBounds(numericalValue)) {
			if (red == null)
				red = getDisplay().getSystemColor(SWT.COLOR_RED);
			if (!red.isDisposed()) {
				text.setStyleRange(null);
				if (!isEditable()) 
					setCurrentFontStyle(text, SWT.ITALIC);
				else
					setCurrentFontStyle(text, SWT.NORMAL);
				text.setForeground(red);
			}
			validBounds = false;
			if ((numericalValue >= maximum && !isMaximumValid()) ||
				(numericalValue > maximum && isMaximumValid())) {
				evt.setMode(Mode.GREATER);
				setTooltipOveride("The value '" + numericalValue + "' is greater than the upper limit.");
			} 
			else if ((numericalValue <= minimum && !isMinimumValid()) ||
					(numericalValue < minimum && isMinimumValid())) {
				evt.setMode(Mode.LESS);
				setTooltipOveride("The value '" + numericalValue + "' is less than the lower limit.");
			}
		} else {
			setCurrentFontStyle(text, SWT.NORMAL);
			setTooltipOveride(null);
			if (isEditable()) {
				if (blue == null)
					blue = getDisplay().getSystemColor(SWT.COLOR_BLUE);
				if (black == null)
					black = getDisplay().getSystemColor(SWT.COLOR_BLACK);
				if (expressionManager != null && expressionManager.isExpressionValid()) {
					if (!blue.isDisposed())
						text.setForeground(blue);
				} else {
					if (!black.isDisposed())
						text.setForeground(black);
				}
			}
			evt.setMode(Mode.LEGAL);
		}
		
		if (isValidBounds(numericalValue) && !isEditable()) {
			if (grey == null)
				grey = getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
			if (!grey.isDisposed())
				text.setForeground(grey);
		}

		try {
			if (currentBoundsMode != evt.getMode())
				eventDelegate.notifyBoundsListeners(evt);
		} finally {
			currentBoundsMode = evt.getMode();
		}
	}

	protected boolean isValidBounds(final double numericalValue) {
		double maximum = getMaximum();
		double minimum = getMinimum();
		if (Double.isNaN(numericalValue))
			return true; // Something else is wrong.
		return ((numericalValue >= minimum && isMinimumValid()) ||
				(numericalValue > minimum && !isMinimumValid())) 
				&& ((numericalValue <= maximum && isMaximumValid()) ||
				(numericalValue < maximum && !isMaximumValid()));
	}

	protected void setupToolTip() {
		StringBuilder buf = new StringBuilder();
		if (getTooltipOveride() != null) {
			buf.append(getTooltipOveride());
			buf.append("\n\n");
		}
		if (getMinimum() == -Double.MAX_VALUE)
			buf.append("-∞");
		else
			buf.append(numberFormat.format(getMinimum()));

		if (unit != null)
			buf.append(" " + unit);
		String minSignToAppend = null;
		if (isMinimumValid())
			minSignToAppend = " <= ";
		else
			minSignToAppend = " < ";
		buf.append(minSignToAppend);
		String field = getFieldName() != null ? getFieldName() : "value";
		buf.append(field);
		String maxSignToAppend = null;
		if (isMaximumValid())
			maxSignToAppend = " <= ";
		else
			maxSignToAppend = " < ";
		buf.append(maxSignToAppend);
		if (getMaximum() == Double.MAX_VALUE)
			buf.append("∞");
		else
			buf.append(numberFormat.format(getMaximum()));
		if (unit != null)
			buf.append(" " + unit);
		text.setToolTipText(buf.toString());
	}

	/**
	 * Call to make work with integers.
	 * 
	 * @param isInt
	 */
	public void setIntegerBox(final boolean isInt) {
		isIntegerBox = isInt;
		setDecimalPlaces(isInt ? 0 : 2);
	}

	/**
	 * @return f
	 */
	public boolean isIntegerBox() {
		return isIntegerBox;
	}

	protected double numericValue;

	/**
	 * Returns the numeric portion of the value or Double.NaN if there is no value.
	 * 
	 * @return double
	 */
	public double getNumericValue() {
		if (text.isDisposed())
			return Double.NaN;
		String txt = text.getText();
		return getNumericValue(txt);
	}

	public double getNumericValue(String txt) {
		if (txt == null)
			return Double.NaN;
		if ("".equals(txt.trim()))
			return Double.NaN;
		if ("-".equals(txt.trim()))
			return -0d;
		if (expressionManager != null && expressionManager.isExpressionValid())
			return expressionManager.getExpressionValue();
		Pattern pattern = getRegExpression();
		if(txt.equals("."))
			return Double.NaN;
		Matcher matcher = pattern.matcher(txt);
		if (matcher.matches()) {
			String group = matcher.group(1);
			if (!group.trim().isEmpty()) {
				Double parsedDouble = Double.parseDouble(group);
				return Double.valueOf(String.format("%." + decimalPlaces + "f", parsedDouble));
			}
		}
		return Double.NaN;
	}

	/**
	 * Called to set the numeric value. this also sets the default value. If setValue(null) is called after
	 * setNumericValue(...) has been called, it resets to the numericValue. Set numericValue to Double.NaN to avoid
	 * this.
	 * 
	 * @param value
	 */
	public void setNumericValue(final double value) {
		numericValue = value;
		checkValue("" + value);
	}

	/**
	 * Unused, just here to make bean recognize value field.
	 */
	protected double integerValue;

	/**
	 * Returns the int portion of the value. If the format decimal places are zero, the user can only type in integers.
	 * 
	 * @return intValue
	 */
	public int getIntegerValue() {
		return (int) getNumericValue();
	}

	/**
	 * Called to set the value.
	 * 
	 * @param value
	 */
	public void setIntegerValue(final int value) {
		checkValue("" + value);
	}

	/**
	 * Disable and enable the widget.
	 * 
	 * @param isEditable
	 */
	public void setEditable(final boolean isEditable) {
		this.isEditable = isEditable;

		if (isDisposed())
			return;

		text.setEditable(isEditable);
		if (isValidBounds()) {
			if (black == null)
				black = getDisplay().getSystemColor(SWT.COLOR_BLACK);
			if (grey == null)
				grey = getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
			if (!black.isDisposed() && !grey.isDisposed())
				text.setForeground(isEditable ? black : grey);

		} 
		else {
			if (red == null)
				red = getDisplay().getSystemColor(SWT.COLOR_RED);
			if (!red.isDisposed())
				text.setForeground(red);
			if (!isEditable)
				setCurrentFontStyle(text, SWT.ITALIC);
		}
		if (button != null)
			button.setEnabled(isEditable);
	}

	private void setCurrentFontStyle(StyledText text, int style) {
		FontData currentFontData = text.getFont().getFontData()[0];
		currentFontData.setStyle(style);
		text.setFont(new Font(text.getDisplay(), currentFontData));
	}

	/**
	 * Used for testing only.
	 * 
	 * @return foregound color of entry box
	 */
	public Color _testGetForeGroundColor() {
		return text.getForeground();
	}

	/**
	 * Enabled state goes straight to text box.
	 */
	@Override
	public void setEnabled(final boolean isEnabled) {
		if (!permanentlyEnabled) {
			setEditable(isEnabled);
			text.setEnabled(isEnabled);
			checkBounds();
		}
	}

	/**
	 * @return double
	 */
	public boolean isEditable() {
		return isEditable;
	}

	/**
	 * @return the decimalPlaces
	 */
	public int getDecimalPlaces() {
		return decimalPlaces;
	}

	/**
	 * @param decimalPlaces
	 *            the decimalPlaces to set
	 */
	public void setDecimalPlaces(int decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
		numberFormat.setMaximumFractionDigits(decimalPlaces);
		numberFormat.setMinimumFractionDigits(decimalPlaces);
	}

	/**
	 * Get the maximum value of the scale box. Default is 1000. NOTE: Can cause recursion errors to have boxes
	 * circularly dependent on bounds.
	 * 
	 * @return the maximum
	 */
	@SuppressWarnings("unchecked")
	public double getMaximum() {
		if (maxProvider != null)
			return maxProvider.getBoundValue();
		if (maxFieldName != null && maxClass != null) {
			ScaleBox max = (ScaleBox) BeanUI.getBeanField(maxFieldName, maxClass);
			if (max != null)
				return max.getNumericValue();
		}
		return maximum;
	}

	/**
	 * Set the maximum value of the scale box. Default is 1000.
	 * 
	 * @param maximum
	 *            the maximum to set
	 */
	public void setMaximum(double maximum) {
		this.maximum = maximum;
		checkBounds();
	}

	/**
	 * Will check passed in maximum if field not available and check for field when checking bounds.
	 * 
	 * @param maximum
	 * @param fieldName
	 * @param fieldClass
	 */
	public void setMaximum(double maximum, String fieldName, Class<?> fieldClass) {
		this.maximum = maximum;
		this.maxFieldName = fieldName;
		this.maxClass = fieldClass;
	}

	/**
	 * If called, overrides setMaximum(double) method. The BoundsProvider passed in is queried for the bound and a value
	 * listener is added to it.
	 * 
	 * @param maxProvider
	 */
	public void setMaximum(final BoundsProvider maxProvider) {
		this.maxProvider = maxProvider;
		checkBounds();
		maxProvider.addValueListener(new BoundsUpdater("maxProviderListener", getBoundsKey()) {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				checkBounds();
			}
		});
	}

	/**
	 * Get the minimum value of the scale box. Default is 0. NOTE: Can cause recursion errors to have boxes circularly
	 * dependent on bounds.
	 * 
	 * @return the minimum
	 */
	@SuppressWarnings("unchecked")
	public double getMinimum() {
		if (minProvider != null)
			return minProvider.getBoundValue();
		if (minFieldName != null && minClass != null) {
			final ScaleBox min = (ScaleBox) BeanUI.getBeanField(minFieldName, minClass);
			if (min != null)
				return min.getNumericValue();
		}
		return minimum;
	}

	/**
	 * Set the minimum value of the scale box. Default is 0.
	 * 
	 * @param minimum
	 *            the minimum to set
	 */
	public void setMinimum(double minimum) {
		this.minimum = minimum;
		checkBounds();
	}

	/**
	 * If called, overrides setMinimum(double) method. The BoundsProvider passed in is queried for the bound.
	 * 
	 * @param minProvider
	 */
	public void setMinimum(final BoundsProvider minProvider) {
		this.minProvider = minProvider;
		checkBounds();
		minProvider.addValueListener(new BoundsUpdater("minProviderListener", getBoundsKey()) {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				checkBounds();
			}
		});
	}

	/**
	 * Will check passed in maximum if field not available and check for field when checking bounds.
	 * 
	 * @param minimum
	 * @param fieldName
	 * @param fieldClass
	 */
	public void setMinimum(double minimum, String fieldName, Class<?> fieldClass) {
		this.minimum = minimum;
		this.minFieldName = fieldName;
		this.minClass = fieldClass;
	}

	/**
	 * Sets the label displayed by the entry box.
	 * 
	 * @param txt
	 */
	public void setLabel(final String txt) {
		GridUtils.setVisible(label, true);
		label.setText(txt);
	}

	/**
	 * Sets the minimum width of the label so that labels can be made to line up when ScaleBoxes are used vertically.
	 * 
	 * @param width
	 */
	public void setLabelWidth(final int width) {
		GridData data = (GridData) label.getLayoutData();
		data.widthHint = width;
	}

	public String getUnit() {
		return unit;
	}

	/**
	 * Currently only one unit us allowed per scaleBox widget. Later versions will take a conversion table from xml and
	 * do conversions between units, returning the value in the SI unit.
	 * 
	 * @param newUnit
	 *            the unit to set
	 */
	public void setUnit(String newUnit) {
		// If we are displaying the unit now, change the text.
		Pattern pattern = getRegExpression();
		Matcher matcher = pattern.matcher(text.getText());
		if (matcher.matches())
			text.setText(matcher.group(1) + " " + newUnit);
		unit = newUnit;
		updateValue();
	}

	/**
	 * The name is used to define which element we are editing. This is then used to link the value into the bean.
	 * 
	 * @return the name
	 */
	@SuppressWarnings(value = { "all" })
	public String getName() {
		return name;
	}

	/**
	 * The name is used to define which element we are editing. This is then used to link the value into the bean.
	 * 
	 * @param elementName
	 */
	public void setName(String elementName) {
		this.name = elementName;
	}

	@Override
	public double getBoundValue() {
		return getNumericValue();
	}

	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		if (activeMode == ACTIVE_MODE.SET_VISIBLE_AND_ACTIVE) {
			setVisible(active);
		} else if (activeMode == ACTIVE_MODE.SET_ENABLED_AND_ACTIVE) {
			setEditable(active);
		}
	}

	public ACTIVE_MODE getActiveMode() {
		return activeMode;
	}

	public void setActiveMode(ACTIVE_MODE activeMode) {
		this.activeMode = activeMode;
	}

	/**
	 * The widget will want an expression manager to handle its units, but this will return true if the widget should
	 * not handle an expression based on the values of the fileds in the underlying bean.
	 * 
	 * @return boolean - if true then this widget should use expressions
	 */
	public boolean getDoNotUseExpressions() {
		return doNotUseExpressions;
	}

	/**
	 * Set to true after construction so that isExpressionAllowed will always return false and so prevent expressions
	 * being used when typed into the widgets control.
	 * 
	 * @param doNotUseExpressions
	 */
	public void setDoNotUseExpressions(boolean doNotUseExpressions) {
		this.doNotUseExpressions = doNotUseExpressions;
	}

	public boolean isValidBounds() {
		return validBounds;
	}

	@Override
	public boolean setFocus() {
		return this.text.setFocus();
	}

	public void copySettings(NumberBox numBox) {
		this.maxProvider = numBox.maxProvider;
		this.minProvider = numBox.minProvider;
		this.maximum = numBox.maximum;
		this.minimum = numBox.minimum;
		this.isIntegerBox = numBox.isIntegerBox;
		this.decimalPlaces = numBox.decimalPlaces;
	}

	public String getTooltipOveride() {
		return tooltipOveride;
	}

	public void setTooltipOveride(String tooltipOveride) {
		this.tooltipOveride = tooltipOveride;
	}

	public boolean isMaximumValid() {
		return maximumValid;
	}

	public void setMaximumValid(boolean maximumValid) {
		this.maximumValid = maximumValid;
	}

	public boolean isMinimumValid() {
		return minimumValid;
	}

	public void setMinimumValid(boolean minimumValid) {
		this.minimumValid = minimumValid;
	}

	/**
	 * Set this to null if no expressions are expected or allowed for this NumberBox.
	 * <p>
	 * RichBeanEditors will automatically create ExpressionManagers for IFieldWidgets.
	 */
	@Override
	public void setExpressionManager(IExpressionManager man) {
		this.expressionManager = man;
		createExpressionLabel(-1); // Does nothing if there already is one.
	}

	@Override
	public boolean isExpressionAllowed() {
		return !getDoNotUseExpressions() && expressionManager != null;
	}

	@Override
	public boolean isExpressionParseRequired(String value) {
		Pattern pattern = getRegExpression();
		Matcher matcher = pattern.matcher(value);
		if (matcher.matches())
			return false;
		if ("".equals(value) || value == null || value.equals(unit))
			return false;
		try {
			Double.parseDouble(value);
			return false;
		} catch (Throwable ignored) {
			//
		}
		return true;
	}

	/**
	 * The bounds key for this instance can be the field name or if a field name has not been set, it will be a unique
	 * and cached string.
	 * 
	 * @return unique key used for bounds.
	 */
	private String getBoundsKey() {
		if (boundsKey == null) {
			if (fieldName != null)
				boundsKey = fieldName; // field name is mostly safe.
			else // Generate a roughly unique and constant name.
				boundsKey = "Widget " + Calendar.getInstance().getTimeInMillis();
		}
		return boundsKey;
	}

	@Override
	protected void createButton() {
		super.createButton();
		if (button != null && button.getLayoutData() instanceof GridData) {
			final GridData bLayout = (GridData) button.getLayoutData();
			// Platform dependant sizes but they work
			// on linux RHEL5 ok.
			bLayout.heightHint = 25;
		}
	}
	
	/**
	 * Set the state of the box permanently. Set any desired states *before* this state is set to false or else
	 * they will not be applied. For example, setEditable(false), then setPermanentlyEnabled(true)
	 * @param enabled
	 */
	public void setPermanentlyEnabled(boolean enabled) {
		this.permanentlyEnabled = enabled;
	}

}

/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.components.wrappers;

import java.text.NumberFormat;
import java.text.ParseException;

import org.dawnsci.common.richbeans.components.FieldComposite;
import org.dawnsci.common.richbeans.event.ValueEvent;
import org.dawnsci.common.richbeans.internal.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author fcp94556
 * 
 * You have to be a widget (even though not needed) so that RCP developer
 * can deal with using the class. Therefore in inherits from Composite.
 */
public class LabelWrapper extends FieldComposite {

	/**
	 *
	 */
	public enum TEXT_TYPE {
	/**
	 * Optional
	 */
	PLAIN_TEXT, 
	/**
	 * Default
	 */
	NUMBER_WITH_UNIT}
	

	
	private int decimalPlaces = 2;
	private Label label;
	
	public void setLabelColor(Color color){ 
 		label.setForeground(color); 
 	} 

	/**
	 * @return the decimalPlaces
	 */
	public int getDecimalPlaces() {
		return decimalPlaces;
	}
	/**
	 * @param decimalPlaces the decimalPlaces to set
	 */
	public void setDecimalPlaces(int decimalPlaces) {
		this.decimalPlaces = decimalPlaces;
		numberFormat.setMaximumFractionDigits(decimalPlaces);
	}

	private NumberFormat numberFormat;
	/**
	 * @param parent
	 * @param style
	 */
	public LabelWrapper(final Composite parent, final int style) {
		super(parent, SWT.NONE);
		setLayout(new FillLayout());
		label = new Label(this, style);
		mainControl = label;
		this.numberFormat  = NumberFormat.getInstance();
		numberFormat.setMaximumFractionDigits(decimalPlaces);
		numberFormat.setGroupingUsed(false);
	}
	
	private String text;
	private TEXT_TYPE textType;
	@Override
	public String getValue() {
		return text;
	}
	
	/**
	 * @param type
	 */
	public void setTextType(TEXT_TYPE type) {
		this.textType = type;
	}
	
	
	@Override
	public void setValue(Object value) {
		if (textType==null||textType==TEXT_TYPE.NUMBER_WITH_UNIT) {
			if (value!=null) {
				text = numberFormat.format(value);
			} else {
				text = "";
			}
			
			String boxValue="";
			if (!"".equals(text)) {
				boxValue = StringUtils.keepDigits(text, decimalPlaces).toString();
				if (unit!=null) boxValue+=" "+unit;
			}
			label.setText(boxValue);
		} else {
			if (value!=null) {
				text = value+"";
			} else {
				text = "";
			}
			label.setText(text);
		}
		
		if (notifyType!=null&&notifyType==NOTIFY_TYPE.VALUE_CHANGED) {
			final ValueEvent evt = new ValueEvent(label,getFieldName());
			evt.setValue(value);
			eventDelegate.notifyValueListeners(evt);
		}
	}
	
	/**
	 * @return the current numeric value.
	 * @throws ParseException
	 */
	public double getNumericValue() throws ParseException {
		final Object val = getValue();
		if (val==null) return Double.NaN;
		if (val instanceof Number) return ((Number)val).doubleValue();
		return (numberFormat.parse(val.toString())).doubleValue();
	}
	
	/*******************************************************************/
	/**        This section will be the same for many wrappers.       **/
	/*******************************************************************/
	@Override
	protected void checkSubclass () {
	}
	
	private String unit;
	/**
	 * @return the unit
	 */
	public String getUnit() {
		return unit;
	}
	/**
	 * @param unit the unit to set
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}

	/**
	 * @param text
	 */
	public void setText(String text) {
		if (textType==TEXT_TYPE.NUMBER_WITH_UNIT&&getUnit()!=null) {
			label.setText(text+" "+unit);			
		} else {
			label.setText(text);
		}
	}
	
	/**
	 * Wrapper function for {@link org.eclipse.swt.widgets.Label#setAlignment(int)}
	 * @see org.eclipse.swt.widgets.Label#setAlignment(int)
	 * @param alignment LEFT, RIGHT or CENTER
	 */
	public void setAlignment(int alignment) {
		label.setAlignment(alignment);
	}

}

	

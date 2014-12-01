/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.celleditor;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.dawnsci.common.richbeans.internal.GridUtils;
import org.dawnsci.common.richbeans.internal.StringUtils;
import org.dawnsci.common.widgets.decorator.BoundsDecorator;
import org.dawnsci.common.widgets.decorator.FloatArrayDecorator;
import org.dawnsci.common.widgets.decorator.FloatDecorator;
import org.dawnsci.common.widgets.decorator.IntegerArrayDecorator;
import org.dawnsci.common.widgets.decorator.IntegerDecorator;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * A cell editor for editing a number or number array using text.
 * 
 * The number class, for instance Double or double[] should be passed into
 * the constructor so that this class knows how to parse the text.
 * 
 * You may optionally specify a unit, in which case when editing the text,
 * the unit appears on the right hand side.
 * 
 * @author Matthew Gerring
 *
 */
public class ClassCellEditor extends TextCellEditor {
	
	protected Class<? extends Object> clazz;
	protected String unit;
	protected Number max;
	protected Number min;

	public ClassCellEditor(Composite parent, Class<? extends Object> clazz, int switches) {
		this(parent, clazz, null, null, null, switches);
	}
	
	public ClassCellEditor(Composite parent, Class<? extends Object> clazz, Number min, Number max, String unit, int switches) {
		setStyle(switches);
		this.clazz = clazz;
		this.min   = min;
		this.max   = max;
		this.unit  = unit;
		create(parent);
	}
	
	protected Control createControl(Composite parent) {
		
		
		Control ret = null;
		if (unit == null) {
			ret = super.createControl(parent);
		} else {
			final Composite comp = new Composite(parent, SWT.None|SWT.NO_FOCUS);
			comp.setLayout(new GridLayout(2, false));
			comp.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			GridUtils.removeMargins(comp);
			
			super.createControl(comp);
			text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			if (unit!=null) {
				Label label = new Label(comp, SWT.NONE);
				label.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
				label.setText("  "+unit+"  ");
			}

			ret = comp;
		}
		
		final boolean isFloat      = Double.class.isAssignableFrom(clazz)   || Float.class.isAssignableFrom(clazz);
		final boolean isFloatArray = double[].class.isAssignableFrom(clazz) || float[].class.isAssignableFrom(clazz);
		
		final boolean isInt        = Integer.class.isAssignableFrom(clazz)  || Long.class.isAssignableFrom(clazz);
		final boolean isIntArray   = int[].class.isAssignableFrom(clazz)    || long[].class.isAssignableFrom(clazz);
		
		BoundsDecorator deco = null;
		if (isFloat) {
			deco = new FloatDecorator(text);
		} else if (isInt) {
			deco = new IntegerDecorator(text);
		} else if (isFloatArray) {
			deco = new FloatArrayDecorator(text);
		} else if (isIntArray) {
			deco = new IntegerArrayDecorator(text);
		}
		
		if (deco!=null) {
		    if (max!=null) deco.setMaximum(max);
		    if (min!=null) deco.setMinimum(min);
		}
		
		
		return ret;
	}

	protected void doSetValue(Object value) {
		if (value instanceof Number)    value = value.toString();
		if (clazz.isArray()) {
			String returnVal = StringUtils.toString(value);
			if (returnVal!=null && returnVal.startsWith("[")) returnVal = returnVal.substring(1);
			if (returnVal!=null && returnVal.endsWith("]"))   returnVal = returnVal.substring(0,returnVal.length()-1);
			value = returnVal;
		}
		if (value==null) return;
		super.doSetValue(value);
	}
	
	protected Object doGetValue() {

		String stringValue = (String)super.doGetValue();

		if (stringValue==null || "".equals(stringValue)) return null;
		stringValue = stringValue.trim();

		if (Double.class.isAssignableFrom(clazz))  return new Double(stringValue);
		if (Float.class.isAssignableFrom(clazz))   return new Float(stringValue);
		if (Integer.class.isAssignableFrom(clazz)) return new Integer(stringValue);
		if (Long.class.isAssignableFrom(clazz))    return new Long(stringValue);

		if (clazz.isArray()) {
			if (stringValue.startsWith("[")) stringValue = stringValue.substring(1);
			if (stringValue.endsWith("]"))   stringValue = stringValue.substring(0,stringValue.length()-1);
			final List<String> strVals = getList(stringValue);
			return getPrimitiveArray(clazz, strVals);
		}

		return stringValue;
	}
	
	private static List<String> getList(final String value) {
		if (value == null)           return null;
		if ("".equals(value.trim())) return null;
		final String[]    vals = value.split(",");
		final List<String> ret = new ArrayList<String>(vals.length);
		for (int i = 0; i < vals.length; i++) ret.add(vals[i].trim());
		return ret;
	}

	

	/**
	 * Not fast or pretty...
	 * 
	 * @param number
	 * @param strVals
	 * @return
	 */
	protected Object getPrimitiveArray(Class<? extends Object> clazz, List<String> strVals) {
		
		if (strVals==null || strVals.isEmpty()) return null;
	
		Object array = null;
		if (double[].class.isAssignableFrom(clazz)) array = new double[strVals.size()];
		if (float[].class.isAssignableFrom(clazz))  array = new float[strVals.size()];
		if (int[].class.isAssignableFrom(clazz))    array = new int[strVals.size()];
		if (long[].class.isAssignableFrom(clazz))   array = new long[strVals.size()];
		
		for (int i = 0; i < strVals.size(); i++) {
			Object value = null;
			if (double[].class.isAssignableFrom(clazz)) value = Double.parseDouble(strVals.get(i));
			if (float[].class.isAssignableFrom(clazz))  value = Float.parseFloat(strVals.get(i));
			if (int[].class.isAssignableFrom(clazz))    value = Integer.parseInt(strVals.get(i));
			if (long[].class.isAssignableFrom(clazz))   value = Long.parseLong(strVals.get(i));
			
			Array.set(array, i, value);
		}
		return array;
	}

}


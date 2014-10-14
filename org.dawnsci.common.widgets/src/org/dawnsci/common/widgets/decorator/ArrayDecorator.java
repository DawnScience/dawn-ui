/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.decorator;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Text;

class ArrayDecorator extends BoundsDecorator {

	protected String delimiter;
	private int      size;

	public ArrayDecorator(Text text, String numberPattern, NumberFormat numFormat, String delimiter, int size) {
		super(text, "("+numberPattern+delimiter+"? *)+", numFormat);
		this.delimiter = delimiter;
		this.size      = size;
	}

	@Override
	protected boolean check(String totalString, String delta) {

		final List<String> strings = getList(totalString, delimiter);
		if (strings==null) return true;
		for (String string : strings) {
			if (!super.check(string, delta)) return false;
		}
		if (size>0 && size!=strings.size()) return false;
		return true;
	}
	
	
	/**
	 * 
	 * @param value
	 * @return v
	 */
	private static List<String> getList(final String value, final String delimiter) {
		if (value == null)           return null;
		if ("".equals(value.trim())) return null;
		final String[]    vals = value.split(delimiter);
		final List<String> ret = new ArrayList<String>(vals.length);
		for (int i = 0; i < vals.length; i++) {
			if ("".equals(vals[i].trim())) continue;
			ret.add(vals[i].trim());
		}
		return ret;
	}

}

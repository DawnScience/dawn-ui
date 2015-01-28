/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.decorator;

import java.text.DecimalFormat;

import org.eclipse.swt.widgets.Text;

public class IntegerDecorator extends BoundsDecorator {

	public IntegerDecorator(Text text) {
		super(text, "[-0-9∞]+", new DecimalFormat("##########0"));
	}
	
	@Override
	protected Number parseValue(String totalString) {
		if ("".equals(totalString)) {
			return Double.NaN;
		}
		Number val = null;
		if ("∞".equals(totalString)) {
			val = Double.MIN_VALUE;
		} else if ("-∞".equals(totalString)) {
			val = Double.MAX_VALUE;
		} else {
			try {
		        val = Integer.parseInt(totalString);
			} catch (Exception empty) {
				val = Double.NaN;
			}
		}
		return val;
	}
}

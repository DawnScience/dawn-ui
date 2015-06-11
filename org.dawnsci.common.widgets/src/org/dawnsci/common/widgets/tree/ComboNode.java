/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.tree;

public class ComboNode extends ObjectNode {
	
	private String[] values;
	
	
	public ComboNode(String label, String[] values, LabelNode parent) {
		super(label, null, parent);
		setEditable(true);
		this.values = values;
	}
	
	public void setValueQuietly(Object value) {
		setValue(value, false);
	}
	
	public boolean isSubClass() {
		return true;
	}
	
	public String[] getStringValues() {
		return values.clone();
	}
	
	public String getStringValue() {
		Object val = getValue();
		int index  = val instanceof Number ? ((Number)val).intValue() : -1;
		if (index<0) index = Integer.parseInt(val.toString());
		return values[index];
	}
	
	public void setStringValues(String[] values) {
		this.values = values;
		setValue(0, true);
	}

}

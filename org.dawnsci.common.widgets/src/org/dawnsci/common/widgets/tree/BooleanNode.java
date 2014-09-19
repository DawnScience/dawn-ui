/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.tree;



public class BooleanNode extends ObjectNode {


	public BooleanNode(String label, boolean value, LabelNode parent) {
		super(label, value, parent);
		setEditable(true);
	}

	public boolean isValue() {
		return ((Boolean)value).booleanValue();
	}
	public boolean isSubClass() {
		return true;
	}

}

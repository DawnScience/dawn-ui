/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.common.widgets.tree;

import org.eclipse.swt.graphics.Color;

public class ColorNode extends ObjectNode {

	public ColorNode(String label, Color color, LabelNode parent) {
		super(label, color, parent);
		setEditable(true);
	}

	public Color getColor() {
		return ((Color)getValue());
	}

	public void setColor(Color color) {
		setValue(color, true);
	}
	public boolean isSubClass() {
		return true;
	}
}

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

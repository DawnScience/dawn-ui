package org.dawnsci.common.widgets.tree;

import org.eclipse.swt.graphics.Color;

public class ColorNode extends LabelNode {

	private Color color;

	public ColorNode(String label, Color color, LabelNode parent) {
		super(label, parent);
		this.color = color;
		setEditable(true);
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

}

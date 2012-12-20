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

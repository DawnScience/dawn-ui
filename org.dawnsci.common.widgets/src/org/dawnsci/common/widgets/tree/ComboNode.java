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
		return values[(Integer)getValue()];
	}
	
	public void setStringValues(String[] values) {
		this.values = values;
		setValue(0, true);
	}

}

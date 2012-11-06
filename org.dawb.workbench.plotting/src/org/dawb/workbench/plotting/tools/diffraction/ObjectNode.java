package org.dawb.workbench.plotting.tools.diffraction;


/**
 * This class may be used with TreeNodeContentProvider to create a Tree of editable
 * items. It can also be used with a swing tree.
 * 
 * @author fcp94556
 *
 */
public class ObjectNode extends LabelNode {

    private Object value;

	public ObjectNode(String label, Object value, LabelNode parent) {
		super(label, parent);
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}

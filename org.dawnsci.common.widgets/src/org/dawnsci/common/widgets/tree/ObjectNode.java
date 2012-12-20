package org.dawnsci.common.widgets.tree;


/**
 * This class may be used with TreeNodeContentProvider to create a Tree of editable
 * items. It can also be used with a swing tree.
 * 
 * The classes LabelNode, NumericNode and ObjectNode are generic and may be used 
 * elsewhere. They have not been moved somewhere generic yet because they create a 
 * dependency on jscience.

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ObjectNode other = (ObjectNode) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}

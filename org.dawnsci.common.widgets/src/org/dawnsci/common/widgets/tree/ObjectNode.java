package org.dawnsci.common.widgets.tree;

import java.util.Collection;
import java.util.HashSet;


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

    protected Object value;

	public ObjectNode(String label, Object value, LabelNode parent) {
		super(label, parent);
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		setValue(value, true);
	}
	public void setValue(Object value, boolean fireListeners) {
		this.value = value;
		if (fireListeners) fireValueChanged(new ValueEvent(this, value));
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
	
	
	private void fireValueChanged(ValueEvent evt) {
		if (listeners==null) return;
		for (ValueListener l : listeners) {
			l.valueChanged(evt);
		}
	}

	protected Collection<ValueListener> listeners;
	
	public void addValueListener(ValueListener l) {
		if (listeners==null) listeners = new HashSet<ValueListener>(3);
		listeners.add(l);
	}
	
	public void removeValueListener(ValueListener l) {
		if (listeners==null) return;
		listeners.remove(l);
	}

	public boolean isSubClass() {
		return false;
	}

}

package org.dawnsci.common.widgets.tree;

import java.util.EventObject;

public class ValueEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9037300720398962478L;
	private Object value;

	public ValueEvent(Object source, Object value) {
		super(source);
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}

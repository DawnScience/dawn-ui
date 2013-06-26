package org.dawnsci.common.widgets.decorator;

import java.util.EventObject;

public class ValueChangeEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8983136524290779712L;
	private Number value;

	public ValueChangeEvent(Object source, Number value) {
		super(source);
		this.value = value;
	}

	public Number getValue() {
		return value;
	}

}

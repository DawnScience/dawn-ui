package org.dawnsci.common.widgets.decorator;

import java.util.EventListener;

public interface IValueChangeListener extends EventListener{

	/**
	 * This listener is very trickey. Be warned the value that the box
	 * is currently taking is returned by evt.getValue(), if you ask the 
	 * box for the value or do a ((BoundsDecorator)evt.getSource()).getValue()
	 * it will be the previous value!
	 * @param evt
	 */
	void valueValidating(ValueChangeEvent evt);
}

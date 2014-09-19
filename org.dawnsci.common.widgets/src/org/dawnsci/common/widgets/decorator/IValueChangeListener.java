/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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

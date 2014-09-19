/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.event;

import java.util.Calendar;

/**
 *
 */
public abstract class ValueAdapter implements ValueListener {

	protected String name;

	/**
	 * Generates a semi-unique name based on the time at which this ValueAdapter
	 * is created.
	 */
	public ValueAdapter() {
		this.name = "Unnamed "+Calendar.getInstance().getTimeInMillis();
	}
	/**
	 * 
	 * If you have a unique name for the listener use this constructor to avoid
	 * ever getting duplicate listeners in the SWT listener list. The unique name
	 * is used in a map of listeners rather than a list as is normally the case.
	 * 
	 * Adding too many listeners is easily done and can fire more events that are required.
	 * 
	 * For instance:
	 * 1. A choice changes and an anonymous ValueListener is added to an IFieldWidget in the callback.
	 * 2. SWT can fire listeners more than expected so two value listeners are actually added for each time the choice changes.
	 * 3. The value of the IFieldWidget widget changes and more updates are made than necessary
	 *    in the callback to the widget, which in this case reads a motor and sets another one and then does plot for instance.
	 * 4. GDA Client grinds to a halt.
	 *    
	 * Solution: If a unique name is known for a listener use that, then extra listeners will not be maintained
	 * in the listener collection. Also avoid adding listeners except in the constructor.
	 * 
	 * @param name
	 */
	public ValueAdapter(final String name) {
		this.name = name;
	}
	
	@Override
	public String getValueListenerName() {
		return name;
	}
	
}

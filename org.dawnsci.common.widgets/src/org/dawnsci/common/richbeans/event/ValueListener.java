/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.richbeans.event;

import java.util.EventListener;

/**
 * Listener interface for being notified of value changes
 * @author fcp94556
 *
 */
public interface ValueListener extends EventListener {

	/**
	 * Implement in notification code.
	 * @param e
	 */
	public void valueChangePerformed(ValueEvent e);
	
	/**
	 * May be implemented to return null or a name.
	 * 
	 * If a name is returned it ensures that anonymous
	 * listeners do not build up in the listener array.
	 * 
	 * ValueAdapter can be used to help by automatically
	 * implementing this method. ValueAdapter() is essentially
	 * the same as assuming you are adding a unique instance once
	 * and ValueAdapter(String name) defines the map key and protects
	 * against duplicates in code that may be called more than once.
	 * 
	 * @return name
	 */
	public String getValueListenerName();
	
}

	
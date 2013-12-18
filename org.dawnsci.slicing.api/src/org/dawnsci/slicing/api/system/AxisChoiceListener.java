/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.slicing.api.system;

import java.util.EventListener;

public interface AxisChoiceListener extends EventListener{

	/**
	 * Called when the user changes their axis choice.
	 * @param evt
	 */
	public void axisChoicePerformed(AxisChoiceEvent evt);
}

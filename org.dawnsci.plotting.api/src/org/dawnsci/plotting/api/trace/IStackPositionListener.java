/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.api.trace;

import java.util.EventListener;

/**
 * Interface used to notify of the stack position changing.
 * @author fcp94556
 *
 */
public interface IStackPositionListener extends EventListener {

	public void stackPositionChanged(final StackPositionEvent evt);
}

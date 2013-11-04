/*
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.api.axis;

import java.util.EventListener;

/**
 * It is a good idea to implement hasCode and equals so that removeAxisListener(...) works ok.
 * @author fcp94556
 *
 */
public interface IAxisListener extends EventListener{

	/**
	 * Called when axis changes range e.g. zoom.
	 * @param evt
	 */
	public void rangeChanged(AxisEvent evt);
	
	/**
	 * Called when axis needs to be revalidated on the redraw.
	 * @param evt
	 */
	public void revalidated(AxisEvent evt);

}

/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.jreality.tool;

import java.util.EventListener;

/**
 * PanActionListener listens to panning events executed by the PanningTool
 */

public interface PanActionListener extends EventListener {

	public void panPerformed(double xTrans, double yTrans);
	
}

/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.histogram.functions.classes;


public class NCDGreenTransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		if (value >= 0.749) return 1;
		if (value <= 0.447) return 0;
		if (value <= 0.569) return 0.639 * (value - 0.447) / (0.569 - 0.447);
		if (value >= 0.690) return 0.639 + (1 - 0.639) * (value - 0.690) / (0.749 - 0.690); 
		return 0.639;
	}


}

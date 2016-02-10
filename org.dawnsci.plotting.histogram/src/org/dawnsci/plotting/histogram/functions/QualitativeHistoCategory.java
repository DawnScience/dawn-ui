/*-
 * Copyright 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.plotting.histogram.functions;

import org.eclipse.dawnsci.plotting.api.histogram.HistoCategory;
import org.eclipse.dawnsci.plotting.api.histogram.IHistogramCategory;

public class QualitativeHistoCategory implements IHistogramCategory {

	@Override
	public HistoCategory getCategory() {
		return HistoCategory.QUALITATIVE;
	}
}

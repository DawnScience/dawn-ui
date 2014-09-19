/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.spectrum.ui.processing;

import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;

public class DerivativeProcess extends AbstractProcess {

	@Override
	protected Dataset process(Dataset x, Dataset y) {
		return Maths.derivative(x, y, 1);
	}

	@Override
	protected String getAppendingName() {
		return "_derivative";
	}
}

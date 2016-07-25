/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.spectrum.ui.processing;

import org.eclipse.january.dataset.Dataset;

import uk.ac.diamond.scisoft.analysis.optimize.ApachePolynomial;

public class PolySmoothProcess extends AbstractProcess {

	@Override
	protected Dataset process(Dataset x, Dataset y) {
		try {
			return ApachePolynomial.getPolynomialSmoothed(x,y,13,9);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	protected String getAppendingName() {
		return "_smooth";
	}

}

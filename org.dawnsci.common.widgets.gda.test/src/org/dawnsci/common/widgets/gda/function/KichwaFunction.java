/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.common.widgets.gda.function;

import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;

import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CoordinatesIterator;

public class KichwaFunction extends AFunction implements IFunction {
	private static final double[] params = new double[]{};

	/**
	 * Zero-argument constructor required for extension-point instantiation
	 */
	public KichwaFunction() {
		super(params);
	}
	
	public KichwaFunction(int numberOfParameters) {
		super(numberOfParameters);
	}

	public KichwaFunction(double... params) {
		super(params);
	}

	public KichwaFunction(IParameter... params) {
		super(params);
	}

	@Override
	public double val(double... values) {
		return 0;
	}
	
	@Override
	public String getName() {
		return "Kichwa Test Function";
	}

	@Override
	public void fillWithValues(DoubleDataset data, CoordinatesIterator it) {
	}

}

/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.fitting;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IOperator;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IPeak;

public class NullFunction implements IPeak {

	private static final long serialVersionUID = IFunction.serialVersionUID;

	@Override
	public String getName() {
		return null;
	}

	@Override
	public void setName(String newName) {
	}

	@Override
	public double val(double... values) {
		return 0;
	}

	@Override
	public IParameter getParameter(int index) {
		return null;
	}

	@Override
	public IParameter[] getParameters() {
		return null;
	}

	@Override
	public int getNoOfParameters() {
		return 0;
	}

	@Override
	public double getParameterValue(int index) {
		return 0;
	}

	@Override
	public double[] getParameterValues() {
		return null;
	}

	@Override
	public void setParameter(int index, IParameter parameter) {
	}

	@Override
	public void setParameterValues(double... params) {
	}

	@Override
	public double partialDeriv(int Parameter, double... position) {
		return 0;
	}

	@Override
	public double partialDeriv(IParameter param, double... values) {
		return 0;
	}

	@Override
	public DoubleDataset makeDataset(IDataset... values) {
		return null;
	}

	@Override
	public double residual(boolean allValues, IDataset data, IDataset... values) {
		return 0;
	}

	@Override
	public double residual(boolean allValues, IDataset data, IDataset weight, IDataset... values) {
		return 0;
	}

	@Override
	public double getPosition() {
		return 0;
	}

	@Override
	public double getFWHM() {
		return 0;
	}

	@Override
	public double getArea() {
		return 0;
	}

	@Override
	public double getHeight() {
		return 0;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public void setDescription(String newDescription) {
	}

	@Override
	public void setMonitor(IMonitor monitor) {
	}

	@Override
	public IMonitor getMonitor() {
		return null;
	}

	@Override
	public IFunction copy() throws Exception {
		return null;
	}

	@Override
	public void setDirty(boolean isDirty) {
	}

	@Override
	public IDataset calculateValues(IDataset... coords) {
		return null;
	}

	@Override
	public IDataset calculatePartialDerivativeValues(IParameter param, IDataset... coords) {
		return null;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public IOperator getParentOperator() {
		return null;
	}

	@Override
	public void setParentOperator(IOperator parent) {
	}
}

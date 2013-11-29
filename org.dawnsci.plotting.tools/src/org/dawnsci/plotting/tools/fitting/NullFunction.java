package org.dawnsci.plotting.tools.fitting;

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IParameter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IPeak;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

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
	public String getParameterName(int index) {
		return null;
	}

	@Override
	public void setParameterName(String name, int index) {
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
}

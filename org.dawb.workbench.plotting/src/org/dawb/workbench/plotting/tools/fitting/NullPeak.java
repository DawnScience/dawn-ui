package org.dawb.workbench.plotting.tools.fitting;

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IParameter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IPeak;

public class NullPeak implements IPeak {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setName(String newName) {
		// TODO Auto-generated method stub

	}

	@Override
	public double val(double... values) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IParameter getParameter(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IParameter[] getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getNoOfParameters() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNoOfFunctions() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IFunction getFunction(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getParameterValue(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double[] getParameterValues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParameterValues(double... params) {
		// TODO Auto-generated method stub

	}

	@Override
	public double partialDeriv(int Parameter, double... position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DoubleDataset makeDataset(IDataset... values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double residual(boolean allValues, IDataset data, IDataset... values) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPosition() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getFWHM() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getArea() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDescription(String newDescription) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getParameterName(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParameterName(String name, int index) {
		// TODO Auto-generated method stub
		
	}

}

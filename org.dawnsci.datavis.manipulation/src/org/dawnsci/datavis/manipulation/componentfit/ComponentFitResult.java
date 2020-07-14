package org.dawnsci.datavis.manipulation.componentfit;

import java.util.Iterator;
import java.util.List;

import org.dawnsci.datavis.manipulation.DataManipulationUtils;
import org.eclipse.january.dataset.IDataset;

public class ComponentFitResult {

	private IDataset data;
	private IDataset fitted;
	private IDataset sum;
	private IDataset residual;
	private List<IDataset> componentValue;
	private double axisPosition;

	public ComponentFitResult(IDataset data, IDataset fitted, IDataset sum, IDataset residual, List<IDataset> componentValues, double axisPosition) {
		
		this.data = data;
		this.fitted = fitted;
		this.sum = sum;
		this.residual = residual;
		this.componentValue = componentValues;
		this.axisPosition = axisPosition;
	}

	public IDataset getData() {
		return data;
	}

	public IDataset getFitted() {
		return fitted;
	}

	public IDataset getSum() {
		return sum;
	}

	public IDataset getResidual() {
		return residual;
	}

	public List<IDataset> getComponentValue() {
		return componentValue;
	}
	
	public Iterator<IDataset> getFittedDataIterator() {
		
		return DataManipulationUtils.getXYIterator(fitted);
		
	}
	
	public double getAxisPosition() {
		return axisPosition;
	}
}

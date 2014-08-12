package org.dawnsci.processing.ui;

import org.eclipse.jface.viewers.ILabelProvider;

import uk.ac.diamond.scisoft.analysis.processing.model.IOperationModel;

public class OperationPropertyDescriptorData {
	public IOperationModel model;
	public String name;
	public ILabelProvider labelProvider;

	public OperationPropertyDescriptorData() {
	}
}
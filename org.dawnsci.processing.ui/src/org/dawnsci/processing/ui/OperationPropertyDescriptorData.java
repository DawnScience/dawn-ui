package org.dawnsci.processing.ui;

import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.jface.viewers.ILabelProvider;

public class OperationPropertyDescriptorData {
	public IOperationModel model;
	public String name;
	public ILabelProvider labelProvider;

	public OperationPropertyDescriptorData() {
	}
}
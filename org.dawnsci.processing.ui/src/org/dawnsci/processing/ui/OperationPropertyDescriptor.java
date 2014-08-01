package org.dawnsci.processing.ui;

import org.eclipse.ui.views.properties.PropertyDescriptor;

import uk.ac.diamond.scisoft.analysis.processing.model.IOperationModel;

public class OperationPropertyDescriptor extends PropertyDescriptor {

	private IOperationModel model;
	private String name;

	public OperationPropertyDescriptor(IOperationModel model, String name) {
		super(name, name);
		this.model = model;
		this.name  = name;
	}

}

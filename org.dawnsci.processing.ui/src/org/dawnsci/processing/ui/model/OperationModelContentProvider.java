package org.dawnsci.processing.ui.model;

import org.dawnsci.processing.ui.processing.OperationDescriptor;
import org.eclipse.dawnsci.analysis.api.processing.model.ModelField;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class OperationModelContentProvider implements IStructuredContentProvider {


	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

	@Override
	public Object[] getElements(Object inputElement) {
		OperationDescriptor     des    = (OperationDescriptor)inputElement;
		return (ModelField[])des.getAdapter(ModelField.class);
	}

}

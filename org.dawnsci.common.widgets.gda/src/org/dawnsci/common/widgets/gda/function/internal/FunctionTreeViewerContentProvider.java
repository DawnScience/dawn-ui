package org.dawnsci.common.widgets.gda.function.internal;

import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModelRoot;
import org.dawnsci.common.widgets.gda.function.internal.model.FunctionModelElement;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class FunctionTreeViewerContentProvider implements ITreeContentProvider {

	// Currently unused, needed to properly implement getParent?
	@SuppressWarnings("unused")
	private FunctionModelRoot input;

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.input = (FunctionModelRoot) newInput;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		FunctionModelRoot root = (FunctionModelRoot) inputElement;
		return root.getChildren();
	}

	private Object[] getItems(final Object parentElement) {
		if (parentElement instanceof FunctionModelElement) {
			FunctionModelElement functionTreeModelItem = (FunctionModelElement) parentElement;
			return functionTreeModelItem.getChildren();
		}
		return new Object[0];
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return getItems(parentElement);
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof FunctionModelElement) {
			FunctionModelElement getParent = (FunctionModelElement) element;
			return getParent.getParentModel();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return getItems(element).length > 0;
	}

}

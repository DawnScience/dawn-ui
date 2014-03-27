package org.dawb.workbench.ui.diffraction.table;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class DiffCalContentProvider implements IStructuredContentProvider {
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement == null) {
			return null;
		}

		if (inputElement instanceof DiffractionDataManager) {
			return ((DiffractionDataManager) inputElement).toArray();
		}

		return null;
	}
}
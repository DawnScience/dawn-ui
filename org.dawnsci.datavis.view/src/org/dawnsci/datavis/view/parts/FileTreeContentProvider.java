package org.dawnsci.datavis.view.parts;

import java.util.function.IntPredicate;

import org.dawnsci.datavis.model.SimpleTreeObject;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class FileTreeContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return inputElement instanceof SimpleTreeObject ? ((SimpleTreeObject)inputElement).getChildren() : null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return parentElement instanceof SimpleTreeObject ? ((SimpleTreeObject)parentElement).getChildren() : null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return element instanceof SimpleTreeObject ? ((SimpleTreeObject)element).hasChildren() : false;
	}

}

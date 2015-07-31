package org.dawnsci.mapping.ui;

import org.dawnsci.mapping.ui.datamodel.MapObject;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class MapFileTreeContentProvider implements ITreeContentProvider {

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		
	}
	
	@Override
	public void dispose() {
		
	}
	
	@Override
	public boolean hasChildren(Object element) {
		return element instanceof MapObject ? ((MapObject)element).hasChildren() : false;
	}
	
	@Override
	public Object getParent(Object element) {
		return null;
	}
	
	@Override
	public Object[] getElements(Object inputElement) {
		return inputElement instanceof MapObject ? ((MapObject)inputElement).getChildren(): null;
	}
	
	@Override
	public Object[] getChildren(Object parentElement) {
		return parentElement instanceof MapObject ? ((MapObject)parentElement).getChildren(): null;
	}

}

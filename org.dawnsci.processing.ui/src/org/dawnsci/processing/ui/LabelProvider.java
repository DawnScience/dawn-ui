package org.dawnsci.processing.ui;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

class LabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {

	@Override
	public StyledString getStyledText(Object element) {
		if(!(element instanceof OperationDescriptor)) return null;
		OperationDescriptor des = (OperationDescriptor)element;
		return new StyledString(des.getName());
	}
	public Image getImage(Object element) {
		if(!(element instanceof OperationDescriptor)) return null;
		OperationDescriptor des = (OperationDescriptor)element;
		return des.getImage();
	}

}

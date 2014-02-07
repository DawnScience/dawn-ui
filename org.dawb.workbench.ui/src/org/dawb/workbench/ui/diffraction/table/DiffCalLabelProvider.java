package org.dawb.workbench.ui.diffraction.table;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public class DiffCalLabelProvider implements ITableLabelProvider {

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element == null)
			return null;

		DiffractionTableData data = (DiffractionTableData) element;
		if (columnIndex == 0) {
			return data.name;
		} else if (columnIndex == 1) { // # of rings
			if (data.rois == null)
				return null;
			return String.valueOf(data.nrois);
		} else if (columnIndex == 2) { // distance
			return String.format("%.2f", data.distance) + "*";
		}
		return null;
	}
}
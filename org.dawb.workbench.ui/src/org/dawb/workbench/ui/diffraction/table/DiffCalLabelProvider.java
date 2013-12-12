package org.dawb.workbench.ui.diffraction.table;

import org.dawb.workbench.ui.Activator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;

public class DiffCalLabelProvider implements ITableLabelProvider {

	private static final Image TICKED = Activator.getImageDescriptor(
			"icons/ticked.png").createImage();
	private static final Image UNTICKED = Activator.getImageDescriptor(
			"icons/unticked.gif").createImage();

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
		if (columnIndex != 0)
			return null;
		if (element == null)
			return null;

		DiffractionTableData data = (DiffractionTableData) element;
		if (data.use)
			return TICKED;
		return UNTICKED;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (columnIndex == 0)
			return null;
		if (element == null)
			return null;

		DiffractionTableData data = (DiffractionTableData) element;
		if (columnIndex == 1) {
			return data.name;
		} else if (columnIndex == 2) {
			if (data.rois == null)
				return null;
			return String.valueOf(data.nrois);
		}

		IDiffractionMetadata md = data.md;
		if (md == null)
			return null;

		if (columnIndex == 3) { // distance
			return String.format("%.2f", data.distance) + "*";
		} else if (columnIndex == 4) {
			DetectorProperties dp = md.getDetector2DProperties();
			if (dp == null)
				return null;
			return String.format("%.2f", dp.getBeamCentreCoords()[0]);
		} else if (columnIndex == 5) {
			DetectorProperties dp = md.getDetector2DProperties();
			if (dp == null)
				return null;
			return String.format("%.2f", dp.getBeamCentreCoords()[1]);
		} else if (columnIndex == 6) {
			if (data.use && data.q != null) {
				return String.format("%.2f", Math.sqrt(data.q.getResidual()));
			}
		}
		return null;
	}
}
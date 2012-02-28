package org.dawb.workbench.plotting.tools;

import java.text.DecimalFormat;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerColumn;

/**
 * A label provider with the ability to show:
 * 1. Name
 * 2. Position
 * 3. FWHM
 * 4. Area
 * 5. Algorithm type
 * 
 * @author fcp94556
 *
 */
public class FittingLabelProvider extends ColumnLabelProvider {

	private int column;
	private ColumnViewer viewer;
	private DecimalFormat format;

	public FittingLabelProvider(int i) {
		this.column = i;
		this.format = new DecimalFormat("##0.#####E0");
	}

	protected void initialize(ColumnViewer viewer, ViewerColumn column) {
		this.viewer = viewer;
	}
	
	@Override
	public String getText(Object element) {
		
		final Integer    peakNumber = (Integer)element;
		final FittedPeaksBean bean = (FittedPeaksBean)viewer.getInput();
		if (bean==null)     return "";
		if (bean.isEmpty()) return "";
		
		switch(column) {
		case 0:
			return bean.getPeakName(peakNumber);
		case 1:
			return format.format(bean.getPosition(peakNumber));
		case 2:
			return format.format(bean.getFWHM(peakNumber));
		case 3:
			return format.format(bean.getArea(peakNumber));
		case 4:
			return bean.getPeakType(peakNumber);
		case 5:
			return bean.getAlgorithmType(peakNumber);
		default:
			return "Not found";
		}
	}
}

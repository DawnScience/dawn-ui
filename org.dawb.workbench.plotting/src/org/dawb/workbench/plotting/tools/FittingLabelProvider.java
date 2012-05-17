package org.dawb.workbench.plotting.tools;

import java.text.DecimalFormat;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.graphics.Color;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IPeak;

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
		
		final IPeak    peak  = (IPeak)element;
		final FittedPeaks bean = (FittedPeaks)viewer.getInput();
		if (bean==null)     return "";
		if (bean.isEmpty()) return "";
		if (peak instanceof NullPeak) return "";
		
		final int peakNumber = bean.getPeaks().indexOf(peak);
		
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
	
	/**
	 * foreground
	 * @param element
	 * @return
	 */
	public Color getForeground(final Object element) {
		
		final FittedPeaks bean = (FittedPeaks)viewer.getInput();
		if (bean==null)     return super.getForeground(element);
		if (bean.isEmpty()) return super.getForeground(element);
	
		final IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
		if (sel==null) return super.getForeground(element);
		
		if (sel.getFirstElement()==element) return ColorConstants.darkGreen;
		
		return super.getForeground(element);
	}
}

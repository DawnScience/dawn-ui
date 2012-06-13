package org.dawb.workbench.plotting.tools;

import java.text.DecimalFormat;

import org.dawb.workbench.plotting.Activator;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

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

	private int           column;
	private ColumnViewer  viewer;
	private DecimalFormat format;
	private Image         savedIcon;

	public FittingLabelProvider(int i) {
		this.column = i;
		this.format = new DecimalFormat("##0.#####E0");
		this.savedIcon = Activator.getImage("icons/plot-tool-peak-fit-savePeak.png");
	}
	
	public void dispsose() {
		super.dispose();
		savedIcon.dispose();
	}

	protected void initialize(ColumnViewer viewer, ViewerColumn column) {
		this.viewer = viewer;
	}
	
	@Override
	public String getText(Object element) {
		
		if (element==null) return "";
		if (!(element instanceof FittedPeak)) return "";
		final FittedPeak  peak  = (FittedPeak)element;
		if (peak.getPeak() instanceof NullPeak) return "";
		final FittedPeaks bean = (FittedPeaks)viewer.getInput();
		
		
		switch(column) {
		case 0:
			return peak.getPeakName();
		case 1:
			return format.format(peak.getPosition());
		case 2:
			return format.format(peak.getFWHM());
		case 3:
			return format.format(peak.getArea());
		case 4:
			return peak.getPeakType();
		case 5:
			return bean.getAlgorithmType();
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
	
	public Image getImage(Object element) {
		
		if (element==null) return null;
		if (!(element instanceof FittedPeak)) return null;
		final FittedPeak  peak  = (FittedPeak)element;
		if (peak.isSaved() && column==0) return savedIcon;
		return null;
	}
}

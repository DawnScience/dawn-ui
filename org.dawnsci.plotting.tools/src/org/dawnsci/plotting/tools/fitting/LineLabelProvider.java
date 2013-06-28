package org.dawnsci.plotting.tools.fitting;

import java.text.DecimalFormat;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial;

public class LineLabelProvider extends ColumnLabelProvider{
	private int           column;
	private ColumnViewer  viewer;
	@SuppressWarnings("unused")
	private DecimalFormat intFormat, format;
	private Image         savedIcon;

	public LineLabelProvider(int i) {
		this.column = i;
		this.intFormat = new DecimalFormat("###0");
		this.format = new DecimalFormat("##0.#####");
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
		if (!(element instanceof FittedFunction)) return "";
		final FittedFunction  peak  = (FittedFunction)element;
		//if (peak. instanceof NullFunction) return "";
		@SuppressWarnings("unused")
		final FittedFunctions bean = (FittedFunctions)viewer.getInput();
		
		
		switch(column) {
		case 0:
			return peak.getDataTrace().getName();
		case 1:
			return peak.getPeakName();
		case 2:
			return getPolynomialName((Polynomial)peak.getFunction().getFunction(0));
		case 3:
			return ((Polynomial)peak.getFunction().getFunction(0)).getStringEquation();
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
		
		final FittedFunctions bean = (FittedFunctions)viewer.getInput();
		if (bean==null)     return super.getForeground(element);
		if (bean.isEmpty()) return super.getForeground(element);
	
		final IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
		if (sel==null) return super.getForeground(element);
		
		if (sel.getFirstElement()==element) return ColorConstants.darkGreen;
		
		return super.getForeground(element);
	}
	
	public Image getImage(Object element) {
		
		if (element==null) return null;
		if (!(element instanceof FittedFunction)) return null;
		final FittedFunction  peak  = (FittedFunction)element;
		if (peak.isSaved() && column==0) return savedIcon;
		return null;
	}
	
	@SuppressWarnings("unused")
	private String getPolynomialDescription(Polynomial func){
		
		StringBuilder out = new StringBuilder();
		
		int nParams = func.getNoOfParameters();
		
		DecimalFormat df = new DecimalFormat("0.#####E0");
		
		for (int i = nParams-1; i >= 2; i--) {
			out.append(df.format(func.getParameter(nParams - 1 -i).getValue()));
			out.append(String.format("x^%d + ", i));
		}
		
		if (nParams >= 2)
			out.append(df.format(func.getParameter(nParams-2).getValue()) + "x + ");
		if (nParams >= 1)
			out.append(df.format(func.getParameter(nParams-1).getValue()));
		
		return out.toString();
	}
	
	private String getPolynomialName(Polynomial func){
		
		Integer order = func.getNoOfParameters() - 1;
		
		String name = func.getName();
		
		return ordinal(order) + " Order " + name;
	
	}
	
	private String ordinal(int i) {
	    String[] sufixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
	    switch (i % 100) {
	    case 11:
	    case 12:
	    case 13:
	        return i + "th";
	    default:
	        return i + sufixes[i % 10];

	    }
	}

	
	

}

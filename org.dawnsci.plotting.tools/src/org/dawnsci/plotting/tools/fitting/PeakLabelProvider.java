/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.fitting;

import java.text.DecimalFormat;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import uk.ac.diamond.scisoft.analysis.fitting.FittingConstants;

/**
 * A label provider with the ability to show:
 * 1. Name
 * 2. Position
 * 3. FWHM
 * 4. Area
 * 5. Algorithm type
 * 
 * @author Matthew Gerring
 *
 */
public class PeakLabelProvider extends ColumnLabelProvider {

	private int           column;
	private ColumnViewer  viewer;
	private Image         savedIcon;

	public PeakLabelProvider(int i) {
		this.column = i;
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
		if (peak.getPeak() instanceof NullFunction) return "";
		final FittedFunctions bean = (FittedFunctions)viewer.getInput();
		
		IPreferenceStore store = Activator.getPlottingPreferenceStore();
		DecimalFormat format   = new DecimalFormat(store.getString(FittingConstants.REAL_FORMAT));

		switch(column) {
		case 0:
			return peak.getDataTrace().getName();
		case 1:
			return peak.getPeakName();
		case 2:
			return format.format(peak.getPosition());
		case 3:
			return format.format(peak.getDataValue());
		case 4:
			return format.format(peak.getPeakValue());
		case 5:
			return format.format(peak.getFWHM());
		case 6:
			return format.format(peak.getArea());
		case 7:
			return peak.getPeakType();
		case 8:
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
}

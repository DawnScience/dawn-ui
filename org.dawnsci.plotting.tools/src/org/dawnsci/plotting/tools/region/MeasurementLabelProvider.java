/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.region;

import java.text.DecimalFormat;

import org.dawb.common.util.number.DoubleUtils;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.preference.RegionEditorConstants;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeasurementLabelProvider extends ColumnLabelProvider {
	
	public enum LabelType {
		ROINAME, STARTX, STARTY, ROITYPE, DX, DY, LENGTH, ROISTRING;
	}

	private static final Logger logger = LoggerFactory.getLogger(MeasurementLabelProvider.class);
	private LabelType column;
	private MeasurementTool tool;
	private Image checkedIcon;
	private Image uncheckedIcon;
	private int precision = 3;

	public MeasurementLabelProvider(MeasurementTool tool, LabelType i) {
		this.column = i;
		this.tool   = tool;
	}

	private static final String NA = "-";

	@Override
	public String getText(Object element) {
		
		if (!(element instanceof IRegion)) return null;
		final IRegion    region = (IRegion)element;

		IROI roi = tool.getROI(region);

		try {
			Object fobj = null;
			if (element instanceof String) return "";
			ICoordinateSystem coords = region.getCoordinateSystem();
			if(roi == null) return "";
			double[] startPoint = getAxisPoint(coords, roi.getPoint());

			IPreferenceStore store = Activator.getPlottingPreferenceStore();
			DecimalFormat pointFormat = new DecimalFormat(store.getString(RegionEditorConstants.POINT_FORMAT));

			switch(column) {
			case ROINAME:
				return region.getLabel();
			case STARTX:
				fobj = startPoint[0];
				return fobj == null ? NA : pointFormat.format((Double)fobj);
			case STARTY: // dx
				fobj = startPoint[1];
				return fobj == null ? NA : pointFormat.format((Double)fobj);
			case ROITYPE: //ROI type
				return region.getRegionType().getName();
			case DX: // dx
				if (roi instanceof LinearROI) {
					LinearROI lroi = (LinearROI) roi;
					fobj = lroi.getEndPoint()[0] - lroi.getPointX();
				} else if (roi instanceof RectangularROI) {
					RectangularROI rroi = (RectangularROI) roi;
					fobj = rroi.getEndPoint()[0] - rroi.getPointX();
				}
				return fobj == null ? NA : getCalibratedValue((Double)fobj);
			case DY: // dy
				if (roi instanceof LinearROI) {
					LinearROI lroi = (LinearROI) roi;
					fobj = lroi.getEndPoint()[1] - lroi.getPointY();
				} else if (roi instanceof RectangularROI) {
					RectangularROI rroi = (RectangularROI) roi;
					fobj = rroi.getEndPoint()[1] - rroi.getPointY();
				}
				return fobj == null ? NA : getCalibratedValue((Double)fobj);
			case LENGTH: // length
				
				//String unit="";
				if (roi instanceof LinearROI) {
					
					LinearROI lroi = (LinearROI) roi;
					fobj = lroi.getLength();
		
				} else if (roi instanceof RectangularROI) {
					RectangularROI rroi = (RectangularROI) roi;
					double[] lens = rroi.getLengths();
					fobj = Math.hypot(lens[0], lens[1]);
				}
				return fobj == null ? NA : getCalibratedValue((Double)fobj);
			case ROISTRING: // region
				return tool.getROI(region).toString();
			default:
				return "";
			}
		} catch (Throwable ne) {
			// One must not throw RuntimeExceptions like null pointers from this
			// method because the user gets an eclipse dialog confusing them with 
			// the error
			logger.error("Cannot get value in info table", ne);
			return "";
		}
	}

	private String getCalibratedValue(double pixelLength) {
		
	    // TODO FIXME bodge for calibrated MeasurementTools to show length
		// Using direct cast for now.
		if (tool instanceof MeasurementTool) {
			double xFactor = ((MeasurementTool)tool).getxCalibratedAxisFactor();
			double yFactor = ((MeasurementTool)tool).getyCalibratedAxisFactor();
	        if (xFactor==yFactor && !Double.isNaN(xFactor)) {
	        	double value = pixelLength*xFactor;
	        	String unit = ((MeasurementTool)tool).getUnitName();
	        	return DoubleUtils.roundDouble(value, precision)+" "+unit;
	        }
		} 
		
		return String.valueOf(DoubleUtils.roundDouble(pixelLength, precision));

	}

	public String getToolTipText(Object element) {
		return "Any selection region can be used in measurement tool. Try box and axis selections as well as line...";
	}

	@Override
	public void dispose(){
		super.dispose();
		if (checkedIcon != null && uncheckedIcon != null) {
			checkedIcon.dispose();
			uncheckedIcon.dispose();
		}
	}

	/**
	 * get point in axis coords
	 * @param coords
	 * @return
	 */
	private double[] getAxisPoint(ICoordinateSystem coords, double... vals) {
		if (coords==null) return vals;
		try {
			return coords.getValueAxisLocation(vals);
		} catch (Exception e) {
			return vals;
		}
	}
}

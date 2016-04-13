/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.slicing.component;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.slicing.api.system.AxisType;
import org.eclipse.dawnsci.slicing.api.system.DimsData;
import org.eclipse.dawnsci.slicing.api.system.SliceSource;
import org.eclipse.dawnsci.slicing.api.util.SliceUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SliceColumnLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {
	
	private final static Logger logger = LoggerFactory.getLogger(SliceColumnLabelProvider.class); 

	private int             col;
	private TableViewer     viewer;
	private SliceSystemImpl system;
	private NumberFormat    format;
	
	public SliceColumnLabelProvider(SliceSystemImpl system, TableViewer viewer, int i) {
		this.col    = i;
		this.system = system;
		this.viewer = viewer;
		this.format = DecimalFormat.getNumberInstance();
	}
	@Override
	public StyledString getStyledText(Object element) {
		
		if (viewer.getTable().getColumn(col).getWidth()<1) return new StyledString();
				
		final DimsData data = (DimsData)element;
		final StyledString ret = new StyledString();
		switch (col) {
		case 0:
			ret.append((data.getDimension()+1)+"");
			break;
		case 1:
			ret.append( getAxisLabel(data) );
			break;
		case 2:
			if (data.isTextRange()) {
				ret.append(data.getSliceRange(true)!=null ? new StyledString(data.getSliceRange(true)) 
				                                          : new StyledString("all"));
			} else {
				final int slice = data.getSlice();
				String formatValue = String.valueOf(slice);
				try {
					formatValue = getFormatValue(data, slice);
					if (data.getPlotAxis().isAdvanced()) {
						final StringBuilder buf = new StringBuilder();
						buf.append("[ ");
						buf.append(formatValue);
						buf.append(" : ");
						buf.append(getFormatValue(data, slice+data.getSliceSpan()));
						buf.append(" ]");
						formatValue = buf.toString();
					}
				} catch (Throwable ne) {
					formatValue = String.valueOf(slice);
				}
				ret.append( slice>-1 ? formatValue : "" );
			}
			ILazyDataset lazy = system.getLazyDataset();
			if (lazy != null) {
				try {
					final int[] shape = lazy.getShape();
					if ((data.isSlice() || data.isTextRange()) && !system.isErrorVisible() && shape[data.getDimension()]>1) {
						ret.append(new StyledString(" *", StyledString.DECORATIONS_STYLER));
					}
				} catch (Throwable largelyIgnored) {
					logger.error("Unable to determine if editable.");
				}
			}
			break;
		case 3:
			if (system.getCurrentSlice()!=null) {
				final String axisName = SliceUtils.getAxisLabel(system.getCurrentSlice(), data);
				if (axisName!=null) ret.append(axisName);
			}
			break;
		case 4:
			if (data.getPlotAxis().isAdvanced()) {
				ret.append(String.valueOf(data.getSliceSpan()));
			}
			break;
		default:
			ret.append( "" );
			break;
		}
		
		return ret;
	}
	
	
	private String getFormatValue(DimsData data, int index) {
		SliceSource d = system.getData();
		if (d == null)
			return String.valueOf(index);

		int max = d.getLazySet().getShape()[data.getDimension()];
		if (index>=max) index = max-1;
		String formatValue = String.valueOf(index);
		try {
			if (system.isAxesVisible()) {
				Number value = SliceUtils.getAxisValue(system.getCurrentSlice(), system.getData().getVariableManager(), data, index, null);
				formatValue = format.format(value);
			} else {
				formatValue = String.valueOf(index);
			}
		} catch (Throwable ne) {
			formatValue = String.valueOf(index);
		}
		return formatValue;
	}
	/**
	 * This method attempts to get a label for the dimension 
	 * given the DimsData and the current sliceType.
	 * 
	 * TODO Create better algorithm for this my using
	 * getPlotAxisLabel() all the time and ensuring that
	 * the DimsData is correctly set up.
	 * 
	 * @param data
	 * @return
	 */
	protected String getAxisLabel(DimsData data) {

		final AxisType axis = data.getPlotAxis();
		
		Enum sliceType = system.getSliceType();
        if (PlotType.class!=sliceType.getClass()) {
        	return axis.getLabel();
        }
		if (sliceType==PlotType.ISOSURFACE || sliceType == PlotType.VOLUME) {
            return axis.getLabel();
		}
		
		if (data.isTextRange()) return data.getPlotAxis().getLabel();
		if (data.getPlotAxis().isAdvanced()) return data.getPlotAxis().getLabel();
		
		// Bit naughty but we test the kind of slice they have
		// set to do in the labels that we show them here.
		if (sliceType==PlotType.XY) {
			return axis.getIndex()>-1 ? AxisType.X.getLabel() : AxisType.SLICE.getLabel();
		}
		if (sliceType==PlotType.XY_STACKED) {
			if (axis == AxisType.X) return axis.getLabel();
		}

		if (sliceType instanceof PlotType && system.getPlottingSystem()!=null) {
			if (system.isReversedImage()) {
				return axis==AxisType.X ? AxisType.Y.getLabel() : axis==AxisType.Y ? AxisType.X.getLabel() : AxisType.SLICE.getLabel();				
			}
		}
		return axis==AxisType.X ? AxisType.X.getLabel() : axis==AxisType.Y ? AxisType.Y.getLabel() : AxisType.SLICE.getLabel();
	}


			
}

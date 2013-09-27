package org.dawnsci.slicing.component;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.slicing.api.system.DimsData;
import org.dawnsci.slicing.api.util.SliceUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
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
			if (data.isRange()) {
				ret.append(data.getSliceRange()!=null? new StyledString(data.getSliceRange()) : new StyledString("all"));
			} else {
				final int slice = data.getSlice();
				String formatValue = String.valueOf(slice);
				try {
					if (system.isAxesVisible()) {
						Number value = SliceUtils.getAxisValue(system.getCurrentSlice(), system.getData().getVariableManager(), data, slice, null);
						formatValue = format.format(value);
					} else {
						formatValue = String.valueOf(slice);
					}
				} catch (Throwable ne) {
					formatValue = String.valueOf(slice);
				}
				ret.append( slice>-1 ? formatValue : "" );
			}
			
			try {
				final int[] shape = system.getLazyDataset().getShape();
				if ((data.isSlice() || data.isRange()) && !system.isErrorVisible() && shape[data.getDimension()]>1) {
					ret.append(new StyledString(" (click to change)", StyledString.QUALIFIER_STYLER));
				}
			} catch (Throwable largelyIgnored) {
				logger.error("Unable to determine if editable.");
			}
			break;
		case 3:
			if (system.getCurrentSlice()!=null) {
				final String axisName = SliceUtils.getAxisName(system.getCurrentSlice(), data);
				if (axisName!=null) ret.append(axisName);
			}
		default:
			ret.append( "" );
			break;
		}
		
		return ret;
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

		final int axis = data.getPlotAxis();
		
		Enum sliceType = system.getSliceType();
        if (PlotType.class!=sliceType.getClass()) {
        	return data.getPlotAxisLabel();
        }
		
		if (data.isRange()) return "(Range)";
		
		// Bit naughty but we test the kind of slice they have
		// set to do in the labels that we show them here.
		if (sliceType==PlotType.XY) {
			return axis>-1 ? "X" : "(Slice)";
		}
		if (sliceType==PlotType.XY_STACKED) {
			return axis==0 ? "X" : axis==1 ? "Y (Many)" : "(Slice)";
		}
		if (sliceType instanceof PlotType && system.getPlottingSystem()!=null) {
			if (system.isReversedImage()) {
				return axis==0 ? "Y" : axis==1 ? "X" : "(Slice)";				
			}
		}
		return axis==0 ? "X" : axis==1 ? "Y" : "(Slice)";
	}


			
}

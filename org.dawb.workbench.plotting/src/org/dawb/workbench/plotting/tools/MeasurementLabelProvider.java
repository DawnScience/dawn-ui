package org.dawb.workbench.plotting.tools;

import java.text.DecimalFormat;

import org.dawb.common.ui.plot.region.IRegion;
import org.eclipse.jface.viewers.ColumnLabelProvider;

public class MeasurementLabelProvider extends ColumnLabelProvider {

	
	private int column;
	private MeasurementTool tool;
	private DecimalFormat format;

	public MeasurementLabelProvider(MeasurementTool tool, int i) {
		this.column = i;
		this.tool   = tool;
		this.format = new DecimalFormat("##0.00E0");
	}

	@Override
	public String getText(Object element) {
		
		if (!(element instanceof IRegion)) return null;
		final IRegion    region = (IRegion)element;
		
		switch(column) {
		case 0:
			return region.getName();
		case 1:
			return region.getRegionType().getName();
		case 2: // dx
			return format.format(tool.getBounds(region).getDx());
		case 3: // dy
			return format.format(tool.getBounds(region).getDy());
		case 4: // length
			return format.format(tool.getBounds(region).getLength());
		case 5: // max
			return format.format(tool.getMax(region));
		case 6: // region
			return tool.getBounds(region).toString();

		default:
			return "Not found";
		}
	}
	
	public String getToolTipText(Object element) {
		return "Any selection region can be used in measurement tool. Try box and axis selections as well as line...";
	}

}

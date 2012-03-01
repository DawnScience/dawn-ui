package org.dawb.workbench.plotting.tools;

import org.dawb.common.ui.plot.region.IRegion;
import org.eclipse.jface.viewers.ColumnLabelProvider;

public class MeasurementLabelProvider extends ColumnLabelProvider {

	
	private int column;
	private MeasurementTool tool;

	public MeasurementLabelProvider(MeasurementTool tool, int i) {
		this.column = i;
		this.tool   = tool;
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
		case 2:
			return tool.getBounds(region).toString();
		default:
			return "Not found";
		}
	}
	
	public String getToolTipText(Object element) {
		return "Any selection region can be used in measurement tool. Try box and axis selections as well as line...";
	}

}

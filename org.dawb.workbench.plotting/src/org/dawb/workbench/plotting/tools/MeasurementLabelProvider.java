package org.dawb.workbench.plotting.tools;

import java.text.DecimalFormat;

import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeasurementLabelProvider extends ColumnLabelProvider {

	private static final Logger logger = LoggerFactory.getLogger(MeasurementLabelProvider.class);
	
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
		
		try {
			switch(column) {
			case 0:
				return region.getName();
			case 1:
				return region.getRegionType().getName();
			case 2: // dx
				if (region.getRegionType()==RegionType.RING) return "-";
				return format.format(tool.getBounds(region).getDx());
			case 3: // dy
				if (region.getRegionType()==RegionType.RING) return "-";
				return format.format(tool.getBounds(region).getDy());
			case 4: // length
				if (region.getRegionType()==RegionType.RING) return "-";
				return format.format(tool.getBounds(region).getLength());
			case 5: // max
				final double max = tool.getMaxIntensity(region);
			    if (Double.isNaN(max)) return "-";
				return format.format(max);
			case 6: // in rad
				if (region.getRegionType()!=RegionType.RING) return "-";
				return format.format(tool.getBounds(region).getInner());
			case 7: // out rad
				if (region.getRegionType()!=RegionType.RING) return "-";
				return format.format(tool.getBounds(region).getOuter());
			case 8: // region
				return tool.getBounds(region).toString();
	
			default:
				return "Not found";
			}
		} catch (Throwable ne) {
			// One must not throw RuntimeExceptions like null pointers from this
			// methd becuase the user gets an eclipse dialog confusing them with 
			// the error
			logger.error("Cannot get value in info table", ne);
			return "";
		}
	}
	
	public String getToolTipText(Object element) {
		return "Any selection region can be used in measurement tool. Try box and axis selections as well as line...";
	}

}

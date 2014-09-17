package org.dawnsci.slicing.tools.plot;

import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.slicing.api.system.AxisType;
import org.eclipse.dawnsci.slicing.api.system.DimsDataList;
import org.eclipse.dawnsci.slicing.api.tool.AbstractSlicingTool;

/**
 * This is a simple type of slice tool based on the available plot
 * options of the plotting system. However custom slice tools may
 * exist which have more complex UI.
 * 
 * @author fcp94556
 *
 */
public class LineSlicingTool extends AbstractSlicingTool {

	@Override
	public void militarize(boolean newData) {
		
		
		boolean wasImage = getSlicingSystem().getSliceType()==PlotType.IMAGE || 
				           getSlicingSystem().getSliceType()==PlotType.SURFACE;
		getSlicingSystem().setSliceType(getSliceType());
		
		final DimsDataList dimsDataList = getSlicingSystem().getDimsDataList();
		if (dimsDataList!=null) {
			if (wasImage&&dimsDataList.isXFirst()) {
				dimsDataList.setSingleAxisOnly(AxisType.Y, AxisType.X);   		
			} else {
				dimsDataList.setSingleAxisOnly(AxisType.X, AxisType.X);
			}
		}
		getSlicingSystem().update(false);
	}

	@Override
	public Enum getSliceType() {
		return PlotType.XY;
	}
}

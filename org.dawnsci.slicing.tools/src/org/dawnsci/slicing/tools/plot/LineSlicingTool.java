package org.dawnsci.slicing.tools.plot;

import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.slicing.api.system.DimsDataList;
import org.dawnsci.slicing.api.tool.AbstractSlicingTool;

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
	public void militarize() {
		
		
		boolean wasImage = getSlicingSystem().getSliceType()==PlotType.IMAGE || 
				           getSlicingSystem().getSliceType()==PlotType.SURFACE;
		getSlicingSystem().setSliceType(getSliceType());
		
		final DimsDataList dimsDataList = getSlicingSystem().getDimsDataList();
		if (dimsDataList!=null) {
			if (wasImage&&dimsDataList.isXFirst()) {
				dimsDataList.setSingleAxisOnly(1, 0);   		
			} else {
				dimsDataList.setSingleAxisOnly(0, 0);
			}
		}
		getSlicingSystem().update(false);
	}

	@Override
	public Enum getSliceType() {
		return PlotType.XY;
	}
}

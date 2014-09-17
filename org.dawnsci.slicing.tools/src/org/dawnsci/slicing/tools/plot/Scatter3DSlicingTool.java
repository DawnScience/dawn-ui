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
 */
public class Scatter3DSlicingTool extends AbstractSlicingTool {

	@Override
	public void militarize(boolean newData) {
		getSlicingSystem().setSliceType(getSliceType());

		final DimsDataList dimsDataList = getSlicingSystem().getDimsDataList();
		if (dimsDataList!=null)
			dimsDataList.setThreeAxesOnly(AxisType.X, AxisType.Y, AxisType.Z);

		getSlicingSystem().update(true);
	}

	@Override
	public Enum<?> getSliceType() {
		return PlotType.XY_SCATTER_3D;
	}
}

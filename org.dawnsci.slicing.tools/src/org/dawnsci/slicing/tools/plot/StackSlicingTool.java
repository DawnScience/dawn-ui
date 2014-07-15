package org.dawnsci.slicing.tools.plot;

import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.slicing.api.system.AxisType;
import org.eclipse.dawnsci.slicing.api.system.DimsData;
import org.eclipse.dawnsci.slicing.api.system.DimsDataList;
import org.eclipse.dawnsci.slicing.api.tool.AbstractSlicingTool;

import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;

/**
 * This is a simple type of slice tool based on the available plot
 * options of the plotting system. However custom slice tools may
 * exist which have more complex UI.
 * 
 * @author fcp94556
 *
 */
public class StackSlicingTool extends AbstractSlicingTool {

	private static final int MAX_STACK = 100;
	
	@Override
	public void militarize() {
		
		
		getSlicingSystem().setSliceType(getSliceType());
		
		final DimsDataList dimsDataList = getSlicingSystem().getDimsDataList();
		if (dimsDataList!=null) {
			dimsDataList.setTwoAxesOnly(AxisType.X, AxisType.Y_MANY);
			dimsDataList.removeLargeStacks(getSlicingSystem(), MAX_STACK);
		}

		getSlicingSystem().update(true);
	}

	@Override
	public Enum getSliceType() {
		return PlotType.XY_STACKED;
	}
}

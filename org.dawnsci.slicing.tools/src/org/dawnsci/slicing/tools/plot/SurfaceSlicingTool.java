package org.dawnsci.slicing.tools.plot;

import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.dawnsci.slicing.api.system.DimsDataList;
import org.dawnsci.slicing.api.system.AxisType;
import org.dawnsci.slicing.api.tool.AbstractSlicingTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple type of slice tool based on the available plot
 * options of the plotting system. However custom slice tools may
 * exist which have more complex UI.
 * 
 * @author fcp94556
 *
 */
public class SurfaceSlicingTool extends AbstractSlicingTool {
	
	private static final Logger logger = LoggerFactory.getLogger(SurfaceSlicingTool.class);

	@Override
	public void militarize() {
		
		getSlicingSystem().setSliceType(getSliceType());
		
		final DimsDataList dimsDataList = getSlicingSystem().getDimsDataList();
		if (dimsDataList!=null) dimsDataList.setTwoAxesOnly(AxisType.X, AxisType.Y);   
		getSlicingSystem().refresh();
		getSlicingSystem().update(true);
		
		showWindowTool();
		
	}
	protected void showWindowTool() {
		try {
			final IToolPageSystem system = (IToolPageSystem)getSlicingSystem().getPlottingSystem().getAdapter(IToolPageSystem.class);
			system.setToolVisible("org.dawb.workbench.plotting.tools.windowTool", ToolPageRole.ROLE_3D, 
					                      "org.dawb.workbench.plotting.views.toolPageView.3D");
		} catch (Exception e1) {
			logger.error("Cannot open window tool!", e1);
		}
	}

	@Override
	public Enum getSliceType() {
		return PlotType.SURFACE;
	}
}

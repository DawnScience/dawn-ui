/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawnsci.plotting.jreality;

import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;

/**
 *
 */
public enum PlottingMode {
	/**
	 * 
	 */
	ONED(GuiPlotMode.ONED),
	/**
	 * 
	 */
	ONED_THREED(GuiPlotMode.ONED_THREED), 
	/**
	 * 
	 */
	TWOD(GuiPlotMode.TWOD), 
	/**
	 * 
	 */
	SURF2D(GuiPlotMode.SURF2D),
	/**
	 * 
	 */
	MULTI2D(GuiPlotMode.MULTI2D), 
	/**
	 * 
	 */
	BARCHART(null),
	/**
	 * 
	 */
    SCATTER2D(GuiPlotMode.SCATTER2D),
    /**
     * 
     */
	SCATTER3D(GuiPlotMode.SCATTER3D),
	/**
	 * NULL MODE 
	 */
	EMPTY(null);
	
	private GuiPlotMode plotMode;

	private PlottingMode(GuiPlotMode plotMode) {
		this.plotMode = plotMode;
	}

	/**
	 * Get GUI plot mode from plotting mode
	 * @return GUI plot mode
	 */
	public GuiPlotMode getGuiPlotMode() {
		return plotMode;
	}

	/**
	 * Find PlottingMode corresponding to a GUI plot mode
	 * @param guiMode
	 * @return plotting mode
	 */
	public static PlottingMode plottingModeFromGui(GuiPlotMode guiMode) {
		for (PlottingMode p : PlottingMode.values())
			if (p.plotMode.equals(guiMode))
				return p;

		return null;
	}
}

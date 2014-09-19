/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawnsci.plotting.preference;

import org.dawnsci.plotting.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.dawnsci.plotting.api.histogram.HistogramBound;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.HistoType;
import org.eclipse.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
import org.eclipse.dawnsci.plotting.api.preferences.PlottingConstants;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;
import org.eclipse.jface.preference.IPreferenceStore;


public class PlottingPreferenceInitializer extends AbstractPreferenceInitializer {

		
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		//store.setDefault(PlottingConstants.PLOTTING_SYSTEM_CHOICE, "org.dawb.workbench.editors.plotting.lightWeightPlottingSystem");
        store.setDefault(PlottingConstants.PLOT_X_DATASET,  true);
		store.setDefault(PlottingConstants.XY_SHOWLEGEND,   true);
		store.setDefault(PlottingConstants.COLOUR_SCHEME,   "Gray Scale");
		store.setDefault(PlottingConstants.ORIGIN_PREF,     ImageOrigin.TOP_LEFT.getLabel());
		store.setDefault(PlottingConstants.ASPECT,          true);
		store.setDefault(PlottingConstants.SNAP_TO_GRID,    false);
		store.setDefault(PlottingConstants.LABEL_ZOOM_ENABLED, true);
		store.setDefault(PlottingConstants.SHOW_AXES,       true);
		store.setDefault(PlottingConstants.SHOW_INTENSITY,  true);
		store.setDefault(PlottingConstants.IGNORE_RGB,      false);
		store.setDefault(PlottingConstants.HISTO ,          false);
		store.setDefault(PlottingConstants.DOWNSAMPLE_PREF, DownsampleType.MAXIMUM.getLabel());
		store.setDefault(PlottingConstants.HISTO_PREF ,     HistoType.OUTLIER_VALUES.getLabel());
		store.setDefault(PlottingConstants.HISTO_LO ,       00.01);
		store.setDefault(PlottingConstants.HISTO_HI ,       99.99);
		store.setDefault(PlottingConstants.MIN_CUT,         HistogramBound.DEFAULT_MINIMUM.toString());
		store.setDefault(PlottingConstants.MAX_CUT,         HistogramBound.DEFAULT_MAXIMUM.toString());
		store.setDefault(PlottingConstants.NAN_CUT,         HistogramBound.DEFAULT_NAN.toString());
		store.setDefault(PlottingConstants.FREE_DRAW_WIDTH, 4);
		store.setDefault(PlottingConstants.MASK_DRAW_TYPE,       "direct");
		store.setDefault(PlottingConstants.MASK_DRAW_MULTIPLE,   false);
		store.setDefault(PlottingConstants.MASK_PEN_SIZE,   10);
		store.setDefault(PlottingConstants.MASK_PEN_MASKOUT,     true);
		store.setDefault(PlottingConstants.MASK_AUTO_APPLY,      false);
		store.setDefault(PlottingConstants.MASK_REGIONS_ENABLED, false);
		store.setDefault(PlottingConstants.RESET_ON_DEACTIVATE,  true);
		store.setDefault(PlottingConstants.INCLUDE_ORIGINAL,     true);
		store.setDefault(PlottingConstants.LOAD_IMAGE_STACKS,    false);
		store.setDefault(PlottingConstants.ZOOM_INTO_WHITESPACE, true);
		store.setDefault(PlottingConstants.USE_PALETTE_FUNCTIONS, false);
		
		store.setDefault(PlottingConstants.GLOBAL_SHOW_ERROR_BARS, true);
		store.setDefault(PlottingConstants.AUTO_HIDE_ERROR_SIZE,   25);
		
	}
}
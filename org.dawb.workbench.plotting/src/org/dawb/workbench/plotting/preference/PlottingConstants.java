/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.workbench.plotting.preference;


public class PlottingConstants {

	
	public static final String PLOTTING_SYSTEM_CHOICE = "org.dawb.plotting.system.choice";

	public static final String PLOT_X_DATASET = "org.dawb.lightweight.plot.x.choice";

	
	/**
	 * 
	 */
	public final static String XY_SHOWLEGEND = "org.csstudio.swt.xygraph.preferences.show.legend";

	/**
	 * Used to store palette preference
	 */
	public static final String P_PALETTE = "org.dawb.plotting.system.paletteChoice";

	/**
	 * Used to store origin preference
	 */
	public static final String ORIGIN_PREF = "org.dawb.plotting.system.originChoice";

	/**
	 * true when the image should keep aspect ratio, otherwise it will stretch to the available area.
	 */
	public static final String ASPECT = "org.dawb.plotting.system.aspectRatio";

	/**
	 * true when zooming should rehistogram, also rehistograms when pressed.
	 */
	public static final String HISTO = "org.dawb.plotting.system.rehistogram";

	/**
	 * Used to store origin preference
	 */
	public static final String HISTO_PREF = "org.dawb.plotting.system.histogram.type";
	
	/**
	 * Used for cut pixels
	 */
	public static final String MIN_CUT = "org.dawb.plotting.system.histogram.min.cut";
	public static final String MAX_CUT = "org.dawb.plotting.system.histogram.max.cut";
	public static final String NAN_CUT = "org.dawb.plotting.system.histogram.nan.cut";

	/**
	 * Used to store the users preference for free draw.
	 */
	public static final String FREE_DRAW_WIDTH = "org.dawb.plotting.system.region.freedraw.width";

}
/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawnsci.plotting.api.preferences;


public class BasePlottingConstants {
	
	
	/**
	 * 
	 */
	public final static String XY_SHOWLEGEND = "org.csstudio.swt.xygraph.preferences.show.legend";

	/**
	 * Used to store palette preference
	 */
	public static final String COLOUR_SCHEME = "org.dawb.plotting.system.colourSchemeName";

	/**
	 * Used to store origin preference, do not change string
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
	 * Used to store histo preference
	 */
	public static final String HISTO_PREF = "org.dawb.plotting.system.histogram.type";

	/**
	 * 
	 */
	public static final String HISTO_LO = "org.dawb.plotting.system.histogram.lo";
	
	/**
	 * 
	 */
	public static final String HISTO_HI = "org.dawb.plotting.system.histogram.hi";

	/**
	 * Used to store downsample preference
	 */
	public static final String DOWNSAMPLE_PREF = "org.dawb.plotting.system.downsample.type";
	
	
	/**
	 * Used for cut pixels
	 */
	public static final String MIN_CUT = "org.dawb.plotting.system.histogram.min.cut";
	public static final String MAX_CUT = "org.dawb.plotting.system.histogram.max.cut";
	public static final String NAN_CUT = "org.dawb.plotting.system.histogram.nan.cut";

}
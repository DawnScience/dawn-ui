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



public class PlottingConstants extends BasePlottingConstants {

	
	public static final String PLOTTING_SYSTEM_CHOICE = "org.dawb.plotting.system.choice";

	public static final String PLOT_X_DATASET = "org.dawb.lightweight.plot.x.choice";

	/**
	 * Used to store the users preference for free draw.
	 */
	public static final String FREE_DRAW_WIDTH = "org.dawb.plotting.system.region.freedraw.width";

	/**
	 * 
	 */
	public static final String MASK_DRAW_TYPE = "org.dawb.plotting.system.masking.draw.type";

	/**
	 * 
	 */
	public static final String MASK_DRAW_MULTIPLE = "org.dawb.plotting.system.region.multi.draw.mode";

	/**
	 * 
	 */
	public static final String MASK_AUTO_APPLY = "org.dawb.plotting.system.masking.auto.apply";

	/**
	 * 
	 */
	public static final String MASK_REGIONS_ENABLED = "org.dawb.plotting.system.masking.regions.enabled";
	
	/**
	 * 
	 */
	public static final String MASK_PEN_SIZE = "org.dawb.plotting.system.masking.pen.size";

	/**
	 * 
	 */
	public static final String MASK_PEN_SHAPE = "org.dawb.plotting.system.masking.pen.shape";
	
	/**
	 * 
	 */
	public static final String MASK_PEN_MASKOUT = "org.dawb.plotting.system.masking.pen.mask.out";

	/**
	 * 
	 */
	public static final String RESET_ON_DEACTIVATE = "org.dawb.plotting.system.derivative.reset.when.deactivate";

	/**
	 * Property to store if axes should be shown.
	 */
	public static final String SHOW_AXES           = "org.dawb.workbench.plotting.preference.showAxes";
	
	/**
	 * Save if image history should include original plot or not.
	 */
	public static final String INCLUDE_ORIGINAL    = "org.dawb.workbench.plotting.preference.includeOriginalPlot";

	/**
	 * Stores if intensity scale should be shown.
	 */
	public static final String SHOW_INTENSITY = "org.dawb.workbench.plotting.preference.showIntensity";
	
	/**
	 * Tells the Image editor to attempt to load stacks.
	 */
	public static final String LOAD_IMAGE_STACKS = "org.dawb.workbench.plotting.preference.loadImageStacks";

}
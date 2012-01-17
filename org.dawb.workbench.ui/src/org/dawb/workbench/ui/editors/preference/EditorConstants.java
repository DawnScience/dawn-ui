/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.workbench.ui.editors.preference;


public class EditorConstants {

	// Intentionally same as diamond ones! - NOTE the term 'nexus' is redundant
	public static final String IGNORE_DATASET_FILTERS = "ignore.data.set.filters";
	public static final String SHOW_XY_COLUMN         = "show.xy.column.in.nexus.editor";
	public static final String SHOW_DATA_SIZE         = "show.data.size.in.nexus.editor";
	public static final String SHOW_DIMS              = "show.dims.in.nexus.editor";
	public static final String SHOW_SHAPE             = "show.shape.in.nexus.editor";
	public static final String SHOW_VARNAME           = "show.variable.name.in.nexus.editor";
	public static final String DATA_FORMAT            = "data.format.editor.view";
	public static final String PLAY_SPEED             = "data.format.slice.play.speed";
	
	
	public static final String PLOTTING_SYSTEM_CHOICE = "org.dawb.plotting.system.choice";
	
	
	/**
	 * Used to record the preference as to wether a workflow should be used with
	 * highlighting to the actors or not.
	 */
	public static final String HIGHLIGHT_ACTORS_CHOICE = "org.dawb.actor.highlight.choice";
	
	/**
	 * Used to record if user would like to plot as indices (false) or as x data set (true)
	 */
	public static final String PLOT_X_DATASET = "org.dawb.lightweight.plot.x.choice";

}
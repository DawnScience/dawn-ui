/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
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
	public static final String SHOW_LOCALNAME         = "use.local.name.in.nexus.editor";
	public static final String DATA_FORMAT            = "data.format.editor.view";
	public static final String PLAY_SPEED             = "data.format.slice.play.speed";
	
	/**
	 * Used to record the preference as to wether a workflow should be used with
	 * highlighting to the actors or not.
	 */
	public static final String HIGHLIGHT_ACTORS_CHOICE = "org.dawb.actor.highlight.choice";
	
	// Properties for saving
	public static final String SAVE_SEL_DATA         = "data.selected.save";
	public static final String DATA_SEL = "org.dawb.workbench.ui.editors.plotdata.selected";
	
	public static final String SAVE_LOG_FORMAT       = "data.format.save.log.format";
	public static final String SAVE_TIME_FORMAT      = "data.format.save.time.format";
	public static final String SAVE_FORMAT_STRING    = "data.format.save.time.format";
	
	public static final String RESCALE_SETTING       = "org.dawb.workbench.ui.editors.preference.plot.editor.rescale";
	public static final String PLOT_DATA_NAME_WIDTH  = "org.dawb.workbench.ui.editors.preference.plot.editor.data.name.width";
	
	/**
	 * Property used for storing some axis settings.
	 */
	public static final String XAXIS_PROP_STUB = "org.dawb.workbench.ui.editors.preference.plot.xaxis.";
	/**
	 * Property used for storing some axis settings.
	 */
	public static final String YAXIS_PROP_STUB = "org.dawb.workbench.ui.editors.preference.plot.yaxis.";

}
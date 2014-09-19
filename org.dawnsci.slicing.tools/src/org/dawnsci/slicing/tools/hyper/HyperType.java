/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.slicing.tools.hyper;

public enum HyperType {
	
	Box_Axis("Hyper 3D (Box and Axis Region)",   "icons/cutter_box.png"), 
	Line_Line("Hyper 3D (Line and Axis Line)",   "icons/cutter_line.png"), 
	Line_Axis("Hyper 3D (Line and Axis Region)", "icons/cutter_axis_line.png");
	
	private final String label, iconPath;
	HyperType(String label, String iconPath) {
		this.label = label;
		this.iconPath = iconPath;
	}
	
	/**
	 * This method is called by reflection to determine the 
	 * number of non-slice dimensions to be shown to the user.
	 * 
	 * If there are new HyperTypes in future which do not have
	 * 3 dimensions to the data which must be sliced, the dims
	 * will be an argument to the enum @see PlotType
	 * 
	 * @return
	 */
	public int getDimensions() {
		return 3;
	}
	public String getLabel() {
		return label;
	}

	public String getIconPath() {
		return iconPath;
	}

	public String toString() {
		return label;
	}
}

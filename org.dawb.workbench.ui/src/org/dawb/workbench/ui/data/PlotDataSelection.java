/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.workbench.ui.data;

public enum PlotDataSelection {

	AUTO("Last data selected (Automatic)"), FIXED("Named data (Fixed)"), NONE("None");
	
	private final String label;
	PlotDataSelection(String label) {
		this.label = label;
	}


	public boolean isFixed() {
		return this==FIXED;
	}
	public static boolean isFixed(String pds) {
		return valueOf(pds).isFixed();
	}

	/**
	 * 
	 * @param autoType
	 * @return true if this PlotDataSelection is active
	 */
	public boolean isActive() {
		return NONE!=this;
	}
	public static boolean isActive(String pds) {
		return valueOf(pds).isActive();
	}

	public boolean isAuto() {
		return AUTO==this;
	}
	public static boolean isAuto(String pds) {
		return valueOf(pds).isAuto();
	}

	public String getLabel() {
		return label;
	}
}

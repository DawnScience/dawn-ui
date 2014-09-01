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

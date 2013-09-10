package org.dawnsci.slicing.tools.hyper;

public enum HyperType {
	Box_Axis, Line_Line, Line_Axis;
	
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

}

package org.dawnsci.plotting.api;

public enum ActionType {

	XY, IMAGE, XYANDIMAGE, THREED, ALL;

	public boolean isCompatible(PlotType type) {
		
		if (this==ALL) return true;
		
		if (type.is1D() && this==XY)    {
			return true;
		}
		if (type.is2D() && this==IMAGE) {
			return true;
		}
		if ((type.is2D()||type.is1D()) && this==XYANDIMAGE) {
			return true;
		}
		if (type.is3D() && this==THREED) {
			return true;
		}
		
		return false;
	}
}

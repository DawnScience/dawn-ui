package org.dawnsci.plotting.tools.powderlines;

public class PowderLineModel {

	private double dSpacing; // The fundamental value
	private double beamWavelength;
	
	public PowderLineModel() {
		dSpacing = 0.0;
	}
	
	public PowderLineModel(double dSpacing) {
		this.dSpacing = dSpacing;
	}
	
	public void setWavelength(double beamWavelength) {
		this.beamWavelength = beamWavelength;
	}
	
	public double getDSpacing() {
		return dSpacing;
	}
	
	public double getQ() {
		return 2*Math.PI/dSpacing;
	}
	
	public double get2Theta(double beamWavelength) {
		return twoThetaFromD(dSpacing, beamWavelength);
	}
	
	public double get(PowderLinesModel.PowderLineCoord coord) {
		switch (coord) {
		case ANGLE:
			return get2Theta(this.beamWavelength);
		case Q:
			return getQ();
		case D_SPACING:
		default:
			return getDSpacing();
		}
	}
	
	protected static double twoThetaFromD(double dSpacing, double beamWavelength) {
		return Math.toDegrees(2*Math.asin(beamWavelength/2/dSpacing));
	}
}

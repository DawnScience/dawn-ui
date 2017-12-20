package org.dawnsci.plotting.tools.powderlines;

public class EoSLineModel extends PowderLineModel {
	
	private double lengthRatio;
	
	@Override
	public double getDSpacing() {
		return lengthRatio*super.getDSpacing();
	}
	
	@Override
	public double getQ() {
		return super.getQ()/lengthRatio;
	}
	
	@Override
	public double get2Theta(double beamWavelength) {
		return twoThetaFromD(this.getDSpacing(), beamWavelength);
	}
	
	public void setLengthRatio(double lengthRatio) {
		this.lengthRatio = lengthRatio;
	}
}

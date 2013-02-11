package org.dawnsci.plotting.preference.detector;

import java.io.Serializable;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

public class DiffractionDetector implements Serializable{

	private static final long serialVersionUID = -1133345866155946034L;
	
	//public final static Unit<Length> MICROMETER = SI.MILLIMETER;
	
	private String detectorName;
	private Amount<Length> xPixelSize;
	private Amount<Length> yPixelSize;
	private String units;
	
	public String getDetectorName() {
		return detectorName;
	}
	public void setDetectorName(String name) {
		this.detectorName = name;
	}
	public Amount<Length> getxPixelSize() {
		return xPixelSize;
	}
	public void setxPixelSize(Amount<Length> xPixelSize) {
		this.xPixelSize = xPixelSize;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((detectorName == null) ? 0 : detectorName.hashCode());
		result = prime * result + ((units == null) ? 0 : units.hashCode());
		result = prime * result
				+ ((xPixelSize == null) ? 0 : xPixelSize.hashCode());
		result = prime * result
				+ ((yPixelSize == null) ? 0 : yPixelSize.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DiffractionDetector other = (DiffractionDetector) obj;
		if (detectorName == null) {
			if (other.detectorName != null)
				return false;
		} else if (!detectorName.equals(other.detectorName))
			return false;
		if (units == null) {
			if (other.units != null)
				return false;
		} else if (!units.equals(other.units))
			return false;
		if (xPixelSize == null) {
			if (other.xPixelSize != null)
				return false;
		} else if (!xPixelSize.equals(other.xPixelSize))
			return false;
		if (yPixelSize == null) {
			if (other.yPixelSize != null)
				return false;
		} else if (!yPixelSize.equals(other.yPixelSize))
			return false;
		return true;
	}
	public Amount<Length> getyPixelSize() {
		return yPixelSize;
	}
	public void setyPixelSize(Amount<Length> yPixelSize) {
		this.yPixelSize = yPixelSize;
	}
	public String getUnits() {
		return units;
	}
	public void setUnits(String units) {
		this.units = units;
	}
	
	public double getXPixelMM() {
		if (xPixelSize ==  null) return Double.NaN;
		return xPixelSize.doubleValue(SI.MILLIMETER);
	}
	
	public void setXPixelMM(double pixmm) {
		xPixelSize = Amount.valueOf(pixmm, SI.MILLIMETER);
	}
	
	public double getYPixelMM() {
		if (yPixelSize ==  null) return Double.NaN;
		return yPixelSize.doubleValue(SI.MILLIMETER);
	}
	
	public void setYPixelMM(double pixmm) {
		yPixelSize = Amount.valueOf(pixmm, SI.MILLIMETER);
	}
}

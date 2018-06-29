package org.dawnsci.dedi.configuration.devices;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import org.eclipse.dawnsci.analysis.api.unit.UnitUtils;

// TODO Test immutability.

/**
 * A class to represent any device that has a circular shape with a {@link Amount<Length>} diameter,
 * that is placed on a 2D screen consisting of pixels.
 * The subclasses of this class are meant to be immutable, although there is no way to enforce this.
 * The 2 known subclasses are {@link Beamstop} and {@link CameraTube}. Both are immutable.
 *
 */
public class CircularDevice {
	private Quantity<Length> diameter;
	private Double xcentre; // in pixels
	private Double ycentre; // in pixels

	/**
	 * @param diameter
	 *           The diameter of the device.
	 * @param xcentre
	 *           The x coordinate of the centre of the device in pixels.
	 * @param ycentre
	 *           The y coordinate of the centre of the device in pixels.
	 */
	public CircularDevice(Quantity<Length> diameter, Double xcentre, Double ycentre) {
		super();
		this.diameter = (diameter == null) ? null : UnitUtils.copy(diameter);
		this.xcentre = xcentre;
		this.ycentre = ycentre;
	}

	public Quantity<Length> getDiameter() {
		return (diameter == null) ? null : UnitUtils.copy(diameter);
	}

	/**
	 * @return The x coordinate of the centre of the device in pixels.
	 */
	public Double getXCentre() {
		return xcentre;
	}

	/**
	 * @return The y coordinate of the centre of the device in pixels.
	 */
	public Double getYCentre() {
		return ycentre;
	}

	/**
	 * @return The diameter of the device in millimetres.
	 */
	public Double getDiameterMM(){
		return UnitUtils.convertToMM(diameter);
	}

	/**
	 * @return The radius of the device in millimetres.
	 */
	public Double getRadiusMM(){
		return getDiameterMM()/2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((diameter == null) ? 0 : diameter.hashCode());
		result = prime * result + ((xcentre == null) ? 0 : xcentre.hashCode());
		result = prime * result + ((ycentre == null) ? 0 : ycentre.hashCode());
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
		CircularDevice other = (CircularDevice) obj;
		if (diameter == null) {
			if (other.diameter != null) {
				return false;
			}
		} else if(other.diameter == null) {
			return false;
		} else if (!diameter.equals(other.diameter) && UnitUtils.convertToMM(diameter) != UnitUtils.convertToMM(other.diameter)) {
			return false;
		}
		if (xcentre == null) {
			if (other.xcentre != null)
				return false;
		} else if (!xcentre.equals(other.xcentre))
			return false;
		if (ycentre == null) {
			if (other.ycentre != null)
				return false;
		} else if (!ycentre.equals(other.ycentre))
			return false;
		return true;
	}
}

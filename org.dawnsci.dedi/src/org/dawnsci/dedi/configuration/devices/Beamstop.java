package org.dawnsci.dedi.configuration.devices;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

public class Beamstop extends CircularDevice {
	public Beamstop(Quantity<Length> diameter, Double xcentre, Double ycentre) {
		super(diameter, xcentre, ycentre);
	}
}

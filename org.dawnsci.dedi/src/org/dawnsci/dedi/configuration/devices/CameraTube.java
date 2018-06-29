package org.dawnsci.dedi.configuration.devices;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

public class CameraTube extends CircularDevice {
	public CameraTube(Quantity<Length> diameter, Double xcentre, Double ycentre) {
		super(diameter, xcentre, ycentre);
	}
}

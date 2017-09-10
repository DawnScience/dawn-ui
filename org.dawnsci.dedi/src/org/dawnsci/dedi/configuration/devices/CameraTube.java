package org.dawnsci.dedi.configuration.devices;

import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

public class CameraTube extends CircularDevice {
	public CameraTube(Amount<Length> diameter, Double xcentre, Double ycentre) {
		super(diameter, xcentre, ycentre);
	}
}

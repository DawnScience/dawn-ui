package org.dawnsci.rcp.functions.classes;


/**
 * Very basic transfer function which provides a simple squared function
 * @author ssg37927
 *
 */
public class Cos90TransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		return Math.abs(Math.cos(value*Math.PI*0.5));
	}

}

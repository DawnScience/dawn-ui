package org.dawnsci.rcp.functions.classes;


/**
 * Very basic transfer function which provides a simple squared function
 * @author ssg37927
 *
 */
public class Sin90TransferFunction extends AbstractTransferFunction {

	@Override
	public double getPoint(double value) {
		return Math.sin(value*Math.PI*0.5);
	}

}

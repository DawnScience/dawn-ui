package org.dawnsci.plotting.services;

import uk.ac.diamond.scisoft.analysis.histogram.mapfunctions.AbstractMapFunction;

/**
 * To be used as a function object in ImageServiceBean to override 
 * the normal 8-bit mapping and provide functions for use with SWTImageUtils
 * 
 * @author fcp94556
 *
 */
public class FunctionContainer {
	
	private AbstractMapFunction redFunc;
	private AbstractMapFunction greenFunc;
	private AbstractMapFunction blueFunc;
	private boolean inverseRed;
	private boolean inverseGreen;
	private boolean inverseBlue;
	public AbstractMapFunction getRedFunc() {
		return redFunc;
	}
	public void setRedFunc(AbstractMapFunction redFunc) {
		this.redFunc = redFunc;
	}
	public AbstractMapFunction getGreenFunc() {
		return greenFunc;
	}
	public void setGreenFunc(AbstractMapFunction greenFunc) {
		this.greenFunc = greenFunc;
	}
	public AbstractMapFunction getBlueFunc() {
		return blueFunc;
	}
	public void setBlueFunc(AbstractMapFunction blueFunc) {
		this.blueFunc = blueFunc;
	}
	public boolean isInverseRed() {
		return inverseRed;
	}
	public void setInverseRed(boolean inverseRed) {
		this.inverseRed = inverseRed;
	}
	public boolean isInverseGreen() {
		return inverseGreen;
	}
	public void setInverseGreen(boolean inverseGreen) {
		this.inverseGreen = inverseGreen;
	}
	public boolean isInverseBlue() {
		return inverseBlue;
	}
	public void setInverseBlue(boolean inverseBlue) {
		this.inverseBlue = inverseBlue;
	}

}

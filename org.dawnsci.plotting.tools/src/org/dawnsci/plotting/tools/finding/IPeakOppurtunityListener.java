package org.dawnsci.plotting.tools.finding;

import java.util.EventListener;

/**
 * TODO: data pass for peaks
 * 
 * @author Dean P. Ottewell
 *
 */
public interface IPeakOppurtunityListener extends EventListener {

	public void peaksAdded();
	
	public void peaksRemoved();
	
	public void regionBoundsAdjustment();
	
}

package org.dawnsci.plotting.tools.finding;

import java.util.EventListener;

import org.eclipse.january.dataset.Dataset;

/**
 * TODO: data pass for peaks
 * TODO: region bounds might be better off as separate
 * @author Dean P. Ottewell
 */
public interface IPeakOpportunityListener extends EventListener {

	public void peaksChanged(PeakOpportunityEvent  evt);
	
	public void boundsChanged(double upper, double lower);
	
	public void dataChanged(Dataset nXData, Dataset nYData);
	
	public void isPeakFinding();
	
	public void finishedPeakFinding();
	
	public void activateSearchRegion();
}

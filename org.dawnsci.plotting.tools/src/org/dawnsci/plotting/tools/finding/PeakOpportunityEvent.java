package org.dawnsci.plotting.tools.finding;

import java.util.EventObject;
import java.util.List;


import uk.ac.diamond.scisoft.analysis.peakfinding.Peak;

/**
 *
 * @author Dean P. Ottewell
 */
public class PeakOpportunityEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	private IPeakOpportunity peakOpp;
	
	public PeakOpportunityEvent(Object source, IPeakOpportunity result) {
		super(source);
		this.peakOpp = result;
	}
	
	public IPeakOpportunity getPeakOpp(){
		return peakOpp;
	}

	
}

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
	
	private List<Peak> peaks;
	
	public PeakOpportunityEvent(Object source, List<Peak> result) {
		super(source);
		this.peaks = result;
	}
	
	public List<Peak> getPeaks(){
		return peaks;
	}

	
}

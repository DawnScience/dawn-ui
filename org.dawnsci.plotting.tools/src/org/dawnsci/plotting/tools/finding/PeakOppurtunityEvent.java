package org.dawnsci.plotting.tools.finding;

import java.util.EventObject;
import java.util.List;

import uk.ac.diamond.scisoft.analysis.peakfinding.Peak;

/**
 * @author Dean P. Ottewell
 *
 */
public class PeakOppurtunityEvent extends EventObject {

	private static final long serialVersionUID = 1L;
	
	List<Peak> peaks;
	
	public PeakOppurtunityEvent(Object source) {
		super(source);
		// TODO Auto-generated constructor stub
	}
	
	public List<Peak> getPeaks(){
		return peaks;
	}

}

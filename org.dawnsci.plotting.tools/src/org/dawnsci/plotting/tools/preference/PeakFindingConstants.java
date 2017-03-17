package org.dawnsci.plotting.tools.preference;

import java.util.Collection;
import java.util.Map;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.dawnsci.analysis.api.peakfinding.IPeakFinderParameter;

import uk.ac.diamond.scisoft.analysis.peakfinding.IPeakFindingData;
import uk.ac.diamond.scisoft.analysis.peakfinding.IPeakFindingService;

/**
 * TODO: should really place in the peak finder... might already be constants there...
 * @author Dean P. Ottewell
 */
public class PeakFindingConstants {
	
	 //TODO: is okay to have copy names...should be separated by classes so shouldn't cross over
	public static final String PeakAlgorithm           = "uk.ac.diamond.scisoft.analysis.peakfinding.peakfinders";
	public static final String PEAKFINDINGPARAMS 		= "uk.ac.diamond.scisoft.analysis.peakfinding.peakfinders.paramater";
	
	private static IPeakFindingService peakFindServ = (IPeakFindingService)Activator.getService(IPeakFindingService.class);
	
	public static Collection<String> PEAKFINDERS = peakFindServ.getRegisteredPeakFinders();	
}

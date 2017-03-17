package org.dawnsci.plotting.tools.preference;

import java.util.Collection;
import org.dawnsci.plotting.tools.Activator;
import uk.ac.diamond.scisoft.analysis.peakfinding.IPeakFindingService;

/**
 * @author Dean P. Ottewell
 */
public class PeakFindingConstants {
	
	public static final String PeakAlgorithm           = "uk.ac.diamond.scisoft.analysis.peakfinding.peakfinders";
	public static final String PEAKFINDINGPARAMS 		= "uk.ac.diamond.scisoft.analysis.peakfinding.peakfinders.paramater";
	
	private static IPeakFindingService peakFindServ = (IPeakFindingService)Activator.getService(IPeakFindingService.class);
	
	public static Collection<String> PEAKFINDERS = peakFindServ.getRegisteredPeakFinders();	
}

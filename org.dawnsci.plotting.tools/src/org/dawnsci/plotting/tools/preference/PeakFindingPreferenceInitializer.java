package org.dawnsci.plotting.tools.preference;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.dawnsci.analysis.api.peakfinding.IPeakFinderParameter;
import org.eclipse.jface.preference.IPreferenceStore;

import uk.ac.diamond.scisoft.analysis.peakfinding.IPeakFindingService;

import org.dawnsci.plotting.tools.preference.PeakFindingConstants;

public class PeakFindingPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getPlottingPreferenceStore();
		//My Preferences are loaded from the dataServ
		store.setDefault(PeakFindingConstants.PeakAlgorithm, PeakFindingConstants.PEAKFINDERS.iterator().next());
		
		IPeakFindingService peakFindServ = (IPeakFindingService)Activator.getService(IPeakFindingService.class);
		
		Iterator<String> peakfinder = peakFindServ.getRegisteredPeakFinders().iterator();
		while(peakfinder.hasNext()){
			Map<String, IPeakFinderParameter> peakParams = peakFindServ.getPeakFinderParameters(peakfinder.next());
			for (Entry<String, IPeakFinderParameter> peakParam : peakParams.entrySet()){
				IPeakFinderParameter param = peakParam.getValue();
				String name = param.getName();
				Number val = param.getValue();
				store.setDefault(name, val.doubleValue());
			}
		}
	}
	
}

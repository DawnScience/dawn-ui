package org.dawnsci.plotting.api.region;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.region.IRegion.RegionType;

import uk.ac.diamond.scisoft.analysis.roi.IROI;

/**
 * Service for managing IROI (data region) and IRegion
 * object for managing user interface
 * 
 * @author fcp94556
 *
 */
public interface IRegionService {
	
	
    /**
     * Create a region for a given IROI. An IRegion is the user interface
     * object for a selection region.
     * 
     * @param system
     * @param roi
     * @param roiName
     * @return
     */
	public IRegion createRegion(IPlottingSystem system, IROI roi, String roiName) throws Exception;

	/**
	 * Create a region for a given region type
	 * @return
	 */
	//public Class<? extends IROI> createRegion();

	/**
	 * Get the UI RegionType for a given IROI 
	 * @param iroi
	 * @return
	 */
	public RegionType forROI(IROI iroi);
	
	/**
	 * Get the UI RegionType which best fits the class of IROI
	 * @param clazz
	 * @return
	 */
	public RegionType getRegion(Class<? extends IROI> clazz);

}

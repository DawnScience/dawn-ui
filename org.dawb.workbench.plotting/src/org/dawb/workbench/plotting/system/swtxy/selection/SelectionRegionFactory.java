package org.dawb.workbench.plotting.system.swtxy.selection;

import java.util.Collection;
import java.util.HashSet;

import org.csstudio.swt.xygraph.figures.Axis;
import org.dawb.common.ui.plot.region.IRegion.RegionType;

/**
 * Class giving access to selection regions.
 * 
 * @author fcp94556
 *
 */
public class SelectionRegionFactory {
	
	private static final Collection<RegionType> SUPPORTED_REGIONS;
	static {
		SUPPORTED_REGIONS= new HashSet<RegionType>();
		SUPPORTED_REGIONS.add(RegionType.LINE);
		SUPPORTED_REGIONS.add(RegionType.BOX);
		SUPPORTED_REGIONS.add(RegionType.RING);
		SUPPORTED_REGIONS.add(RegionType.XAXIS);
		SUPPORTED_REGIONS.add(RegionType.YAXIS);
		SUPPORTED_REGIONS.add(RegionType.XAXIS_LINE);
		SUPPORTED_REGIONS.add(RegionType.YAXIS_LINE);
		SUPPORTED_REGIONS.add(RegionType.FREE_DRAW);
		SUPPORTED_REGIONS.add(RegionType.POINT);
	}

	/**
	 * Call this method to create a selection region based on type.
	 * TODO Use a map lookup one day? The numbers are small so list is acceptable.
	 * This is a simple test on regionType at the moment.
     *
	 * @param name
	 * @param x
	 * @param y
	 * @param regionType
	 * @return
	 */
	public static AbstractSelectionRegion createSelectionRegion(final String name,
			                                                    final Axis   x,
			                                                    final Axis   y,
			                                                    final RegionType regionType) {

		AbstractSelectionRegion region = null;
		if (regionType==RegionType.LINE) {
			region = new LineSelection(name, x, y);

		} else if (regionType==RegionType.BOX) {
			region = new BoxSelection(name, x, y);
			
		} else if (regionType==RegionType.SECTOR) {
			// TODO FIXME 
			//region = new SectorSelection(name, x, y);
			
		} else if (regionType==RegionType.RING) {
			region = new RingSelection(name, x, y);

		} else if (regionType==RegionType.FREE_DRAW) {
			region = new FreeDrawSelection(name, x, y);
			
		} else if (regionType==RegionType.POINT) {
			region = new PointSelection(name, x, y);

		} else if (regionType==RegionType.XAXIS || regionType==RegionType.YAXIS || regionType==RegionType.XAXIS_LINE || regionType==RegionType.YAXIS_LINE) {
			region = new AxisSelection(name, x, y, regionType);
					
		} else {
			throw new NullPointerException("Cannot deal with "+regionType+" regions yet - sorry!");
		}	
		return region;
	}
	
	public static boolean isSupportedType(RegionType type) {
		return SUPPORTED_REGIONS.contains(type);
	}

}

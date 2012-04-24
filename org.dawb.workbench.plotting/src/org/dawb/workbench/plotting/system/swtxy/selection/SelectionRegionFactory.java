package org.dawb.workbench.plotting.system.swtxy.selection;

import java.util.Collection;
import java.util.HashSet;

import org.csstudio.swt.xygraph.figures.Axis;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.workbench.plotting.Activator;
import org.dawb.workbench.plotting.system.swtxy.XYRegionConfigDialog;
import org.dawb.workbench.plotting.system.swtxy.XYRegionGraph;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Display;

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

	public static IContributionManager fillActions(final IContributionManager    manager, 
			                                       final IRegion                 region,
			                                       final XYRegionGraph           xyGraph) {
		
		manager.add(new Separator("org.dawb.workbench.plotting.system.region.start"));

		final Action sendToBack = new Action("Send '"+region.getName()+"' to back", Activator.getImageDescriptor("icons/RegionToBack.png")) {
			public void run() {
				region.toBack();
			}
		};
		manager.add(sendToBack);
		
		final Action bringToFront = new Action("Bring '"+region.getName()+"' to front", Activator.getImageDescriptor("icons/RegionToFront.png")) {
			public void run() {
				region.toFront();
			}
		};
		manager.add(bringToFront);
		
		final Action delete = new Action("Delete '"+region.getName()+"'", Activator.getImageDescriptor("icons/RegionDelete.png")) {
			public void run() {
				xyGraph.removeRegion((AbstractSelectionRegion)region);
			}
		};
		if (region instanceof AbstractSelectionRegion) manager.add(delete);

		final Action configure = new Action("Configure '"+region.getName()+"'", Activator.getImageDescriptor("icons/RegionProperties.png")) {
			public void run() {
				final XYRegionConfigDialog dialog = new XYRegionConfigDialog(Display.getCurrent().getActiveShell(), xyGraph);
				dialog.setSelectedRegion(region);
				dialog.open();
			}
		};
		manager.add(configure);
		
		manager.add(new Separator("org.dawb.workbench.plotting.system.region.end"));
		
		return manager;
	}

}

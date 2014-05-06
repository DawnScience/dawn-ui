package org.dawnsci.plotting.draw2d.swtxy.selection;

import java.util.Collection;
import java.util.HashSet;

import org.dawb.common.services.ITransferService;
import org.dawb.common.services.ServiceManager;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.axis.ICoordinateSystem;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.draw2d.Activator;
import org.dawnsci.plotting.draw2d.swtxy.XYRegionGraph;
import org.eclipse.gef.ui.actions.Clipboard;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class giving access to selection regions.
 * 
 * @author fcp94556
 *
 */
public class SelectionRegionFactory {
	
	private static Logger logger = LoggerFactory.getLogger(SelectionRegionFactory.class);

	private static final Collection<RegionType> SUPPORTED_REGIONS;
	static {
		SUPPORTED_REGIONS = new HashSet<RegionType>();
		SUPPORTED_REGIONS.add(RegionType.POINT);
		SUPPORTED_REGIONS.add(RegionType.LINE);
		SUPPORTED_REGIONS.add(RegionType.BOX);
		SUPPORTED_REGIONS.add(RegionType.PERIMETERBOX);
		SUPPORTED_REGIONS.add(RegionType.GRID);
		SUPPORTED_REGIONS.add(RegionType.RING);
		SUPPORTED_REGIONS.add(RegionType.XAXIS);
		SUPPORTED_REGIONS.add(RegionType.YAXIS);
		SUPPORTED_REGIONS.add(RegionType.XAXIS_LINE);
		SUPPORTED_REGIONS.add(RegionType.YAXIS_LINE);
		SUPPORTED_REGIONS.add(RegionType.FREE_DRAW);
		SUPPORTED_REGIONS.add(RegionType.CIRCLE);
		SUPPORTED_REGIONS.add(RegionType.CIRCLEFIT);
		SUPPORTED_REGIONS.add(RegionType.ELLIPSE);
		SUPPORTED_REGIONS.add(RegionType.ELLIPSEFIT);
		SUPPORTED_REGIONS.add(RegionType.PARABOLA);
		SUPPORTED_REGIONS.add(RegionType.HYPERBOLA);
		SUPPORTED_REGIONS.add(RegionType.POLYLINE);
		SUPPORTED_REGIONS.add(RegionType.POLYGON);
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
	public static AbstractSelectionRegion<?> createSelectionRegion(final String            name,
			                                                    final ICoordinateSystem coords,
			                                                    final RegionType        regionType) {

		AbstractSelectionRegion<?> region = null;
		switch (regionType) {
		case BOX:
			region = new BoxSelection(name, coords);
			break;
		case CIRCLE:
			region = new CircleSelection(name, coords);
			break;
		case CIRCLEFIT:
			region = new CircleFitSelection(name, coords);
			break;
		case ELLIPSE:
			region = new EllipseSelection(name, coords);
			break;
		case ELLIPSEFIT:
			region = new EllipseFitSelection(name, coords);
			break;
		case FREE_DRAW:
			region = new FreeDrawSelection(name, coords);
			break;
		case GRID:
			region = new GridSelection(name, coords);
			break;
		case HYPERBOLA:
			region = new HyperbolaSelection(name, coords);
			break;
		case LINE:
			region = new LineSelection(name, coords);
			break;
		case PARABOLA:
			region = new ParabolaSelection(name, coords);
			break;
		case PERIMETERBOX:
			region = new PerimeterBoxSelection(name, coords);
			break;
		case POINT:
			region = new PointSelection(name, coords);
			break;
		case POLYGON:
			region = new PolygonSelection(name, coords);
			break;
		case POLYLINE:
			region = new PolylineSelection(name, coords);
			break;
		case RING:
			region = new RingSelection(name, coords);
			break;
		case SECTOR:
			region = new SectorSelection(name, coords);
			break;
		case XAXIS:
		case XAXIS_LINE:
		case YAXIS:
		case YAXIS_LINE:
			region = new AxisSelection(name, coords, regionType);
			break;
		default:
			throw new NullPointerException("Cannot deal with "+regionType+" regions yet - sorry!");
		}

		return region;
	}

	public static boolean isSupportedType(RegionType type) {
		return SUPPORTED_REGIONS.contains(type);
	}

	private static AbstractSelectionRegion<?> staticBuffer;
	public static AbstractSelectionRegion<?> getStaticBuffer() {
		return staticBuffer;
	}
	
	public static IContributionManager fillActions(final IContributionManager    manager, 
			                                       final IRegion                 region,
			                                       final XYRegionGraph           xyGraph,
			                                       final IPlottingSystem         system) {
		
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
				xyGraph.removeRegion((AbstractSelectionRegion<?>) region);
			}
		};
		if (region instanceof AbstractSelectionRegion) manager.add(delete);
		
		final Action copy = new Action("Copy '"+region.getName()+"'", Activator.getImageDescriptor("icons/RegionCopy.png")) {
			public void run() {
				staticBuffer = (AbstractSelectionRegion<?>) region;
				
				// We also copy the region as a pastable into workflows.
				ITransferService service=null;
				try {
					service = (ITransferService)ServiceManager.getService(ITransferService.class);
				} catch (Exception e) {
					logger.error("Cannot get ITransferService!", e);
				}
				if (service!=null) {
					try {
						final Object transferable = service.createROISource(region.getName(), region.getROI());
						if (transferable!=null) Clipboard.getDefault().setContents(transferable);
					} catch (Exception ne) {
						logger.trace("Cannot set the copied region as a workflow actor!", ne);
					}
				}
			}
		};
		if (region instanceof AbstractSelectionRegion) manager.add(copy);

		
		return manager;
	}

}

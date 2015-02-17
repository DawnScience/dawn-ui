package org.dawnsci.plotting.tools.profile;

import org.dawnsci.plotting.util.ColorUtility;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.IRegionListener;
import org.eclipse.dawnsci.plotting.api.region.RegionEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.swt.widgets.Display;

/**
 * A Line profile which is created by drawing a rectangle and it creates two lines
 * in the x and y 
 * @author fcp94556
 *
 */
public class CrossProfileTool extends LineProfileTool {

	private IRegionListener boxListener;

	public CrossProfileTool() {
		super();
		
		this.boxListener = new IRegionListener.Stub() {
			@Override
			public void regionAdded(RegionEvent evt) {
				try {
					createBoxLines(evt.getRegion());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}
	
	
	protected void createBoxLines(IRegion region) throws Exception {
		
		IPlottingSystem sys = getPlottingSystem();
		if (sys==null) return; // Unlikely.
		if (isDisposed()) return;
		
		// Draws two lines, then deletes the box
		if (region.getRegionType()!=RegionType.BOX) return;
		IRectangularROI roi = (IRectangularROI)region.getROI();
		
		
		// Y-line
		LinearROI yline = new LinearROI(new double[]{roi.getPoint()[0]+roi.getLength(0)/2d, roi.getPoint()[1]},
				                        new double[]{roi.getPoint()[0]+roi.getLength(0)/2d, roi.getPoint()[1]+roi.getLength(1)});
		
		add(yline, "Y Cross");
		
		// X-line
		LinearROI xline = new LinearROI(new double[]{roi.getPoint()[0],                  roi.getPoint()[1]+roi.getLength(1)/2d},
                                        new double[]{roi.getPoint()[0]+roi.getLength(0), roi.getPoint()[1]+roi.getLength(1)/2d});

		add(xline, "X Cross");
		
		
		sys.removeRegion(region);
		update(null, null, false);
	}
	
	protected ITrace createProfile(	IImageTrace   image, 
						            final IRegion region, 
						            IROI          rbs, 
						            boolean       tryUpdate,
						            boolean       isDrag,
						            IProgressMonitor monitor) {
		
		final ITrace trace = super.createProfile(image, region, rbs, tryUpdate, isDrag, monitor);
		if (trace!=null && trace instanceof ILineTrace) {
			final ILineTrace ltrace = (ILineTrace)trace;
			if (ltrace.getTraceColor()!=region.getRegionColor()) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						ltrace.setTraceColor(region.getRegionColor());
					}
				});
			}
		}
		return trace;
	}

	
	private final IRegion add(final LinearROI line, String name) throws Exception {
		
		IPlottingSystem sys = getPlottingSystem();
		final IRegion   reg = sys.createRegion(RegionUtils.getUniqueName(name, sys), RegionType.LINE);
		reg.setROI(line);
		if (sys.getRegions()!=null) {
			reg.setRegionColor(ColorUtility.getSwtColour(sys.getRegions().size()));
		}
		sys.addRegion(reg);		
	
		return reg;
	}


	@Override
	protected RegionType getCreateRegionType() {
		return RegionType.BOX;
	}

	public void activate() {

		super.activate();
		if (getPlottingSystem()!=null) {
			getPlottingSystem().addRegionListener(boxListener);
		}
	}
	
	public void deactivate() {

		super.deactivate();
		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeRegionListener(boxListener);
		}
	}

}

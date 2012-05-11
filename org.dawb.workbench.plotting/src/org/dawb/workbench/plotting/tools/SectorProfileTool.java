package org.dawb.workbench.plotting.tools;

import java.util.Arrays;
import java.util.Collection;

import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.workbench.plotting.Activator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

public abstract class SectorProfileTool extends ProfileTool {


	private MenuAction      center;
	private IRegionListener sectorRegionListener;

	@Override
	protected void configurePlottingSystem(AbstractPlottingSystem plotter) {

		// We will add an action here for centering the sector.
		this.center = new MenuAction("Center selection");
		center.setImageDescriptor(Activator.getImageDescriptor("icons/sector-center-menu.png"));
		
		getSite().getActionBars().getToolBarManager().add(center);
		
		updateSectors();
		
		this.sectorRegionListener  = new IRegionListener.Stub() {
			
			@Override
			public void regionsRemoved(RegionEvent evt) {
				updateSectors();
			}
			
			@Override
			public void regionRemoved(RegionEvent evt) {
				updateSectors();
			}
			@Override
			public void regionAdded(RegionEvent evt) {
				updateSectors();
			}
		};
	}
	
	public void activate() {
		super.activate();
		
		if (getPlottingSystem()!=null && sectorRegionListener!=null) {
			getPlottingSystem().addRegionListener(sectorRegionListener);
		}
	}
	
	public void deactivate() {
		super.deactivate();
		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeRegionListener(sectorRegionListener);
		}
	}
	
	private void updateSectors() {
		
		if (getPlottingSystem()==null) return;
		
		center.clear();
		
		final Collection<IRegion> regions = getPlottingSystem().getRegions();
		if (regions!=null) for (final IRegion region : regions) {
			if (isRegionTypeSupported(region.getRegionType())) {
				
				final Action centerRegion = new Action("Center sector '"+region.getName()+"'") {
					public void run() {
						center.setSelectedAction(this);
						final double[] cen = getBeamCenter();
						if (cen!=null) {
							final SectorROI sroi = (SectorROI)region.getROI();
							sroi.setPoint(cen);
							region.setROI(sroi);
							
							center.setSelectedAction(this);
						}
					}
				};
				centerRegion.setImageDescriptor(Activator.getImageDescriptor("icons/sector-center-action.png"));
			
				center.add(centerRegion);
			}
		}
		
		if (center.size()>0) center.setSelectedAction(0);
			
		// TODO likely to cause flicker
		getSite().getActionBars().getToolBarManager().update(true);
	}

	protected double[] getBeamCenter() {
		
        IMetaData meta = getMetaData();
        if (meta==null || !(meta instanceof IDiffractionMetadata)) {
        	return getImageCenter();
        }
         	
        IDiffractionMetadata dm = (IDiffractionMetadata)meta;
        
        if (dm.getDetector2DProperties()==null) return getImageCenter();
        
        return dm.getDetector2DProperties().getBeamLocation();
 	}

	private double[] getImageCenter() {
    	final AbstractDataset image = getImageTrace().getData();
    	return new double[]{image.getShape()[1]/2d, image.getShape()[0]/2d};
	}

	protected abstract AbstractDataset getXAxis(final SectorROI sroi, final AbstractDataset integral);
	
	/**
	 * Please name the integral the same as the name you would like to plot.
	 * 
	 * @param data
	 * @param mask
	 * @param sroi
	 * @param region
	 * @param isDrag
	 * @return
	 */
	protected abstract AbstractDataset getIntegral( final AbstractDataset data,
										            final AbstractDataset mask, 
										            final SectorROI       sroi, 
						                            final IRegion         region,
										            final boolean         isDrag);


	@Override
	protected void createProfile(IImageTrace  image, 
			                     IRegion      region, 
			                     ROIBase      rbs, 
			                     boolean      tryUpdate,
			                     boolean      isDrag,
			                     IProgressMonitor monitor) {
        
		if (monitor.isCanceled()) return;
		if (image==null) return;
		
		if (!isRegionTypeSupported(region.getRegionType())) return;

		final SectorROI sroi = (SectorROI) (rbs==null ? region.getROI() : rbs);
		if (sroi==null) return;
		if (!region.isVisible()) return;

		if (monitor.isCanceled()) return;
		
		final AbstractDataset data = isDrag ? image.getDownsampled()     : image.getData();
		final AbstractDataset mask = isDrag ? image.getDownsampledMask() : image.getMask();
		
		SectorROI downsroi = null;
		if (isDrag) {
			downsroi = sroi.copy();
			downsroi.downsample(image.getDownsampleBin());
		}
			
		final AbstractDataset integral = getIntegral(data, mask, isDrag ? downsroi : sroi, region, isDrag);	
        if (integral==null) return;
				
		final AbstractDataset xi = getXAxis(sroi, integral);
		
		if (tryUpdate) {
			final ILineTrace x_trace = (ILineTrace)profilePlottingSystem.getTrace(integral.getName());
			
			if (x_trace!=null) {
				getControl().getDisplay().syncExec(new Runnable() {
					public void run() {
						x_trace.setData(xi, integral);
					}
				});
			}		
			
		} else {
						
			Collection<ITrace> plotted = profilePlottingSystem.createPlot1D(xi, Arrays.asList(new AbstractDataset[]{integral}), monitor);
			registerTraces(region, plotted);			
		}
			
	}
	
	@Override
	protected boolean isRegionTypeSupported(RegionType type) {
		return type==RegionType.SECTOR || type==RegionType.RING;
	}

	@Override
	protected RegionType getCreateRegionType() {
		return RegionType.SECTOR;
	}

}

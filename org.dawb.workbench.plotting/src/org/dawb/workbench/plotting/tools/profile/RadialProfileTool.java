package org.dawb.workbench.plotting.tools.profile;

import java.util.Collection;

import javax.vecmath.Vector3d;

import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.gda.extensions.loaders.H5Utils;
import org.dawb.workbench.plotting.Activator;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

public class RadialProfileTool extends SectorProfileTool {
	
	private enum XAxis {
		PIXEL, RESOLUTION, ANGLE, Q,
	}
	
	private XAxis axis = XAxis.PIXEL;
    private MenuAction profileAxis;
	
	@Override
	protected void configurePlottingSystem(AbstractPlottingSystem plotter) {
		
		profileAxis = new MenuAction("Select X Axis");
		profileAxis.setToolTipText("Select x axis values");
		
		final Action pixelAxis = new Action("Px", IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				axis = XAxis.PIXEL;
				profileAxis.setSelectedAction(this);
				update(null, null, false);
			}
		};
		
		final Action resolutionAxis = new Action("d ", IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				axis = XAxis.RESOLUTION;
				profileAxis.setSelectedAction(this);
				update(null, null, false);
			}
		};
		
		final Action angleAxis = new Action("2\u03b8", IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				axis = XAxis.ANGLE;
				profileAxis.setSelectedAction(this);
				update(null, null, false);
			}
		};
		
		final Action qAxis = new Action("q ", IAction.AS_RADIO_BUTTON) {
			@Override
			public void run() {
				axis = XAxis.Q;
				profileAxis.setSelectedAction(this);
				update(null, null, false);
			}
		};
		
		
		getSite().getActionBars().getToolBarManager().add(profileAxis);

		getSite().getActionBars().getMenuManager().add(profileAxis);
		
		profileAxis.setSelectedAction(pixelAxis);
		profileAxis.add(pixelAxis);
		profileAxis.add(resolutionAxis);
		profileAxis.add(angleAxis);
		profileAxis.add(qAxis);
		
		setActionsEnabled(false);
		
		IMetaData meta = getMetaData();
		
		if (meta!=null && (meta instanceof IDiffractionMetadata))
			setActionsEnabled(true);
		
		super.configurePlottingSystem(plotter);

	}


	@Override
	protected AbstractDataset[] getXAxis(final SectorROI sroi, AbstractDataset[] integrals) {
		
		final AbstractDataset xi = DatasetUtils.linSpace(sroi.getRadius(0), sroi.getRadius(1), integrals[0].getSize(), AbstractDataset.FLOAT64);
		xi.setName("Radius (pixel)");
		
		IMetaData meta = getMetaData();
		
		if (!sroi.hasSeparateRegions())  {
			
			if (meta!=null && (meta instanceof IDiffractionMetadata)) {
				setActionsEnabled(true);
				return new AbstractDataset[]{pixelToValue(xi,(IDiffractionMetadata)meta)};
			}
			
			return new AbstractDataset[]{xi};
			
		} else {

			final AbstractDataset xii = DatasetUtils.linSpace(sroi.getRadius(0), sroi.getRadius(1), integrals[1].getSize(), AbstractDataset.FLOAT64);
			xii.setName("Radius (pixel)");
			
			if (meta!=null && (meta instanceof IDiffractionMetadata)) {
				setActionsEnabled(true);
				return new AbstractDataset[]{pixelToValue(xi,(IDiffractionMetadata)meta),pixelToValue(xii,(IDiffractionMetadata)meta)};
			}

			return new AbstractDataset[]{xi, xii};
		}
		
	}
	
	private void setActionsEnabled(boolean enable) {
		profileAxis.setEnabled(enable);
	}
	
	
	private AbstractDataset pixelToValue(AbstractDataset dataset, IDiffractionMetadata metadata) {
		
		DetectorProperties detprops = metadata.getDetector2DProperties();
    	DiffractionCrystalEnvironment diffexp = metadata.getDiffractionCrystalEnvironment();
    	
    	if (detprops == null && diffexp == null) return dataset;
    		
    	double[] beamCen = detprops.getBeamCentreCoords();
    		
    	QSpace qSpace = new QSpace(detprops, diffexp);
    	
    	switch (axis) {
    	case PIXEL:
    		return dataset;
    	case RESOLUTION:
    		for (int i = 0; i < dataset.getSize(); ++i) {
        		double val = dataset.getDouble(i);
        		Vector3d vect= qSpace.qFromPixelPosition(beamCen[0] + val, beamCen[1]);
        		dataset.set((2*Math.PI)/vect.length(), i);
        	}
    		dataset.setName("d-spacing (\u00c5)");
    		return dataset;
    	case ANGLE:
    		for (int i = 0; i < dataset.getSize(); ++i) {
        		double val = dataset.getDouble(i);
        		Vector3d vect= qSpace.qFromPixelPosition(beamCen[0] + val, beamCen[1]);
        		dataset.set(Math.toDegrees(qSpace.scatteringAngle(vect)), i);
        	}
    		dataset.setName("2\u03b8 (\u00b0)");
    		return dataset;
    	case Q:
    		for (int i = 0; i < dataset.getSize(); ++i) {
        		double val = dataset.getDouble(i);
        		Vector3d vect= qSpace.qFromPixelPosition(beamCen[0] + val, beamCen[1]);
        		dataset.set(vect.length(), i);
        	}
    		dataset.setName("q (1/\u00c5)");
    		return dataset;
    	default:
    		return dataset;

    	}
    	
	}
	

	@Override
	protected AbstractDataset[] getIntegral(AbstractDataset data,
			                              AbstractDataset mask, 
			                              SectorROI       sroi, 
			                              IRegion         region,
			                              boolean         isDrag) {


		AbstractDataset[] profile = ROIProfile.sector(data, mask, sroi, true, false, isDrag);
		
        if (profile==null) return null;
				
		final AbstractDataset integral = profile[0];
		integral.setName("Radial Profile "+region.getName());
		
		// If not symmetry profile[2] is null, otherwise plot it.
	    if (profile.length>=3 && profile[2]!=null && sroi.hasSeparateRegions()) {
	    	
			final AbstractDataset reflection = profile[2];
			reflection.setName("Symmetry "+region.getName());

			return new AbstractDataset[]{integral, reflection};
	    	
	    } else {
	    	return new AbstractDataset[]{integral};
	    }
	}

	@Override
	public DataReductionInfo export(DataReductionSlice slice) throws Exception {
		
		final IImageTrace   image   = getImageTrace();
		final Collection<IRegion> regions = getPlottingSystem().getRegions();
		
		for (IRegion region : regions) {
			if (!isRegionTypeSupported(region.getRegionType())) continue;
			if (!region.isVisible())    continue;
			if (!region.isUserRegion()) continue;
			
			final SectorROI sroi = (SectorROI)region.getROI();
			AbstractDataset[] profile = ROIProfile.sector(slice.getData(), image.getMask(), sroi, true, false, false);
		
			AbstractDataset integral = profile[0];
			integral.setName("radial_"+region.getName().replace(' ', '_'));     
			H5Utils.appendDataset(slice.getFile(), slice.getParent(), integral);
			
		    if (profile.length>=3 && profile[2]!=null && sroi.hasSeparateRegions()) {
				final AbstractDataset reflection = profile[2];
				reflection.setName("radial_sym_"+region.getName().replace(' ', '_'));     
				H5Utils.appendDataset(slice.getFile(), slice.getParent(), reflection);
		    }
		}
		return new DataReductionInfo(Status.OK_STATUS);
	}
}

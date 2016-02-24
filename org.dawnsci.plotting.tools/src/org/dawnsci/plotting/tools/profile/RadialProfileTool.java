/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.profile;

import java.util.Collection;
import java.util.Iterator;

import javax.vecmath.Vector3d;

import org.dawb.common.ui.image.IconUtils;
import org.dawb.common.ui.menu.MenuAction;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.utils.ToolUtils;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;
import org.eclipse.dawnsci.analysis.api.diffraction.DiffractionCrystalEnvironment;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;

import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
import uk.ac.diamond.scisoft.analysis.roi.XAxis;

public class RadialProfileTool extends SectorProfileTool {
	
	private XAxis axis = XAxis.PIXEL;
    private MenuAction profileAxis;
	private IAction metaLock;
	
	@Override
	protected void configurePlottingSystem(IPlottingSystem<?> plotter) {
		
		profileAxis = new MenuAction("Select X Axis");
		profileAxis.setToolTipText("Select x axis values");
		
		final Action pixelAxis = new Action("Px",IconUtils.createIconDescriptor("Px")) {
			@Override
			public void run() {
				axis = XAxis.PIXEL;
				profileAxis.setSelectedAction(this);
				
				if (!combineSymmetry.isEnabled()) { 
					combineSymmetry.setChecked(false);
					combineSymmetry.run();
					combineSymmetry.setEnabled(true);
				}
				
				profilePlottingSystem.clear();
				update(null, null, false);
			}
		};
		
		pixelAxis.setId("org.dawb.workbench.plotting.tools.profile.pixelAxisAction");
		
		final Action resolutionAxis = new Action("d ",IconUtils.createIconDescriptor("d")) {
			@Override
			public void run() {
				axis = XAxis.RESOLUTION;
				profileAxis.setSelectedAction(this);
				
				if (combineSymmetry.isEnabled()) { 
					combineSymmetry.setChecked(false);
					combineSymmetry.run();
					combineSymmetry.setEnabled(false);
				}
				
				profilePlottingSystem.clear();
				update(null, null, false);
			}
		};
		
		final Action angleAxis = new Action("2\u03b8",IconUtils.createIconDescriptor("2\u03b8")) {
			@Override
			public void run() {
				axis = XAxis.ANGLE;
				profileAxis.setSelectedAction(this);
				
				if (combineSymmetry.isEnabled()) { 
					combineSymmetry.setChecked(false);
					combineSymmetry.run();
					combineSymmetry.setEnabled(false);
				}
				
				profilePlottingSystem.clear();
				update(null, null, false);
			}
		};
		
		final Action qAxis = new Action("q ",IconUtils.createIconDescriptor("q")) {
			@Override
			public void run() {
				axis = XAxis.Q;
				profileAxis.setSelectedAction(this);
				
				if (combineSymmetry.isEnabled()) { 
					combineSymmetry.setChecked(false);
					combineSymmetry.run();
					combineSymmetry.setEnabled(false);
				}
				profilePlottingSystem.clear();
				update(null, null, false);
			}
		};
		
		
		final String fullImageSector = "Full_Image_Sector";
		
		final Action addFullSector = new Action("Add full area sector", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				
				if (this.isChecked()) {
					IPlottingSystem<?> plot = getPlottingSystem();
					String name = RegionUtils.getUniqueName(getRegionName() +" Full Image", plot);
					try {
						IRegion region = plot.createRegion(name, RegionType.SECTOR);
						SectorROI sector = getFullSector();
						region.setROI(sector);
						region.setUserObject(fullImageSector);
						plot.addRegion(region);
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					IPlottingSystem<?> plot = getPlottingSystem();
					Collection<IRegion> regions = plot.getRegions();
					if (regions != null && !regions.isEmpty()) {
						Iterator<IRegion> iterator = regions.iterator();
						while (iterator.hasNext()) {
							IRegion next = iterator.next();
							if (fullImageSector == next.getUserObject()) plot.removeRegion(next);
						}
					}
				}
			}
		};
		
		addFullSector.setImageDescriptor(Activator.getImageDescriptor("icons/sector-full.png"));
		
		this.metaLock = createMetaLock(profileAxis);
		
		getSite().getActionBars().getToolBarManager().add(addFullSector);
		getSite().getActionBars().getToolBarManager().add(profileAxis);
		getSite().getActionBars().getToolBarManager().add(metaLock);
		getSite().getActionBars().getToolBarManager().add(new Separator());
		getSite().getActionBars().getMenuManager().add(addFullSector);
		getSite().getActionBars().getMenuManager().add(profileAxis);
		getSite().getActionBars().getMenuManager().add(metaLock);
		getSite().getActionBars().getToolBarManager().add(new Separator());
		
		profileAxis.setSelectedAction(pixelAxis);
		profileAxis.add(pixelAxis);
		profileAxis.add(resolutionAxis);
		profileAxis.add(angleAxis);
		profileAxis.add(qAxis);

		profileAxis.setEnabled(false);
		metaLock.setEnabled(isValidMetadata(getMetaData()));
		
		super.configurePlottingSystem(plotter);

	}
	
	public void activate () {
		super.activate();	
		//setup the lock action to work for valid metadata
		checkMetaLock(metaLock);
	}
	
	public void deactivate() {
		super.deactivate();
		unregisterMetadataListeners();
	}

	@Override
	protected Dataset[] getXAxis(final SectorROI sroi, Dataset[] integrals) {
		
		if (integrals[2] != null) {
			return new Dataset[]{integrals[2], integrals[3]};
		}
		
		final Dataset xi = DatasetFactory.createLinearSpace(sroi.getRadius(0), sroi.getRadius(1), integrals[0].getSize(), Dataset.FLOAT64);
		xi.setName("Radius (pixel)");
		
		IMetadata meta = getMetaData();
		
		if (!sroi.hasSeparateRegions())  {
			
			if (isValidMetadata(meta)) {
				metaLock.setEnabled(true);
				return new Dataset[]{pixelToValue(xi,(IDiffractionMetadata)meta)};
			}
			
			return new Dataset[]{xi};
			
		} else {

			final Dataset xii = DatasetFactory.createLinearSpace(sroi.getRadius(0), sroi.getRadius(1), integrals[1].getSize(), Dataset.FLOAT64);
			xii.setName("Radius (pixel)");
			
			if (isValidMetadata(meta)) {
				metaLock.setEnabled(true);
				return new Dataset[]{pixelToValue(xi,(IDiffractionMetadata)meta),pixelToValue(xii,(IDiffractionMetadata)meta)};
			}

			return new Dataset[]{xi, xii};
		}
		
	}
	
	
	private Dataset pixelToValue(Dataset dataset, IDiffractionMetadata metadata) {
		if (!isValidMetadata(metadata)) return dataset;

		try {
			DetectorProperties detprops = metadata.getDetector2DProperties();
	    	DiffractionCrystalEnvironment diffexp = metadata.getDiffractionCrystalEnvironment();
	    	
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
		} catch (NullPointerException npe) {
    		return dataset;
		}
	}

	@Override
	protected Dataset[] getIntegral(IDataset data,
			                              IDataset mask, 
			                              SectorROI       sroi, 
			                              IRegion         region,
			                              boolean         isDrag,
			                              int             downsample) {


		IDiffractionMetadata dm = null;
		IMetadata meta = getMetaData();
		QSpace qSpace = null;

		try {
			if (isValidMetadata(meta)) {
				dm = (IDiffractionMetadata)meta;
				DetectorProperties detprops = dm.getDetector2DProperties().clone();
				DiffractionCrystalEnvironment diffexp = dm.getDiffractionCrystalEnvironment().clone();
				// Update metadata values for downsampled datasets
				if (downsample != 1) {
					double[] beamCoords = detprops.getBeamCentreCoords();
					double hps = detprops.getHPxSize();
					double vps = detprops.getVPxSize();
					int px = detprops.getPx();
					int py = detprops.getPy();
					detprops.setHPxSize(hps * downsample);
					detprops.setVPxSize(vps * downsample);
					detprops.setPx(px / downsample);
					detprops.setPy(py / downsample);
					detprops.setBeamCentreCoords(new double[] {beamCoords[0] / downsample, beamCoords[1] / downsample});
				}
				qSpace = new QSpace(detprops, diffexp);
			}
		} catch (NullPointerException npe) {
			// continue as normal
		}

		Dataset[] profile = ROIProfile.sector(DatasetUtils.convertToDataset(data), DatasetUtils.convertToDataset(mask), sroi, true, false, false, qSpace, axis, false);
		
        if (profile == null) {
        	return null;
        }
        
		final Dataset integral = profile[0];
		final Dataset ax = profile[4];
		integral.setName("Radial Profile "+region.getName());
		
		// If not symmetry profile[2] is null, otherwise plot it.
	    if (profile.length>=3 && profile[2]!=null && sroi.hasSeparateRegions()) {
	    	
			final Dataset reflection = profile[2];
			final Dataset axref = profile[6];
			reflection.setName("Symmetry "+region.getName());

			return new Dataset[]{integral, reflection, ax, axref};
	    	
	    } else {
	    	return new Dataset[]{integral, null, ax, null};
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
			Dataset[] profile = ROIProfile.sector(DatasetUtils.convertToDataset(slice.getData()), DatasetUtils.convertToDataset(image.getMask()), sroi, true, false, false);
		
			Dataset integral = profile[0];
			integral.setName("radial_"+region.getName().replace(' ', '_'));     
			slice.appendData(integral);
			
		    if (profile.length>=3 && profile[2]!=null && sroi.hasSeparateRegions()) {
				final Dataset reflection = profile[2];
				reflection.setName("radial_sym_"+region.getName().replace(' ', '_'));     
				slice.appendData(reflection);
		    }
		}
		return new DataReductionInfo(Status.OK_STATUS);
	}
	
	private SectorROI getFullSector() {
		
		int[] shape = new int[2];

		if (getMetaData() instanceof IDiffractionMetadata) {
			shape[0] = ((IDiffractionMetadata)getMetaData()).getDetector2DProperties().getPx();
			shape[1] = ((IDiffractionMetadata)getMetaData()).getDetector2DProperties().getPy();
			
		} else {
			shape = getImageTrace().getData().getShape();
			int temp = shape[0];
			shape[0] = shape[1];
			shape[1] = temp;
		}
		
		double[] beamCenter = ToolUtils.getBeamCenter(getImageTrace(), getPart());
		double[] farCorner = new double[]{0,0};
		if (beamCenter[0] < shape[0]/2.0) farCorner[0] = shape[0];
		if (beamCenter[1] < shape[1]/2.0) farCorner[1] = shape[1];
		double maxDistance = Math.sqrt(Math.pow(beamCenter[0]-farCorner[0],2)+Math.pow(beamCenter[1]-farCorner[1],2));
		SectorROI sector = new SectorROI(beamCenter[0], beamCenter[1], 0, maxDistance, 0, 2*Math.PI);
		return sector;
	}
}

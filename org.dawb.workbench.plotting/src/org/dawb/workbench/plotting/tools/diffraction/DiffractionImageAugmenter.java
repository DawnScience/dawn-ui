package org.dawb.workbench.plotting.tools.diffraction;

import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.vecmath.Vector3d;

import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.dawb.workbench.plotting.Activator;
import org.dawb.workbench.plotting.system.swtxy.selection.AbstractSelectionRegion;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.diffraction.DSpacing;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorPropertyEvent;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironmentEvent;
import uk.ac.diamond.scisoft.analysis.diffraction.IDetectorPropertyListener;
import uk.ac.diamond.scisoft.analysis.diffraction.IDiffractionCrystalEnvironmentListener;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;
import uk.ac.diamond.scisoft.analysis.roi.EllipticalROI;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.ResolutionRing;
import uk.ac.diamond.scisoft.analysis.roi.ResolutionRingList;
import uk.ac.diamond.sda.meta.page.DiffractionMetadataCompositeEvent;
import uk.ac.diamond.sda.meta.page.IDiffractionMetadataCompositeListener;

/**
 * Class to augment a diffraction image with beam centre, rings, etc. It has actions available for adding to a menu
 */
public class DiffractionImageAugmenter implements IDetectorPropertyListener, IDiffractionCrystalEnvironmentListener, IDiffractionMetadataCompositeListener
{

	private static Logger logger = LoggerFactory.getLogger(DiffractionImageAugmenter.class);
	
	private AbstractPlottingSystem plottingSystem;
	private DetectorProperties detprop;
	private DiffractionCrystalEnvironment diffenv;
	private ResolutionRingList standardRingsList;
	private ArrayList<IRegion> standardRingsRegionList;
	private ResolutionRingList iceRingsList;
	private ArrayList<IRegion> iceRingsRegionList;
	private ResolutionRingList calibrantRingsList;
	private ArrayList<IRegion> calibrantRingsRegionList;
	private IRegion beamCentreRegion;
	private Action beamCentre;
	private Action standardRings;
	private Action iceRings;
	private Action calibrantRings;

	private double[] imageCentrePC;

	protected final static double[] iceResolution = new double[] { 3.897, 3.669, 3.441, 2.671, 2.249, 2.072, 1.948,
		1.918, 1.883, 1.721 };// angstrom

	/**
	 * Create and tie augmenter to a plotting system
	 * @param system
	 */
	public DiffractionImageAugmenter(AbstractPlottingSystem system) {
		plottingSystem = system;
	}

	/**
	 * Set image centre (used in fall-back if detector is not available)
	 * @param coords
	 */
	public void setImageCentre(double... coords) {
		imageCentrePC = coords;
	}
	
	public boolean isShowingBeamCenter() {
		return this.beamCentre.isChecked();
	}

	protected void drawBeamCentre(boolean isChecked) {
		beamCentre.setChecked(isChecked);
		if (beamCentreRegion != null)
			plottingSystem.removeRegion(beamCentreRegion);
			
		if (isChecked) { 
			if (detprop != null) {
				double[] beamCentrePC = detprop.getBeamLocation();
				double length = (1 + Math.sqrt(detprop.getPx() * detprop.getPx() + detprop.getPy() * detprop.getPy()) * 0.01);
				DecimalFormat df = new DecimalFormat("#.##");
				String label = df.format(beamCentrePC[0]) + "px, " + df.format(beamCentrePC[1])+"px";
				beamCentreRegion = drawCrosshairs(beamCentrePC, length, ColorConstants.red, ColorConstants.black, "beam centre", label);
			}
			else {
				DecimalFormat df = new DecimalFormat("#.##");
				String label = df.format(imageCentrePC[0]) + "px, " + df.format(imageCentrePC[1])+"px";
				beamCentreRegion = drawCrosshairs(imageCentrePC, imageCentrePC[1]/50, ColorConstants.red, ColorConstants.black, "beam centre", label);
			}
		}
	}

	protected void drawCalibrantRings(boolean isChecked) {
		if (calibrantRingsRegionList!=null && calibrantRingsList != null) {
			removeRings(calibrantRingsRegionList, calibrantRingsList);
		}
	
		if (isChecked) {
			calibrantRingsList = new ResolutionRingList();
	
			IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
//			@SuppressWarnings("unused")
//			String standardName;
//			if (preferenceStore.isDefault(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME))
//				standardName = preferenceStore.getDefaultString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME);
//			else
//				standardName = preferenceStore.getString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME);
	
			String standardDistances;
			if (preferenceStore.isDefault(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES))
				standardDistances = preferenceStore.getDefaultString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES);
			else
				standardDistances = preferenceStore.getString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES);
	
			String[] tokens = standardDistances.split(",");
			for (String t : tokens) {
				try {
					calibrantRingsList.add(new ResolutionRing(Double.valueOf(t), true, ColorConstants.red, true, false, false));
				} catch (NumberFormatException e) {
					logger.warn("Could not parse item {} in standard distances", t);
				}
			}
			calibrantRingsRegionList = drawResolutionRings(calibrantRingsList, "calibrant");
		}
	}

	protected IRegion drawCrosshairs(double[] beamCentre, double length, Color colour, Color labelColour, String nameStub, String labelText) {
		IRegion region;
		try {
			final String regionName = RegionUtils.getUniqueName(nameStub, plottingSystem);
			region = plottingSystem.createRegion(regionName, RegionType.LINE);
		} catch (Exception e) {
			logger.error("Can't create region", e);
			return null;
		}
	
		final LinearROI lroi = new LinearROI(length, 0);
		double dbc[] = {(double)beamCentre[0], (double)beamCentre[1]};
		lroi.setMidPoint(dbc);
		lroi.setCrossHair(true);
		region.setROI(lroi);
		region.setRegionColor(colour);
		region.setAlpha(100);
		region.setUserRegion(false);
		region.setShowPosition(false);
		
		region.setLabel(labelText);
		((AbstractSelectionRegion)region).setShowLabel(true);
		
		plottingSystem.addRegion(region);
		region.setMobile(false); // NOTE: Must be done **AFTER** calling the addRegion method.
	
		return region;
	}

	/**
	 * Add actions to given menu
	 * @param menu
	 */
	public void addActions(MenuAction menu) {
	    standardRings = new Action("Standard rings", Activator.getImageDescriptor("/icons/standard_rings.png")) {
	    	@Override
	    	public void run() {
	    		drawStandardRings(isChecked());
	    	}
		};
		standardRings.setChecked(false);
		menu.add(standardRings);
		iceRings = new Action("Ice rings", Activator.getImageDescriptor("/icons/ice_rings.png")) {
			@Override
			public void run() {
				drawIceRings(isChecked());
			}
		};
		iceRings.setChecked(false);
		menu.add(iceRings);
		calibrantRings = new Action("Calibrant", Activator.getImageDescriptor("/icons/calibrant_rings.png")) {
			@Override
			public void run() {
				drawCalibrantRings(isChecked());
			}
		};
		calibrantRings.setChecked(false);
		menu.add(calibrantRings);
		beamCentre = new Action("Beam centre", Activator.getImageDescriptor("/icons/beam_centre.png")) {
			@Override
			public void run() {
				drawBeamCentre(isChecked());
			}
		};
		beamCentre.setChecked(false);
		menu.add(beamCentre);
	}

	protected void removeRings(ArrayList<IRegion> regionList, ResolutionRingList resolutionRingList) {
		for (IRegion region : regionList) {
			plottingSystem.removeRegion(region);
		}
		regionList.clear();
		resolutionRingList.clear();
	}
	
	/*
		 * handle ring drawing, removal and clearing
		 */
	//	protected IRegion drawRing(double[] beamCentre, double innerRadius, double outerRadius, Color colour, Color labelColour, String nameStub, String labelText) {
	//		IRegion region;
	//		try {
	//			final String regionName = RegionUtils.getUniqueName(nameStub, plottingSystem);
	//			region = plottingSystem.createRegion(regionName, RegionType.RING);
	//		} catch (Exception e) {
	//			logger.error("Can't create region", e);
	//			return null;
	//		}
	//	    final SectorROI sroi = new SectorROI(innerRadius, outerRadius);
	//	    sroi.setPoint(beamCentre[0], beamCentre[1]);
	//		region.setROI(sroi);
	//		region.setRegionColor(colour);
	//		region.setAlpha(100);
	//		region.setUserRegion(false);
	//		region.setMobile(false);
	//		
	//		region.setLabel(labelText);
	//		((AbstractSelectionRegion)region).setShowLabel(true);
	//		((AbstractSelectionRegion)region).setForegroundColor(labelColour);
	//		
	//		region.setShowPosition(false);
	//		plottingSystem.addRegion(region);
	//		
	//		return region;
	//	}

	/*
	 * handle ring drawing, removal and clearing
	 */
	protected IRegion drawEllipse(double[] beamCentre, EllipticalROI eroi, Color colour, Color labelColour,
			String nameStub, String labelText) {
		IRegion region;
		try {
			final String regionName = RegionUtils.getUniqueName(nameStub, plottingSystem);
			region = plottingSystem.createRegion(regionName, RegionType.ELLIPSE);
		} catch (Exception e) {
			logger.error("Can't create region", e);
			return null;
		}
		region.setROI(eroi);
		region.setRegionColor(colour);
		region.setAlpha(100);
		region.setUserRegion(false);
		region.setMobile(false);

		region.setLabel(labelText);
		((AbstractSelectionRegion) region).setShowLabel(true);
		((AbstractSelectionRegion) region).setForegroundColor(labelColour);

		region.setShowPosition(false);
		plottingSystem.addRegion(region);

		return region;
	}

	protected void drawIceRings(boolean isChecked) {
		if (iceRingsRegionList!=null && iceRingsList!=null)
			removeRings(iceRingsRegionList, iceRingsList);
		
		if (isChecked) {
			iceRingsList = new ResolutionRingList();
			for (double res : iceResolution) {
				iceRingsList.add(new ResolutionRing(res, true, ColorConstants.blue, true, false, false));
			}
			iceRingsRegionList = drawResolutionRings(iceRingsList, "ice");
		}
	}

	//	protected IRegion drawResolutionRing(ResolutionRing ring, String name) {
	//		if (detprop != null && diffenv != null) {
	//			double[] beamCentre = detprop.getBeamLocation(); // detConfig.pixelCoords(detConfig.getBeamPosition());
	//			double radius = Resolution.circularResolutionRingRadius(detprop, diffenv, ring.getResolution());
	//			DecimalFormat df = new DecimalFormat("#.00");
	//			return drawRing(beamCentre, radius, radius+4.0, ring.getColour(), ring.getColour(), name, df.format(ring.getResolution())+"Å");
	//		}
	//		else
	//			return null;
	//	}
		
	protected IRegion drawResolutionEllipse(ResolutionRing ring, String name) {
		if (detprop != null && diffenv != null) {
			double[] beamCentre = detprop.getBeamLocation(); // detConfig.pixelCoords(detConfig.getBeamPosition());
			EllipticalROI ellipse = DSpacing.ellipseFromDSpacing(detprop, diffenv, ring.getResolution());
			DecimalFormat df = new DecimalFormat("#.00");
			return drawEllipse(beamCentre, ellipse, ring.getColour(), ring.getColour(), name,
					df.format(ring.getResolution()) + "Å");
		} else
			return null;
	}

	protected ArrayList<IRegion> drawResolutionRings(ResolutionRingList ringList, String typeName) {
			ArrayList<IRegion> regions = new ArrayList<IRegion>(); 
			for (int i = 0; i < ringList.size(); i++) {
	//			regions.add(drawResolutionRing(ringList.get(i), typeName+i));
				regions.add(drawResolutionEllipse(ringList.get(i), typeName+i));
			}
			return regions;
		}

	protected void drawStandardRings(boolean isChecked) {
		if (standardRingsRegionList != null && standardRingsList != null)
			removeRings(standardRingsRegionList, standardRingsList); 
	
		if (isChecked && diffenv!= null && detprop != null) {
			standardRingsList = new ResolutionRingList();
			Double numberEvenSpacedRings = 6.0;
			double lambda = diffenv.getWavelength();
			Vector3d longestVector = detprop.getLongestVector();
			double step = longestVector.length() / numberEvenSpacedRings; 
			double d, twoThetaSpacing;
			Vector3d toDetectorVector = new Vector3d();
			Vector3d beamVector = detprop.getBeamPosition();
			for (int i = 0; i < numberEvenSpacedRings - 1; i++) {
				// increase the length of the vector by step.
				longestVector.normalize();
				longestVector.scale(step + (step * i));
	
				toDetectorVector.add(beamVector, longestVector);
				twoThetaSpacing = beamVector.angle(toDetectorVector);
				d = lambda / Math.sin(twoThetaSpacing);
				standardRingsList.add(new ResolutionRing(d, true, ColorConstants.yellow, false, true, true));
			}
			standardRingsRegionList = drawResolutionRings(standardRingsList, "standard");
		}
	}

	public void dispose() {
		diffenv.removeDiffractionCrystalEnvironmentListener(this);
		detprop.removeDetectorPropertyListener(this);
	}

	@Override
	public void detectorPropertiesChanged(DetectorPropertyEvent evt) {
		beamCentre.run();
		standardRings.run();
		iceRings.run();
		calibrantRings.run();
	}

	@Override
	public void diffractionCrystalEnvironmentChanged(DiffractionCrystalEnvironmentEvent evt) {
		standardRings.run();
		iceRings.run();
		calibrantRings.run();
	}

	@Override
	public void diffractionMetadataCompositeChanged(DiffractionMetadataCompositeEvent evt) {
		if (evt.hasBeamCentreChanged()) {
			if (beamCentre.isChecked()) {
				beamCentre.setChecked(false);
				plottingSystem.removeRegion(beamCentreRegion);
			} else {
				beamCentre.setChecked(true);
				drawBeamCentre(true);
			}
		}
	}

	public void setDiffractionMetadata(IDiffractionMetadata metadata) {
		diffenv = metadata.getDiffractionCrystalEnvironment();
		diffenv.addDiffractionCrystalEnvironmentListener(this);
		detprop = metadata.getDetector2DProperties();
		detprop.addDetectorPropertyListener(this);
	}
}

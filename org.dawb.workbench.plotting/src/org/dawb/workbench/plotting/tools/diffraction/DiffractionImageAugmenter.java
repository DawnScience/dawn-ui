package org.dawb.workbench.plotting.tools.diffraction;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.measure.unit.NonSI;
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
import org.eclipse.swt.graphics.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSelectedListener;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSelectionEvent;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSpacing;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationFactory;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationStandards;
import uk.ac.diamond.scisoft.analysis.crystallography.HKL;
import uk.ac.diamond.scisoft.analysis.diffraction.DSpacing;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorPropertyEvent;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironmentEvent;
import uk.ac.diamond.scisoft.analysis.diffraction.IDetectorPropertyListener;
import uk.ac.diamond.scisoft.analysis.diffraction.IDiffractionCrystalEnvironmentListener;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.roi.EllipticalROI;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.ResolutionRing;
import uk.ac.diamond.sda.meta.page.DiffractionMetadataCompositeEvent;
import uk.ac.diamond.sda.meta.page.IDiffractionMetadataCompositeListener;

/**
 * Class to augment a diffraction image with beam centre, rings, etc. It has actions available for adding to a menu
 */
public class DiffractionImageAugmenter implements IDetectorPropertyListener, IDiffractionCrystalEnvironmentListener, IDiffractionMetadataCompositeListener, CalibrantSelectedListener
{

	private static DiffractionImageAugmenter activeAugmenter;
	/**
	 * Actions should be static so that opening multiple files in an editor,
	 * plots the same ring configuration without having to manually choose.
	 */
	private static Action beamCentre;
	private static Action standardRings;
	private static Action iceRings;
	private static Action calibrantRings;
	
	static {
        standardRings = new Action("Standard rings", Activator.getImageDescriptor("/icons/standard_rings.png")) {
	    	@Override
	    	public void run() {
	    		if (activeAugmenter==null) return;
	    		activeAugmenter.drawStandardRings(isChecked());
	    	}
		};
		standardRings.setChecked(false);
  
		iceRings = new Action("Ice rings", Activator.getImageDescriptor("/icons/ice_rings.png")) {
			@Override
			public void run() {
	    		if (activeAugmenter==null) return;
				activeAugmenter.drawIceRings(isChecked());
			}
		};
		iceRings.setChecked(false);
	
		calibrantRings = new Action("Calibrant", Activator.getImageDescriptor("/icons/calibrant_rings.png")) {
			@Override
			public void run() {
	    		if (activeAugmenter==null) return;
				activeAugmenter.drawCalibrantRings(isChecked(), CalibrationFactory.getCalibrationStandards().getCalibrant());
			}
		};
		calibrantRings.setChecked(false);

		beamCentre = new Action("Beam centre", Activator.getImageDescriptor("/icons/beam_centre.png")) {
			@Override
			public void run() {
	    		if (activeAugmenter==null) return;
				activeAugmenter.drawBeamCentre(isChecked());
			}
		};
		beamCentre.setChecked(false);
	}	
	
	private static Logger logger = LoggerFactory.getLogger(DiffractionImageAugmenter.class);
	
	private AbstractPlottingSystem plottingSystem;
	private DetectorProperties detprop;
	private DiffractionCrystalEnvironment diffenv;
	private IRegion beamCentreRegion;
	
    private enum RING_TYPE {
    	ICE, STANDARD, CALIBRANT;
    }

	private double[] imageCentrePC;

	protected final static double[] iceResolution = new double[] { 3.897, 3.669, 3.441, 2.671, 2.249, 2.072, 1.948,
		1.918, 1.883, 1.721 };// angstrom

	/**
	 * Create and tie augmenter to a plotting system
	 * @param system
	 */
	public DiffractionImageAugmenter(AbstractPlottingSystem system) {
		plottingSystem = system;
		CalibrationFactory.addCalibrantSelectionListener(this);
		if (activeAugmenter==null) activeAugmenter = this;
	}
	
	private boolean active = true;
	public void activate() {
		activeAugmenter = this;
		active = true;
		updateAll();
	}
	
	public void deactivate() {
		if (activeAugmenter == this) activeAugmenter=null;
		active = false;
	}

	/**
	 * Set image centre (used in fall-back if detector is not available)
	 * @param coords
	 */
	public void setImageCentre(double... coords) {
		imageCentrePC = coords;
	}
	
	public boolean isShowingBeamCenter() {
		return beamCentre.isChecked();
	}

	protected void drawBeamCentre(boolean isChecked) {
		if (!active) return; // We are likely off screen.
		beamCentre.setChecked(isChecked);
		if (beamCentreRegion != null)
			plottingSystem.removeRegion(beamCentreRegion);
			
		if (isChecked) { 
			if (detprop != null) {
				double[] beamCentrePC = detprop.getBeamCentreCoords();
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

	@Override
	public void calibrantSelectionChanged(CalibrantSelectionEvent evt) {
		CalibrationStandards standards = (CalibrationStandards)evt.getSource();
		/**
		 * Important take CalibrantSpacing from event because it might be a standards the user is
		 * editing in the preference editor.
		 */
		if (calibrantRings!=null) drawCalibrantRings(calibrantRings.isChecked(), standards.getCalibrant());
	}
	
	protected void drawCalibrantRings(boolean isChecked, CalibrantSpacing spacing) {
		if (!active) return; // We are likely off screen.
		removeRings(RING_TYPE.CALIBRANT);
	
		if (isChecked) {
			List<ResolutionRing> calibrantRingsList = new ArrayList<ResolutionRing>(7);
	
			for (HKL hkl : spacing.getHKLs()) {
				final double d = Double.valueOf(hkl.getD().doubleValue(NonSI.ANGSTROM));
				try {
					calibrantRingsList.add(new ResolutionRing(d, true, ColorConstants.red, true, false, false));
				} catch (NumberFormatException e) {
					logger.warn("Could not parse item {} in standard distances", d);
				}
			}
			drawResolutionRings(calibrantRingsList, "calibrant", RING_TYPE.CALIBRANT);
		}
	}

	protected IRegion drawCrosshairs(double[] beamCentre, double length, Color colour, Color labelColour, String nameStub, String labelText) {
		if (!active) return null; // We are likely off screen.
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
	public void addActions(final MenuAction menu) {
		
		menu.add(standardRings);
		menu.add(iceRings);
		menu.add(calibrantRings);
		menu.add(beamCentre);
	}

	protected void removeRings(Object marker) {
		if (plottingSystem==null) return;
		if (plottingSystem.getRegions()==null) return;
		for (IRegion region : plottingSystem.getRegions()) {
			try {
				if (region.getUserObject()!=marker) continue;
			    plottingSystem.removeRegion(region);
			} catch (Throwable ne) {
				// They can delete regions themselves.
			}
		}
	}

	/*
	 * handle ring drawing, removal and clearing
	 */
	protected void drawEllipse(double[] beamCentre, 
			                      EllipticalROI eroi, 
			                      Color colour,
			                      Color labelColour,
			                      String nameStub,
			                      String labelText,
			                      Object marker) {

		if (!active) return; // We are likely off screen.
        IRegion region;
		try {
			final String regionName = RegionUtils.getUniqueName(nameStub, plottingSystem);
			region = plottingSystem.createRegion(regionName, RegionType.ELLIPSE);
		} catch (Exception e) {
			logger.error("Can't create region", e);
			return;
		}
		region.setROI(eroi);
		region.setRegionColor(colour);
		region.setAlpha(100);
		region.setUserRegion(false);

		region.setLabel(labelText);
		((AbstractSelectionRegion) region).setShowLabel(true);
		((AbstractSelectionRegion) region).setForegroundColor(labelColour);

		region.setShowPosition(false);
		plottingSystem.addRegion(region);
		region.setMobile(false);
		region.setUserObject(marker);

	}

	protected void drawIceRings(boolean isChecked) {
		if (!active) return; // We are likely off screen.
	    
		removeRings(RING_TYPE.ICE);
		
		if (isChecked) {
			List<ResolutionRing> iceRingsList = new ArrayList<ResolutionRing>(7);
			for (double res : iceResolution) {
				iceRingsList.add(new ResolutionRing(res, true, ColorConstants.blue, true, false, false));
			}
			drawResolutionRings(iceRingsList, "ice", RING_TYPE.ICE);
		}
	}
		
	protected void drawResolutionEllipse(ResolutionRing ring, String name, Object marker) {
		if (!active) return; // We are likely off screen.
		if (detprop != null && diffenv != null) {
			double[] beamCentre = detprop.getBeamCentreCoords(); // detConfig.pixelCoords(detConfig.getBeamPosition());
			EllipticalROI ellipse = DSpacing.ellipseFromDSpacing(detprop, diffenv, ring.getResolution());
			DecimalFormat df = new DecimalFormat("#.00");
			drawEllipse(beamCentre, ellipse, ring.getColour(), ring.getColour(), name,
					df.format(ring.getResolution()) + "Å", marker);
		}
	}

	protected void drawResolutionRings(List<ResolutionRing> ringList, String typeName, Object marker) {
		for (int i = 0; i < ringList.size(); i++) {
			drawResolutionEllipse(ringList.get(i), typeName+i, marker);
		}
	}

	protected void drawStandardRings(boolean isChecked) {
		if (!active) return; // We are likely off screen.
		
		removeRings(RING_TYPE.STANDARD); 
	
		if (isChecked && diffenv!= null && detprop != null) {
			List<ResolutionRing> standardRingsList = new ArrayList<ResolutionRing>(7);
			Double numberEvenSpacedRings = 6.0;
			double lambda = diffenv.getWavelength();
			Vector3d longestVector = detprop.getLongestVector();
			double step = longestVector.length() / numberEvenSpacedRings; 
			double d, twoThetaSpacing;
			Vector3d toDetectorVector = new Vector3d();
			Vector3d beamVector = detprop.getBeamCentrePosition();
			for (int i = 0; i < numberEvenSpacedRings - 1; i++) {
				// increase the length of the vector by step.
				longestVector.normalize();
				longestVector.scale(step + (step * i));
	
				toDetectorVector.add(beamVector, longestVector);
				twoThetaSpacing = beamVector.angle(toDetectorVector);
				d = lambda / Math.sin(twoThetaSpacing);
				standardRingsList.add(new ResolutionRing(d, true, ColorConstants.yellow, false, true, true));
			}
			drawResolutionRings(standardRingsList, "standard", RING_TYPE.STANDARD);
		}
	}

	public void dispose() {
		diffenv.removeDiffractionCrystalEnvironmentListener(this);
		detprop.removeDetectorPropertyListener(this);
		CalibrationFactory.removeCalibrantSelectionListener(this);
	}

	@Override
	public void detectorPropertiesChanged(DetectorPropertyEvent evt) {
		updateAll();
	}

	private void updateAll() {
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
		if (diffenv!=null) {
			diffenv.addDiffractionCrystalEnvironmentListener(this);
		} else {
			logger.error("DiffractionCrystalEnvironment is null!");
			Thread.dumpStack();
		}
		detprop = metadata.getDetector2DProperties();
		if (detprop!=null) {
		    detprop.addDetectorPropertyListener(this);
		} else {
			logger.error("DetectorProperties is null!");
			Thread.dumpStack();
		}
	}

}

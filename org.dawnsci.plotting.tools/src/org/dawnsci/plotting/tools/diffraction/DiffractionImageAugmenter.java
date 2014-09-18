package org.dawnsci.plotting.tools.diffraction;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.unit.NonSI;
import javax.vecmath.Vector3d;

import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.plot.roi.ResolutionRing;
import org.dawnsci.plotting.tools.Activator;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.ILockableRegion;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.region.RegionUtils;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
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
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorPropertyEvent.EventType;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironmentEvent;
import uk.ac.diamond.scisoft.analysis.diffraction.IDetectorPropertyListener;
import uk.ac.diamond.scisoft.analysis.diffraction.IDiffractionCrystalEnvironmentListener;
import uk.ac.diamond.scisoft.analysis.io.ILoaderService;
import uk.ac.diamond.scisoft.analysis.metadata.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.roi.EllipticalROI;
import uk.ac.diamond.scisoft.analysis.roi.HyperbolicROI;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.ParabolicROI;
import uk.ac.diamond.scisoft.analysis.roi.PointROI;
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
	
	private IPlottingSystem plottingSystem;
	private DetectorProperties detprop;
	private DiffractionCrystalEnvironment diffenv;
	private IRegion crosshairs;
	private IRegion beamPosition;
	private IROIListener roilistener;

    private enum RING_TYPE {
    	ICE, STANDARD, CALIBRANT, BEAM_CENTRE, BEAM_POSITION_HANDLE;
    }

	private double[] imageCentrePC;

	protected final static double[] iceResolution = new double[] { 3.897, 3.669, 3.441, 2.671, 2.249, 2.072, 1.948,
		1.918, 1.883, 1.721 };// angstrom

	/**
	 * Create and tie augmenter to a plotting system
	 * @param system
	 */
	public DiffractionImageAugmenter(IPlottingSystem system) {
		plottingSystem = system;
		if (activeAugmenter==null) activeAugmenter = this;
		resROIs = new ArrayList<IROI>();
		
		roilistener = new IROIListener.Stub() {
			
			@Override
			public void roiDragged(ROIEvent evt) {
				updateBeamCentre(evt, false); 
			}
			
			@Override
			public void roiChanged(ROIEvent evt) {
				updateBeamCentre(evt, true);
			}
			
			private void updateBeamCentre(ROIEvent evt, boolean force) {
				if (evt.getROI() != null && evt.getROI() instanceof PointROI) {
					PointROI roi = (PointROI)evt.getROI();
					if (evt.getSource() != null && evt.getSource() instanceof IRegion) {
						IRegion region = (IRegion)evt.getSource();
						if (region.getUserObject() == RING_TYPE.BEAM_POSITION_HANDLE) {
							forceRedraw = force;
							if (detprop != null)  detprop.setBeamCentreCoords(roi.getPoint());
						}
					}
				}
			}
		};
	}
	
	private boolean active = true;
	private IDiffractionMetadata dmd;
	private List<IROI> resROIs;
	private boolean centreMoved = false;
	private boolean forceRedraw = false;

	/**
	 * @return list of ROIs representing resolution rings
	 */
	public List<IROI> getResolutionROIs() {
		return resROIs;
	}

	public void activate() {
		activeAugmenter = this;
		active = true;
		updateAll();
		//registerListeners(true);
	}
	
	public void deactivate(boolean leaveRegions) {
		if (activeAugmenter == this) activeAugmenter=null;
		active = false;
		
		if (!leaveRegions) {
			if (crosshairs != null && plottingSystem != null) {
				plottingSystem.removeRegion(crosshairs);
				crosshairs = null;
			}
			
			for (RING_TYPE rt : RING_TYPE.values()) removeConics(rt);
		}
		registerListeners(false);
		if (beamPosition != null) beamPosition.removeROIListener(roilistener);
		beamPosition = null;
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

	public void drawBeamCentre(boolean isChecked) {
		if (!active) return; // We are likely off screen.
		beamCentre.setChecked(isChecked);
			
		if (isChecked) { 
			DecimalFormat df = new DecimalFormat("#.##");
			if (detprop != null) {
				double[] beamCentrePC = detprop.getBeamCentreCoords();
				if (beamCentrePC[0] == 0) // ensure there are no negative zeros
					beamCentrePC[0] = 0;
				if (beamCentrePC[1] == 0)
					beamCentrePC[1] = 0;
				double length = (1 + Math.sqrt(detprop.getPx() * detprop.getPx() + detprop.getPy() * detprop.getPy()) * 0.01);
				String label = df.format(beamCentrePC[0]) + "px, " + df.format(beamCentrePC[1])+"px";
				drawCrosshairs(beamCentrePC, length, ColorConstants.red, ColorConstants.black, "beam centre", label);
			} else if (imageCentrePC!=null) {
				String label = df.format(imageCentrePC[0]) + "px, " + df.format(imageCentrePC[1])+"px";
				drawCrosshairs(imageCentrePC, imageCentrePC[1]/50, ColorConstants.red, ColorConstants.black, "beam centre", label);
			}
		} else if (crosshairs != null) {
			plottingSystem.removeRegion(crosshairs);
			crosshairs = null;
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
	
	public void drawCalibrantRings(boolean isChecked, CalibrantSpacing spacing) {
		if (!active) return; // We are likely off screen.
	
		if (isChecked) {
			if (!calibrantRings.isChecked()) // override setting
				calibrantRings.setChecked(true);
			List<ResolutionRing> calibrantRingsList = new ArrayList<ResolutionRing>(7);
	
			for (HKL hkl : spacing.getHKLs()) {
				final double d = Double.valueOf(hkl.getD().doubleValue(NonSI.ANGSTROM));
				try {
					calibrantRingsList.add(new ResolutionRing(d, true, ColorConstants.red, true, false, false));
				} catch (NumberFormatException e) {
					logger.warn("Could not parse item {} in standard distances", d);
				}
			}
			drawResolutionConics(calibrantRingsList, "calibrant", RING_TYPE.CALIBRANT);
			drawResolutionBeamPosition();
		} else {
			hideConics(RING_TYPE.CALIBRANT);
			hideConics(RING_TYPE.BEAM_POSITION_HANDLE);
			beamPosition = null;
		}
	}

	private void drawCrosshairs(double[] beamCentre, double length, Color colour, Color labelColour, String nameStub, String labelText) {
		if (!active) return; // We are likely off screen.

		if (crosshairs == null) {
			try {
				final String regionName = RegionUtils.getUniqueName(nameStub, plottingSystem);
				crosshairs = plottingSystem.createRegion(regionName, RegionType.LINE);
				crosshairs.setUserRegion(false);
			} catch (Exception e) {
				logger.error("Can't create region", e);
				return;
			}

			final LinearROI lroi = new LinearROI(length, 0);
			lroi.setMidPoint(beamCentre);
			lroi.setCrossHair(true);
			crosshairs.setROI(lroi);
			crosshairs.setRegionColor(colour);
			crosshairs.setAlpha(100);
			crosshairs.setShowPosition(false);
			crosshairs.setUserObject(RING_TYPE.BEAM_CENTRE);

			crosshairs.setLabel(labelText);
			crosshairs.setShowLabel(true);

			plottingSystem.addRegion(crosshairs);
			crosshairs.setMobile(false); // NOTE: Must be done **AFTER** calling the
										// addRegion method.
			crosshairs.toBack();
		} else {
			LinearROI lroi = (LinearROI) crosshairs.getROI();
			lroi.setLength(length);
			lroi.setMidPoint(beamCentre);
			crosshairs.setRegionColor(colour);
			crosshairs.setLabel(labelText);
			crosshairs.toBack();
//			crosshairs.setROI(lroi);
		}
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
	

	public void addBeamCenterAction(IContributionManager man) {
		man.add(beamCentre);
	}

	private void hideConics(RING_TYPE marker) {
		if (plottingSystem==null) return;
		if (plottingSystem.getRegions()==null) return;
		for (IRegion region : plottingSystem.getRegions()) {
			try {
				if (region.getUserObject()!=marker) continue;
				region.setVisible(false);
			} catch (Throwable ne) {
				// They can delete regions themselves.
			}
		}
	}

	private void removeConics(RING_TYPE marker) {
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

	private void drawResolutionConics(List<ResolutionRing> conicList, String typeName, final RING_TYPE marker) {
		
		if (!centreMoved || forceRedraw) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					removeConics(marker);
				}
			});
		}
		resROIs.clear();
		if (!active) // We are likely to be off-screen
			return;
		if (detprop == null || diffenv == null)
			return;

		int nRings = conicList.size();
		double[] alphas = new double[nRings];
		for (int i = 0; i < nRings; i++) {
			try {
				alphas[i] = DSpacing.coneAngleFromDSpacing(diffenv, conicList.get(i).getResolution());
			} catch (Exception e) {
				alphas[i] = Double.NaN;
			}
		}
		IROI[] rois = DSpacing.conicsFromAngles(detprop, alphas);
		if (rois == null)
			return;
		
		if (centreMoved && !forceRedraw) {
			List<IRegion> regions = new ArrayList<IRegion>();
			if (plottingSystem==null) return;
			if (plottingSystem.getRegions()==null) return;
			for (IRegion region : plottingSystem.getRegions()) {
				try {
					if (region.getUserObject()!=marker) continue;
				    regions.add(region);
				} catch (Throwable ne) {
				}
			}
			
			if (rois.length < regions.size()) return;
			
			for (int i = 0; i < regions.size(); i++) {
				IROI conic = rois[i];
				resROIs.add(conic);
				if (conic != null)
					updateResolutionConic(conic, regions.get(i));
			}
			
		} else {
			for (int i = 0; i < nRings; i++) {
				IROI conic = rois[i];
				resROIs.add(conic);
				if (conic != null)
					drawResolutionConic(conicList.get(i), conic, typeName+i, marker, false);
			}
		}
	}
	
	private void updateResolutionConic(IROI roi, IRegion region) {
		if (region.getROI().getClass() != roi.getClass()) {
			region.setVisible(false);
			return;
		}
		region.setROI(roi);
	}

	private void drawResolutionConic(ResolutionRing ring, IROI roi, String name, RING_TYPE marker, 
											boolean isMobile) {
		RegionType type = getConicRegionType(roi);
		if (type == null)
			return;
		final String regionName = RegionUtils.getUniqueName(name, plottingSystem);
		IRegion region;
		try {
			region = plottingSystem.createRegion(regionName, type);
		} catch (Exception e) {
			logger.error("Could not create region", e);
			return;
		}

		Color colour = ring.getColour();
		plottingSystem.addRegion(region);
		region.setROI(roi);
		region.setRegionColor(colour);
		region.setAlpha(100);
		region.setUserRegion(true);

		DecimalFormat df = new DecimalFormat("#.00");
		region.setLabel(df.format(ring.getResolution()) + "Å");
		region.setShowLabel(true);
		if (crosshairs != null) {
			crosshairs.setShowLabel(true);
			crosshairs.setRegionColor(colour);
		}

		region.setShowPosition(false);
		region.setUserRegion(false);
		region.setVisible(true);
		region.setMobile(isMobile);
		region.setUserObject(marker);
		region.toBack();
		if (isMobile) {
			ILockableRegion lockable = region instanceof ILockableRegion ? (ILockableRegion) region : null;
			if (lockable == null)
				return;
			lockable.setCentreMovable(true);
			lockable.setOuterMovable(false);
		}
	}

	private static RegionType getConicRegionType(IROI roi) {
		RegionType type = null;
		if (roi instanceof EllipticalROI) {
			type = RegionType.ELLIPSE;
		} else if (roi instanceof ParabolicROI) {
			type = RegionType.PARABOLA;
		} else if (roi instanceof HyperbolicROI) {
			type = RegionType.HYPERBOLA;
		}
		return type;
	}

	private void drawIceRings(boolean isChecked) {
		if (!active) return; // We are likely off screen.
	    
		if (isChecked) {
			List<ResolutionRing> iceRingsList = new ArrayList<ResolutionRing>(7);
			for (double res : iceResolution) {
				iceRingsList.add(new ResolutionRing(res, true, ColorConstants.blue, true, false, false));
			}
			drawResolutionConics(iceRingsList, "ice", RING_TYPE.ICE);
		} else {
			hideConics(RING_TYPE.ICE);
		}
	}
		
	private void drawStandardRings(boolean isChecked) {
		if (!active) return; // We are likely off screen.
			
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
			drawResolutionConics(standardRingsList, "standard", RING_TYPE.STANDARD);
		} else {
			hideConics(RING_TYPE.STANDARD);
		}
	}

	private void drawResolutionBeamPosition() {
		if (!active) return; // We are likely off screen.
		
		if (detprop == null) return;

		double[] beamCentrePC = detprop.getBeamCentreCoords();
		if (beamCentrePC[0] == 0) // ensure there are no negative zeros
			beamCentrePC[0] = 0;
		if (beamCentrePC[1] == 0)
			beamCentrePC[1] = 0;

		if (beamPosition == null) {
			try {
				final String regionName = RegionUtils.getUniqueName("Calibrant beam position", plottingSystem);
				beamPosition = plottingSystem.createRegion(regionName, RegionType.POINT);
			} catch (Exception e) {
				logger.error("Can't create region", e);
				return;
			}

			final PointROI proi = new PointROI(beamCentrePC);
			beamPosition.setROI(proi);
			beamPosition.setRegionColor(ColorConstants.red);
			beamPosition.setAlpha(100);
			beamPosition.setUserRegion(false);
			beamPosition.setShowPosition(false);
			beamPosition.setUserObject(RING_TYPE.BEAM_POSITION_HANDLE);
			
			beamPosition.addROIListener(roilistener);

			plottingSystem.addRegion(beamPosition);
			beamPosition.setMobile(true); // NOTE: Must be done **AFTER** calling the
										// addRegion method.
		} else {
			PointROI proi = (PointROI) beamPosition.getROI();
			proi.setPoint(beamCentrePC);
			beamPosition.setRegionColor(ColorConstants.red);

		}
	}
	
	public void dispose() {
		ILoaderService service = (ILoaderService)PlatformUI.getWorkbench().getService(ILoaderService.class);
		deactivate(service.getLockedDiffractionMetaData()!=null);
	}

	@Override
	public void detectorPropertiesChanged(DetectorPropertyEvent evt) {
		
		if (evt.getType()  == EventType.BEAM_CENTRE) {
			centreMoved = true;
		}
		updateAll();
		
	}

	private void updateAll() {
		beamCentre.run();
		standardRings.run();
		iceRings.run();
		calibrantRings.run();
		
		if ((!centreMoved || forceRedraw) && beamPosition!= null) beamPosition.toFront();
		
		centreMoved = false;
		forceRedraw = false;
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
				if (crosshairs != null) {
					plottingSystem.removeRegion(crosshairs);
					crosshairs = null;
				}
			} else {
				beamCentre.setChecked(true);
				drawBeamCentre(true);
			}
		}
	}

	public void setDiffractionMetadata(IDiffractionMetadata metadata) {
		if (diffenv != null && detprop != null) registerListeners(false);
		dmd = metadata;
		diffenv = metadata.getDiffractionCrystalEnvironment();
		detprop = metadata.getDetector2DProperties();
		registerListeners(true);
		imageCentrePC = detprop!=null ? detprop.getBeamCentreCoords() : null;
		updateAll();
	}

	public IDiffractionMetadata getDiffractionMetadata() {
		return dmd;
	}
	
	public boolean isActive() {
		return active;
	}

	private void registerListeners(boolean register) {
		
		if (register) {
			CalibrationFactory.addCalibrantSelectionListener(this);
		} else {
			CalibrationFactory.removeCalibrantSelectionListener(this);
		}
		
		if (diffenv!=null) {
			if (register) {
			    diffenv.addDiffractionCrystalEnvironmentListener(this);
			} else {
				diffenv.removeDiffractionCrystalEnvironmentListener(this);
			}
		} else {
			logger.error("DiffractionCrystalEnvironment is null!");
		}
		if (detprop!=null) {
			if (register) {
				detprop.addDetectorPropertyListener(this);
			} else {
				detprop.removeDetectorPropertyListener(this);
			}		    
		} else {
			logger.error("DetectorProperties is null!");
		}
        
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (active ? 1231 : 1237);
		result = prime * result + ((detprop == null) ? 0 : detprop.hashCode());
		result = prime * result + ((diffenv == null) ? 0 : diffenv.hashCode());
		result = prime * result + Arrays.hashCode(imageCentrePC);
		result = prime * result
				+ ((plottingSystem == null) ? 0 : plottingSystem.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DiffractionImageAugmenter other = (DiffractionImageAugmenter) obj;
		if (active != other.active)
			return false;
		if (detprop == null) {
			if (other.detprop != null)
				return false;
		} else if (!detprop.equals(other.detprop))
			return false;
		if (diffenv == null) {
			if (other.diffenv != null)
				return false;
		} else if (!diffenv.equals(other.diffenv))
			return false;
		if (!Arrays.equals(imageCentrePC, other.imageCentrePC))
			return false;
		if (plottingSystem == null) {
			if (other.plottingSystem != null)
				return false;
		} else if (!plottingSystem.equals(other.plottingSystem))
			return false;
		return true;
	}

}

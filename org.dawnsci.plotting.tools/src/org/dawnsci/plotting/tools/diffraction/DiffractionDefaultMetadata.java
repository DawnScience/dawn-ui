package org.dawnsci.plotting.tools.diffraction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.vecmath.Vector3d;

import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.preference.DiffractionToolConstants;
import org.dawnsci.plotting.tools.preference.detector.DiffractionDetector;
import org.dawnsci.plotting.tools.preference.detector.DiffractionDetectorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.io.DiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.metadata.IDiffractionMetadata;

public class DiffractionDefaultMetadata {
	
	private static Logger logger = LoggerFactory.getLogger(DiffractionDefaultMetadata.class);
	
	/**
	 * Static method to produce a Detector properties object populated with persisted values
	 * from the preferences store
	 * 
	 * @param shape
	 *            shape from the Dataset the detector properties are created for
	 *            Used to produce the initial detector origin
	 *            
	 */
	public static DetectorProperties getPersistedDetectorProperties(int[] shape) { 
		DetectorBean bean = getPersistedDetectorPropertiesBean(shape);
		return bean.getDetectorProperties();
	}

	/**
	 * Static method to produce a DiffractionCrystalEnvironment properties object populated with persisted values
	 * from the preferences store
	 */
	public static DiffractionCrystalEnvironment getPersistedDiffractionCrystalEnvironment() {
		double lambda = Activator.getPlottingPreferenceStore().getDouble(DiffractionToolConstants.LAMBDA);
		double startOmega = Activator.getPlottingPreferenceStore().getDouble(DiffractionToolConstants.START_OMEGA);
		double rangeOmega = Activator.getPlottingPreferenceStore().getDouble(DiffractionToolConstants.RANGE_OMEGA);
		double exposureTime = Activator.getPlottingPreferenceStore().getDouble(DiffractionToolConstants.EXPOSURE_TIME);
		
		return new DiffractionCrystalEnvironment(lambda, startOmega, rangeOmega, exposureTime);
	}
	
	/**
	 * Static method to produce a Detector properties object populated with default values
	 * from the preferences store
	 * 
	 * @param shape
	 *            shape from the Dataset the detector properties are created for
	 *            Used to produce the initial detector origin
	 *            
	 */
	public static DetectorProperties getDefaultDetectorProperties(int[] shape) {
		
		int heightInPixels = shape[0];
		int widthInPixels = shape[1];
		
		// Get the default values from the preference store
		double pixelSizeX  = Activator.getPlottingPreferenceStore().getDefaultDouble(DiffractionToolConstants.PIXEL_SIZE_X);
		double pixelSizeY  = Activator.getPlottingPreferenceStore().getDefaultDouble(DiffractionToolConstants.PIXEL_SIZE_Y);
		double distance = Activator.getPlottingPreferenceStore().getDefaultDouble(DiffractionToolConstants.DISTANCE);
		
		// Create the detector origin vector based on the above
		double[] detectorOrigin = { (widthInPixels - widthInPixels/2d) * pixelSizeX, (heightInPixels - heightInPixels/2d) * pixelSizeY, distance };
		
		// The rotation of the detector relative to the reference frame - assume no rotation
		double detectorRotationX = Activator.getPlottingPreferenceStore().getDefaultDouble(DiffractionToolConstants.DETECTOR_ROTATION_X);
		double detectorRotationY = Activator.getPlottingPreferenceStore().getDefaultDouble(DiffractionToolConstants.DETECTOR_ROTATION_Y);
		double detectorRotationZ = Activator.getPlottingPreferenceStore().getDefaultDouble(DiffractionToolConstants.DETECTOR_ROTATION_Z);

		DetectorProperties detprop =new DetectorProperties(new Vector3d(detectorOrigin), heightInPixels, widthInPixels, 
				pixelSizeX, pixelSizeY, detectorRotationX, detectorRotationY, detectorRotationZ);
		
		
		double x = Activator.getPlottingPreferenceStore().getDefaultDouble(DiffractionToolConstants.BEAM_CENTRE_X);
		double y = Activator.getPlottingPreferenceStore().getDefaultDouble(DiffractionToolConstants.BEAM_CENTRE_Y);
		
		double yaw = Activator.getPlottingPreferenceStore().getDefaultDouble(DiffractionToolConstants.DETECTOR_YAW);
		double pitch = Activator.getPlottingPreferenceStore().getDefaultDouble(DiffractionToolConstants.DETECTOR_PITCH);
		double roll = Activator.getPlottingPreferenceStore().getDefaultDouble(DiffractionToolConstants.DETECTOR_ROLL);
		
		detprop.setBeamCentreCoords(new double[] {x,y});
		
		detprop.setNormalAnglesInDegrees(yaw, pitch, roll);
		
		return detprop;
	}
	
	/**
	 * Static method to produce a DiffractionCrystalEnvironment properties object populated with default values
	 * from the preferences store
	 */
	public static DiffractionCrystalEnvironment getDefaultDiffractionCrystalEnvironment() {
		double lambda = Activator.getPlottingPreferenceStore().getDefaultDouble(DiffractionToolConstants.LAMBDA);
		double startOmega = Activator.getPlottingPreferenceStore().getDefaultDouble(DiffractionToolConstants.START_OMEGA);
		double rangeOmega = Activator.getPlottingPreferenceStore().getDefaultDouble(DiffractionToolConstants.RANGE_OMEGA);
		double exposureTime = Activator.getPlottingPreferenceStore().getDefaultDouble(DiffractionToolConstants.EXPOSURE_TIME);
		
		return new DiffractionCrystalEnvironment(lambda, startOmega, rangeOmega, exposureTime);
	}
	
	/**
	 * Static method to set the default DiffractionCrystalEnvironment values in the 
	 * from the preferences store
	 */
	public static void setPersistedDiffractionCrystalEnvironmentValues(DiffractionCrystalEnvironment dce){
		Activator.getPlottingPreferenceStore().setValue(DiffractionToolConstants.LAMBDA, dce.getWavelength());
		Activator.getPlottingPreferenceStore().setValue(DiffractionToolConstants.START_OMEGA, dce.getPhiStart());
		Activator.getPlottingPreferenceStore().setValue(DiffractionToolConstants.RANGE_OMEGA, dce.getPhiRange());
	}
	
	/**
	 * Static method to set the default DetectorProperties values in the 
	 * from the preferences store
	 */
	public static void setPersistedDetectorPropertieValues(DetectorProperties detprop) {
		Activator.getPlottingPreferenceStore().setValue(DiffractionToolConstants.PIXEL_SIZE_X, detprop.getVPxSize());
		Activator.getPlottingPreferenceStore().setValue(DiffractionToolConstants.PIXEL_SIZE_Y, detprop.getHPxSize());
		Activator.getPlottingPreferenceStore().setValue(DiffractionToolConstants.DISTANCE, detprop.getBeamCentreDistance());
		Activator.getPlottingPreferenceStore().setValue(DiffractionToolConstants.BEAM_CENTRE_X, detprop.getBeamCentreCoords()[0]);
		Activator.getPlottingPreferenceStore().setValue(DiffractionToolConstants.BEAM_CENTRE_Y, detprop.getBeamCentreCoords()[1]);
		double[] normalAngles = detprop.getNormalAnglesInDegrees();
		Activator.getPlottingPreferenceStore().setValue(DiffractionToolConstants.DETECTOR_YAW,normalAngles[0]);
		Activator.getPlottingPreferenceStore().setValue(DiffractionToolConstants.DETECTOR_PITCH,normalAngles[1]);
		Activator.getPlottingPreferenceStore().setValue(DiffractionToolConstants.DETECTOR_ROLL,normalAngles[2]);
		
		
	}
	
	/**
	 * Static method to obtain a DiffractionMetaDataAdapter populated with default values
	 * from the preferences store to act as a starting point for images without metadata
	 */
	public static IDiffractionMetadata getDiffractionMetadata(String filePath, int[] shape) {
		
		final DetectorBean detbean = getPersistedDetectorPropertiesBean(shape);
		final DiffractionCrystalEnvironment diffenv = getPersistedDiffractionCrystalEnvironment();
		
		logger.debug("Meta read from preferences");
		
		DiffractionMetadata meta = new DiffractionMetadata(filePath, detbean.getDetectorProperties(), diffenv);
		
		Collection<Serializable> col = new ArrayList<Serializable>();
		col.add(detbean.getDiffractionDetector());
		meta.setUserObjects(col);
		
		return meta;
		
	}
	
	/**
	 * Static method to produce a Detector properties object populated with persisted values
	 * from the preferences store
	 * 
	 * @param shape
	 *            shape from the Dataset the detector properties are created for
	 *            Used to produce the initial detector origin
	 *            
	 */
	public static DetectorBean getPersistedDetectorPropertiesBean(int[] shape) {
		
		int heightInPixels = shape[0];
		int widthInPixels = shape[1];
		
		// Get the default values from the preference store
		double pixelSizeX  = Activator.getPlottingPreferenceStore().getDouble(DiffractionToolConstants.PIXEL_SIZE_X);
		double pixelSizeY  = Activator.getPlottingPreferenceStore().getDouble(DiffractionToolConstants.PIXEL_SIZE_Y);
		double distance = Activator.getPlottingPreferenceStore().getDouble(DiffractionToolConstants.DISTANCE);
		
		//Guess pixel Size from the shape of the image
		DiffractionDetector det = DiffractionDetectorHelper.getMatchingDetector(shape);
		
		if (det != null) {
			pixelSizeX = det.getXPixelMM();
			pixelSizeY = det.getYPixelMM();
		} else {
			det = new DiffractionDetector();
			det.setDetectorName("Default");
			det.setNumberOfPixelsX(shape[0]);
			det.setNumberOfPixelsY(shape[1]);
			det.setXPixelMM(pixelSizeX);
			det.setYPixelMM(pixelSizeY);
		}
		
		// Create the detector origin vector based on the above
		double[] detectorOrigin = { (widthInPixels - widthInPixels/2d) * pixelSizeX, (heightInPixels - heightInPixels/2d) * pixelSizeY, distance };
		
		// The rotation of the detector relative to the reference frame - assume no rotation
		double detectorRotationX = Activator.getPlottingPreferenceStore().getDouble(DiffractionToolConstants.DETECTOR_ROTATION_X);
		double detectorRotationY = Activator.getPlottingPreferenceStore().getDouble(DiffractionToolConstants.DETECTOR_ROTATION_Y);
		double detectorRotationZ = Activator.getPlottingPreferenceStore().getDouble(DiffractionToolConstants.DETECTOR_ROTATION_Z);
		
		DetectorProperties detprop =new DetectorProperties(new Vector3d(detectorOrigin), heightInPixels, widthInPixels, 
				pixelSizeX, pixelSizeY, detectorRotationX, detectorRotationY, detectorRotationZ);
		
		double x = Activator.getPlottingPreferenceStore().getDouble(DiffractionToolConstants.BEAM_CENTRE_X);
		double y = Activator.getPlottingPreferenceStore().getDouble(DiffractionToolConstants.BEAM_CENTRE_Y);
		
		double yaw = Activator.getPlottingPreferenceStore().getDouble(DiffractionToolConstants.DETECTOR_YAW);
		double pitch = Activator.getPlottingPreferenceStore().getDouble(DiffractionToolConstants.DETECTOR_PITCH);
		double roll = Activator.getPlottingPreferenceStore().getDouble(DiffractionToolConstants.DETECTOR_ROLL);
		
		detprop.setBeamCentreCoords(new double[] {x,y});
		
		detprop.setNormalAnglesInDegrees(yaw, pitch, roll);
		
		detprop.setBeamCentreDistance(distance);
		
		return new DetectorBean(detprop, det);
	}
	
	public static IDiffractionMetadata getDiffractionMetadata(int[] shape) {
		
		final DetectorBean detbean = getPersistedDetectorPropertiesBean(shape);
		final DiffractionCrystalEnvironment diffenv = getPersistedDiffractionCrystalEnvironment();
		
		logger.debug("Meta read from preferences");
		
		DiffractionMetadata meta = new DiffractionMetadata(null, detbean.getDetectorProperties(), diffenv);
		
		Collection<Serializable> col = new ArrayList<Serializable>();
		col.add(detbean.getDiffractionDetector());
		meta.setUserObjects(col);
		
		return meta;
		
	}
}

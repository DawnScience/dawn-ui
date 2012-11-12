package org.dawb.workbench.plotting.tools.diffraction;

import javax.vecmath.Vector3d;

import org.dawb.workbench.plotting.Activator;
import org.dawb.workbench.plotting.preference.DiffractionToolConstants;

import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.io.DiffractionMetaDataAdapter;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;

public class DiffractionDefaultMetadata {
	/**
	 * Static method to produce a Detector properties object populated with default values
	 * from the preferences store
	 * 
	 * @param shape
	 *            shape from the AbstractDataset the detector properties are created for
	 *            Used to produce the initial detector origin
	 *            
	 */
	public static DetectorProperties getDefaultDetectorProperties(int[] shape) {
		
		int heightInPixels = shape[0];
		int widthInPixels = shape[1];
		
		// Get the default values from the preference store
		double pixelSizeX  = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.PIXEL_SIZE_X);
		double pixelSizeY  = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.PIXEL_SIZE_Y);
		double distance = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.DISTANCE);
		
		// Create the detector origin vector based on the above
		double[] detectorOrigin = { (widthInPixels - widthInPixels/2d) * pixelSizeX, (heightInPixels - heightInPixels/2d) * pixelSizeY, distance };
		
		// The rotation of the detector relative to the reference frame - assume no rotation
		double detectorRotationX = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.DETECTOR_ROTATION_X);
		double detectorRotationY = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.DETECTOR_ROTATION_Y);
		double detectorRotationZ = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.DETECTOR_ROTATION_Z);

		return new DetectorProperties(new Vector3d(detectorOrigin), heightInPixels, widthInPixels, 
				pixelSizeX, pixelSizeY, detectorRotationX, detectorRotationY, detectorRotationZ);
	}
	
	/**
	 * Static method to produce a DiffractionCrystalEnvironment properties object populated with default values
	 * from the preferences store
	 */
	public static DiffractionCrystalEnvironment getDefaultDiffractionCrystalEnvironment() {
		double lambda = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.LAMBDA);
		double startOmega = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.START_OMEGA);
		double rangeOmega = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.RANGE_OMEGA);
		double exposureTime = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.EXPOSURE_TIME);
		
		return new DiffractionCrystalEnvironment(lambda, startOmega, rangeOmega, exposureTime);
	}
	
	/**
	 * Static method to obtain a DiffractionMetaDataAdapter populated with default values
	 * from the preferences store to act as a starting point for images without metadata
	 */
	public static IDiffractionMetadata getDiffractionMetadata(int[] shape) {
		
		final DetectorProperties detprop = getDefaultDetectorProperties(shape);
		final DiffractionCrystalEnvironment diffenv = getDefaultDiffractionCrystalEnvironment();
		
		return new DiffractionMetaDataAdapter() {
			private static final long serialVersionUID = DiffractionMetaDataAdapter.serialVersionUID;

			@Override
			public DiffractionCrystalEnvironment getDiffractionCrystalEnvironment() {
				return diffenv;
			}

			@Override
			public DetectorProperties getDetector2DProperties() {
				return detprop;
			}
			
			@Override
			public DetectorProperties getOriginalDetector2DProperties() {
				return detprop;
			}

			@Override
			public DiffractionCrystalEnvironment getOriginalDiffractionCrystalEnvironment() {
				return diffenv;
			}

			@Override
			public DiffractionMetaDataAdapter clone() {
				return new DiffractionMetaDataAdapter() {
					private static final long serialVersionUID = DiffractionMetaDataAdapter.serialVersionUID;

					@Override
					public DiffractionCrystalEnvironment getDiffractionCrystalEnvironment() {
						return diffenv.clone();
					}

					@Override
					public DetectorProperties getDetector2DProperties() {
						return detprop.clone();
					}
					
					@Override
					public DetectorProperties getOriginalDetector2DProperties() {
						return detprop.clone();
					}

					@Override
					public DiffractionCrystalEnvironment getOriginalDiffractionCrystalEnvironment() {
						return diffenv.clone();
					}
				};
			}
		};
		
	}

}

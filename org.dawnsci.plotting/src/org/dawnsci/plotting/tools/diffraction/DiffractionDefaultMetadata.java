package org.dawnsci.plotting.tools.diffraction;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.vecmath.Vector3d;

import org.dawb.common.services.ILoaderService;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.plotting.Activator;
import org.dawnsci.plotting.preference.DiffractionToolConstants;
import org.dawnsci.plotting.preference.detector.DiffractionDetectorHelper;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.io.DiffractionMetaDataAdapter;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.io.IExtendedMetadata;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;

public class DiffractionDefaultMetadata {
	
	private static Logger logger = LoggerFactory.getLogger(DiffractionDefaultMetadata.class);
	
	public static IDiffractionMetadata getDiffractionMetaData(IImageTrace imageTrace, IWorkbenchPart part) {
		
		// Now always returns IDiffractionMetadata to prevent creation of a new
		// metadata object after listeners have been added to the old metadata

		ILoaderService       service    = (ILoaderService)PlatformUI.getWorkbench().getService(ILoaderService.class);
		IDiffractionMetadata lockedMeta = service.getLockedDiffractionMetaData();

		if (lockedMeta != null) {

			if (imageTrace==null) return lockedMeta;

			IMetaData mdImage = imageTrace.getData().getMetadata();

			if (mdImage == null) {
				imageTrace.getData().setMetadata(lockedMeta.clone());
			} else if (!(mdImage instanceof IDiffractionMetadata)) {
				IDiffractionMetadata idm = DiffractionDefaultMetadata.getDiffractionMetadata(imageTrace.getData().getShape(),mdImage);
				DiffractionDefaultMetadata.copyNewOverOld(lockedMeta, idm);
				imageTrace.getData().setMetadata(idm);
			} else if (mdImage instanceof IDiffractionMetadata) {
				if (!diffractionMetadataAreEqual((IDiffractionMetadata)mdImage,lockedMeta)) {
					DiffractionDefaultMetadata.copyNewOverOld(lockedMeta, (IDiffractionMetadata)mdImage);
					imageTrace.getData().setMetadata(mdImage);
				}
			}
			return lockedMeta;

		}


		//If not see if the trace has diffraction meta data
		if (imageTrace==null) return null;
		IMetaData mdImage = imageTrace.getData().getMetadata();

		if (mdImage !=null && mdImage  instanceof IDiffractionMetadata) return (IDiffractionMetadata)mdImage;

		// if it is null try and get it from the loader service
		if (mdImage == null) {

			IMetaData md = null;
			if (part instanceof IEditorPart) {
				try {
					md = service.getMetaData(EclipseUtils.getFilePath(((IEditorPart)part).getEditorInput()), null);
				} catch (Exception e) {
					logger.error("Cannot read meta data from "+part.getTitle(), e);
				}
			}

			// If it is there and diffraction data return it
			if (md!=null && md instanceof IDiffractionMetadata) return (IDiffractionMetadata)md;

			if (md != null)
				mdImage = md;
		}

		//if the file contains IMetaData but not IDiffraction meta data, wrap the old meta in a 
		// new IDiffractionMetadata object and put it back in the dataset
		if (mdImage!=null) {
			mdImage = DiffractionDefaultMetadata.getDiffractionMetadata(imageTrace.getData().getShape(),mdImage);
			imageTrace.getData().setMetadata(mdImage);
			return (IDiffractionMetadata)mdImage;
		}

		// if there is no meta create default IDiff and put it in the dataset
		mdImage = DiffractionDefaultMetadata.getDiffractionMetadata(imageTrace.getData().getShape());
		imageTrace.getData().setMetadata(mdImage);
		//			}

		return (IDiffractionMetadata)mdImage;
	}

	
	private static boolean diffractionMetadataAreEqual(IDiffractionMetadata meta1,IDiffractionMetadata meta2) {
		
		if (meta1.getDetector2DProperties().equals(meta2.getDetector2DProperties()) &&
				meta1.getDiffractionCrystalEnvironment().equals(meta2.getDiffractionCrystalEnvironment())) {
			return true;
		}
		
		return false;
		
	}

	
	/**
	 * Static method to produce a Detector properties object populated with persisted values
	 * from the preferences store
	 * 
	 * @param shape
	 *            shape from the AbstractDataset the detector properties are created for
	 *            Used to produce the initial detector origin
	 *            
	 */
	public static DetectorProperties getPersistedDetectorProperties(int[] shape) {
		
		int heightInPixels = shape[0];
		int widthInPixels = shape[1];
		
		// Get the default values from the preference store
		double pixelSizeX  = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.PIXEL_SIZE_X);
		double pixelSizeY  = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.PIXEL_SIZE_Y);
		double distance = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.DISTANCE);
		
		//Guess pixel Size from the shape of the image
		double[] pixelXY = DiffractionDetectorHelper.getXYPixelSizeMM(shape);
		
		if (pixelXY != null) {
			pixelSizeX = pixelXY[0];
			pixelSizeY = pixelXY[1];
		}
		
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
	 * Static method to produce a DiffractionCrystalEnvironment properties object populated with persisted values
	 * from the preferences store
	 */
	public static DiffractionCrystalEnvironment getPersistedDiffractionCrystalEnvironment() {
		double lambda = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.LAMBDA);
		double startOmega = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.START_OMEGA);
		double rangeOmega = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.RANGE_OMEGA);
		double exposureTime = Activator.getDefault().getPreferenceStore().getDouble(DiffractionToolConstants.EXPOSURE_TIME);
		
		return new DiffractionCrystalEnvironment(lambda, startOmega, rangeOmega, exposureTime);
	}
	
	/**
	 * Static method to replace the values in the old DiffractionCrystalEnvironment with the new
	 */
	public static void copyNewOverOld(DiffractionCrystalEnvironment newDCE, DiffractionCrystalEnvironment oldDCE) {
		
		oldDCE.setExposureTime(newDCE.getExposureTime());
		oldDCE.setPhiRange(newDCE.getPhiRange());
		oldDCE.setPhiStart(newDCE.getPhiStart());
		oldDCE.setWavelength(newDCE.getWavelength());

	}
	
	/**
	 * Static method to replace the values in the old DetectorProperties with the new
	 */
	public static void copyNewOverOld(DetectorProperties newDP, DetectorProperties oldDP) {
		
		oldDP.setOrigin(new Vector3d(newDP.getOrigin()));
		oldDP.setBeamVector(new Vector3d(newDP.getBeamVector()));
		oldDP.setPx(newDP.getPx());
		oldDP.setPy(newDP.getPy());
		oldDP.setVPxSize(newDP.getVPxSize());
		oldDP.setHPxSize(newDP.getHPxSize());
		oldDP.setOrientation(newDP.getOrientation());

	}
	
	/**
	 * Static method to replace the values in the DiffractionCrystalEnvironment and DetectorProperties
	 *  of an old IDiffractionmetaData with a new
	 */
	public static void copyNewOverOld(IDiffractionMetadata newDM, IDiffractionMetadata oldDM){
		copyNewOverOld(newDM.getDetector2DProperties(), oldDM.getDetector2DProperties());
		copyNewOverOld(newDM.getDiffractionCrystalEnvironment(), oldDM.getDiffractionCrystalEnvironment());
	}
	
	
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
		double pixelSizeX  = Activator.getDefault().getPreferenceStore().getDefaultDouble(DiffractionToolConstants.PIXEL_SIZE_X);
		double pixelSizeY  = Activator.getDefault().getPreferenceStore().getDefaultDouble(DiffractionToolConstants.PIXEL_SIZE_Y);
		double distance = Activator.getDefault().getPreferenceStore().getDefaultDouble(DiffractionToolConstants.DISTANCE);
		
		// Create the detector origin vector based on the above
		double[] detectorOrigin = { (widthInPixels - widthInPixels/2d) * pixelSizeX, (heightInPixels - heightInPixels/2d) * pixelSizeY, distance };
		
		// The rotation of the detector relative to the reference frame - assume no rotation
		double detectorRotationX = Activator.getDefault().getPreferenceStore().getDefaultDouble(DiffractionToolConstants.DETECTOR_ROTATION_X);
		double detectorRotationY = Activator.getDefault().getPreferenceStore().getDefaultDouble(DiffractionToolConstants.DETECTOR_ROTATION_Y);
		double detectorRotationZ = Activator.getDefault().getPreferenceStore().getDefaultDouble(DiffractionToolConstants.DETECTOR_ROTATION_Z);

		return new DetectorProperties(new Vector3d(detectorOrigin), heightInPixels, widthInPixels, 
				pixelSizeX, pixelSizeY, detectorRotationX, detectorRotationY, detectorRotationZ);
	}
	
	/**
	 * Static method to produce a DiffractionCrystalEnvironment properties object populated with default values
	 * from the preferences store
	 */
	public static DiffractionCrystalEnvironment getDefaultDiffractionCrystalEnvironment() {
		double lambda = Activator.getDefault().getPreferenceStore().getDefaultDouble(DiffractionToolConstants.LAMBDA);
		double startOmega = Activator.getDefault().getPreferenceStore().getDefaultDouble(DiffractionToolConstants.START_OMEGA);
		double rangeOmega = Activator.getDefault().getPreferenceStore().getDefaultDouble(DiffractionToolConstants.RANGE_OMEGA);
		double exposureTime = Activator.getDefault().getPreferenceStore().getDefaultDouble(DiffractionToolConstants.EXPOSURE_TIME);
		
		return new DiffractionCrystalEnvironment(lambda, startOmega, rangeOmega, exposureTime);
	}
	
	/**
	 * Static method to set the default DiffractionCrystalEnvironment values in the 
	 * from the preferences store
	 */
	public static void setPersistedDiffractionCrystalEnvironmentValues(DiffractionCrystalEnvironment dce){
		Activator.getDefault().getPreferenceStore().setValue(DiffractionToolConstants.LAMBDA, dce.getWavelength());
		Activator.getDefault().getPreferenceStore().setValue(DiffractionToolConstants.START_OMEGA, dce.getPhiStart());
		Activator.getDefault().getPreferenceStore().setValue(DiffractionToolConstants.RANGE_OMEGA, dce.getPhiRange());
	}
	
	/**
	 * Static method to set the default DetectorProperties values in the 
	 * from the preferences store
	 */
	public static void setPersistedDetectorPropertieValues(DetectorProperties detprop) {
		Activator.getDefault().getPreferenceStore().setValue(DiffractionToolConstants.PIXEL_SIZE_X, detprop.getVPxSize());
		Activator.getDefault().getPreferenceStore().setValue(DiffractionToolConstants.PIXEL_SIZE_Y, detprop.getHPxSize());
		Activator.getDefault().getPreferenceStore().setValue(DiffractionToolConstants.DISTANCE, detprop.getOrigin().z);
		
	}
	
	/**
	 * Static method to obtain a DiffractionMetaDataAdapter populated with default values
	 * from the preferences store to act as a starting point for images without metadata
	 */
	public static IDiffractionMetadata getDiffractionMetadata(int[] shape) {
		
		final DetectorProperties detprop = getPersistedDetectorProperties(shape);
		final DiffractionCrystalEnvironment diffenv = getPersistedDiffractionCrystalEnvironment();
		
		final DetectorProperties detpropOrig = getDefaultDetectorProperties(shape);
		final DiffractionCrystalEnvironment diffenvOrig = getDefaultDiffractionCrystalEnvironment();
		
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
				return detpropOrig;
			}

			@Override
			public DiffractionCrystalEnvironment getOriginalDiffractionCrystalEnvironment() {
				return diffenvOrig;
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
	
	/**
	 * Static method to obtain a DiffractionMetaDataAdapter wrapping the argument metadata
	 *  and populated with default values from the preferences store 
	 */
	public static IDiffractionMetadata getDiffractionMetadata(int[] shape, final IMetaData metaData) {
		
		//final IMetaData innerMeta = metaData.clone();
		
		final DetectorProperties detprop = getPersistedDetectorProperties(shape);
		final DiffractionCrystalEnvironment diffenv = getPersistedDiffractionCrystalEnvironment();
		
		final DetectorProperties detpropOrig = getDefaultDetectorProperties(shape);
		final DiffractionCrystalEnvironment diffenvOrig = getDefaultDiffractionCrystalEnvironment();
		
		if (metaData instanceof IExtendedMetadata) {
			
			final IExtendedMetadata exMeta = (IExtendedMetadata)metaData;
			
			return new DiffractionMetaDataAdapter() {
				private static final long serialVersionUID = DiffractionMetaDataAdapter.serialVersionUID;
				
				//All methods required for IMetadata
				public Collection<String> getDataNames() {return exMeta.getDataNames();}

				/**
				 * Can be implemented to return sizes of datasets
				 * 
				 * @return map of sizes
				 */
				public Map<String, Integer> getDataSizes() {return exMeta.getDataSizes();}

				/**
				 * Can be implemented to return shapes of dataset
				 * 
				 * @return map of sizes
				 */
				public Map<String, int[]> getDataShapes(){return exMeta.getDataShapes();}

				/**
				 * Returns string value or null if not implemented
				 * 
				 * @param key
				 * @return value
				 */
				public Serializable getMetaValue(String key) throws Exception {return exMeta.getMetaValue(key);}

				/**
				 * Returns a collection of metadata names
				 * @return collection
				 * @throws Exception
				 */
				public Collection<String> getMetaNames() throws Exception {return exMeta.getMetaNames();}

				/**
				 * May be implemented to provide custom metadata in the form of a collection of objects
				 * 
				 * @return collection
				 */
				public Collection<Serializable> getUserObjects() {return exMeta.getUserObjects();}
				
				//All methods required for IExtendedMetadata
				/**
				 * This should be the timestamp of when the experiment or measurement took place which should
				 * be recorded in the header of the file, if applicable
				 * 
				 * @return a date object to represent when the data was created
				 */
				@Override
				public Date getCreation() {return exMeta.getCreation();}

				/**
				 * @return a date object that indicated when the data was last modified
				 */
				@Override
				public Date getLastModified() {return exMeta.getLastModified();}

				/**
				 * @return a string representing the user who created the file
				 */
				@Override
				public String getCreator()  {return exMeta.getCreator();}

				/**
				 * @return a string containing the filename
				 */
				@Override
				public String getFileName()  {return exMeta.getFileName();}

				/**
				 * @return the owner of the file
				 */
				public String getFileOwner() {return exMeta.getFileOwner();}

				/**
				 * @return a long representing the size of the file in bytes
				 */
				@Override
				public long getFileSize() {return exMeta.getFileSize();}

				/**
				 * @return the full path string of the file
				 */
				@Override
				public String getFullPath() {return exMeta.getFullPath();}

				/**
				 * @return The scan command as a string that was used to generate the data. This can be null as not always
				 *         applicable
				 */
				@Override
				public String getScanCommand() {return exMeta.getScanCommand();}

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
					return detpropOrig;
				}

				@Override
				public DiffractionCrystalEnvironment getOriginalDiffractionCrystalEnvironment() {
					return diffenvOrig;
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
			
		} else {
			return new DiffractionMetaDataAdapter() {
				private static final long serialVersionUID = DiffractionMetaDataAdapter.serialVersionUID;
				
				//All methods required for IMetadata
				public Collection<String> getDataNames() {return metaData.getDataNames();}

				/**
				 * Can be implemented to return sizes of datasets
				 * 
				 * @return map of sizes
				 */
				public Map<String, Integer> getDataSizes() {return metaData.getDataSizes();}

				/**
				 * Can be implemented to return shapes of dataset
				 * 
				 * @return map of sizes
				 */
				public Map<String, int[]> getDataShapes(){return metaData.getDataShapes();}

				/**
				 * Returns string value or null if not implemented
				 * 
				 * @param key
				 * @return value
				 */
				public Serializable getMetaValue(String key) throws Exception {return metaData.getMetaValue(key);}

				/**
				 * Returns a collection of metadata names
				 * @return collection
				 * @throws Exception
				 */
				public Collection<String> getMetaNames() throws Exception {return metaData.getMetaNames();}

				/**
				 * May be implemented to provide custom metadata in the form of a collection of objects
				 * 
				 * @return collection
				 */
				public Collection<Serializable> getUserObjects() {return metaData.getUserObjects();}

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
					return detpropOrig;
				}

				@Override
				public DiffractionCrystalEnvironment getOriginalDiffractionCrystalEnvironment() {
					return diffenvOrig;
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
}

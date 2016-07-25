/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.diffraction;

import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionMetadataUtils;
import uk.ac.diamond.scisoft.analysis.io.NexusDiffractionCalibrationReader;

public class DiffractionUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(DiffractionUtils.class);

	/**
	 * Fetch diffraction metadata, doesn't add it to the IDataset
	 * @param image
	 * @param altPath alternative for file path if metadata is null or does not hold it
	 * @param service
	 * @param statusText returned message (can be null)
	 * @return diffraction metadata
	 */
	public static IDiffractionMetadata getDiffractionMetadata(ILazyDataset image, String altPath, ILoaderService service, String[] statusText) {
		// Now always returns IDiffractionMetadata to prevent creation of a new
		// metadata object after listeners have been added to the old metadata
		//TODO improve this section- it's pretty horrible
		IDiffractionMetadata lockedMeta = service.getLockedDiffractionMetaData();
		
		if (image == null)
			return lockedMeta;

		int[] shape = image.getShape();
		IMetadata mdImage = null;
		try {
			mdImage = image.getMetadata();
		} catch (Exception e1) {
			// do nothing
		}
		if (lockedMeta != null) {
			if (mdImage instanceof IDiffractionMetadata) {
				IDiffractionMetadata dmd = (IDiffractionMetadata) mdImage;
				if (!dmd.getDiffractionCrystalEnvironment().equals(lockedMeta.getDiffractionCrystalEnvironment()) ||
						!dmd.getDetector2DProperties().equals(lockedMeta.getDetector2DProperties())) {
					try {
						DiffractionMetadataUtils.copyNewOverOld(lockedMeta, (IDiffractionMetadata)mdImage);
					} catch (IllegalArgumentException e) {
						if (statusText != null)
							statusText[0] = "Locked metadata does not match image dimensions!";
					}
				}
			} else {
				//TODO what if the image is rotated?
				
				if (shape[0] == lockedMeta.getDetector2DProperties().getPx() &&
					shape[1] == lockedMeta.getDetector2DProperties().getPy()) {
				} else {
					IDiffractionMetadata clone = lockedMeta.clone();
					clone.getDetector2DProperties().setPx(shape[0]);
					clone.getDetector2DProperties().setPy(shape[1]);
					if (statusText != null)
						statusText[0] = "Locked metadata does not match image dimensions!";
				}
			}
			if (statusText != null && statusText[0] == null) {
				statusText[0] = "Metadata loaded from locked version";
			}
			return lockedMeta;
		}

		//If not see if the trace has diffraction meta data
		if (mdImage instanceof IDiffractionMetadata) {
			if (statusText != null && statusText[0] == null) {
				statusText[0] = "Metadata loaded from image";
			}
			return (IDiffractionMetadata) mdImage;
		}
		
		//Try and get the filename here, it will help later on
		String filePath = mdImage == null ? null : mdImage.getFilePath();
		
		if (filePath == null) {
			filePath = altPath;
		}
		
		if (filePath != null) {
			//see if we can read diffraction info from nexus files
			IDiffractionMetadata difMet = null;
			try {
				difMet = NexusDiffractionCalibrationReader.getDiffractionMetadataFromNexus(filePath, null, image.getName());
			} catch (DatasetException e) {
				logger.debug("Could not read diffraction metadata for " + image.getName(), e);
			}
			if (difMet == null)
				try {
					difMet = NexusDiffractionCalibrationReader.getDiffractionMetadataFromNexus(filePath, null, null);
				} catch (DatasetException e) {
					logger.debug("Could not read default diffraction metadata", e);
				}
			if (difMet !=null) {
				//TODO comment out
				//image.setMetadata(difMet);
				if (statusText != null && statusText[0] == null) {
					statusText[0] = "Metadata  loaded from nexus tree";
					return difMet;
				}
			}
		}

		// if it is null try and get it from the loader service
		if (mdImage == null && filePath != null) {
			IMetadata md = null;
			try {
				md = service.getMetadata(filePath, null);
			} catch (Exception e) {
				logger.error("Cannot read meta data from part", e);
			}
			
			//If it is there and diffraction data return it
			if (md instanceof IDiffractionMetadata) {
				if (statusText != null && statusText[0] == null) {
					statusText[0] = "Metadata loaded from file";
				}
				return (IDiffractionMetadata) md;
			}
		}
		
		// if there is no meta or is not nexus or IDiff create default IDiff and put it in the dataset
		mdImage = DiffractionDefaultMetadata.getDiffractionMetadata(filePath, shape);
		//image.setMetadata(mdImage);
		if (statusText != null && statusText[0] == null) {
			statusText[0] = "No metadata found. Values loaded from preferences:";
		}
		return (IDiffractionMetadata) mdImage;
	}

}

package org.dawnsci.plotting.tools.diffraction;

import org.dawnsci.plotting.preference.detector.DiffractionDetectorHelper;

import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.io.NexusDiffractionMetaReader;


public class NexusDiffractionMetaCreator {
	
	
	String filePath;
	NexusDiffractionMetaReader nexusDiffraction = null;
	
	public NexusDiffractionMetaCreator(String filePath) {
		this.filePath = filePath;
	}
	
	/**
	 * Read the diffraction metadata from a Nexus file.
	 * Other methods on the class can be used to determine how complete the read is
	 * May return null
	 * Uses NexusDiffactionMetaReader to read the data, this class just gives access to the 
	 * metadata stored in the preference file
	 * 
	 * @param imageSize. Size of the image the diffraction metadata is associated with in pixels (can be null)
	 */
	public IDiffractionMetadata getDiffractionMetadataFromNexus(int[] imageSize) {
		
		nexusDiffraction = new NexusDiffractionMetaReader(filePath); 
		
		final DetectorProperties detprop = DiffractionDefaultMetadata.getPersistedDetectorProperties(imageSize);
		final DiffractionCrystalEnvironment diffcrys = DiffractionDefaultMetadata.getPersistedDiffractionCrystalEnvironment();
		
		double[] xyPixelSize = DiffractionDetectorHelper.getXYPixelSizeMM(imageSize);
		
		return nexusDiffraction.getDiffractionMetadataFromNexus(imageSize, detprop, diffcrys, xyPixelSize);
	}
	
	/**
	 * Have complete DetectorProperties and DiffractionCrystalEnvironment values been read
	 */
	public boolean isCompleteRead() {
		return nexusDiffraction.isCompleteRead();
	}
	
	/**
	 * Have enough values to perform downstream calculations been read (ie exposure time not read)
	 */
	public boolean isPartialRead() {
		return nexusDiffraction.isPartialRead();
	}
	
	/**
	 * Were any values read from the Nexus file
	 */
	public boolean anyValuesRead() {
		return nexusDiffraction.anyValuesRead();
	}

}

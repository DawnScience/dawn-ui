package org.dawnsci.plotting.preference.detector;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;

import org.dawnsci.plotting.Activator;

public class DiffractionDetectorHelper {
	
	private static int MIN_BIN = 2;
	private static int MAX_BIN = 6;
	
	private static int PILATUSXGAP = 7;
	private static int PILATUSYGAP = 17;
	
	private static int PILATUSMODULEX = 487;
	private static int PILATUSMODULEY = 195;
	
	private static double PILATUSPIXELMM = 0.172;
	
	/**
	 * Static method to determine the detector pixel size using the image dimensions.
	 * Known detectors are stored in the preferences store.
	 * Pilatus detector are hardcoded and guessed from the pilatus module size
	 */
	public static double[] getXYPixelSizeMM(int[] imageSize) {
		
		DiffractionDetectors detectors = getDetectorsFromPreferences();
		
		if (detectors == null || detectors.getDiffractionDetectors().isEmpty()) return null;
		
		double[] output = null;
		
		output = checkForFullDetector(detectors, imageSize);
		
		if (output != null) return output;
		
		output = checkForPilatusRegions(imageSize);
		
		if (output != null) return output;
		
		output = checkForBinning(detectors, imageSize);
		
		return output;
	}
	
	/**
	 * Static method to determine whether an image was collected on a 
	 * Pilatus detector from the size of the image.
	 * If it was it returns the x and y pixel size in mm.
	 * If not it returns null
	 */
	public static double[] checkForPilatusRegions(int[] imageSize) {
		
		double[] pilatusPixels = new double[] {PILATUSPIXELMM,PILATUSPIXELMM};
		
		if (imageSize[0] == PILATUSMODULEX && imageSize[1] == PILATUSMODULEY) return pilatusPixels;
		
		if (imageSize[0] == PILATUSMODULEY && imageSize[1] == PILATUSMODULEX) return pilatusPixels;
		
		if (imageSize[0] % (PILATUSMODULEX+ PILATUSXGAP) == PILATUSMODULEX && 
			imageSize[1] % (PILATUSMODULEY+ PILATUSYGAP) == PILATUSMODULEY) {
			return pilatusPixels;
		}
		
		if (imageSize[1] % (PILATUSMODULEX+ PILATUSXGAP) == PILATUSMODULEX && 
			imageSize[0] % (PILATUSMODULEY+ PILATUSYGAP) == PILATUSMODULEY) {
			return pilatusPixels;
		}
		
		return null;
	}
	
	private static DiffractionDetectors getDetectorsFromPreferences() {
		String xml = Activator.getDefault().getPreferenceStore().getString(DiffractionDetectorConstants.DETECTOR);
		XMLDecoder xmlDecoder =new XMLDecoder(new ByteArrayInputStream(xml.getBytes()));
		return (DiffractionDetectors) xmlDecoder.readObject();
	}
	
	private static double[] checkForFullDetector(DiffractionDetectors detectors, int[] imageSize) {
		
		for (DiffractionDetector det: detectors.getDiffractionDetectors()) {
			if (imageSize[0] == det.getNumberOfPixelsX() && imageSize[1] == det.getNumberOfPixelsY()) {
				return new double[] {det.getXPixelMM(),det.getYPixelMM()};
			} else if (imageSize[0] == det.getNumberOfPixelsY() && imageSize[1] == det.getNumberOfPixelsX()){
				return new double[] {det.getYPixelMM(),det.getXPixelMM()};
			}
		}
		
		return null;
		
	}
	
	private static double[] checkForBinning(DiffractionDetectors detectors, int[] imageSize) {
		for (DiffractionDetector det: detectors.getDiffractionDetectors()) {
			
			for (int i = MIN_BIN; i < MAX_BIN; i++) {
				
				if (imageSize[0] == det.getNumberOfPixelsX()/i && imageSize[1] == det.getNumberOfPixelsY()/i) {
					return new double[] {det.getXPixelMM()*i,det.getYPixelMM()*i};
				} else if (imageSize[0] == det.getNumberOfPixelsY()/i && imageSize[1] == det.getNumberOfPixelsX()/i){
					return new double[] {det.getYPixelMM()*i,det.getXPixelMM()*i};
				}
			}
		}
		
		return null;
	}

}

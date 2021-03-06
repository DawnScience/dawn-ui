/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.preference.detector;

import java.util.ArrayList;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.quantity.Length;

import org.dawnsci.plotting.tools.Activator;

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
		
		DiffractionDetector out = getMatchingDetector(imageSize);
		
		if (out == null) return null;
		
		return new double[] {out.getXPixelMM(),out.getYPixelMM()};
	}
	
	/**
	 * Static method to determine the diffraction detector from the image size.
	 * <p>
	 * Known detectors are stored in the preferences store.
	 * Pilatus detectors are hardcoded and guessed from the pilatus module size
	 */
	public static DiffractionDetector getMatchingDetector(int[] imageSize) {
		DiffractionDetectors detectors = getDetectorsFromPreferences();
		
		return getMatchingDetector(imageSize, detectors);
	}
	
	/**
	 * Static method to determine the diffraction detector from the image size, uses only defaults in
	 * preference initialiser
	 * <p>
	 * This method works when there is no workspace (and therefore no preferences)
	 * 
	 */
	public static DiffractionDetector getMatchingDefaultDetector(int[] imageSize) {
		DiffractionDetectors detectors = DiffractionDetectorPreferenceInitializer.getDefaultDetectors();
		
		return getMatchingDetector(imageSize, detectors);
	}
	
	/**
	 * Static method to determine the most likely detector from the input list,
	 * using the image size.
	 *
	 */
	public static DiffractionDetector getMatchingDetector(int[] imageSize, DiffractionDetectors detectors) {
		
		if (detectors == null || detectors.getDiffractionDetectors().isEmpty()) return null;
		
		DiffractionDetector output = null;
		
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
	public static DiffractionDetector checkForPilatusRegions(int[] imageSize) {
		DiffractionDetector pilatusPlate = new DiffractionDetector();
		pilatusPlate.setDetectorName("PilatusPlate");
		pilatusPlate.setNumberOfPixelsX(PILATUSMODULEX);
		pilatusPlate.setNumberOfPixelsX(PILATUSMODULEY);
		pilatusPlate.setXPixelMM(PILATUSPIXELMM);
		pilatusPlate.setYPixelMM(PILATUSPIXELMM);
		
		if (imageSize[0] == PILATUSMODULEX && imageSize[1] == PILATUSMODULEY) return pilatusPlate;
		
		if (imageSize[0] == PILATUSMODULEY && imageSize[1] == PILATUSMODULEX) return pilatusPlate;
		
		if (imageSize[0] % (PILATUSMODULEX+ PILATUSXGAP) == PILATUSMODULEX && 
			imageSize[1] % (PILATUSMODULEY+ PILATUSYGAP) == PILATUSMODULEY) {
			return pilatusPlate;
		}
		
		if (imageSize[1] % (PILATUSMODULEX+ PILATUSXGAP) == PILATUSMODULEX && 
			imageSize[0] % (PILATUSMODULEY+ PILATUSYGAP) == PILATUSMODULEY) {
			return pilatusPlate;
		}

		return null;
	}

	public static List<String> getDiffractionDetectorNames() {
		DiffractionDetectors detectors = getDetectorsFromPreferences();

		List<DiffractionDetector> ds =detectors.getDiffractionDetectors();
		int nd = ds.size();

		List<String> names = new ArrayList<String>(nd);

		for (int i = 0; i < nd; i++) {
			names.add(ds.get(i).getDetectorName());
		}
		return names;
	}

	public static List<Quantity<Length>> getXYPixelSizeAmount(String name) {
		DiffractionDetectors detectors = getDetectorsFromPreferences();
		for (DiffractionDetector dd : detectors.getDiffractionDetectors()) {
			if (dd.getDetectorName().equals(name)) {
				List<Quantity<Length>> out = new ArrayList<>(2);
				out.add(dd.getxPixelSize());
				out.add(dd.getyPixelSize());
				return out;
			}
		}
		return null;
	}

	public static DiffractionDetectors getDetectorsFromPreferences() {
		String xml = Activator.getPlottingPreferenceStore().getString(DiffractionDetectorConstants.DETECTOR);
		return DiffractionDetectors.createDetectors(xml);
	}

	private static DiffractionDetector checkForFullDetector(DiffractionDetectors detectors, int[] imageSize) {
		for (DiffractionDetector det: detectors.getDiffractionDetectors()) {
			if (imageSize[0] == det.getNumberOfPixelsX() && imageSize[1] == det.getNumberOfPixelsY()) {
				return det;
			} else if (imageSize[0] == det.getNumberOfPixelsY() && imageSize[1] == det.getNumberOfPixelsX()){
				return rotateDetector(det);
			}
		}

		return null;
	}

	private static DiffractionDetector checkForBinning(DiffractionDetectors detectors, int[] imageSize) {
		for (DiffractionDetector det: detectors.getDiffractionDetectors()) {
			
			for (int i = MIN_BIN; i < MAX_BIN; i++) {
				
				if (imageSize[0] == det.getNumberOfPixelsX()/i && imageSize[1] == det.getNumberOfPixelsY()/i) {
					return binDetector(det,i);
				} else if (imageSize[0] == det.getNumberOfPixelsY()/i && imageSize[1] == det.getNumberOfPixelsX()/i){
					return rotateDetector(binDetector(det,i));
				}
			}
		}

		return null;
	}

	private static DiffractionDetector rotateDetector(DiffractionDetector detector) {
		DiffractionDetector det = new DiffractionDetector();
		det.setDetectorName(detector.getDetectorName() +" rotated");
		det.setNumberOfPixelsX(detector.getNumberOfPixelsY());
		det.setNumberOfPixelsY(detector.getNumberOfPixelsX());
		det.setXPixelMM(detector.getYPixelMM());
		det.setYPixelMM(detector.getXPixelMM());

		return det;
	}

	private static DiffractionDetector binDetector(DiffractionDetector detector, int binning) {
		DiffractionDetector det = new DiffractionDetector();
		det.setDetectorName(detector.getDetectorName() +" binned");
		det.setXPixelMM(detector.getXPixelMM()*binning);
		det.setYPixelMM(detector.getYPixelMM()*binning);
		det.setNumberOfPixelsY(detector.getNumberOfPixelsY()/binning);
		det.setNumberOfPixelsX(detector.getNumberOfPixelsX()/binning);

		return det;
	}
}
/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.preference.detector;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;

import javax.measure.unit.SI;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jscience.physics.amount.Amount;

public class DiffractionDetectorPreferenceInitializer extends AbstractPreferenceInitializer  {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getPlottingPreferenceStore();
		
		DiffractionDetectors dds = new DiffractionDetectors();
		
		// Pilatus 2M
		DiffractionDetector dd = new DiffractionDetector();
		dd.setDetectorName("Pilatus2m");
		dd.setxPixelSize(Amount.valueOf(0.172, SI.MILLIMETRE));
		dd.setyPixelSize(Amount.valueOf(0.172, SI.MILLIMETRE));
		dd.setNumberOfPixelsX(1475);
		dd.setNumberOfPixelsY(1679);
		dds.addDiffractionDetector(dd);
		dds.setDiffractionDetector(dd);
		
		// Pilatus 6M
		DiffractionDetector dd2 = new DiffractionDetector();
		dd2.setDetectorName("Pilatus6m");
		dd2.setxPixelSize(Amount.valueOf(0.172, SI.MILLIMETRE));
		dd2.setyPixelSize(Amount.valueOf(0.172, SI.MILLIMETRE));
		dd2.setNumberOfPixelsX(2527);
		dd2.setNumberOfPixelsY(2463);
		dds.addDiffractionDetector(dd2);
		
		// Pilatus 100k
		DiffractionDetector dd3 = new DiffractionDetector();
		dd3.setDetectorName("Pilatus100k");
		dd3.setxPixelSize(Amount.valueOf(0.172, SI.MILLIMETRE));
		dd3.setyPixelSize(Amount.valueOf(0.172, SI.MILLIMETRE));
		dd3.setNumberOfPixelsX(195);
		dd3.setNumberOfPixelsY(487);
		dds.addDiffractionDetector(dd3);

		// Pilatus 300k
		DiffractionDetector dd4 = new DiffractionDetector();
		dd4.setDetectorName("Pilatus300k");
		dd4.setxPixelSize(Amount.valueOf(0.172, SI.MILLIMETRE));
		dd4.setyPixelSize(Amount.valueOf(0.172, SI.MILLIMETRE));
		dd4.setNumberOfPixelsX(619);
		dd4.setNumberOfPixelsY(487);
		dds.addDiffractionDetector(dd4);
		
		// Pilatus 300k-W
		DiffractionDetector dd4w = new DiffractionDetector();
		dd4w.setDetectorName("Pilatus300k-W");
		dd4w.setxPixelSize(Amount.valueOf(0.172, SI.MILLIMETRE));
		dd4w.setyPixelSize(Amount.valueOf(0.172, SI.MILLIMETRE));
		dd4w.setNumberOfPixelsX(195);
		dd4w.setNumberOfPixelsY(1495); // 3*487 + (3-1)*17
		dds.addDiffractionDetector(dd4w);
		
		// Pixium RF4343
		DiffractionDetector dd5 = new DiffractionDetector();
		dd5.setDetectorName("Pixium RF4343");
		dd5.setxPixelSize(Amount.valueOf(0.148, SI.MILLIMETRE));
		dd5.setyPixelSize(Amount.valueOf(0.148, SI.MILLIMETRE));
		dd5.setNumberOfPixelsX(2880);
		dd5.setNumberOfPixelsY(2881);
		dds.addDiffractionDetector(dd5);
		
		// Perkin Elmer 1621 EN
		DiffractionDetector dd6 = new DiffractionDetector();
		dd6.setDetectorName("Perkin Elmer 1621 EN");
		dd6.setxPixelSize(Amount.valueOf(0.2, SI.MILLIMETRE));
		dd6.setyPixelSize(Amount.valueOf(0.2, SI.MILLIMETRE));
		dd6.setNumberOfPixelsX(2048);
		dd6.setNumberOfPixelsY(2048);
		dds.addDiffractionDetector(dd6);
		
		// ADSC Q315r 
		DiffractionDetector dd7 = new DiffractionDetector();
		dd7.setDetectorName("ADSC Q315r");
		dd7.setxPixelSize(Amount.valueOf(0.1026, SI.MILLIMETRE));
		dd7.setyPixelSize(Amount.valueOf(0.1026, SI.MILLIMETRE));
		dd7.setNumberOfPixelsX(3072);
		dd7.setNumberOfPixelsY(3072);
		dds.addDiffractionDetector(dd7);
		
		// MAR 345 image plate
		DiffractionDetector dd8 = new DiffractionDetector();
		dd8.setDetectorName("MAR 345 image plate");
		dd8.setxPixelSize(Amount.valueOf(0.1000, SI.MILLIMETRE));
		dd8.setyPixelSize(Amount.valueOf(0.1000, SI.MILLIMETRE));
		dd8.setNumberOfPixelsX(3450);
		dd8.setNumberOfPixelsY(3450);
		dds.addDiffractionDetector(dd8);
		
		// Photonic Science X-ray FDI-VHR 125mm
		DiffractionDetector dd9 = new DiffractionDetector();
		dd9.setDetectorName("Photonic Science X-ray FDI-VHR 125mm");
		dd9.setxPixelSize(Amount.valueOf(26.0, SI.MICRO(SI.METRE)));
		dd9.setyPixelSize(Amount.valueOf(26.0, SI.MICRO(SI.METRE)));
		dd9.setNumberOfPixelsX(4008);
		dd9.setNumberOfPixelsY(2663);
		dds.addDiffractionDetector(dd9);
		
		DiffractionDetector dd10 = new DiffractionDetector();
		dd10.setDetectorName("PLS CMOS");
		dd10.setxPixelSize(Amount.valueOf(25.3, SI.MICRO(SI.METRE)));
		dd10.setyPixelSize(Amount.valueOf(25.3, SI.MICRO(SI.METRE)));
		dd10.setNumberOfPixelsX(4098);
		dd10.setNumberOfPixelsY(2045);
		dds.addDiffractionDetector(dd10);
		
		DiffractionDetector dd11 = new DiffractionDetector();
		dd11.setDetectorName("imXPAD-S70");
		dd11.setxPixelSize(Amount.valueOf(130, SI.MICRO(SI.METRE)));
		dd11.setyPixelSize(Amount.valueOf(130, SI.MICRO(SI.METRE)));
		dd11.setNumberOfPixelsX(1114);
		dd11.setNumberOfPixelsY(1164);
		dds.addDiffractionDetector(dd11);
		
		DiffractionDetector dd12 = new DiffractionDetector();
		dd12.setDetectorName("Rayonix LX255-HS");
		dd12.setxPixelSize(Amount.valueOf(40, SI.MICRO(SI.METRE)));
		dd12.setyPixelSize(Amount.valueOf(40, SI.MICRO(SI.METRE)));
		dd12.setNumberOfPixelsX(1920);
		dd12.setNumberOfPixelsY(5760);
		dds.addDiffractionDetector(dd12);
		
		DiffractionDetector dd14 = new DiffractionDetector();
		dd14.setDetectorName("Perkin Elmer 1611");
		dd14.setxPixelSize(Amount.valueOf(100, SI.MICRO(SI.METRE)));
		dd14.setyPixelSize(Amount.valueOf(100, SI.MICRO(SI.METRE)));
		dd14.setNumberOfPixelsX(4096);
		dd14.setNumberOfPixelsY(4096);
		dds.addDiffractionDetector(dd14);
		
		DiffractionDetector dd13 = new DiffractionDetector();
		dd13.setDetectorName("ADSC 210r CCD");
		dd13.setxPixelSize(Amount.valueOf(51, SI.MICRO(SI.METRE)));
		dd13.setyPixelSize(Amount.valueOf(51, SI.MICRO(SI.METRE)));
		dd13.setNumberOfPixelsX(4096);
		dd13.setNumberOfPixelsY(4096);
		dds.addDiffractionDetector(dd13);
		
		DiffractionDetector dd15 = new DiffractionDetector();
		dd13.setDetectorName("Perkin Elmer XRD 4343 CT");
		dd13.setxPixelSize(Amount.valueOf(150, SI.MICRO(SI.METRE)));
		dd13.setyPixelSize(Amount.valueOf(150, SI.MICRO(SI.METRE)));
		dd13.setNumberOfPixelsX(2880);
		dd13.setNumberOfPixelsY(2880);
		dds.addDiffractionDetector(dd15);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XMLEncoder xmlEncoder = new XMLEncoder(baos);
		xmlEncoder.writeObject(dds);
		xmlEncoder.close();
		
		store.setDefault(DiffractionDetectorConstants.DETECTOR, baos.toString());
		
	}

}

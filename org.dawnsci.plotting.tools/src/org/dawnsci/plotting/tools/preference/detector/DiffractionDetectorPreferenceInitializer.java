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
import java.util.Arrays;
import java.util.List;

import org.dawnsci.plotting.tools.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.dawnsci.analysis.api.unit.UnitUtils;
import org.eclipse.jface.preference.IPreferenceStore;

import tec.units.indriya.quantity.Quantities;

public class DiffractionDetectorPreferenceInitializer extends AbstractPreferenceInitializer  {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getPlottingPreferenceStore();

		DiffractionDetectors dds = getDefaultDetectors();
		store.setDefault(DiffractionDetectorConstants.DETECTOR, dds.toSerializedString());
	}

	public static DiffractionDetectors getDefaultDetectors() {
		DiffractionDetectors dds = new DiffractionDetectors();

		// Pilatus 2M
		DiffractionDetector dd0 = new DiffractionDetector();
		dd0.setDetectorName("Pilatus P3-2M");
		dd0.setxPixelSize(Quantities.getQuantity(0.172, UnitUtils.MILLIMETRE));
		dd0.setyPixelSize(Quantities.getQuantity(0.172, UnitUtils.MILLIMETRE));
		dd0.setNumberOfPixelsX(1475);
		dd0.setNumberOfPixelsY(1679);
		dd0.setNumberOfHorizontalModules(3);
		dd0.setNumberOfVerticalModules(8);
		dd0.setXGap(7);
		dd0.setYGap(17);

		dds.addDiffractionDetector(dd0);
		dds.setDiffractionDetector(dd0);

		// Pilatus 2M for WAXS - Pilatus 2M with 3 modules missing
		DiffractionDetector dd1 = new DiffractionDetector();
		dd1.setDetectorName("Pilatus P3-2M-DLS-L (for WAXS)");
		dd1.setxPixelSize(Quantities.getQuantity(0.172, UnitUtils.MILLIMETRE));
		dd1.setyPixelSize(Quantities.getQuantity(0.172, UnitUtils.MILLIMETRE));
		dd1.setNumberOfPixelsX(1475);
		dd1.setNumberOfPixelsY(1679);
		dd1.setNumberOfHorizontalModules(3);
		dd1.setNumberOfVerticalModules(8);
		dd1.setXGap(7);
		dd1.setYGap(17);
		List<Integer> missingModules = new ArrayList<>();
		missingModules.addAll(Arrays.asList(17, 20, 23));
		dd1.setMissingModules(missingModules);
		dds.addDiffractionDetector(dd1);

		// Pilatus 6M
		DiffractionDetector dd2 = new DiffractionDetector();
		dd2.setDetectorName("Pilatus6m");
		dd2.setxPixelSize(Quantities.getQuantity(0.172, UnitUtils.MILLIMETRE));
		dd2.setyPixelSize(Quantities.getQuantity(0.172, UnitUtils.MILLIMETRE));
		dd2.setNumberOfPixelsX(2527);
		dd2.setNumberOfPixelsY(2463);
		dd2.setNumberOfHorizontalModules(12);
		dd2.setNumberOfVerticalModules(5);
		dd2.setXGap(17);
		dd2.setYGap(7);
		dds.addDiffractionDetector(dd2);

		// Pilatus 100k
		DiffractionDetector dd3 = new DiffractionDetector();
		dd3.setDetectorName("Pilatus100k");
		dd3.setxPixelSize(Quantities.getQuantity(0.172, UnitUtils.MILLIMETRE));
		dd3.setyPixelSize(Quantities.getQuantity(0.172, UnitUtils.MILLIMETRE));
		dd3.setNumberOfPixelsX(195);
		dd3.setNumberOfPixelsY(487);
		dd3.setNumberOfHorizontalModules(1);
		dd3.setNumberOfVerticalModules(1);
		dd3.setXGap(0);
		dd3.setYGap(0);
		dds.addDiffractionDetector(dd3);

		// Pilatus 300k
		DiffractionDetector dd4 = new DiffractionDetector();
		dd4.setDetectorName("Pilatus300k");
		dd4.setxPixelSize(Quantities.getQuantity(0.172, UnitUtils.MILLIMETRE));
		dd4.setyPixelSize(Quantities.getQuantity(0.172, UnitUtils.MILLIMETRE));
		dd4.setNumberOfPixelsX(619);
		dd4.setNumberOfPixelsY(487);
		dd4.setNumberOfHorizontalModules(3);
		dd4.setNumberOfVerticalModules(1);
		dd4.setXGap(17);
		dd4.setYGap(0);
		dds.addDiffractionDetector(dd4);
		
		// Pilatus 300k-W
		DiffractionDetector dd4w = new DiffractionDetector();
		dd4w.setDetectorName("Pilatus300k-W");
		dd4w.setxPixelSize(Quantities.getQuantity(0.172, UnitUtils.MILLIMETRE));
		dd4w.setyPixelSize(Quantities.getQuantity(0.172, UnitUtils.MILLIMETRE));
		dd4w.setNumberOfPixelsX(195);
		dd4w.setNumberOfPixelsY(1495); // 3*487 + (3-1)*17
		dd4w.setNumberOfHorizontalModules(1);
		dd4w.setNumberOfVerticalModules(3);
		dd4w.setXGap(0);
		dd4w.setYGap(7);
		dds.addDiffractionDetector(dd4w);

		// Pixium RF4343
		DiffractionDetector dd5 = new DiffractionDetector();
		dd5.setDetectorName("Pixium RF4343");
		dd5.setxPixelSize(Quantities.getQuantity(0.148, UnitUtils.MILLIMETRE));
		dd5.setyPixelSize(Quantities.getQuantity(0.148, UnitUtils.MILLIMETRE));
		dd5.setNumberOfPixelsX(2880);
		dd5.setNumberOfPixelsY(2881);
		dds.addDiffractionDetector(dd5);

		// Perkin Elmer 1621 EN
		DiffractionDetector dd6 = new DiffractionDetector();
		dd6.setDetectorName("Perkin Elmer 1621 EN");
		dd6.setxPixelSize(Quantities.getQuantity(0.2, UnitUtils.MILLIMETRE));
		dd6.setyPixelSize(Quantities.getQuantity(0.2, UnitUtils.MILLIMETRE));
		dd6.setNumberOfPixelsX(2048);
		dd6.setNumberOfPixelsY(2048);
		dds.addDiffractionDetector(dd6);

		// ADSC Q315r 
		DiffractionDetector dd7 = new DiffractionDetector();
		dd7.setDetectorName("ADSC Q315r");
		dd7.setxPixelSize(Quantities.getQuantity(0.1026, UnitUtils.MILLIMETRE));
		dd7.setyPixelSize(Quantities.getQuantity(0.1026, UnitUtils.MILLIMETRE));
		dd7.setNumberOfPixelsX(3072);
		dd7.setNumberOfPixelsY(3072);
		dds.addDiffractionDetector(dd7);

		// MAR 345 image plate
		DiffractionDetector dd8 = new DiffractionDetector();
		dd8.setDetectorName("MAR 345 image plate");
		dd8.setxPixelSize(Quantities.getQuantity(0.1000, UnitUtils.MILLIMETRE));
		dd8.setyPixelSize(Quantities.getQuantity(0.1000, UnitUtils.MILLIMETRE));
		dd8.setNumberOfPixelsX(3450);
		dd8.setNumberOfPixelsY(3450);
		dds.addDiffractionDetector(dd8);

		// Photonic Science X-ray FDI-VHR 125mm
		DiffractionDetector dd9 = new DiffractionDetector();
		dd9.setDetectorName("Photonic Science X-ray FDI-VHR 125mm");
		dd9.setxPixelSize(Quantities.getQuantity(26.0, UnitUtils.MICROMETRE));
		dd9.setyPixelSize(Quantities.getQuantity(26.0, UnitUtils.MICROMETRE));
		dd9.setNumberOfPixelsX(4008);
		dd9.setNumberOfPixelsY(2663);
		dds.addDiffractionDetector(dd9);

		DiffractionDetector dd10 = new DiffractionDetector();
		dd10.setDetectorName("PLS CMOS");
		dd10.setxPixelSize(Quantities.getQuantity(25.3, UnitUtils.MICROMETRE));
		dd10.setyPixelSize(Quantities.getQuantity(25.3, UnitUtils.MICROMETRE));
		dd10.setNumberOfPixelsX(4098);
		dd10.setNumberOfPixelsY(2045);
		dds.addDiffractionDetector(dd10);

		DiffractionDetector dd11 = new DiffractionDetector();
		dd11.setDetectorName("imXPAD-S70");
		dd11.setxPixelSize(Quantities.getQuantity(130, UnitUtils.MICROMETRE));
		dd11.setyPixelSize(Quantities.getQuantity(130, UnitUtils.MICROMETRE));
		dd11.setNumberOfPixelsX(1114);
		dd11.setNumberOfPixelsY(1164);
		dds.addDiffractionDetector(dd11);

		DiffractionDetector dd12 = new DiffractionDetector();
		dd12.setDetectorName("Rayonix LX255-HS");
		dd12.setxPixelSize(Quantities.getQuantity(40, UnitUtils.MICROMETRE));
		dd12.setyPixelSize(Quantities.getQuantity(40, UnitUtils.MICROMETRE));
		dd12.setNumberOfPixelsX(1920);
		dd12.setNumberOfPixelsY(5760);
		dds.addDiffractionDetector(dd12);

		DiffractionDetector dd14 = new DiffractionDetector();
		dd14.setDetectorName("Perkin Elmer 1611");
		dd14.setxPixelSize(Quantities.getQuantity(100, UnitUtils.MICROMETRE));
		dd14.setyPixelSize(Quantities.getQuantity(100, UnitUtils.MICROMETRE));
		dd14.setNumberOfPixelsX(4096);
		dd14.setNumberOfPixelsY(4096);
		dds.addDiffractionDetector(dd14);

		DiffractionDetector dd13 = new DiffractionDetector();
		dd13.setDetectorName("ADSC 210r CCD");
		dd13.setxPixelSize(Quantities.getQuantity(51, UnitUtils.MICROMETRE));
		dd13.setyPixelSize(Quantities.getQuantity(51, UnitUtils.MICROMETRE));
		dd13.setNumberOfPixelsX(4096);
		dd13.setNumberOfPixelsY(4096);
		dds.addDiffractionDetector(dd13);

		DiffractionDetector dd15 = new DiffractionDetector();
		dd15.setDetectorName("Perkin Elmer XRD 4343 CT");
		dd15.setxPixelSize(Quantities.getQuantity(150, UnitUtils.MICROMETRE));
		dd15.setyPixelSize(Quantities.getQuantity(150, UnitUtils.MICROMETRE));
		dd15.setNumberOfPixelsX(2880);
		dd15.setNumberOfPixelsY(2880);
		dds.addDiffractionDetector(dd15);

		DiffractionDetector dd16 = new DiffractionDetector();
		dd16.setDetectorName("EXCALIBUR");
		dd16.setxPixelSize(Quantities.getQuantity(55, UnitUtils.MICROMETRE));
		dd16.setyPixelSize(Quantities.getQuantity(55, UnitUtils.MICROMETRE));
		dd16.setNumberOfPixelsX(1796);
		dd16.setNumberOfPixelsY(2069);
		dds.addDiffractionDetector(dd16);

		DiffractionDetector dd17 = new DiffractionDetector();
		dd17.setDetectorName("Eiger 500k");
		dd17.setxPixelSize(Quantities.getQuantity(75, UnitUtils.MICROMETRE));
		dd17.setyPixelSize(Quantities.getQuantity(75, UnitUtils.MICROMETRE));
		dd17.setNumberOfPixelsX(1030);
		dd17.setNumberOfPixelsY(514);
		dds.addDiffractionDetector(dd17);

		DiffractionDetector dd18 = new DiffractionDetector();
		dd18.setDetectorName("Eiger 1M");
		dd18.setxPixelSize(Quantities.getQuantity(75, UnitUtils.MICROMETRE));
		dd18.setyPixelSize(Quantities.getQuantity(75, UnitUtils.MICROMETRE));
		dd18.setNumberOfPixelsX(1030);
		dd18.setNumberOfPixelsY(1065);
		dds.addDiffractionDetector(dd18);

		DiffractionDetector dd19 = new DiffractionDetector();
		dd19.setDetectorName("Eiger 4M");
		dd19.setxPixelSize(Quantities.getQuantity(75, UnitUtils.MICROMETRE));
		dd19.setyPixelSize(Quantities.getQuantity(75, UnitUtils.MICROMETRE));
		dd19.setNumberOfPixelsX(2070);
		dd19.setNumberOfPixelsY(2167);
		dds.addDiffractionDetector(dd19);

		DiffractionDetector dd20 = new DiffractionDetector();
		dd20.setDetectorName("Eiger 9M");
		dd20.setxPixelSize(Quantities.getQuantity(75, UnitUtils.MICROMETRE));
		dd20.setyPixelSize(Quantities.getQuantity(75, UnitUtils.MICROMETRE));
		dd20.setNumberOfPixelsX(3110);
		dd20.setNumberOfPixelsY(3269);
		dds.addDiffractionDetector(dd20);

		DiffractionDetector dd21 = new DiffractionDetector();
		dd21.setDetectorName("Eiger 16M");
		dd21.setxPixelSize(Quantities.getQuantity(75, UnitUtils.MICROMETRE));
		dd21.setyPixelSize(Quantities.getQuantity(75, UnitUtils.MICROMETRE));
		dd21.setNumberOfPixelsX(4150);
		dd21.setNumberOfPixelsY(4371);
		dds.addDiffractionDetector(dd21);

		// MAR 165 CCD Camera
		DiffractionDetector dd22 = new DiffractionDetector();
		dd22.setDetectorName("MAR 165 CCD");
		dd22.setxPixelSize(Quantities.getQuantity(15, UnitUtils.MICROMETRE));
		dd22.setyPixelSize(Quantities.getQuantity(15, UnitUtils.MICROMETRE));
		dd22.setNumberOfPixelsX(4096);
		dd22.setNumberOfPixelsY(4096);
		dds.addDiffractionDetector(dd22);

		return dds;
	}
}

package org.dawnsci.plotting.preference.detector;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;

import javax.measure.unit.SI;

import org.dawnsci.plotting.Activator;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jscience.physics.amount.Amount;

public class DiffractionDetectorPreferenceInitializer extends AbstractPreferenceInitializer  {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		DiffractionDetectors dds = new DiffractionDetectors();
		
		//Pilatus 2M
		DiffractionDetector dd = new DiffractionDetector();
		dd.setDetectorName("Pilatus2m");
		dd.setxPixelSize(Amount.valueOf(0.172, SI.MILLIMETER));
		dd.setyPixelSize(Amount.valueOf(0.172, SI.MILLIMETER));
		dd.setNumberOfPixelsX(1679);
		dd.setNumberOfPixelsY(1475);
		dds.addDiffractionDetector(dd);
		dds.setDiffractionDetector(dd);
		
		//Pilatus 2M
		DiffractionDetector dd2 = new DiffractionDetector();
		dd2.setDetectorName("Pilatus6m");
		dd2.setxPixelSize(Amount.valueOf(0.172, SI.MILLIMETER));
		dd2.setyPixelSize(Amount.valueOf(0.172, SI.MILLIMETER));
		dd2.setNumberOfPixelsX(2463);
		dd2.setNumberOfPixelsY(2527);
		dds.addDiffractionDetector(dd2);
		
		//Pilatus100k
		DiffractionDetector dd3 = new DiffractionDetector();
		dd3.setDetectorName("Pilatus100k");
		dd3.setxPixelSize(Amount.valueOf(0.172, SI.MILLIMETER));
		dd3.setyPixelSize(Amount.valueOf(0.172, SI.MILLIMETER));
		dd3.setNumberOfPixelsX(195);
		dd3.setNumberOfPixelsY(487);
		dds.addDiffractionDetector(dd3);
		
		//Pilatus100k
		DiffractionDetector dd4 = new DiffractionDetector();
		dd4.setDetectorName("Pilatus300k");
		dd4.setxPixelSize(Amount.valueOf(0.172, SI.MILLIMETER));
		dd4.setyPixelSize(Amount.valueOf(0.172, SI.MILLIMETER));
		dd4.setNumberOfPixelsX(487);
		dd4.setNumberOfPixelsY(619);
		dds.addDiffractionDetector(dd4);
		
		//Pixium RF4343
		DiffractionDetector dd5 = new DiffractionDetector();
		dd5.setDetectorName("Pixium RF4343");
		dd5.setxPixelSize(Amount.valueOf(0.148, SI.MILLIMETER));
		dd5.setyPixelSize(Amount.valueOf(0.148, SI.MILLIMETER));
		dd5.setNumberOfPixelsX(2880);
		dd5.setNumberOfPixelsY(2881);
		dds.addDiffractionDetector(dd5);
		
		//Perkin Elmer 1621 EN
		DiffractionDetector dd6 = new DiffractionDetector();
		dd6.setDetectorName("Perkin Elmer 1621 EN");
		dd6.setxPixelSize(Amount.valueOf(0.2, SI.MILLIMETER));
		dd6.setyPixelSize(Amount.valueOf(0.2, SI.MILLIMETER));
		dd6.setNumberOfPixelsX(2048);
		dd6.setNumberOfPixelsY(2048);
		dds.addDiffractionDetector(dd6);
		
		//ADSC 	Q315r 
		DiffractionDetector dd7 = new DiffractionDetector();
		dd7.setDetectorName("ADSC Q315r");
		dd7.setxPixelSize(Amount.valueOf(0.1026, SI.MILLIMETER));
		dd7.setyPixelSize(Amount.valueOf(0.1026, SI.MILLIMETER));
		dd7.setNumberOfPixelsX(3072);
		dd7.setNumberOfPixelsY(3072);
		dds.addDiffractionDetector(dd7);
		
		//MAR 345 image plate
		DiffractionDetector dd8 = new DiffractionDetector();
		dd8.setDetectorName("MAR 345 image plate");
		dd8.setxPixelSize(Amount.valueOf(0.1000, SI.MILLIMETER));
		dd8.setyPixelSize(Amount.valueOf(0.1000, SI.MILLIMETER));
		dd8.setNumberOfPixelsX(3450);
		dd8.setNumberOfPixelsY(3450);
		dds.addDiffractionDetector(dd8);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XMLEncoder xmlEncoder = new XMLEncoder(baos);
		xmlEncoder.writeObject(dds);
		xmlEncoder.close();
		
		store.setDefault(DiffractionDetectorConstants.DETECTOR, baos.toString());
		
	}

}

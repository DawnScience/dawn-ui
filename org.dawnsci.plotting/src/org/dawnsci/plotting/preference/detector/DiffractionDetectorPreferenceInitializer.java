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
		DiffractionDetector dd = new DiffractionDetector();
		dd.setDetectorName("Default Detector");
		dd.setxPixelSize(Amount.valueOf(0.125, SI.MILLIMETER));
		dd.setyPixelSize(Amount.valueOf(0.125, SI.MILLIMETER));
		dds.addDiffractionDetector(dd);
		dds.setDiffractionDetector(dd);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XMLEncoder xmlEncoder = new XMLEncoder(baos);
		xmlEncoder.writeObject(dds);
		xmlEncoder.close();
		
		store.setDefault(DiffractionDetectorConstants.DETECTOR, baos.toString());
		
	}

}

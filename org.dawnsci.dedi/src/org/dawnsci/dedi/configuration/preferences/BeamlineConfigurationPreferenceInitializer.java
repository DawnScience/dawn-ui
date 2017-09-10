package org.dawnsci.dedi.configuration.preferences;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.unit.SI;

import org.dawnsci.dedi.Activator;
import org.dawnsci.plotting.tools.preference.detector.DiffractionDetector;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jscience.physics.amount.Amount;

public class BeamlineConfigurationPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		BeamlineConfigurations beamlineConfigurations = getDefaultBeamlineConfigurations(); 
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XMLEncoder xmlEncoder = new XMLEncoder(baos);
		xmlEncoder.writeObject(beamlineConfigurations);
		xmlEncoder.close();
		
		store.setDefault(PreferenceConstants.BEAMLINE_CONFIGURATION, baos.toString());
		
		BeamlineConfigurationPreferenceHelper.addDetectorPropertyChangeListener(e -> initializeDefaultPreferences());
	}
	
	
	public static BeamlineConfigurations getDefaultBeamlineConfigurations(){
		List<DiffractionDetector> detectors = BeamlineConfigurationPreferenceHelper.getDetectorsListFromPreference();
		BeamlineConfigurations beamlineConfigurations = new BeamlineConfigurations(); 
		
		BeamlineConfigurationBean bc1 = new BeamlineConfigurationBean();
		bc1.setName("I22 SAXS");
		bc1.setBeamstopDiameter(4);
		bc1.setBeamstopXCentre(737.5);
		bc1.setBeamstopYCentre(839.5);
		bc1.setCameraTubeDiameter(350);
		bc1.setCameraTubeXCentre(737.5);
		bc1.setCameraTubeYCentre(839.5);
		bc1.setClearance(10);
		bc1.setMinWavelength(0.062);
		bc1.setMaxWavelength(0.3351);
		bc1.setMinCameraLength(1.9);
		bc1.setMaxCameraLength(9.9);
		bc1.setCameraLengthStepSize(0.25);
		
		
		BeamlineConfigurationBean bc2 = new BeamlineConfigurationBean();
		bc2.setName("I22 WAXS");
		bc2.setBeamstopDiameter(4);
		bc2.setBeamstopXCentre(737.5);
		bc2.setBeamstopYCentre(839.5);
		bc2.setCameraTubeDiameter(0);  // Set the camera tube diameter to 0 to "disable" the camera tube.
		bc2.setCameraTubeXCentre(0);
		bc2.setCameraTubeYCentre(0);
		bc2.setClearance(10);
		bc2.setMinWavelength(0.062);
		bc2.setMaxWavelength(0.3351);
		bc2.setMinCameraLength(0.18);
		bc2.setMaxCameraLength(0.58);
		bc2.setCameraLengthStepSize(0.01);
		
		
		// Pilatus 2M - the default detector for bc1
		DiffractionDetector dd = new DiffractionDetector();
		dd.setDetectorName("Pilatus P3-2M");
		dd.setxPixelSize(Amount.valueOf(0.172, SI.MILLIMETRE));
		dd.setyPixelSize(Amount.valueOf(0.172, SI.MILLIMETRE));
		dd.setNumberOfPixelsX(1475);
		dd.setNumberOfPixelsY(1679);
		dd.setNumberOfHorizontalModules(3);
		dd.setNumberOfVerticalModules(8);
		dd.setXGap(7);
		dd.setYGap(17);
		
		
		if(detectors != null){
			int index = detectors.indexOf(dd);
			if(index != -1){
				bc1.setDetector(detectors.get(index));
			} else if (!detectors.isEmpty()){
				bc1.setDetector(detectors.get(0));
			}
		}
		
		
		// Pilatus 2M for WAXS - the default detector for bc2
		DiffractionDetector dd2 = new DiffractionDetector();
		dd2.setDetectorName("Pilatus P3-2M-DLS-L (for WAXS)");
		dd2.setxPixelSize(Amount.valueOf(0.172, SI.MILLIMETRE));
		dd2.setyPixelSize(Amount.valueOf(0.172, SI.MILLIMETRE));
		dd2.setNumberOfPixelsX(1475);
		dd2.setNumberOfPixelsY(1679);
		dd2.setNumberOfHorizontalModules(3);
		dd2.setNumberOfVerticalModules(8);
		dd2.setXGap(7);
		dd2.setYGap(17);
		List<Integer> missingModules = new ArrayList<>();
		missingModules.addAll(Arrays.asList(17, 20, 23));
		dd2.setMissingModules(missingModules);
		// Doing it this way because Arrays.asList() returns Arrays.ArrayList, not java.util.ArrayList,
		// so need to convert it somehow, as setMissingModules(Arrays.asList(17, 20, 23)) was causing the following error 
		// when creating the XML representation with XMLEncoder: java.lang.InstantiationException: java.util.Arrays$ArrayList.
		
		
		if(detectors != null){
			int index = detectors.indexOf(dd2);
			if(index != -1){
				bc2.setDetector(detectors.get(index));
			} else if (!detectors.isEmpty()){
				bc2.setDetector(detectors.get(0));
			}
		}
		
		
		
		beamlineConfigurations.addBeamlineConfiguration(bc1);
		beamlineConfigurations.setBeamlineConfiguration(bc1);
		beamlineConfigurations.addBeamlineConfiguration(bc2);
		
		return beamlineConfigurations;
	}
}

package org.dawnsci.dedi.configuration.preferences;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dawnsci.dedi.Activator;
import org.dawnsci.plotting.tools.preference.detector.DiffractionDetector;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.dawnsci.analysis.api.unit.UnitUtils;
import org.eclipse.jface.preference.IPreferenceStore;

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
	}
	
	
	public static BeamlineConfigurations getDefaultBeamlineConfigurations(){
		List<DiffractionDetector> detectors = BeamlineConfigurationPreferenceHelper.getDetectorsListFromPreference();
		BeamlineConfigurations beamlineConfigurations = new BeamlineConfigurations(); 
		
		BeamlineConfigurationBean bc1 = new BeamlineConfigurationBean();
		bc1.setName("I22 SAXS Anisotropic");
		bc1.setBeamstopDiameter(4);
		bc1.setBeamstopXCentre(738);
		bc1.setBeamstopYCentre(840);
		bc1.setCameraTubeDiameter(310);
		bc1.setCameraTubeXCentre(738);
		bc1.setCameraTubeYCentre(840);
		bc1.setClearance(10);
		bc1.setMinWavelength(0.062);
		bc1.setMaxWavelength(0.3351);
		bc1.setMinCameraLength(1.9);
		bc1.setMaxCameraLength(9.9);
		bc1.setCameraLengthStepSize(0.25);
		
		
		BeamlineConfigurationBean bc2 = new BeamlineConfigurationBean();
		bc2.setName("I22 SAXS Isotropic");
		bc2.setBeamstopDiameter(4);
		bc2.setBeamstopXCentre(738);
		bc2.setBeamstopYCentre(100);
		bc2.setCameraTubeDiameter(310);
		bc2.setCameraTubeXCentre(738);
		bc2.setCameraTubeYCentre(840);
		bc2.setClearance(10);
		bc2.setMinWavelength(0.062);
		bc2.setMaxWavelength(0.3351);
		bc2.setMinCameraLength(1.9);
		bc2.setMaxCameraLength(9.9);
		bc2.setCameraLengthStepSize(0.25);
		
		
		BeamlineConfigurationBean bc3 = new BeamlineConfigurationBean();
		bc3.setName("I22 GiSAXS");
		bc3.setBeamstopDiameter(4);
		bc3.setBeamstopXCentre(725);
		bc3.setBeamstopYCentre(1400);
		bc3.setCameraTubeDiameter(310);  // Set the camera tube diameter to 0 to "disable" the camera tube.
		bc3.setCameraTubeXCentre(725);
		bc3.setCameraTubeYCentre(840);
		bc3.setClearance(10);
		bc3.setMinWavelength(0.062);
		bc3.setMaxWavelength(0.3351);
		bc3.setMinCameraLength(0.18);
		bc3.setMaxCameraLength(9.9);
		bc3.setCameraLengthStepSize(0.01);
		
		
		BeamlineConfigurationBean bc4 = new BeamlineConfigurationBean();
		bc4.setName("I22 WAXS");
		bc4.setBeamstopDiameter(4);
		bc4.setBeamstopXCentre(1100);
		bc4.setBeamstopYCentre(1080);
		bc4.setCameraTubeDiameter(0);  // Set the camera tube diameter to 0 to "disable" the camera tube.
		bc4.setCameraTubeXCentre(0);
		bc4.setCameraTubeYCentre(0);
		bc4.setClearance(10);
		bc4.setMinWavelength(0.062);
		bc4.setMaxWavelength(0.3351);
		bc4.setMinCameraLength(0.18);
		bc4.setMaxCameraLength(1.0);
		bc4.setCameraLengthStepSize(0.01);
		
		
		BeamlineConfigurationBean bc5 = new BeamlineConfigurationBean();
		bc5.setName("I22 GiWAXS");
		bc5.setBeamstopDiameter(4);
		bc5.setBeamstopXCentre(1240);
		bc5.setBeamstopYCentre(1600);
		bc5.setCameraTubeDiameter(0);  // Set the camera tube diameter to 0 to "disable" the camera tube.
		bc5.setCameraTubeXCentre(0);
		bc5.setCameraTubeYCentre(0);
		bc5.setClearance(10);
		bc5.setMinWavelength(0.062);
		bc5.setMaxWavelength(0.3351);
		bc5.setMinCameraLength(0.18);
		bc5.setMaxCameraLength(1.0);
		bc5.setCameraLengthStepSize(0.01);
		
		
		// Pilatus 2M - the default detector for bc1
		DiffractionDetector dd = new DiffractionDetector();
		dd.setDetectorName("Pilatus P3-2M");
		dd.setxPixelSize(UnitUtils.getQuantity(0.172, UnitUtils.MILLIMETRE));
		dd.setyPixelSize(UnitUtils.getQuantity(0.172, UnitUtils.MILLIMETRE));
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
				bc2.setDetector(detectors.get(index));
				bc3.setDetector(detectors.get(index));
			} else if (!detectors.isEmpty()){
				bc1.setDetector(detectors.get(0));
				bc2.setDetector(detectors.get(0));
				bc3.setDetector(detectors.get(0));
			}
		}
		
		
		// Pilatus 2M for WAXS - the default detector for bc2
		DiffractionDetector dd2 = new DiffractionDetector();
		dd2.setDetectorName("Pilatus P3-2M-DLS-L (for WAXS)");
		dd2.setxPixelSize(UnitUtils.getQuantity(0.172, UnitUtils.MILLIMETRE));
		dd2.setyPixelSize(UnitUtils.getQuantity(0.172, UnitUtils.MILLIMETRE));
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
				bc4.setDetector(detectors.get(index));
				bc5.setDetector(detectors.get(index));
			} else if (!detectors.isEmpty()){
				bc4.setDetector(detectors.get(0));
				bc5.setDetector(detectors.get(0));
			}
		}
		
		
		beamlineConfigurations.addBeamlineConfiguration(bc1);
		beamlineConfigurations.setBeamlineConfiguration(bc1);
		beamlineConfigurations.addBeamlineConfiguration(bc2);
		beamlineConfigurations.addBeamlineConfiguration(bc3);
		beamlineConfigurations.addBeamlineConfiguration(bc4);
		beamlineConfigurations.addBeamlineConfiguration(bc5);
		
		return beamlineConfigurations;
	}
}
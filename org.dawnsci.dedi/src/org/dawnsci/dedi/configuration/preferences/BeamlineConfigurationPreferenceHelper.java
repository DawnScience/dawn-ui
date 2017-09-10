package org.dawnsci.dedi.configuration.preferences;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.util.List;

import org.dawnsci.dedi.Activator;
import org.dawnsci.plotting.tools.preference.detector.DiffractionDetector;
import org.dawnsci.plotting.tools.preference.detector.DiffractionDetectorConstants;
import org.dawnsci.plotting.tools.preference.detector.DiffractionDetectorPreferenceInitializer;
import org.dawnsci.plotting.tools.preference.detector.DiffractionDetectors;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;

public class BeamlineConfigurationPreferenceHelper {
	private static IPreferenceStore detectorStore = org.dawnsci.plotting.tools.Activator.getPlottingPreferenceStore();
	private static IPreferenceStore beamlineConfigurationsPreferenceStore = Activator.getDefault().getPreferenceStore();
	
	
	private BeamlineConfigurationPreferenceHelper() {
		throw new IllegalStateException("This class is not meant to be instantiated.");
	}
	
	
	public static List<DiffractionDetector> getDetectorsListFromPreference(){
		List<DiffractionDetector> detectors = null;
		
		String xml = detectorStore.getString(DiffractionDetectorConstants.DETECTOR);
		if(xml != null && !xml.equals("")){
			 try{
				 XMLDecoder xmlDecoder = new XMLDecoder(new ByteArrayInputStream(xml.getBytes()));
				 DiffractionDetectors diffDetectors = (DiffractionDetectors) xmlDecoder.readObject();
				 detectors = diffDetectors.getDiffractionDetectors();
				 xmlDecoder.close();
			 } catch(Exception e){
				 e.printStackTrace();
			 }
		 }
		
		 if(detectors != null) return detectors;
		 
		 new DiffractionDetectorPreferenceInitializer().initializeDefaultPreferences();
		 xml = detectorStore.getDefaultString(DiffractionDetectorConstants.DETECTOR);
		 try{
			 XMLDecoder xmlDecoder = new XMLDecoder(new ByteArrayInputStream(xml.getBytes()));
			 DiffractionDetectors diffDetectors = (DiffractionDetectors) xmlDecoder.readObject();
			 detectors = diffDetectors.getDiffractionDetectors();
			 xmlDecoder.close();
		 } catch(Exception e){
			 e.printStackTrace();
		 }
		 
		 if(detectors != null) return detectors;
		 
		 return DiffractionDetectorPreferenceInitializer.getDefaultDetectors().getDiffractionDetectors();
	}
	
	
	public static void addDetectorPropertyChangeListener(IPropertyChangeListener listener){
		detectorStore.addPropertyChangeListener(listener);
	}
	
	
	public static List<BeamlineConfigurationBean> getBeamlineConfigurationsListFromPreferences() {
		List<BeamlineConfigurationBean> beamlineConfigurations = null;
		
		String xml = beamlineConfigurationsPreferenceStore.getString(PreferenceConstants.BEAMLINE_CONFIGURATION);
		if(xml != null && !xml.equals("")){
			try{
				XMLDecoder xmlDecoder = new XMLDecoder(new ByteArrayInputStream(xml.getBytes()));
				BeamlineConfigurations configs = (BeamlineConfigurations) xmlDecoder.readObject();
				beamlineConfigurations = configs.getBeamlineConfigurations();
				xmlDecoder.close();
			}catch(Exception e){
				 e.printStackTrace();
			}
		}
		
		if(beamlineConfigurations != null) return beamlineConfigurations;
		
		xml = beamlineConfigurationsPreferenceStore.getDefaultString(PreferenceConstants.BEAMLINE_CONFIGURATION);
		try{
			XMLDecoder xmlDecoder = new XMLDecoder(new ByteArrayInputStream(xml.getBytes()));
			BeamlineConfigurations configs = (BeamlineConfigurations) xmlDecoder.readObject();
			beamlineConfigurations = configs.getBeamlineConfigurations();
			xmlDecoder.close();
		}catch(Exception e){
			 e.printStackTrace();
		}
		
		if(beamlineConfigurations != null) return beamlineConfigurations;
		
		return BeamlineConfigurationPreferenceInitializer.getDefaultBeamlineConfigurations().getBeamlineConfigurations();
	}
	
	
	public static BeamlineConfigurations getBeamlineConfigurationsFromPreferences(){
		BeamlineConfigurations beamlineConfigurations = null;
		String xml = beamlineConfigurationsPreferenceStore.getString(PreferenceConstants.BEAMLINE_CONFIGURATION);
		try{
			XMLDecoder xmlDecoder = new XMLDecoder(new ByteArrayInputStream(xml.getBytes()));
			beamlineConfigurations = (BeamlineConfigurations) xmlDecoder.readObject();
			xmlDecoder.close();
		}catch(Exception e){
		}
		return beamlineConfigurations;
	}
	
	
	public static void addBeamlineConfigurationPropertyChangeListener(IPropertyChangeListener listener){
		beamlineConfigurationsPreferenceStore.addPropertyChangeListener(listener);
	}
}

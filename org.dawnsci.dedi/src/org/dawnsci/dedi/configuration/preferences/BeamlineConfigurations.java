package org.dawnsci.dedi.configuration.preferences;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A serializable class to hold a list of {@link BeamlineConfigurationBean}s.
 */
public class BeamlineConfigurations implements Serializable {
	private static final long serialVersionUID = 7297838000309018662L;
	
	private List<BeamlineConfigurationBean> beamlineConfigurationBeans;
	private BeamlineConfigurationBean selected;

	public BeamlineConfigurations() {
		beamlineConfigurationBeans = new ArrayList<>();
	}
	
	public List<BeamlineConfigurationBean> getBeamlineConfigurations() {
		return beamlineConfigurationBeans;
	}

	public void setBeamlineConfigurations(List<BeamlineConfigurationBean> beamlineConfigurations) {
		this.beamlineConfigurationBeans = beamlineConfigurations;
	}
	
	public void addBeamlineConfiguration(BeamlineConfigurationBean beamlineConfiguration){
		beamlineConfigurationBeans.add(beamlineConfiguration);
	}
	
	public void removeBeamlineConfiguration(BeamlineConfigurationBean beamlineConfiguration){
		beamlineConfigurationBeans.remove(beamlineConfiguration);
	}
	
	public BeamlineConfigurationBean getSelected(){
		return selected;
	}
	
	public void setBeamlineConfiguration(BeamlineConfigurationBean selected){
		this.selected = selected;
	}
}

package org.dawnsci.dedi.ui.views.configuration;

import java.util.List;
import java.util.Observable;

import org.dawnsci.dedi.configuration.preferences.BeamlineConfigurationBean;
import org.dawnsci.dedi.configuration.preferences.BeamlineConfigurationPreferenceHelper;
import org.dawnsci.dedi.configuration.preferences.PreferenceConstants;
import org.dawnsci.dedi.ui.GuiHelper;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;


public class BeamlineConfigurationTemplatesPanel extends Observable {
	/**
	 * The currently selected beamline configuration template.
	 */
	private BeamlineConfigurationBean beamlineConfigurationTemplate;
	/**
	 * The combo holding the list of predefined beamline configuration templates.
	 */
	private ComboViewer beamlineConfigurationsComboViewer;
	
	private static final String TITLE =  "Beamline configuration templates";
	
	
	public BeamlineConfigurationTemplatesPanel(Composite parent) {
		beamlineConfigurationTemplate = null;
		
		Group beamlineConfigurationGroup = GuiHelper.createGroup(parent, TITLE, 2);
		
		GuiHelper.createLabel(beamlineConfigurationGroup, "Choose a predefined beamline configuration");
		
		/*
		 * Create the combo with the list of predefined beamline configuration templates.
		 */
		Combo beamlineConfigurationsCombo = new Combo(beamlineConfigurationGroup, SWT.READ_ONLY | SWT.H_SCROLL);
		beamlineConfigurationsComboViewer = new ComboViewer(beamlineConfigurationsCombo);
		beamlineConfigurationsComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		
		
        /*
         * Add a handler to the combo.
         */
		beamlineConfigurationsComboViewer.addSelectionChangedListener(event -> {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			    if (selection.size() > 0){
			    	beamlineConfigurationTemplate = (BeamlineConfigurationBean) selection.getFirstElement();
			    	setChanged();
			    	notifyObservers();
			    }
		});
			
		
		/*
		 * Get the list of predefined beamline configuration templates from preferences.
		 */
		getBeamlineTemplatesFromPreferences();
		
		BeamlineConfigurationPreferenceHelper.addBeamlineConfigurationPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if(event.getProperty() == PreferenceConstants.BEAMLINE_CONFIGURATION){
					getBeamlineTemplatesFromPreferences();
					beamlineConfigurationGroup.layout();
				}
				
			}
		});
		
		beamlineConfigurationGroup.layout();
	}

	
	/**
	 * Gets the templates from preferences, sends them to the templates combo and selects the first one (if the list isn't empty).
	 */
	private void getBeamlineTemplatesFromPreferences() {
		List<BeamlineConfigurationBean> beamlineConfigurations = BeamlineConfigurationPreferenceHelper.getBeamlineConfigurationsListFromPreferences();
		beamlineConfigurationsComboViewer.setInput(beamlineConfigurations);
		if(beamlineConfigurations != null && !beamlineConfigurations.isEmpty())
			beamlineConfigurationsComboViewer.setSelection(new StructuredSelection(beamlineConfigurations.get(0)));
	}
	
	
	/**
	 * @return The currently selected beamline configuration template.
	 */
	public BeamlineConfigurationBean getPredefinedBeamlineConfiguration(){
		return beamlineConfigurationTemplate;
	}
}

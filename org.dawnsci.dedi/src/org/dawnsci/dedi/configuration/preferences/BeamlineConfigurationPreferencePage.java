package org.dawnsci.dedi.configuration.preferences;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.dawb.common.ui.util.GridUtils;
import org.dawnsci.dedi.Activator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.richbeans.api.binding.IBeanController;
import org.eclipse.richbeans.api.binding.IBeanService;
import org.eclipse.richbeans.widgets.selector.VerticalListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class BeamlineConfigurationPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private VerticalListEditor beamlineConfigurationEditor;
	private BeamlineConfigurations beamlineConfigurations;
	
	
	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		beamlineConfigurations = BeamlineConfigurationPreferenceHelper.getBeamlineConfigurationsFromPreferences();
		setBean(beamlineConfigurations);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(1, false));
		GridData gdc = new GridData(SWT.FILL, SWT.FILL, true, true);
		main.setLayoutData(gdc);
		
		beamlineConfigurationEditor  = new VerticalListEditor(main, SWT.BORDER);
		Composite beamlineConfigurationComposite = new BeamlineConfigurationsComposite(beamlineConfigurationEditor, SWT.NONE); 
		beamlineConfigurationComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		beamlineConfigurationEditor.setRequireSelectionPack(false);
		beamlineConfigurationEditor.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		beamlineConfigurationEditor.setMinItems(0);
		beamlineConfigurationEditor.setMaxItems(25);
		beamlineConfigurationEditor.setDefaultName("Beamline configuration");
		beamlineConfigurationEditor.setEditorClass(BeamlineConfigurationBean.class);
		beamlineConfigurationEditor.setEditorUI(beamlineConfigurationComposite);
		beamlineConfigurationEditor.setNameField("name");
		beamlineConfigurationEditor.setAdditionalFields(new String[]{"detector", "beamstopDiameter", "beamstopXCentre","beamstopYCentre","clearance", 
				                       "cameraTubeDiameter", "cameraTubeXCentre", "cameraTubeYCentre", "maxWavelength", 
				                       "minWavelength", "minCameraLength", "maxCameraLength", "cameraLengthStepSize"});
		beamlineConfigurationEditor.setColumnWidths(150,150,100,100,100,100,100,100,100,100,100,100,100,100);
		beamlineConfigurationEditor.setColumnNames("Name", "Detector", "Beamstop diameter", "Beamstop x coordinate","Beamstop y coordinate", "Clearance",
												   "Camera tube diameter", "Camera tube x coordinate", "Camera tube y coordinate",
				                                   "Maximum wavelength", "Minimum wavelength", "Minimum camera length", 
				                                   "Maximum camera length", "Step size of the camera length");
		beamlineConfigurationEditor.setColumnFormat("##0.####");
		beamlineConfigurationEditor.setListHeight(150);
		beamlineConfigurationEditor.setAddButtonText("Add beamline configuration");
		beamlineConfigurationEditor.setRemoveButtonText("Remove beamline configuration");

		GridUtils.setVisible(beamlineConfigurationEditor, true);
		beamlineConfigurationEditor.getParent().layout(new Control[]{beamlineConfigurationEditor});

		beamlineConfigurations = BeamlineConfigurationPreferenceHelper.getBeamlineConfigurationsFromPreferences();
		setBean(beamlineConfigurations);
		beamlineConfigurationEditor.setShowAdditionalFields(true);
		return main;
	}
	
	// This MUST have the name "get" + name of the class that is used to store 
	// the list of BeamlineConfigurationBean objects into the preference store.
	// This method is called by the BeanUI when transferring values from the bean into the VerticalListEditor.s
	public VerticalListEditor getBeamlineConfigurations(){
		return beamlineConfigurationEditor;
	}
	
	
	private void setBean(Object bean) {
		if(bean == null) return;
		try {
			IBeanService service = (IBeanService)Activator.getService(IBeanService.class);
			IBeanController controller = service.createController(this, bean);
			controller.beanToUI();
			controller.switchState(true);
			controller.fireValueListeners();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean performOk() {
		try {			
			IBeanService service = (IBeanService)Activator.getService(IBeanService.class);
			IBeanController controller = service.createController(this, beamlineConfigurations);
			controller.uiToBean();
			setBeamlineConfigurationsToPreference();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.performOk();
	}
	
	
	private void setBeamlineConfigurationsToPreference() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XMLEncoder xmlEncoder = new XMLEncoder(baos);
		xmlEncoder.writeObject(beamlineConfigurations);
		xmlEncoder.close();
		getPreferenceStore().setValue(PreferenceConstants.BEAMLINE_CONFIGURATION, baos.toString());
	}
	
	
	@Override
	protected void performDefaults() {
		getDefaultBeamlineConfigurationsFromPreference();
	}
	
	@SuppressWarnings("resource")
	private void getDefaultBeamlineConfigurationsFromPreference() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XMLEncoder xmlEncoder = new XMLEncoder(baos);
		xmlEncoder.writeObject(beamlineConfigurations);
		xmlEncoder.close();
		String xml = getPreferenceStore().getDefaultString(PreferenceConstants.BEAMLINE_CONFIGURATION);
		XMLDecoder xmlDecoder = new XMLDecoder(new ByteArrayInputStream(xml.getBytes()));
		beamlineConfigurations = (BeamlineConfigurations) xmlDecoder.readObject();
		setBeamlineConfigurationsToPreference(); // Don't forget to store the new Preferences (which just happen to be the default preferences).
		setBean(beamlineConfigurations);
	}

}

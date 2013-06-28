package org.dawnsci.plotting.tools.preference.detector;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.dawb.common.ui.util.GridUtils;
import org.dawnsci.plotting.tools.Activator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.richbeans.components.selector.VerticalListEditor;

public class DiffractionDetectorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private DiffractionDetectors detectors;
	private VerticalListEditor detectorEditor;
	public static final String ID = "org.dawnsci.plotting.preference.detector";
	
	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getPlottingPreferenceStore());
		getDetectorsFromPreference();
		
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(1, false));
		GridData gdc = new GridData(SWT.FILL, SWT.FILL, true, true);
		main.setLayoutData(gdc);
		
		detectorEditor  = new VerticalListEditor(main, SWT.BORDER);
		Composite detectorComposite = new DiffractionDetectorComposite(detectorEditor, SWT.NONE); 
		detectorComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		detectorEditor.setRequireSelectionPack(false);
		detectorEditor.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		detectorEditor.setMinItems(0);
		detectorEditor.setMaxItems(25);
		detectorEditor.setDefaultName("Detector");
		detectorEditor.setEditorClass(DiffractionDetector.class);
		// TODO make editor UI
		detectorEditor.setEditorUI(detectorComposite);
		detectorEditor.setNameField("detectorName");
		detectorEditor.setAdditionalFields(new String[]{"XPixelMM","YPixelMM","NumberOfPixelsX","NumberOfPixelsY"});
		detectorEditor.setColumnWidths(80,80,80,80,80);
		detectorEditor.setColumnNames("Name", "X Pixel (mm)", "Y Pixel (mm)","X (pixels)", "Y (pixels");
		detectorEditor.setColumnFormat("##0.####");
		detectorEditor.setListHeight(150);
		detectorEditor.setAddButtonText("Add Detector");
		detectorEditor.setRemoveButtonText("Remove Detector");

		GridUtils.setVisible(detectorEditor, true);
		detectorEditor.getParent().layout(new Control[]{detectorEditor});
		
		
		getDetectorsFromPreference();
		detectorEditor.setShowAdditionalFields(true);
		return main;
	}
	
	public VerticalListEditor getDiffractionDetectors() {
		return detectorEditor;
	}
	
	@Override
	public boolean performOk() {
		try {			
			
			
			BeanUI.uiToBean(this, detectors);
			setDetectorsToPreference();
			
		} catch (Exception e) {
		}
		return super.performOk();
	}
	
	@Override
	protected void performDefaults() {
		getDefaultDetectorsFromPreference();
	}
	
	private void setDetectorsToPreference() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XMLEncoder xmlEncoder = new XMLEncoder(baos);
		xmlEncoder.writeObject(detectors);
		xmlEncoder.close();
		getPreferenceStore().setValue(DiffractionDetectorConstants.DETECTOR, baos.toString());
	}
	
	private void getDetectorsFromPreference() {
		String xml = getPreferenceStore().getString(DiffractionDetectorConstants.DETECTOR);
		XMLDecoder xmlDecoder =new XMLDecoder(new ByteArrayInputStream(xml.getBytes()));
		detectors = (DiffractionDetectors) xmlDecoder.readObject();
		setBean(detectors);

	}
	
	private void getDefaultDetectorsFromPreference() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XMLEncoder xmlEncoder = new XMLEncoder(baos);
		xmlEncoder.writeObject(detectors);
		xmlEncoder.close();
		String xml = getPreferenceStore().getDefaultString(DiffractionDetectorConstants.DETECTOR);
		XMLDecoder xmlDecoder =new XMLDecoder(new ByteArrayInputStream(xml.getBytes()));
		detectors = (DiffractionDetectors) xmlDecoder.readObject();
		setBean(detectors);

	}
	
	public void setBean(Object bean) {
		try {
			BeanUI.beanToUI(bean, this);
			BeanUI.switchState(bean, this, true);
			BeanUI.fireValueListeners(bean, this);
		} catch (Exception e) {
			//logger.error("Cannot send "+bean+" to dialog!", e);
			e.printStackTrace();
		}
	}
}

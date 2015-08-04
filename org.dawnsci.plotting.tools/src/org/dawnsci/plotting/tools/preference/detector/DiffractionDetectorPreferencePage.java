/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.preference.detector;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.dawb.common.ui.util.GridUtils;
import org.dawnsci.plotting.tools.Activator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.richbeans.api.reflection.IBeanController;
import org.eclipse.richbeans.api.reflection.IBeanService;
import org.eclipse.richbeans.widgets.selector.VerticalListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiffractionDetectorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	private static Logger logger = LoggerFactory.getLogger(DiffractionDetectorPreferencePage.class);
	
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
			IBeanService service = (IBeanService)Activator.getService(IBeanService.class);
			IBeanController controller = service.createController(this, detectors);
			controller.uiToBean();
			setDetectorsToPreference();
			
		} catch (Exception e) {
			logger.warn("Internal error, could not merge bean and ui!", e);
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
	
	@SuppressWarnings("resource")
	private void getDetectorsFromPreference() {
		String xml = getPreferenceStore().getString(DiffractionDetectorConstants.DETECTOR);
		XMLDecoder xmlDecoder =new XMLDecoder(new ByteArrayInputStream(xml.getBytes()));
		detectors = (DiffractionDetectors) xmlDecoder.readObject();
		setBean(detectors);

	}
	
	@SuppressWarnings("resource")
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
	
	private void setBean(Object bean) {
		try {
			IBeanService service = (IBeanService)Activator.getService(IBeanService.class);
			IBeanController controller = service.createController(this, bean);
			controller.beanToUI();
			controller.switchState(true);
			controller.fireValueListeners();
			
		} catch (Exception e) {
			logger.warn("Cannot send "+bean+" to dialog!", e);
		}
	}
}

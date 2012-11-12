/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawb.workbench.plotting.preference.diffraction;

import org.dawb.common.ui.util.GridUtils;
import org.dawb.workbench.plotting.Activator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSpacing;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationStandards;
import uk.ac.diamond.scisoft.analysis.crystallography.HKL;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.richbeans.components.selector.VerticalListEditor;


public class DiffractionPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String ID = "org.dawb.workbench.plotting.preference.diffraction.calibrantPreferencePage";
	private static final Logger logger = LoggerFactory.getLogger(DiffractionPreferencePage.class);
	
	private VerticalListEditor hkls;
	private CCombo calibrantChoice;
	
	public DiffractionPreferencePage() {

	}

	/**
	 * @wbp.parser.constructor
	 */
	public DiffractionPreferencePage(String title) {
		super(title);
	}

	public DiffractionPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(1, false));
		GridData gdc = new GridData(SWT.FILL, SWT.FILL, true, true);
		main.setLayoutData(gdc);
		
		final Composite buttons = new Composite(main, SWT.NONE);
		buttons.setLayout(new GridLayout(4, false));
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Label caliLabel = new Label(buttons, SWT.NONE);
		caliLabel.setText("Calibrant ");
		
		this.calibrantChoice = new CCombo(buttons, SWT.READ_ONLY|SWT.BORDER);
		calibrantChoice.setItems(CalibrationStandards.getCalibrantList().toArray(new String[CalibrationStandards.getCalibrantList().size()])); // TODO Add listener to 
		calibrantChoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		calibrantChoice.select(0); // TODO From Preference
		calibrantChoice.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setCalibrantName(calibrantChoice.getItem(calibrantChoice.getSelectionIndex()));
			}
		});
		
		final Button addCalibrant = new Button(buttons, SWT.NONE);
		addCalibrant.setText("Create");
		addCalibrant.setToolTipText("Create a new calibrant (you can optionally copy the current one.");
		
		final Button removeCalibrant = new Button(buttons, SWT.NONE);
		removeCalibrant.setText("Delete");
		removeCalibrant.setToolTipText("Delete the selected calibrant.");

		this.hkls  = new VerticalListEditor(main, SWT.NONE);
		Composite      hklEditor = new DiffractionRingsComposite(hkls, SWT.NONE); 
		hklEditor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		hkls.setRequireSelectionPack(false);
		hkls.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		hkls.setMinItems(0);
		hkls.setMaxItems(25);
		hkls.setDefaultName("Position");
		hkls.setEditorClass(HKL.class);
		hkls.setEditorUI(hklEditor);
		hkls.setNameField("ringName");
		hkls.setAdditionalFields(new String[]{"dNano"});
		hkls.setColumnWidths(200, 200);
		hkls.setColumnNames("Name", "d");
		hkls.setColumnFormat("##0.####");
		hkls.setListHeight(150);
		hkls.setAddButtonText("Add Position");
		hkls.setRemoveButtonText("Remove Position");

		GridUtils.setVisibleAndLayout(hkls, true);
		
		// initialize
		setCalibrantName(calibrantChoice.getItem(calibrantChoice.getSelectionIndex()));
		
		hkls.setShowAdditionalFields(true);

		return main;
	}
	
	public VerticalListEditor getHKLs() {
		return hkls;
	}

	private void setCalibrantName(String name) {
		// TODO Save name
		CalibrantSpacing spacing = CalibrationStandards.getCalibrationPeakMap(name);
		setBean(spacing);
	}

	@Override
	public boolean performOk() {
		// TODO Store string preference in XML
		return super.performOk();
	}
	
	@Override
    public boolean performCancel() {
		// TODO Ensure that beans are reset from
		return super.performCancel();
	}


	@Override
	protected void performDefaults() {
		// TODO Read bean from default preference value
	}

	public void setBean(Object bean) {
		try {
			BeanUI.beanToUI(bean, this);
			BeanUI.switchState(bean, this, true);
			BeanUI.fireValueListeners(bean, this);
		} catch (Exception e) {
			logger.error("Cannot send "+bean+" to dialog!", e);
		}		
	}

}

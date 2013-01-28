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

import java.util.List;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.workbench.plotting.Activator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSelectedListener;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSelectionEvent;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSpacing;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationFactory;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationStandards;
import uk.ac.diamond.scisoft.analysis.crystallography.HKL;
import uk.ac.gda.richbeans.beans.BeanUI;
import uk.ac.gda.richbeans.components.selector.VerticalListEditor;

/**
 * NOTE This class does not use preferences because the CalibrationFactory should
 * work without eclipse preferences. It simply saves the object using XML serialization.
 * 
 * @author fcp94556
 *
 */
public class DiffractionPreferencePage extends PreferencePage implements IWorkbenchPreferencePage, CalibrantSelectedListener {

	public static final String ID = "org.dawb.workbench.plotting.preference.diffraction.calibrantPreferencePage";
	private static final Logger logger = LoggerFactory.getLogger(DiffractionPreferencePage.class);
	
	private VerticalListEditor hkls;
	private CCombo calibrantChoice;
	private CalibrationStandards calibrationStandards;
	
	public DiffractionPreferencePage() {

	}
	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		this.calibrationStandards = CalibrationFactory.getCalibrationStandards(true); // Reads from file.
		CalibrationFactory.addCalibrantSelectionListener(this);
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
	protected Control createContents(Composite parent) {
		
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(1, false));
		GridData gdc = new GridData(SWT.FILL, SWT.FILL, true, true);
		main.setLayoutData(gdc);
		
		final Composite buttons = new Composite(main, SWT.NONE);
		buttons.setLayout(new GridLayout(4, false));
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Label caliLabel = new Label(buttons, SWT.NONE);
		caliLabel.setText("Calibrant* ");
		
		this.calibrantChoice = new CCombo(buttons, SWT.READ_ONLY|SWT.BORDER);
		calibrantChoice.setToolTipText("Choose the calibrant to edit.\nNote: this also sets the active calibrant.");
		calibrantChoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		final Button addCalibrant = new Button(buttons, SWT.NONE);
		addCalibrant.setText("Create");
		addCalibrant.setToolTipText("Create a new calibrant (can optionally copy the current one).");
		addCalibrant.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				AddCalibrantWizard wiz=null;
				try {
					wiz = (AddCalibrantWizard)EclipseUtils.openWizard(AddCalibrantWizard.ID, false);
				} catch (Exception e1) {
					logger.error("Cannot open wizard "+AddCalibrantWizard.ID, e1);
					return;
				}
				wiz.setCalibrationStandards(calibrationStandards);
				WizardDialog wd = new  WizardDialog(Display.getCurrent().getActiveShell(), wiz);
				wd.setTitle(wiz.getWindowTitle());
				wd.open();
			}
		});
		
		
		final Button removeCalibrant = new Button(buttons, SWT.NONE);
		removeCalibrant.setText("Delete");
		removeCalibrant.setToolTipText("Delete the selected calibrant.");
		removeCalibrant.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				boolean ok = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), "Confirm Delete", "Do you really want to delete '"+calibrationStandards.getSelectedCalibrant()+"'?");
				if (ok) {
					final String oldCal = calibrationStandards.getSelectedCalibrant();
					final int oldIndex  = calibrationStandards.getCalibrantList().indexOf(oldCal);
					if (oldIndex<0) return;
					int index = oldIndex-1;
					if (index<0) index = 0;
					calibrationStandards.removeCalibrant(oldCal);
					calibrationStandards.setSelectedCalibrant(calibrationStandards.getCalibrantList().get(index), true);
				}
			}
		});

		this.hkls  = new VerticalListEditor(main, SWT.BORDER);
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

		GridUtils.setVisible(hkls, true);
		hkls.getParent().layout(new Control[]{hkls});
		
		final Label info = new Label(main, SWT.NONE);
		info.setText("* the calibrant to edit - also sets the active calibrant.");
		info.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));

		// Initialize
		setDefaultCalibrantChoice(); 
		calibrantChoice.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setCalibrantName(calibrantChoice.getItem(calibrantChoice.getSelectionIndex()), true);
			}
		});
		hkls.setShowAdditionalFields(true);

		return main;
	}
	
	public VerticalListEditor getHKLs() {
		return hkls;
	}

	@Override
	public boolean performOk() {
		try {			
			
			CalibrantSpacing spacing = calibrationStandards.getCalibrationPeakMap(calibrantChoice.getItem(calibrantChoice.getSelectionIndex()));
			BeanUI.uiToBean(this, spacing);
			calibrationStandards.save();
		} catch (Exception e) {
			logger.error("Cannot save standards!", e);
		}
		return super.performOk();
	}
	
	@Override
    public boolean performCancel() {
		return super.performCancel();
	}


	@Override
	protected void performDefaults() {
		this.calibrationStandards = CalibrationFactory.getCalibrationStandards(true); // Reads from file
		setDefaultCalibrantChoice();
	}
	
	public void dispose() {
		CalibrationFactory.removeCalibrantSelectionListener(this);
		super.dispose();
	}
	
	private void setDefaultCalibrantChoice() {
		final List<String> cl = calibrationStandards.getCalibrantList();
		calibrantChoice.setItems(cl.toArray(new String[cl.size()]));
		String selCalib = calibrationStandards.getSelectedCalibrant();
		final int index = cl.indexOf(selCalib);
		calibrantChoice.select(index); 
		
		// initialize
		setCalibrantName(calibrationStandards.getSelectedCalibrant(), false);

	}

	private void setCalibrantName(String name, boolean fireListeners) {
		calibrationStandards.setSelectedCalibrant(name, fireListeners);
		CalibrantSpacing spacing = calibrationStandards.getCalibrationPeakMap(name);
		setBean(spacing);
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
	@Override
	public void calibrantSelectionChanged(CalibrantSelectionEvent evt) {
		final int index = calibrantChoice.getSelectionIndex();
		if (index>-1 && calibrantChoice.getItems()[index].equals(evt.getCalibrant())) return;
		setDefaultCalibrantChoice();
	}

}

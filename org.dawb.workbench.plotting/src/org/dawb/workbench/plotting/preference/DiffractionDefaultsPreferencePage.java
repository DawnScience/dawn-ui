package org.dawb.workbench.plotting.preference;

import org.dawb.workbench.plotting.Activator;
import org.dawb.workbench.plotting.preference.diffraction.DiffractionPreferencePage;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationStandards;

public class DiffractionDefaultsPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	
	private CCombo metaPrompt;
	
	public static final String ID = "org.dawb.workbench.plotting.preference.diffraction.defaultsPreferencePage";
	private static final Logger logger = LoggerFactory.getLogger(DiffractionDefaultsPreferencePage.class);

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
		
		final Label metaDefaultLabel = new Label(main, SWT.NONE);
		metaDefaultLabel.setText("If an image has no metadata should default data be created?: ");
		
		metaPrompt = new CCombo(main, SWT.READ_ONLY|SWT.BORDER);
		metaPrompt.setItems(new String[] {MessageDialogWithToggle.PROMPT,
				MessageDialogWithToggle.ALWAYS, MessageDialogWithToggle.NEVER});
		metaPrompt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		
		metaPrompt.addSelectionListener(new SelectionAdapter() {
			public void widgetSelection(SelectionEvent e) {
				String selected = metaPrompt.getItem(metaPrompt.getSelectionIndex());
				setMetadataPrompt(selected);
			}
		});
		
		initializePage();
		
		return main;
	}
	
	@Override
	protected void performDefaults() {
		super.performDefaults();
		if (metaPrompt != null) {
			metaPrompt.select(metaPrompt.indexOf(getDefaultMetadataPrompt()));
		}
	}
	
	private void initializePage() {
		metaPrompt.select(metaPrompt.indexOf(getMetadataPrompt()));
	}

	@Override
	public boolean performOk() {
		storePreferences();
		return true;
	}
	
	private void storePreferences() {
		setMetadataPrompt(metaPrompt.getItem(metaPrompt.getSelectionIndex()));
	}
	
	
	private String getDefaultMetadataPrompt() {
		return getPreferenceStore().getDefaultString(DiffractionToolConstants.REMEMBER_DIFFRACTION_META);
	}
	
	private void setMetadataPrompt(String prompt) {
		getPreferenceStore().setValue(DiffractionToolConstants.REMEMBER_DIFFRACTION_META, prompt);
	}
	
	private String getMetadataPrompt() {
		return getPreferenceStore().getString(DiffractionToolConstants.REMEMBER_DIFFRACTION_META);
	}

}

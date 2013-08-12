package org.dawnsci.spectrum.ui.preferences;

import org.dawnsci.spectrum.ui.Activator;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class SpectrumPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	
	SpectrumNameListEditor lx;
	SpectrumNameListEditor ly;
	public static final String ID = "org.dawnsci.spectrum.ui.preferences.page";

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected Control createContents(Composite parent) {
		lx = new SpectrumNameListEditor(SpectrumConstants.X_DATASETS, "X-Dataset Name", parent);
		//le.setPreferenceName(SpectrumConstants.X_DATASETS);
		lx.setPreferenceStore(getPreferenceStore());
		lx.load();
		
		ly = new SpectrumNameListEditor(SpectrumConstants.Y_DATASETS, "Y-Dataset Name", parent);
		//le.setPreferenceName(SpectrumConstants.X_DATASETS);
		ly.setPreferenceStore(getPreferenceStore());
		ly.load();
		return null;
	}
	
	@Override
	protected void performDefaults() {
		lx.loadDefault();
		ly.loadDefault();
	}
	
	@Override
	public boolean performOk(){
		lx.store();
		ly.store();
		
		return super.performOk();
	}
}

package org.dawnsci.processing.python.ui.preferences;


import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class SavuProcessingPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	public SavuProcessingPreferencePage() {
		super();
		noDefaultAndApplyButton();
	}
	
	@Override
	protected Control createContents(Composite parent) {
		SavuProcessingPreferenceComposite savu = new SavuProcessingPreferenceComposite(parent, SWT.NONE);
		savu.populateList();
        
		return savu;
	}

	@Override
	public void init(IWorkbench workbench) {
		// no preferences for now
	}

}

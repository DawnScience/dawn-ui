package org.dawnsci.processing.python.ui.preferences;

import java.io.FileNotFoundException;

import org.dawnsci.processing.python.ui.Activator;
import org.dawnsci.processing.python.ui.SavuWindow2;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class SavuProcessingPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	public SavuProcessingPreferencePage() {
	}
	
	private Text remoteURITextBox;

	@Override
	protected Control createContents(Composite parent) {
		Composite savu = null;
		try {
			savu = new SavuWindow2(parent, SWT.NONE);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		return savu;
	}
	
	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
	
	@Override
	public boolean performOk() {
		String text = remoteURITextBox.getText();
//		getPreferenceStore().setValue(ProcessingConstants.REMOTE_RUNNER_URI, text);
//		getPreferenceStore().setValue(ProcessingConstants.FORCE_SERIES, forceSeries.getSelection());
		
		return super.performOk();
	}
	
	@Override
	protected void performDefaults() {
		getPreferenceStore().setValue(
				SavuProcessingConstants.REMOTE_RUNNER_URI, getPreferenceStore().getDefaultString(
						SavuProcessingConstants.REMOTE_RUNNER_URI));
		
		getPreferenceStore().setValue(
				SavuProcessingConstants.FORCE_SERIES, getPreferenceStore().getDefaultString(
						SavuProcessingConstants.FORCE_SERIES));
		
	}

}

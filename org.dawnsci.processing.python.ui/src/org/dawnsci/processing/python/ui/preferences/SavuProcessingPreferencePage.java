package org.dawnsci.processing.python.ui.preferences;

import java.io.FileNotFoundException;

import org.dawnsci.processing.python.ui.SavuWindow2;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class SavuProcessingPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	
	@Override
	protected Control createContents(Composite parent) {
		Composite savu = null;
		try {
			savu = new SavuWindow2(parent, SWT.NONE);
		} catch (FileNotFoundException e) {
			
		}
        
		return savu;
	}

	@Override
	public void init(IWorkbench workbench) {
		// no preferences for now
	}

}

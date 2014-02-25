package org.dawnsci.plotting.tools.fitting;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class OpenFittingToolPreferencesAction extends Action {
	
	public static final String ID = "org.dawb.workbench.plotting.fittingPreferencePage";
	
	public OpenFittingToolPreferencesAction() {
		setText("Preferences...");
		setToolTipText("Preferences...");
		setId(ID);
	}
	
	@Override
	public void run() {
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow()
				.getShell(), OpenFittingToolPreferencesAction.ID, new String[] { OpenFittingToolPreferencesAction.ID }, null);
		dialog.open();
	}

}

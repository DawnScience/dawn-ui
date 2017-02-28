package org.dawnsci.plotting.tools.finding;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.preference.PeakFindingPreferencePage;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PeakFindingActionsDelegate { 	

	private final static Logger logger = LoggerFactory.getLogger(PeakFindingActionsDelegate.class);
	
	PeakFindingController controller;
	
	Action addMode;
	Action removeMode;
	
	public PeakFindingActionsDelegate(PeakFindingController controller){
		this.controller = controller;
	}
	
	public void createActions(IToolBarManager toolbar) {
		final Action createNewSelection = new Action("New Search Selection.", IAction.AS_PUSH_BUTTON) {
			public void run() {
				controller.getPeakfindingtool().createNewSearch();
			}
		};
		createNewSelection.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit.png"));
		toolbar.add(createNewSelection);


		
		addMode = new Action("Add peaks to those already found", IAction.AS_CHECK_BOX) {
			 public void run() {
				controller.isRemoving = false;
				controller.isAdding = true;
				if (removeMode.isChecked())
					removeMode.setChecked(controller.isRemoving);
			}
		};
		addMode.setImageDescriptor(Activator.getImageDescriptor("icons/peakAdd.png"));
		toolbar.add(addMode);

		removeMode = new Action("Delete peaks to those already found", IAction.AS_CHECK_BOX) {
			public void run() {
				controller.isRemoving = true;
				controller.isAdding = false;
				if (addMode.isChecked())
					addMode.setChecked(controller.isAdding);
			}
		};
		removeMode.setImageDescriptor(Activator.getImageDescriptor("icons/peakDelete.png"));
		toolbar.add(removeMode);

		final Action export = new Action("Export peak(s)", IAction.AS_PUSH_BUTTON) {
			public void run() {
				try {
					EclipseUtils.openWizard(PeakFindingExportWizard.ID, true);
				} catch (Exception e) {
					logger.error("Cannot open export " + PeakFindingExportWizard.ID, e);
				}
			}
		};
		export.setImageDescriptor(Activator.getImageDescriptor("icons/mask-export-wiz.png"));
		toolbar.add(export);

		
		final Action preferences = new Action("Preferences...") {
			public void run() {
//				/if (!controller.isToolPageActive()) return;
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), PeakFindingPreferencePage.ID, null, null);
				if (pref != null) pref.open();
			}
		};
		preferences.setImageDescriptor(Activator.getImageDescriptor("icons/Configure.png"));
		toolbar.add(preferences);

	}
	
}

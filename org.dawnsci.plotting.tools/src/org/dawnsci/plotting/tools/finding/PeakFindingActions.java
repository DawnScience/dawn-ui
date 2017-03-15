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


/**
 * @author Dean P. Ottewell
 */
public class PeakFindingActions { 	

	private final static Logger logger = LoggerFactory.getLogger(PeakFindingActions.class);
	
	PeakFindingManager controller;
	
	Action addMode;
	Action removeMode;
	
	PeakFindingTool tool;
	
	public PeakFindingActions(PeakFindingManager controller,PeakFindingTool peakTool){
		this.controller = controller;
		this.tool = peakTool;
	}
	
	public void createActions(IToolBarManager toolbar) {
		
		final Action createNewSelection = new Action("New Search Selection.", IAction.AS_PUSH_BUTTON) {
			public void run() {
				if (removeMode.isChecked()){
					 tool.setRemoveMode(false);
					 removeMode.setChecked(false);
				}
				if (addMode.isChecked()){
					 tool.setAddMode(false);
					 addMode.setChecked(false);
				}
				
				
				tool.createNewSearch();
			}
		};
		createNewSelection.setImageDescriptor(Activator.getImageDescriptor("icons/plot-tool-peak-fit.png"));
		toolbar.add(createNewSelection);
		
		addMode = new Action("Add peaks to those already found", IAction.AS_CHECK_BOX) {
			 public void run() {
				 if (removeMode.isChecked()){
					 tool.resetActions();
					 tool.setRemoveMode(false);
					 removeMode.setChecked(false);
				 }
				 tool.setAddMode(this.isChecked());
			 }
		};
		addMode.setImageDescriptor(Activator.getImageDescriptor("icons/peakAdd.png"));
		toolbar.add(addMode);

		removeMode = new Action("Delete peaks to those already found", IAction.AS_CHECK_BOX) {
			public void run() {
				 if (addMode.isChecked()){
					 tool.resetActions();
					 tool.setAddMode(false);
					 addMode.setChecked(false);
				 }
				 tool.setRemoveMode(this.isChecked());
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
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), PeakFindingPreferencePage.ID, null, null);
				if (pref != null) pref.open();
			}
		};
		preferences.setImageDescriptor(Activator.getImageDescriptor("icons/Configure.png"));
		toolbar.add(preferences);
		
		toolbar.update(true);
	}
	
}

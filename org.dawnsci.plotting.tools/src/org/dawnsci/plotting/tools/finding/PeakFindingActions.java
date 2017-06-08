package org.dawnsci.plotting.tools.finding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.common.widgets.dialog.FileSelectionDialog;
import org.dawnsci.plotting.tools.Activator;
import org.dawnsci.plotting.tools.preference.PeakFindingPreferencePage;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;

/**
 * @author Dean P. Ottewell
 */
public class PeakFindingActions { 	

	private final static Logger logger = LoggerFactory.getLogger(PeakFindingActions.class);
	
	private PeakFindingManager manager;
	
	private Action addMode;
	private Action removeMode;
	
	private PeakFindingTool tool;
	
	private List<IdentifiedPeak> peaksId = new ArrayList<IdentifiedPeak>();
	
	
	public PeakFindingActions(PeakFindingManager manager,PeakFindingTool peakTool){
		this.manager = manager;
		this.tool = peakTool;
	}
	
	public void createActions(IToolBarManager toolbar) {
		
		// TODO: id the listener...
		IPeakOpportunityListener listener = new IPeakOpportunityListener() {
			@Override
			public void peaksChanged(PeakOpportunityEvent evt) {
				//TODO: now ill just place these identifed peaks here too
				if(evt.getPeakOpp().getPeaksId() != null){
					peaksId = evt.getPeakOpp().getPeaksId();		
				}
			}

			@Override
			public void boundsChanged(double upper, double lower) {

			}

			@Override
			public void dataChanged(IDataset nXData, IDataset nYData) {
				// xData = nXData;
				// yData = nYData;
			}

			@Override
			public void isPeakFinding() {
				// TODO Auto-generated method stub
			}

			@Override
			public void finishedPeakFinding() {
				// TODO Auto-generated method stub
			}

			@Override
			public void activateSearchRegion() {
			}
		};
		manager.addPeakListener(listener);
		
		
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
				
//				FileSelectionDialog export = new FileSelectionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
//
//				export.getShell().setSize(100, 100);
//				
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
		
		
		final Action sendPeaks = new Action("SendPeaks...") {
			public void run() {
				if(peaksId != null ){
					manager.sendPeakfindingEvent(peaksId);
				}
			}
		};
		sendPeaks.setImageDescriptor(Activator.getImageDescriptor("icons/broadcastEvent.png"));
		toolbar.add(sendPeaks);
		
		toolbar.update(true);
	}
	
}

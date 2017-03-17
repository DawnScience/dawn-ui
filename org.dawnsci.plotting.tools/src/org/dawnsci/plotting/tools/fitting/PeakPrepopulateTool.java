package org.dawnsci.plotting.tools.fitting;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IPeak;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.richbeans.widgets.decorator.IntegerDecorator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.Fitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Add;
import uk.ac.diamond.scisoft.analysis.fitting.functions.FunctionFactory;

/**
 * TODO: adjust to spawn peak finder widget
 * @edits Dean P. Ottewell
 *
 */
public class PeakPrepopulateTool extends Dialog {
	
	private static final Logger logger = LoggerFactory.getLogger(FunctionFittingTool.class);
	
	//Tool common UI elements
	private Composite dialogContainer;
	
	//Peak finding UI elements
	private Combo peakTypeCombo;
	private Text nrPeaksTxtBox;
	private Button findPeaksButton;
	
	//Background finding UI elements
	private Combo bkgTypeCombo;
	private Button fitInitialBkgButton;
	
	private Integer nrPeaks = null;
	private Dataset[] roiLimits;
	
	private FunctionFittingTool parentFittingTool;
	
	private FindInitialPeaksJob findStartingPeaksJob;
	
	private Add pkCompFunction = null;
	private IFunction bkgFunction = null;
	private Add compFunction = null;
	
	public PeakPrepopulateTool(Shell parentShell, FunctionFittingTool parentFittingTool, Dataset[] roiLimits) {
		//Setup the dialog and get the parent fitting tool as well as the ROI limits we're interested in.
		super(parentShell);
		this.parentFittingTool = parentFittingTool;
		this.roiLimits = roiLimits;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		//Set window title
		newShell.setText("Find Initial Peaks");
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		
		
		
		
		//Create/get the base containers and set up the grid layout
		Composite windowArea = (Composite) super.createDialogArea(parent);
		dialogContainer = new Composite(windowArea, SWT.NONE);
		dialogContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		dialogContainer.setLayout(new GridLayout(2, false));
		
		//Create a peak finding area on the left of the window
		Group peakFindingSpace = new Group(dialogContainer,SWT.NONE);
		peakFindingSpace.setText("Find Peaks");
		peakFindingSpace.setLayoutData(new GridData(SWT.FILL, SWT.FILL,true, true));
		peakFindingSpace.setLayout(new GridLayout(2, false));
		
		
		
		//Button to call initial peak finding
//		findPeaksButton = new Button(peakFindingSpace, SWT.PUSH);
//		GridData findPeaksButtonLayout = new GridData(SWT.CENTER, SWT.CENTER, true, true);
//		findPeaksButtonLayout.horizontalSpan = 2;
//		findPeaksButton.setLayoutData(findPeaksButtonLayout);
//		findPeaksButton.setText("Find Peaks");
//		findPeaksButton.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				findInitialPeaks();
//			}
//		});

		return windowArea;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		//Create Close & Find Peaks buttons.
		createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL, false);
	}
	
	@Override
	protected void buttonPressed(int buttonId){
		if (IDialogConstants.PROCEED_ID == buttonId) {
			findInitialPeaks();
		}
		else if (IDialogConstants.CLOSE_ID == buttonId) {
			setReturnCode(OK); //This is just returning 0 - might not be needed
			close();
		}
	}
	
	
	
	/**
	 * Gets the currently selected peak profile type in the combo box
	 * @return peak function class
	 */
	private Class<? extends IPeak> getProfileFunction(){
		String selectedProfileName = peakTypeCombo.getText();
		
		Class<? extends IPeak> selectedProfile = FunctionFactory.getPeakFunctionClass(selectedProfileName);
		
		return selectedProfile;
	}

	private static void populateCombo(Combo combo, Set<String> items) {
		combo.removeAll();
		for (String s : items) {
			combo.add(s);
		}
	}
	
	/**
	 * Update the composite function with either new peaks or new background.
	 * Uses existing background if none is given and does nothing if there are
	 * no peak or background functions.
	 * @param peaks - new peak Add function
	 * @param bkg - new background function
	 */
	private void updateCompFunction(Add peaks, IFunction bkg) {
		try {// Have to copy peak function info across, otherwise we're 
			 // updating the reference, not the object!
			if (peaks != null) {
				compFunction = (Add)peaks.copy();
			} else if (pkCompFunction != null) {
				compFunction = (Add)pkCompFunction.copy();
			} else {
				compFunction = new Add();
			}
		} catch (Exception e) {
			logger.error("Failed to update fit with peak functions",e);
		}
		if (bkg != null) {
			compFunction.addFunction(bkg);
		} else if (bkgFunction != null) {
			compFunction.addFunction(bkgFunction);
		}
	}
	
	/**
	 * Creates peak finding job and then sets parameters for the peak finding before scheduling job.
	 * JobChangeListener waits until peak finding finishes before calling back to parent tool
	 * to draw in located peaks.
	 */
	private void findInitialPeaks() {
		if (findStartingPeaksJob == null) {
			findStartingPeaksJob = new FindInitialPeaksJob("Find Initial Peaks");
		}
		
		findStartingPeaksJob.setData(roiLimits);
		findStartingPeaksJob.setNrPeaks(nrPeaks);
		findStartingPeaksJob.setPeakFunction(getProfileFunction());
		
		findStartingPeaksJob.schedule();
		
		findStartingPeaksJob.addJobChangeListener(new JobChangeAdapter(){
			@Override
			public void done(IJobChangeEvent event) {
				updateCompFunction(pkCompFunction, null);
				// TODO this wants updating to use something more generic
				parentFittingTool.setInitialPeaks(compFunction);
			}
		});
	}
	
	
	//**********************************
	
	/**
	 * Job to find initial peaks. Uses getInitialPeaks method in FittingUtils 
	 * to do the work
	 */
	private class FindInitialPeaksJob extends FittingJob {

		public FindInitialPeaksJob(String name) {
			super(name);
		}
		
		Integer nrPeaks;
		Class<? extends IPeak> peakFunction;
		
		public void setNrPeaks(Integer nrPeaks) {
			this.nrPeaks = nrPeaks;
		}
		
		public void setPeakFunction(Class<? extends IPeak> peakFunction) {
			this.peakFunction = peakFunction;
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			//TODO FIXME Why does Generic1dFitter rely on reflection whereas Fitter et al. use concrete classes?
			pkCompFunction = FittingUtils.getInitialPeaks(x, y, nrPeaks, peakFunction);
			return Status.OK_STATUS;
		}
	}
	
	
	
}

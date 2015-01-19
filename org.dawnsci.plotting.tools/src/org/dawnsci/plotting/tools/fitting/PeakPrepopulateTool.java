package org.dawnsci.plotting.tools.fitting;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.dawnsci.common.widgets.decorator.IntegerDecorator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import uk.ac.diamond.scisoft.analysis.fitting.FittingConstants;
import uk.ac.diamond.scisoft.analysis.fitting.FittingConstants.FIT_ALGORITHMS;
import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.APeak;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Add;
import uk.ac.diamond.scisoft.analysis.optimize.GeneticAlg;
import uk.ac.diamond.scisoft.analysis.optimize.IOptimizer;

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
	private Map<String, Class <? extends APeak>> peakFnMap = new TreeMap<String, Class <? extends APeak>>();
	private String[] availPeakTypes;
	
	private FunctionFittingTool parentFittingTool;
	
	private FindInitialPeaksJob findStartingPeaksJob;
	private FitBackgroundJob fitBackgroundJob;
	
	private Add pkCompFunction = null;
	private Add bkgFunction = null;
	private Add compFunction = null;
	
	public PeakPrepopulateTool(Shell parentShell, FunctionFittingTool parentFittingTool, Dataset[] roiLimits) {
		//Setup the dialog and get the parent fittingtool as well as the ROI limits we're interested in.
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
		
//		Composite peakFindContainer = new Composite(peakFindingSpace, SWT.NONE);
//		peakFindContainer.setLayoutData(new GridData(SWT.FILL,SWT.FILL, true, true));
//		peakFindContainer.setLayout(new GridLayout(2, false));
		
		//Number of peaks text field
		Label nrPeaksTxtLbl = new Label(peakFindingSpace, SWT.None);
		nrPeaksTxtLbl.setText("Number of Peaks:");
		nrPeaksTxtBox = new Text(peakFindingSpace, SWT.BORDER); 
		nrPeaksTxtBox.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		//This limits the input for the text box to +ve integers
		final IntegerDecorator nrPeaksIDec = new IntegerDecorator(nrPeaksTxtBox);
		nrPeaksIDec.setMinimum(0);
		
		//When the value in the text box is changed and valid, convert to an Integer and enable the find peaks button
		nrPeaksTxtBox.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (!nrPeaksIDec.isError()) {
					findPeaksButton.setEnabled(true);
					try {
						nrPeaks = Integer.parseInt(nrPeaksTxtBox.getText());
					} catch (NumberFormatException nfe) {
						// Move on.
					}
				} else {
					findPeaksButton.setEnabled(false);
				}
				
			}
		});
		
		//Profile type combo box
		Label peakTypeCmbLbl = new Label(peakFindingSpace, SWT.None);
		peakTypeCmbLbl.setText("Peak Function Type:");
		peakTypeCombo = new Combo(peakFindingSpace, SWT.READ_ONLY);
		setAvailPeakFunctions();
		setDefaultPeakFunction();
		peakTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		
		//Button to call initial peak finding
		findPeaksButton = new Button(peakFindingSpace, SWT.PUSH);
		GridData findPeaksButtonLayout = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		findPeaksButtonLayout.horizontalSpan = 2;
		findPeaksButton.setLayoutData(findPeaksButtonLayout);
		findPeaksButton.setText("Find Peaks");
		findPeaksButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				findInitialPeaks();
			}
		});
		
		//Create a background finding space on the right of the window
		Group bkgFindingSpace = new Group(dialogContainer,SWT.NONE);
		bkgFindingSpace.setText("Fit Background");
		GridLayout bkgGridLayout = new GridLayout(2, false);
		bkgFindingSpace.setLayoutData(new GridData(SWT.FILL, SWT.FILL,true, true));
		bkgFindingSpace.setLayout(bkgGridLayout);
		
		//Background type combo box
		Composite bkgLabelCombo = new Composite(bkgFindingSpace, SWT.NONE);
		bkgLabelCombo.setLayoutData(new GridData(SWT.FILL,SWT.FILL, true, true));
		bkgLabelCombo.setLayout(new GridLayout(1, false));
		Label bkgTypeCmbLbl = new Label(bkgLabelCombo, SWT.NONE);
		bkgTypeCmbLbl.setText("Background Function Type:");
		bkgTypeCombo = new Combo(bkgLabelCombo, SWT.READ_ONLY);
		//TODO Define options here...
		bkgTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		
		//Background function parameters
		Label bkgFnParamsPlaceHolder = new Label(bkgFindingSpace, SWT.NONE);
		bkgFnParamsPlaceHolder.setText("Function parameters go here");
		
		//Button to call background fitting
		fitInitialBkgButton = new Button(bkgFindingSpace, SWT.PUSH);
		GridData fitInitialBkgButtonGridData = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		fitInitialBkgButtonGridData.horizontalSpan = 2;
		fitInitialBkgButton.setLayoutData(fitInitialBkgButtonGridData);
		fitInitialBkgButton.setText("Fit Background");
		fitInitialBkgButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("Fitting background...");
				fitBackground();
			}
		});
		
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
	 * Gets the list of available function names and their classes from FittingUtils class
	 * and populates combo box with them
	 */
	private void setAvailPeakFunctions() {
		for (final Class<? extends APeak> peak : FittingUtils.getPeakOptions().values()) {
			peakFnMap.put(peak.getSimpleName(), peak);
		}
		Set<String> availPeakTypeSet = peakFnMap.keySet();
		availPeakTypes = (String[]) availPeakTypeSet.toArray(new String[availPeakTypeSet.size()]);
		peakTypeCombo.setItems(availPeakTypes);
	}
	
	/**
	 * Sets default peak profile to Psuedo-Voight or Gaussian (if available)
	 */
	private void setDefaultPeakFunction() {
		int defaultPeakFnIndex = Arrays.asList(availPeakTypes).indexOf("PseudoVoigt") == -1 ? Arrays.asList(availPeakTypes).indexOf("Gaussian") : Arrays.asList(availPeakTypes).indexOf("PseudoVoigt");
		if (defaultPeakFnIndex != -1) {
			peakTypeCombo.select(defaultPeakFnIndex);
		}
	}
	
	/**
	 * Gets the currently selected peak profile type in the combo box
	 * @return peak function class
	 */
	private Class<? extends APeak> getProfileFunction(){
		String selectedProfileName = peakTypeCombo.getText();
		Class<? extends APeak> selectedProfile = peakFnMap.get(selectedProfileName);
		
		return selectedProfile;
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
				parentFittingTool.setInitialPeaks(pkCompFunction);
			}
		});
	}
	
	private void fitBackground() {
	if (fitBackgroundJob == null) {
		fitBackgroundJob = new FitBackgroundJob("Fit Background");
	}
	
	fitBackgroundJob.setData(roiLimits);
	fitBackgroundJob.setPeakCompoundFunction(pkCompFunction);
//	fitBackgroundJob.setBkgFunctionType(getBackgroundFunction());
	
	fitBackgroundJob.schedule();
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
		Class<? extends APeak> peakFunction;
		
		public void setNrPeaks(Integer nrPeaks) {
			this.nrPeaks = nrPeaks;
		}
		
		public void setPeakFunction(Class<? extends APeak> peakFunction) {
			this.peakFunction = peakFunction;
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			pkCompFunction = FittingUtils.getInitialPeaks(x, y, nrPeaks, peakFunction);
			return Status.OK_STATUS;
		}
	}
		
		/**
		 * Job to find background of a dataset, removing the peaks from the 
		 * background first.
		 */
	private class FitBackgroundJob extends FittingJob {
	
		public FitBackgroundJob(String name) {
			super(name);
		}
		
		IFunction bkgFunctionType = null;
		Add peakCompFunction = null;
		
		public void setBkgFunctionType(IFunction bkgFn) {
			bkgFunctionType = bkgFn;
		}
		public void setPeakCompoundFunction(Add peakFn) {
			peakCompFunction = peakFn;
		}
		
		protected IStatus run(IProgressMonitor monitor) {
			//1 Calculate existing peak function
			Dataset peakCompValues = peakCompFunction.calculateValues(x);
			
			//2 Subtract peak data from observed
			Dataset peakDifference = Maths.subtract(y, peakCompValues);
			
			//4 Fit subtracted data to given function.
			try {
				IOptimizer fitRoutine = new GeneticAlg(0.01);
				bkgFunction = Fitter.fit(x, peakDifference, fitRoutine, bkgFunctionType);
			}
			catch (Exception e) {
				//this covers an exception of the fit routine.
				logger.error("Background fitting encountered an error", e);
				return Status.CANCEL_STATUS;
			}
			
			//6 Add background function to compound function. 
		
			return Status.OK_STATUS;
		}
	}
}

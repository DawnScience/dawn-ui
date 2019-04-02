package org.dawnsci.plotting.tools.fitting;

import java.util.List;

import org.dawnsci.plotting.actions.ActionBarWrapper;
import org.dawnsci.plotting.tools.finding.PeakFindingTool;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Add;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;

/**
 * TODO: adjust to spawn peak finder widget
 * 
 * @edits Dean P. Ottewell
 */
public class PeakPrepopulateWizard extends WizardPage {
	
	public PeakPrepopulateWizard() {
		super("Intial peak searching tool");
	}

	private static final Logger logger = LoggerFactory.getLogger(FunctionFittingTool.class);
	
	//Tool common UI elements
	private Composite dialogContainer;
	
	//Peak finding UI elements
	private Combo peakTypeCombo;
	
	private Add pkCompFunction = null;
	private IFunction bkgFunction = null;
	private Add compFunction = null;

	private IPlottingSystem<Composite> plotting;

	private FunctionFittingTool parentFittingTool;
	
	private PeakFindingTool peakFindTool;
	
	public PeakPrepopulateWizard(FunctionFittingTool parentFittingTool) {
		super("Intial peak searching tool");
		this.setDescription("Search for peaks to then pass onto function fitting");
		this.setTitle("Peak Finding");
		
		this.setControl(parentFittingTool.getControl());
		//Setup the dialog and get the parent fitting tool as well as the ROI limits we're interested in.
	
		//Configure controller for peak tool
		this.parentFittingTool = parentFittingTool;		
	}
	
	@Override
	public void createControl(Composite parent) {		
		dialogContainer = new Composite(parent, SWT.NONE);
		dialogContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		dialogContainer.setLayout(new GridLayout(2, false));
		
		Composite left = new Composite(dialogContainer, SWT.FILL);
		left.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		left.setLayout(new GridLayout());

		Composite right = new Composite(dialogContainer, SWT.FILL);
		right.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		right.setLayout(new GridLayout());
		
		createPlottingSystem(right);
		
		//TODO: NEED TO BE ABLE TO GET THE PLOT TO REDRAW FROM CONTROLLER SETUp 
		peakFindTool = new PeakFindingTool(plotting);
		
		peakFindTool.createControl(left);
	}
	
	private void createPlottingSystem(Composite pos){
		Composite plotComp = new Composite(pos, SWT.FILL);
		plotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		plotComp.setLayout(new GridLayout());
		ActionBarWrapper actionBarWrapper = ActionBarWrapper.createActionBars(plotComp, null);
		Composite displayPlotComp  = new Composite(plotComp, SWT.BORDER);
		displayPlotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		displayPlotComp.setLayout(new FillLayout());
		
		try {
			plotting = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			logger.error("cannot create plotting system",e);
		}
		plotting.createPlotPart(displayPlotComp, "Slice", actionBarWrapper, PlotType.XY, null);
		
		ILineTrace searchTrace =  plotting.createLineTrace("test");
		
		if(parentFittingTool.getPlottingSystem().getTraces() != null){
			ILineTrace sampleData = (ILineTrace) parentFittingTool.getPlottingSystem().getTraces().iterator().next();
			searchTrace.setData(sampleData.getXData(), sampleData.getYData());
		}
		
		plotting.addTrace(searchTrace);
		
		//TODO: this was passing some chopped slices
//		for(ITrace trace : parentFittingTool.getPlottingSystem().getTraces())
//			plotting.addTrace(trace);
	}	
	
	public Add gatherPeaksFunc(){
		Add compFuncPeaks = FittingUtils.getSeededPeakFit(peakFindTool.getPeaksId(), (Dataset)peakFindTool.gettingXData(), (Dataset)peakFindTool.gettingYData(), null);
		return compFuncPeaks;
	}
	
	public List<IdentifiedPeak> gatherInitalPeaks(){
		return peakFindTool.getPeaksId();
	}

}

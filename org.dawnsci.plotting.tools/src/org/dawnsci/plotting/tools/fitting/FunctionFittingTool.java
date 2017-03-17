/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools.fitting;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.common.widgets.gda.function.FunctionFittingWidget;
import org.dawnsci.common.widgets.gda.function.IFittedFunctionInvalidatedEvent;
import org.dawnsci.common.widgets.gda.function.ModelModifiedAdapter;
import org.dawnsci.common.widgets.gda.function.descriptors.DefaultFunctionDescriptorProvider;
import org.dawnsci.plotting.tools.Activator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext.ConversionScheme;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IDataBasedFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunctionService;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.eclipse.dawnsci.slicing.api.util.ProgressMonitorWrapper;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.IPageSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.fitting.FittingConstants;
import uk.ac.diamond.scisoft.analysis.fitting.FittingConstants.FIT_ALGORITHMS;
import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Add;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;
import uk.ac.diamond.scisoft.analysis.optimize.ApacheOptimizer;
import uk.ac.diamond.scisoft.analysis.optimize.ApacheOptimizer.Optimizer;
import uk.ac.diamond.scisoft.analysis.optimize.GeneticAlg;
import uk.ac.diamond.scisoft.analysis.optimize.IOptimizer;

public class FunctionFittingTool extends AbstractToolPage implements IFunctionService {

	private static final Logger logger = LoggerFactory.getLogger(FunctionFittingTool.class);

	private static final Image FIT = Activator.getImage("icons/chart_curve_go.png");
	private static final Image UPDATE = Activator.getImage("icons/arrow_refresh_small.png");

	private Control control;
	private boolean autoRefit;

	private boolean firstRun = true;

	protected IROIListener roiListener = new FunctionFittingROIListener();
	protected IRegion region = null;
	private Add compFunction = null;
	protected ILineTrace estimate;
	private ILineTrace fitTrace;
	private Add resultFunction;

	private UpdateFitPlotJob updateFittedPlotJob;
	private ITraceListener traceListener = new FunctionFittingTraceListener();

	private Text chiSquaredValueText;
	private FunctionFittingWidget functionWidget;

	private Button updateAllButton;
	private Button findPeaksButton;

	// These are controls for whether FindPeaksButton should be visible when
	// FunctionFittingTool called (e.g.) as part of
	// a workflow. enableFindPeaksButton is used internally only, whereas
	// showFindPeaksWorkFlow can be used as a control.
	private boolean showFindPeaksWorkFlow = false;
	private boolean enableFindPeaksButton = true;

	private IPreferenceStore prefs = Activator.getPlottingPreferenceStore();

	private boolean connectLater;

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_1D;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		GridUtils.removeMargins(composite);
		// composite is our top level control
		control = composite;

		Composite infoComposite = new Composite(composite, SWT.NONE);
		infoComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		infoComposite.setLayout(new GridLayout(2, true));

		Composite actionComposite = new Composite(infoComposite, SWT.NONE);
		actionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		actionComposite.setLayout(new GridLayout(2, true));

		final Button autoRefitButton = new Button(actionComposite, SWT.TOGGLE);
		autoRefitButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		autoRefitButton.setText("Auto Refit");
		autoRefitButton.setImage(FIT);
		autoRefitButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				autoRefit = autoRefitButton.getSelection();
				updateFunctionPlot(false);
			}
		});

		updateAllButton = new Button(actionComposite, SWT.PUSH);
		updateAllButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		updateAllButton.setText("Update All");
		updateAllButton.setImage(UPDATE);
		updateAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateAllParameters();
			}
		});

		fitOnceButton = new Button(actionComposite, SWT.PUSH);
		fitOnceButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		fitOnceButton.setText("Fit Once");
		fitOnceButton.setEnabled(true);
		fitOnceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateFunctionPlot(true);
			}
		});

		if (enableFindPeaksButton) {
			findPeaksButton = new Button(actionComposite, SWT.PUSH);
			findPeaksButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			findPeaksButton.setText("Find Peaks...");
			findPeaksButton.setEnabled(true);
			findPeaksButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					openPeakPrepopulateWizard();
				}
			});
		}

		Composite resultsComposite = new Composite(infoComposite, SWT.BORDER);
		resultsComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		resultsComposite.setLayout(new GridLayout(1, false));

		Label chiSquaredInfoLabel = new Label(resultsComposite, SWT.NONE);
		chiSquaredInfoLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1));
		chiSquaredInfoLabel.setText("Normalised goodness of fit:");

		chiSquaredValueText = new Text(resultsComposite, SWT.READ_ONLY | SWT.CENTER);
		chiSquaredValueText.setBackground(resultsComposite.getBackground());
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		chiSquaredValueText.setLayoutData(gd);
		chiSquaredValueText.setText("Not Calculated");

		functionWidget = new FunctionFittingWidget(composite, new DefaultFunctionDescriptorProvider(), getSite());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(functionWidget);

		// Initialise with a simple function.
		if (compFunction == null) {
			compFunction = new Add();
		}
		functionWidget.setInput(compFunction);
		functionWidget.expandAll();

		functionWidget.addModelModifiedListener(new ModelModifiedAdapter() {
			@Override
			protected void modelModified() {
				compFunctionModified();
			}

			@Override
			public void fittedFunctionInvalidated(IFittedFunctionInvalidatedEvent event) {
				resultFunction = null;
				chiSquaredValueText.setText("Not Calculated");
				updateAllButton.setEnabled(false);
				// TODO remove fitted trace
			}
		});

		ActionBarWrapper actionBarWrapper = null;
		if (getSite() == null) {
			parent = new Composite(parent, SWT.NONE);
			parent.setLayout(new GridLayout(1, true));
			actionBarWrapper = ActionBarWrapper.createActionBars(parent, null);
		}

		// sashForm = new SashForm(parent, SWT.VERTICAL);
		// if (getSite() == null) sashForm.setLayoutData(new
		// GridData(GridData.FILL_BOTH));

		final IPageSite site = getSite();
		IActionBars actionbars = site != null ? site.getActionBars() : actionBarWrapper;

		if (getSite() != null) {
			getSite().setSelectionProvider(functionWidget.getFunctionViewer());
		} else {

		}
		fillActionBar(actionbars);
		if (connectLater) {
			connectPlotSystemListeners();
			compFunctionModified();
		}
		// track tool usage
		super.createControl(parent);
	}

	@Override
	public Control getControl() {
		return control;
	}

	@Override
	public void setFocus() {
		functionWidget.setFocus();
	}

	@Override
	public void activate() {
		super.activate();
		if (functionWidget != null) {
			// XXX because activate can be called before the controls are
			// created, defer connecting the listeners in that case.
			connectPlotSystemListeners();
		} else {
			connectLater = true;
		}
	}

	private void compFunctionModified() {
		updateFunctionPlot(false);
		fitOnceButton
				.setEnabled(functionWidget.isValid() && compFunction != null && compFunction.getNoOfFunctions() != 0);
	}

	private void connectPlotSystemListeners() {
		try {
			getPlottingSystem().addTraceListener(traceListener);

			region = getPlottingSystem().getRegion("fit_region");

			if (region == null) {
				region = getPlottingSystem().createRegion("fit_region", RegionType.XAXIS);
				region.setRegionColor(ColorConstants.blue);
				region.setROI(new RectangularROI(getPlottingSystem().getSelectedXAxis().getLower(), 0,
						getPlottingSystem().getSelectedXAxis().getUpper()
								- getPlottingSystem().getSelectedXAxis().getLower(),
						100, 0));
				getPlottingSystem().addRegion(region);
			} else {
				region.setVisible(true);
			}
			region.addROIListener(roiListener);

			// Without this, updateFunctionPlot gets called twice at start up
			// for no reason
			if (firstRun) {
				firstRun = false;
				return;
			} else {
				updateFunctionPlot(false);
			}

		} catch (Exception e) {
			logger.error("Failed to activate function fitting tool", e);
		}
	}

	@Override
	public void deactivate() {
		if (region != null) {
			region.removeROIListener(roiListener);
			region.setVisible(false);
		}
		Collection<ITrace> traces = getPlottingSystem().getTraces();
		if (traces.contains(estimate))
			getPlottingSystem().removeTrace(estimate);
		if (traces.contains(fitTrace))
			getPlottingSystem().removeTrace(fitTrace);

		getPlottingSystem().removeTraceListener(traceListener);

		super.deactivate();
	}

	private void setChiSquaredValue(double value, boolean notConverged) {
		String text = Double.toString(value);
		if (notConverged) {
			text = text + " (Not converged)";
		}
		chiSquaredValueText.setText(text);
	}

	private void fillActionBar(IActionBars actionBars) {
		IToolBarManager manager = actionBars.getToolBarManager();
		manager.add(new ExportFittingDataAction());
		manager.add(new ImportFittingDataAction());

		IMenuManager menuManager = actionBars.getMenuManager();
		menuManager.add(new Separator());
		menuManager.add(new OpenFittingToolPreferencesAction());
		menuManager.add(new Separator());
	}

	// XXX Consider separating the trace finding and the ROI getting (Abstract
	// method?)
	/**
	 * Determines the first user trace and returns the ROI limits for it
	 * 
	 * @return ROI limits [x ,y]
	 */
	private Dataset[] getFirstUserTraceROI() {
		boolean firstUserTrace = true;
		for (ITrace selectedTrace : getPlottingSystem().getTraces()) {
			if (selectedTrace instanceof ILineTrace) {
				ILineTrace trace = (ILineTrace) selectedTrace;
				if (trace.isUserTrace() && firstUserTrace) {
					firstUserTrace = false;
					// We chop x and y by the region bounds. We assume the
					// plot is an XAXIS selection therefore the indices in
					// y = indices chosen in x.
					RectangularROI roi = (RectangularROI) region.getROI();

					final double[] p1 = roi.getPointRef();
					final double[] p2 = roi.getEndPoint();

					// We peak fit only the first of the data sets plotted
					// for now.
					Dataset[] traceROI = new Dataset[] { DatasetUtils.convertToDataset(trace.getXData()),
							DatasetUtils.convertToDataset(trace.getYData()) };

					try {
						traceROI = Generic1DFitter.selectInRange(traceROI[0], traceROI[1], p1[0], p2[0]);
					} catch (Throwable npe) {
						// Do nothing
					}
					return traceROI;

				}
			}
		}
		logger.debug("No user traces found in plot.");
		return null;
	}

	/**
	 * Plot the line for the un-fitted function.
	 * 
	 * @param roiLimits
	 *            ROI region [x, y]
	 */
	private void plotEstimateLine(Dataset[] roiLimits) {
		estimate = (ILineTrace) getPlottingSystem().getTrace("Estimate");
		if (estimate == null) {
			estimate = getPlottingSystem().createLineTrace("Estimate");
			estimate.setUserTrace(false);
			estimate.setTraceType(ILineTrace.TraceType.DASH_LINE);
			getPlottingSystem().addTrace(estimate);
		}

		if (compFunction != null) {
			for (IFunction function : compFunction.getFunctions()) {
				if (function instanceof IDataBasedFunction) {
					IDataBasedFunction dataBasedFunction = (IDataBasedFunction) function;
					dataBasedFunction.setData(roiLimits[0], roiLimits[1]);
				}
			}
			DoubleDataset functionData = compFunction.calculateValues(roiLimits[0]);
			estimate.setData(roiLimits[0], functionData);
		}
	}

	private void updateAllParameters() {
		if (resultFunction != null) {
			double[] parameterValues = resultFunction.getParameterValues();
			compFunction.setParameterValues(parameterValues);
			functionWidget.refresh();
			compFunctionModified();
			updateFunctionPlot(false);
		}
	}

	private void updateFunctionPlot(boolean force) {
		if (!functionWidget.isValid()) {
			return;
		}
		getPlottingSystem().removeTraceListener(traceListener);

		Dataset[] traceROI = getFirstUserTraceROI();
		if (traceROI == null) {
			return;
		}
		;

		plotEstimateLine(traceROI);

		// System.out.println(x);
		// System.out.println(y);

		getPlottingSystem().repaint();

		updateFittedPlot(force, traceROI[0], traceROI[1]);
		refreshViewer();
		getPlottingSystem().addTraceListener(traceListener);
	}

	private void updateFittedPlot(boolean force, final Dataset x, final Dataset y) {

		if (force || autoRefit) {

			if (updateFittedPlotJob == null) {
				updateFittedPlotJob = new UpdateFitPlotJob("Update Fitted Plot");
			}
			updateFittedPlotJob.setData(x, y);
			updateFittedPlotJob.schedule();
		}

	}

	private void openPeakPrepopulateWizard() {
		
		getPlottingSystem().removeTraceListener(traceListener);
		PeakPrepopulateWizard peakFindOptions = new PeakPrepopulateWizard(this);
		
		final Wizard wiz = new Wizard() {
			//set 
			@Override
			public boolean performFinish() {
				//TODO: grab peaks
				//IWizardPage peakFindpage = wiz.getStartingPage();
				PeakPrepopulateWizard peakToolpage = (PeakPrepopulateWizard) this.getStartingPage();
				setInitialPeaks(peakToolpage.gatherPeaksFunc());
				return true;
			}
		};
		
		wiz.setNeedsProgressMonitor(true);
		wiz.addPage(peakFindOptions);

		final WizardDialog wd = new WizardDialog(getSite().getShell(),wiz);
		wd.setPageSize(new Point(900, 500));
		wd.create();
		wd.getCurrentPage();
		if (wd.open() == WizardDialog.OK)
		
		getPlottingSystem().addTraceListener(traceListener);	
	}

	public void setInitialPeaks(Add initPeakCompFunc) {
		compFunction = initPeakCompFunc;
		final Dataset[] currRoiLimits = getFirstUserTraceROI();
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				functionWidget.setInput(compFunction);
				functionWidget.setFittedInput(null);

				// From new peak(s), plot estimate line
				plotEstimateLine(new Dataset[] { currRoiLimits[0], currRoiLimits[1] });
				getPlottingSystem().repaint();
				refreshViewer();
			}
		});
	}

	// TODO this job is sometimes unstopped at shutdown, add to dispose
	private class UpdateFitPlotJob extends Job {

		public UpdateFitPlotJob(String name) {
			super(name);
		}

		private Dataset x;
		private Dataset y;

		public void setData(Dataset x, Dataset y) {
			this.x = x.clone();
			this.y = y.clone();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			final IMonitor aMonitor = new ProgressMonitorWrapper(monitor);
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					if (fitTrace != null)
						fitTrace.setVisible(false);
					getPlottingSystem().repaint();
				}
			});

			boolean tooManyItterations = false;
			try {
				double accuracy = prefs.getDouble(FittingConstants.FIT_QUALITY);
				logger.debug("Accuracy is set to {}", accuracy);
				int algoId = prefs.getInt(FittingConstants.FIT_ALGORITHM);
				FIT_ALGORITHMS algorithm = FIT_ALGORITHMS.fromId(algoId);

				// We need to run the fit on a copy of the compFunction
				// otherwise the fit will affect the input values.
				resultFunction = (Add) compFunction.copy();
				resultFunction.setMonitor(aMonitor);
				IFunction[] functionCopies = resultFunction.getFunctions();
				for (IFunction function : functionCopies) {
					if (function instanceof IDataBasedFunction) {
						IDataBasedFunction dataBasedFunction = (IDataBasedFunction) function;
						dataBasedFunction.setData(x, y);
					}
				}
				IOptimizer optimizer = null;
				switch (algorithm) {
				default:
				case APACHENELDERMEAD:
					optimizer = new ApacheOptimizer(Optimizer.SIMPLEX_NM);
					break;
				case GENETIC:
					optimizer = new GeneticAlg(accuracy);
					break;
				case APACHECONJUGATEGRADIENT:
					optimizer = new ApacheOptimizer(Optimizer.CONJUGATE_GRADIENT);
					break;
				case APACHELEVENBERGMAQUARDT:
					optimizer = new ApacheOptimizer(Optimizer.LEVENBERG_MARQUARDT);
					break;
				}
				optimizer.optimize(new IDataset[] { x }, y, resultFunction);

				// TODO (review race condition) this copy of compFunction
				// appears to happen "late" if the job is not scheduled for a
				// "while" then the compFunction can change (by GUI interaction)
				// between when the estimate was plotted and the fit is started.
				// TODO There is no way of cancelling this fit. If an errant fit
				// is attempted (e.g. Add(Gaussian(0,0,0), Box(0,0,0,0,0))) the
				// fitter appears to run forever. This is (one of?) the reasons
				// that "Job found still running after platform shutdown. Jobs
				// should be canceled by the plugin that scheduled them during
				// shutdown:
				// org.dawnsci.plotting.tools.fitting.FunctionFittingTool$UpdateFitPlotJob"
				// error is observed.

			} catch (TooManyEvaluationsException me) {
				tooManyItterations = true;
			} catch (Exception e) {
				return Status.CANCEL_STATUS;
			}

			final boolean notConverged = tooManyItterations;

			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					getPlottingSystem().removeTraceListener(traceListener);
					setChiSquaredValue(resultFunction.residual(true, y, null, new IDataset[] { x }) / x.count(),
							notConverged);

					fitTrace = (ILineTrace) getPlottingSystem().getTrace("Fit");
					if (fitTrace == null) {
						fitTrace = getPlottingSystem().createLineTrace("Fit");
						fitTrace.setUserTrace(false);
						fitTrace.setLineWidth(2);
						getPlottingSystem().addTrace(fitTrace);
					}

					System.out.println("Plotting");
					System.out.println(resultFunction);
					DoubleDataset resultData = resultFunction.calculateValues(x);
					fitTrace.setData(x, resultData);
					fitTrace.setVisible(true);

					getPlottingSystem().repaint();
					refreshViewer();
					getPlottingSystem().addTraceListener(traceListener);

					functionWidget.setFittedInput(resultFunction);
					updateAllButton.setEnabled(true);
				}
			});

			return Status.OK_STATUS;
		}

	}

	public void addFunctionUpdatedListener(SelectionListener listener) {
		if (updateAllButton != null) {
			updateAllButton.addSelectionListener(listener);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class key) {
		if (key == IFunctionService.class)
			return this;
		return super.getAdapter(key);
	}

	@Override
	public Map<String, IFunction> getFunctions() {

		HashMap<String, IFunction> functions = new HashMap<String, IFunction>();

		if (compFunction != null) {
			for (int i = 0; i < compFunction.getNoOfFunctions(); i++) {
				String key = String.format("%03d_initial_%s", i, compFunction.getFunction(i).getName());
				functions.put(key, compFunction.getFunction(i));
			}
		}

		if (resultFunction != null) {
			for (int i = 0; i < resultFunction.getNoOfFunctions(); i++) {
				String key = String.format("%03d_result_%s", i, resultFunction.getFunction(i).getName());
				functions.put(key, resultFunction.getFunction(i));
			}
		}

		return functions;
	}

	@Override
	public void setFunctions(Map<String, IFunction> functions) {
		// clear the composite function
		compFunction = new Add();
		for (String key : functions.keySet()) {
			if (key.contains("_initial_")) {
				compFunction.addFunction((AFunction) functions.get(key));
			}
		}

		resultFunction = new Add();
		updateAllButton.setEnabled(true);
		for (String key : functions.keySet()) {
			if (key.contains("_result_")) {
				resultFunction.addFunction((AFunction) functions.get(key));
			}
		}

		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				functionWidget.setInput(compFunction);
				functionWidget.setFittedInput(resultFunction);
				compFunctionModified();

				getPlottingSystem().repaint();
				refreshViewer();
			}
		});
	}

	/**
	 * Set the list of functions available to the user to select from
	 *
	 * @param functions
	 *            list of functions
	 */
	public void setFunctionList(IFunction[] functions) {

	}

	private void refreshViewer() {
		// TODO what is the condition that this can be null???
		if (functionWidget != null)
			functionWidget.refresh();
	}

	/*
	 * Update function plot if region of interest changes
	 */
	private class FunctionFittingROIListener implements IROIListener {
		@Override
		public void roiDragged(ROIEvent evt) {
			return;
		}

		@Override
		public void roiChanged(ROIEvent evt) {
			updateFunctionPlot(false);
		}

		@Override
		public void roiSelected(ROIEvent evt) {
		}
	}

	private class FunctionFittingTraceListener implements ITraceListener {
		boolean updating = false;

		private void update() {
			if (!updating) {
				try {
					updating = true;
					updateFunctionPlot(false);
				} finally {
					updating = false;
				}
			}
		}

		@Override
		public void tracesUpdated(TraceEvent evt) {
		}

		@Override
		public void tracesRemoved(TraceEvent evet) {
		}

		@Override
		public void tracesAdded(TraceEvent evt) {
			update();
		}

		@Override
		public void traceWillPlot(TraceWillPlotEvent evt) {
		}

		@Override
		public void traceUpdated(TraceEvent evt) {
			update();
		}

		@Override
		public void traceRemoved(TraceEvent evt) {
		}

		@Override
		public void traceCreated(TraceEvent evt) {
		}

		@Override
		public void traceAdded(TraceEvent evt) {
			update();
		}
	}

	/**
	 * TODO review setToolData / setToolData here, it seems not possibly
	 * correct. If getToolData is called before setToolData a NPE happens. Seems
	 * illogical. Perhaps this is a remnant from an unimplemented or old
	 * thing????
	 */
	private Map<String, Serializable> functions = null;

	private Button fitOnceButton;

	/**
	 * Override to set the tool data to something specific
	 *
	 * @param toolData
	 */
	@Override
	public void setToolData(Serializable toolData) {

		// Allows the user to specify whether the peak button will be shown, for
		// example in a workflow
		// tool. By default it is not shown. To show the button, use the
		// setShowFindPeaksWorkFlow method
		if (showFindPeaksWorkFlow == true) {
			enableFindPeaksButton = true;
		} else {
			enableFindPeaksButton = false;
		}

		// final UserPlotBean bean = (UserPlotBean) toolData;
		functions = new HashMap<String, Serializable>();
		functions.put("Function", toolData);

		compFunction = new Add();
		for (String key : functions.keySet()) {
			if (functions.get(key) instanceof AFunction) {
				AFunction function = (AFunction) functions.get(key);
				compFunction.addFunction(function);

			}
		}

		if (functionWidget != null) {
			functionWidget.setInput(compFunction);
			functionWidget.expandAll();
			compFunctionModified();
		}
	}

	@Override
	public Serializable getToolData() {

		// UserPlotBean bean = new UserPlotBean();

		int count = 0;
		for (String key : functions.keySet()) {
			if (count < compFunction.getNoOfFunctions()) {
				functions.put(key, compFunction.getFunction(count));
				count++;
			}
		}

		// Also add the composite function
		functions.put("Comp", compFunction);

		// bean.setFunctions(functions); // We only set functions because it
		// does a
		// // replace merge.

		return compFunction;
	}

	public void setShowFindPeaksWorkFlow(boolean buttonEnable) {
		showFindPeaksWorkFlow = buttonEnable;
	}

	public boolean getShowFindPeaksWorkFlow() {
		return showFindPeaksWorkFlow;
	}
}

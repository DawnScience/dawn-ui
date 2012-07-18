package org.dawb.workbench.plotting.tools;

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.plot.EmptyTool;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.dawb.common.ui.plot.trace.TraceUtils;
import org.dawb.workbench.plotting.Activator;
import org.dawb.workbench.plotting.preference.PlottingConstants;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

public class DerivativeTool extends AbstractToolPage  {

	// Statics
	private static final int SMOOTHING = 1;

	// Logger
	private final static Logger logger = LoggerFactory.getLogger(DerivativeTool.class);


	// GUI Elements
	private Composite composite;
	protected Button dataCheck;
	protected Button derivCheck;
	protected Button deriv2Check;
	private Label infoLabel;

	// Listeners
	private ITraceListener traceListener;
	private SelectionListener updateChecksSelection;


	// Actions
	private IAction resetOnDeactivate;

	// Jobs
	private Job updatePlotData;

	// Internal Items
	private boolean isUpdateRunning = false;

	protected ArrayList<AbstractDataset> dervs  = new ArrayList<AbstractDataset>();
	protected ArrayList<AbstractDataset> dervs2 = new ArrayList<AbstractDataset>();
	protected ArrayList<AbstractDataset> data   = new ArrayList<AbstractDataset>();
	protected ArrayList<AbstractDataset> xs     = new ArrayList<AbstractDataset>();

	public List<ITrace> dataTraces = new ArrayList<ITrace>();
	public List<ITrace> dervTraces = new ArrayList<ITrace>();
	public List<ITrace> derv2Traces = new ArrayList<ITrace>();


	public DerivativeTool() {
		try {
			// Set up the listener for new traces
			this.traceListener = new ITraceListener.Stub() {
				@Override
				public void tracesPlotted(TraceEvent evt) {

					if (!(evt.getSource() instanceof List<?>) && !(evt.getSource() instanceof ITrace)) {
						return;
					}
					if (!isUpdateRunning) {
						updatePlotData();
					}
				}
			};

		} catch (Exception e) {
			logger.error("Cannot get plotting system!", e);
		}

		// Set up the listener for the gui elements		
		updateChecksSelection = new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				logger.debug("Widget Selected Event");
				UIJob job = new UIJob("Update Derivatives") {

					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						try {
							isUpdateRunning = true;
							logger.debug("Widget Update Running");
							updateDerivatives(dataCheck.getSelection(),derivCheck.getSelection(),deriv2Check.getSelection());
						} finally {
							logger.debug("Widget Update Finished");
							isUpdateRunning = false;
						}
						return Status.OK_STATUS;
					}
				};

				job.schedule();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				widgetSelected(event);
			}
		};
	}


	@Override
	public void createControl(Composite parent) {

		composite = new Composite(parent, SWT.RESIZE);
		composite.setLayout(new GridLayout(1, false));	

		infoLabel = new Label(composite, SWT.NONE);
		infoLabel.setText("The derivative tool will leave the derivative traces active unless the clear tool is selected");

		dataCheck = new Button(composite, SWT.CHECK);
		dataCheck.setText("Display Data");
		dataCheck.setSelection(false);
		dataCheck.addSelectionListener(updateChecksSelection);
		derivCheck = new Button(composite, SWT.CHECK);
		derivCheck.setSelection(true);
		derivCheck.setText("Display f'(Data)");
		derivCheck.addSelectionListener(updateChecksSelection);
		deriv2Check = new Button(composite, SWT.CHECK);
		deriv2Check.setText("Display f''(Data)");
		deriv2Check.addSelectionListener(updateChecksSelection);

		resetOnDeactivate= new Action("Reset plot(s) when tool deactivates", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				Activator.getDefault().getPreferenceStore().setValue(PlottingConstants.RESET_ON_DEACTIVATE, isChecked());
			}
		};
		resetOnDeactivate.setImageDescriptor(Activator.getImageDescriptor("icons/reset.gif"));
		resetOnDeactivate.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.RESET_ON_DEACTIVATE));
		getSite().getActionBars().getToolBarManager().add(resetOnDeactivate);

		activate();
	}


	/**
	 * Required if you want to make tools work.
	 * Currently we do not want 1D tools on the derivative page
	 * 
	public Object getAdapter(final Class clazz) {

		if (clazz == IToolPageSystem.class) {
			return plotter;
		}

		return null;
	}
	 */


	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_1D;
	}


	@Override
	public void setFocus() {
		composite.setFocus();
	}


	@Override
	public void activate() {
		super.activate();
		if (getPlottingSystem()!=null) {
			getPlottingSystem().addTraceListener(traceListener);
			updatePlotData();
		}
	}


	@Override
	public void deactivate() {
		super.deactivate();

		Object tool = getToolSystem().getCurrentToolPage(getToolPageRole());
		boolean isEmpty = tool!=null && tool.getClass()==EmptyTool.class;
		boolean deactivateOnExit = Activator.getDefault().getPreferenceStore().getBoolean(PlottingConstants.RESET_ON_DEACTIVATE);

		// if this is the empty tool, clear the plots
		if (isEmpty || deactivateOnExit) {
			updateDerivatives(true,false,false);
		}

		if (getPlottingSystem()!=null) getPlottingSystem().removeTraceListener(traceListener);
	}


	@Override
	public Control getControl() {
		if (composite==null) return null;
		return composite;
	}


	@Override
	public void dispose() {
		deactivate();
		// clear all lists
		if (dervs!=null) dervs.clear();
		if (dervs2!=null) dervs2.clear();
		if (data!=null) data.clear();
		if (xs!=null) xs.clear();
		if (dataTraces!=null) dataTraces.clear();
		if (dervTraces!=null) dervTraces.clear();
		if (derv2Traces!=null) derv2Traces.clear();
		super.dispose();
	}


	/**
	 * The user can optionally nominate an x. In this case, we would like to 
	 * use it for the derivative instead of the indices of the data. Therefore
	 * there is some checking here to see if there are x values to plot.
	 * 
	 * Normally everything will be ILineTraces even if the x is indices.
	 */
	private synchronized void updatePlotData() {

		if (updatePlotData==null) {
			updatePlotData = new Job("Derviative update") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {

					try {

						isUpdateRunning = true;
						logger.debug("Update Running");
						if (!isActive()) return Status.CANCEL_STATUS;

						// First get all the traces which have been plotted which are user traces
						dataTraces.clear();
						for (ITrace trace : getPlottingSystem().getTraces(ILineTrace.class)) {
							if (trace.isUserTrace()) {
								dataTraces.add(trace);
							}
						}

						// now remove any traces which are derivative ones
						for (ITrace trace : dervTraces) {
							dataTraces.remove(trace);
						}

						for (ITrace trace : derv2Traces) {
							dataTraces.remove(trace);
						}

						// quick check for cancel.
						if (monitor.isCanceled()) return  Status.CANCEL_STATUS;

						// Now we have a list which only contains user defined and derivatable plots, calculate the derivatives

						// Clear all the dataset lists
						data.clear();
						xs.clear();
						dervs.clear();
						dervs2.clear();

						for (ITrace trace : dataTraces) {

							// get the datasets out of the plotting
							final AbstractDataset traceData = trace.getData();
							data.add(traceData);

							final AbstractDataset x = (trace instanceof ILineTrace) 
									? ((ILineTrace)trace).getXData() 
											: AbstractDataset.arange(0, traceData.getSize(), 1, AbstractDataset.INT32);
									xs.add(x);	

									// now calculate the derivatives
									AbstractDataset derv = Maths.derivative(x, traceData, SMOOTHING);
									dervs.add(derv);
									dervs2.add(Maths.derivative(x, derv, SMOOTHING));

						}

						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								updateDerivatives(dataCheck.getSelection(),derivCheck.getSelection(),deriv2Check.getSelection());						
							}
						});

						return Status.OK_STATUS;
					} finally {
						logger.debug("Update Finished");
						isUpdateRunning = false;
					}
				}	
			};
			updatePlotData.setSystem(true);
			updatePlotData.setUser(false);
			updatePlotData.setPriority(Job.INTERACTIVE);
		}

		updatePlotData.schedule();
	}


	/**
	 * The user can optionally nominate an x. In this case, we would like to 
	 * use it for the derivative instead of the indices of the data. Therefore
	 * there is some checking here to see if there are x values to plot.
	 * 
	 * Normally everything will be ILineTraces even if the x is indices.
	 * 
	 * Must call in UI thread
	 */
	private synchronized void updateDerivatives(boolean dataVisible, boolean derivVisible, boolean deriv2Visible) {
		// Have a look to see which pieces of data should be plotted
		logger.debug("Updating Derivatives");
		// check for plotting the data first
		for(ITrace trace : dataTraces) {
			trace.setVisible(dataVisible);
		} 

		// now check the derivatives
		if (derivVisible) {
			dervTraces.clear();
			for(int i = 0; i < dervs.size(); i++) {
				ILineTrace trace = TraceUtils.replaceCreateLineTrace(getPlottingSystem(), dervs.get(i).getName());
				trace.setData(xs.get(i), dervs.get(i));
				trace.setUserTrace(true);
				getPlottingSystem().addTrace(trace);
				dervTraces.add(trace);			
			}
		} else {
			// the derivatives are not needed, so delete them
			for (ITrace trace : dervTraces) {
				getPlottingSystem().removeTrace(trace);
			}
		}

		// now check the second derivatives
		if (deriv2Visible) {
			derv2Traces.clear();
			for(int i = 0; i < dervs2.size(); i++) {
				ILineTrace trace = TraceUtils.replaceCreateLineTrace(getPlottingSystem(), dervs2.get(i).getName());
				trace.setData(xs.get(i), dervs2.get(i));
				trace.setUserTrace(true);
				getPlottingSystem().addTrace(trace);
				derv2Traces.add(trace);		
			}
		} else {
			// the derivatives are not needed, so delete them
			for (ITrace trace : derv2Traces) {
				getPlottingSystem().removeTrace(trace);
			}
		}

		getPlottingSystem().autoscaleAxes();
	}

}

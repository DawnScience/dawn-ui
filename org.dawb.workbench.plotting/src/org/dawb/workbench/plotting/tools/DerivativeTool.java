package org.dawb.workbench.plotting.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.plot.EmptyTool;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.dawb.workbench.plotting.Activator;
import org.dawb.workbench.plotting.preference.PlottingConstants;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
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

	private final static Logger logger = LoggerFactory.getLogger(DerivativeTool.class);

	private   ITraceListener         traceListener;
	private boolean isUpdatingDerivatives = false;

	private Composite composite;

	protected Button dataCheck;

	protected Button derivCheck;

	protected Button deriv2Check;

	protected Button deriv3Check;

	private SelectionListener updateChecksSelection;

	private IAction resetOnDeactivate;

	private Label infoLabel;

	public DerivativeTool() {
		try {
			this.traceListener = new ITraceListener.Stub() {
				@Override
				public void tracesPlotted(TraceEvent evt) {

					if (isUpdatingDerivatives) return; // Avoids looping of listeners!
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

		updateChecksSelection = new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				hidePlots(dataCheck.getSelection(),
					derivCheck.getSelection(),
					deriv2Check.getSelection(),
					deriv3Check.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				widgetSelected(event);

			}
		};

	}

	protected void hidePlots(boolean useData, boolean useDeriv, boolean useDeriv2, boolean useDeriv3) {
		
		for (ITrace trace : dataTraces) {
			trace.setVisible(useData);
		}
		
		for (ITrace trace : dervTraces) {
			trace.setVisible(useDeriv);
		}
		
		for (ITrace trace : dervTraces2) {
			trace.setVisible(useDeriv2);
		}
		
		for (ITrace trace : dervTraces3) {
			trace.setVisible(useDeriv3);
		}
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
		deriv3Check = new Button(composite, SWT.CHECK);
		deriv3Check.setText("Display f'''(Data)");
		deriv3Check.addSelectionListener(updateChecksSelection);
		
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

	public void activate() {
		super.activate();
		if (getPlottingSystem()!=null) {
			getPlottingSystem().addTraceListener(traceListener);
			updatePlotData();
		}
	}

	public void deactivate() {
		super.deactivate();
		
		Object tool = getToolSystem().getCurrentToolPage(getToolPageRole());
		boolean isEmpty = tool!=null && tool.getClass()==EmptyTool.class;
		
		// if this is the empty tool, clear the plots
		if (isEmpty) {
			getPlottingSystem().clear();
			for (int i = 0; i < data.size(); i++) {
				getPlottingSystem().updatePlot1D(xs.get(i), data.subList(i, i+1), new NullProgressMonitor());
			}
		}
		
		if (getPlottingSystem()!=null) getPlottingSystem().removeTraceListener(traceListener);
	}

	@Override
	public Control getControl() {
		if (composite==null) return null;
		return composite;
	}

	public void dispose() {
		deactivate();
		super.dispose();
	}


	private Job updatePlotData;
	private boolean isUpdateRunning = false;

	protected ArrayList<ITrace> traces          = new ArrayList<ITrace>();
	protected ArrayList<AbstractDataset> dervs  = new ArrayList<AbstractDataset>();
	protected ArrayList<AbstractDataset> dervs2 = new ArrayList<AbstractDataset>();
	protected ArrayList<AbstractDataset> dervs3 = new ArrayList<AbstractDataset>();
	protected ArrayList<AbstractDataset> data   = new ArrayList<AbstractDataset>();
	protected ArrayList<AbstractDataset> xs     = new ArrayList<AbstractDataset>();

	
	private void generateAllDerivs() {
		dervs.clear();
		for (int i = 0; i < data.size(); i++) {
			//TODO 2D dervs!
			if (xs.get(i).getRank() != 1 || data.get(i).getRank() != 1) {
				logger.trace("Cannot process 2D derviatives as yet!");
				continue;
			}
			dervs.add(Maths.derivative(xs.get(i), data.get(i), 1));
		}
		
		dervs2.clear();
		for (int i = 0; i < dervs.size(); i++) {
			//TODO should make the smoothing a parameter
			//TODO 2D dervs!
			if (xs.get(i).getRank() != 1 || data.get(i).getRank() != 1) {
				logger.trace("Cannot process 2D derviatives as yet!");
				continue;
			}
			dervs2.add(Maths.derivative(xs.get(i), dervs.get(i), 1));
		}
		
		dervs3.clear();
		for (int i = 0; i < dervs2.size(); i++) {
			//TODO should make the smoothing a parameter
			//TODO 2D dervs!
			if (xs.get(i).getRank() != 1 || data.get(i).getRank() != 1) {
				logger.trace("Cannot process 2D derviatives as yet!");
				continue;
			}
			dervs3.add(Maths.derivative(xs.get(i), dervs2.get(i), 1));
		}
	}
	
	
	protected ArrayList<AbstractDataset> getDervs() {
		if (dervs.size() != data.size()) {
			dervs.clear();
			for (int i = 0; i < data.size(); i++) {
				//TODO 2D dervs!
				if (xs.get(i).getRank() != 1 || data.get(i).getRank() != 1) {
					logger.trace("Cannot process 2D derviatives as yet!");
					continue;
				}
				dervs.add(Maths.derivative(xs.get(i), data.get(i), 1));
			}
		}
		return dervs;
	}
	
	protected ArrayList<AbstractDataset> getDervs2() {
		ArrayList<AbstractDataset> derivatives = getDervs();
		if (dervs2.size() != derivatives.size()) {
			dervs2.clear();
			for (int i = 0; i < derivatives.size(); i++) {
				//TODO should make the smoothing a parameter
				//TODO 2D dervs!
				if (xs.get(i).getRank() != 1 || data.get(i).getRank() != 1) {
					logger.trace("Cannot process 2D derviatives as yet!");
					continue;
				}
				dervs2.add(Maths.derivative(xs.get(i), derivatives.get(i), 1));
			}
		}
		return dervs2;
	}
	
	protected ArrayList<AbstractDataset> getDervs3() {
		ArrayList<AbstractDataset> derivatives = getDervs2();
		if (dervs3.size() != derivatives.size()) {
			dervs3.clear();
			for (int i = 0; i < derivatives.size(); i++) {
				//TODO should make the smoothing a parameter
				//TODO 2D dervs!
				if (xs.get(i).getRank() != 1 || data.get(i).getRank() != 1) {
					logger.trace("Cannot process 2D derviatives as yet!");
					continue;
				}
				dervs3.add(Maths.derivative(xs.get(i), derivatives.get(i), 1));
			}
		}
		return dervs3;
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

						if (!isActive()) return Status.CANCEL_STATUS;

						traces.clear();
						traces.addAll(getPlottingSystem().getTraces());
						data.clear();
						dervs.clear();
						dervs2.clear();
						dervs3.clear();
						xs.clear();

						if (monitor.isCanceled()) return  Status.CANCEL_STATUS;
						
						for (ITrace trace : traces) {

							//if (firstTrace==null) firstTrace = trace;

							if (!trace.isUserTrace()) continue;
							
							final AbstractDataset plot = trace.getData();
							data.add(plot);

							final AbstractDataset x = (trace instanceof ILineTrace) 
									              ? ((ILineTrace)trace).getXData() 
									              : AbstractDataset.arange(0, plot.getSize(), 1, AbstractDataset.INT32);
							xs.add(x);			
						}

						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								updateDerivatives();						
							}
						});
						
						return Status.OK_STATUS;
					} finally {
						isUpdateRunning = false;
					}
				}	
			};
			updatePlotData.setSystem(true);
			updatePlotData.setUser(false);
			updatePlotData.setPriority(Job.INTERACTIVE);
		}

		if (isUpdateRunning)  updatePlotData.cancel();
		if (updateRunning <= 0) {
			updatePlotData.schedule();
		}
	}

	
	private DerivativeJob updateDerivatives;
	private int updateRunning = 0;

	public List<ITrace> dataTraces = new ArrayList<ITrace>();

	public List<ITrace> dervTraces = new ArrayList<ITrace>();

	public List<ITrace> dervTraces2 = new ArrayList<ITrace>();

	public List<ITrace> dervTraces3 = new ArrayList<ITrace>();

	/**
	 * The user can optionally nominate an x. In this case, we would like to 
	 * use it for the derivative instead of the indices of the data. Therefore
	 * there is some checking here to see if there are x values to plot.
	 * 
	 * Normally everything will be ILineTraces even if the x is indices.
	 * 
	 * Must call in UI thread
	 */
	private synchronized void updateDerivatives() {
		if (updateDerivatives==null) updateDerivatives = new DerivativeJob();
		updateDerivatives.update(dataCheck.getSelection(),
							     derivCheck.getSelection(),
							     deriv2Check.getSelection(),
							     deriv3Check.getSelection());
	}
	
	private class DerivativeJob extends Job {

		public DerivativeJob() {
			super("Update Derivatives");
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			if (getPlottingSystem()==null)    return Status.CANCEL_STATUS;
			if (data==null || data.isEmpty()) return Status.CANCEL_STATUS;
			if (isUpdatingDerivatives)        return Status.CANCEL_STATUS;
			try {
				updateRunning++;
				isUpdatingDerivatives = true;
				
				// Should only deal with 1D data!
				final Collection<ITrace> lines = getPlottingSystem().getTraces(ILineTrace.class);
				if (lines==null || lines.isEmpty()) return Status.CANCEL_STATUS;
				
				getPlottingSystem().clear();
				generateAllDerivs();
				
				dataTraces.clear();
				dervTraces.clear();
				dervTraces2.clear();
				dervTraces3.clear();
				
				for (int i = 0; i < data.size(); i++) {
					List<ITrace> dataTraceList = getPlottingSystem().updatePlot1D(xs.get(i), data.subList(i, i+1), monitor);
					for (ITrace trace : dataTraceList) {
						trace.setVisible(false);
					}
					dataTraces.addAll(dataTraceList);
					
					List<ITrace> dervTraceList = getPlottingSystem().updatePlot1D(xs.get(i), dervs.subList(i, i+1), monitor);
					for (ITrace trace : dervTraceList) {
						trace.setVisible(false);
					}
					dervTraces.addAll(dervTraceList);
					
					List<ITrace> derv2TraceList = getPlottingSystem().updatePlot1D(xs.get(i), dervs2.subList(i, i+1), monitor);
					for (ITrace trace : derv2TraceList) {
						trace.setVisible(false);
					}
					dervTraces2.addAll(derv2TraceList);
					
					List<ITrace> derv3TraceList = getPlottingSystem().updatePlot1D(xs.get(i), dervs3.subList(i, i+1), monitor);
					for (ITrace trace : derv3TraceList) {
						trace.setVisible(false);
					}
					dervTraces3.addAll(derv3TraceList);
				}
				
				UIJob job = new UIJob("DerivativeUpdate") {
					
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						hidePlots(dataCheck.getSelection(),
								derivCheck.getSelection(),
								deriv2Check.getSelection(),
								deriv3Check.getSelection());
						return Status.OK_STATUS;
					}
				};
				job.schedule();
				
				
				
			} catch (Exception e) {
				logger.error("Failed to display the Diferential", e);
			} finally {
				updateRunning--;
				isUpdatingDerivatives = false;
			}
			
			return Status.OK_STATUS;
		}

		public void update(final boolean isData, 
			                final boolean isDerv, 
			                final boolean isDerv2, 
			                final boolean isDerv3) {
			
			if (getPlottingSystem()==null) return;
			
	        for (Job job : Job.getJobManager().find(null))
	            if (job.getClass()==getClass() && job.getState() != Job.RUNNING)
	        	    job.cancel();
			
			schedule();
		}
	};

}

package org.dawb.workbench.plotting.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

public class DerivativeTool extends AbstractToolPage  {

	private final static Logger logger = LoggerFactory.getLogger(DerivativeTool.class);

	protected AbstractPlottingSystem plotter;
	private   ITraceListener         traceListener;

	private Composite composite;

	private Label dataLabel;

	protected Button dataCheck;

	protected Button derivCheck;

	protected Button deriv2Check;

	protected Button deriv3Check;

	private SelectionListener updateDataSelection;

	private SelectionListener updateDerivSelection;

	private SelectionListener updateDeriv2Selection;

	private SelectionListener updateDeriv3Selection;


	public DerivativeTool() {
		try {
			plotter = PlottingFactory.createPlottingSystem();
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

		updateDataSelection = new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				//derivCheck.setSelection(false);
				//deriv2Check.setSelection(false);
				//deriv3Check.setSelection(false);
				updateDerivatives();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				widgetSelected(event);

			}
		};

		updateDerivSelection = new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				//dataCheck.setSelection(false);
				//deriv2Check.setSelection(false);
				//deriv3Check.setSelection(false);
				updateDerivatives();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				widgetSelected(event);

			}
		};

		updateDeriv2Selection = new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				//derivCheck.setSelection(false);
				//dataCheck.setSelection(false);
				//deriv3Check.setSelection(false);
				updateDerivatives();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				widgetSelected(event);

			}
		};

		updateDeriv3Selection = new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent event) {
				//derivCheck.setSelection(false);
				//deriv2Check.setSelection(false);
				//dataCheck.setSelection(false);
				updateDerivatives();

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				widgetSelected(event);

			}
		};
	}

	@Override
	public void createControl(Composite parent) {


		// final IPageSite site = getSite();

		//		plotter.createPlotPart(parent, 
		//								getTitle(), 
		//								site.getActionBars(), 
		//								PlotType.PT1D,
		//								null);	

		composite = new Composite(parent, SWT.RESIZE);
		composite.setLayout(new GridLayout(1, false));	

		dataCheck = new Button(composite, SWT.CHECK);
		dataCheck.setText("Display Data");
		dataCheck.setSelection(true);
		dataCheck.addSelectionListener(updateDataSelection);
		derivCheck = new Button(composite, SWT.CHECK);
		derivCheck.setText("Display f'(Data)");
		derivCheck.addSelectionListener(updateDerivSelection);
		deriv2Check = new Button(composite, SWT.CHECK);
		deriv2Check.setText("Display f''(Data)");
		deriv2Check.addSelectionListener(updateDeriv2Selection);
		deriv3Check = new Button(composite, SWT.CHECK);
		deriv3Check.setText("Display f'''(Data)");
		deriv3Check.addSelectionListener(updateDeriv3Selection);

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
		return ToolPageRole.ROLE_1D_AND_2D;
	}

	@Override
	public void setFocus() {

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
		if (getPlottingSystem()!=null) getPlottingSystem().removeTraceListener(traceListener);
	}

	@Override
	public Control getControl() {
		if (composite==null) return null;
		return composite;
	}

	public void dispose() {
		deactivate();
		if (plotter!=null) plotter.dispose();
		plotter = null;
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

	
	protected ArrayList<AbstractDataset> getDervs() {
		if (dervs.size() != data.size()) {
			dervs.clear();
			for (int i = 0; i < data.size(); i++) {
				//TODO should make the smoothing a parameter
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
				dervs3.add(Maths.derivative(xs.get(i), derivatives.get(i), 1));
			}
		}
		return dervs3;
	}
	
	

	/**
	 * The user can optionally nominate an x. In this case, we would like to 
	 * use it for the derviative instead of the indices of the data. Therefore
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

						updateDerivatives();						
						
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

	
	private UIJob updateDerivatives;
	private int updateRunning = 0;

	/**
	 * The user can optionally nominate an x. In this case, we would like to 
	 * use it for the derviative instead of the indices of the data. Therefore
	 * there is some checking here to see if there are x values to plot.
	 * 
	 * Normally everything will be ILineTraces even if the x is indices.
	 */
	private synchronized void updateDerivatives() {

		if (updateDerivatives==null) {
			updateDerivatives = new UIJob("Update Derivatives") {
				
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					try {
						updateRunning++;
						getPlottingSystem().clear();
						
						if (dataCheck.getSelection()) {
							for (int i = 0; i < data.size(); i++) {
								getPlottingSystem().updatePlot1D(xs.get(i), data.subList(i, i+1), monitor);
							}
						}
						
						if (derivCheck.getSelection()) {
							
							for (int i = 0; i < data.size(); i++) {
								getPlottingSystem().updatePlot1D(xs.get(i), getDervs().subList(i, i+1), monitor);
							}
						}
						
						if (deriv2Check.getSelection()) {
							
							for (int i = 0; i < data.size(); i++) {
								getPlottingSystem().updatePlot1D(xs.get(i), getDervs2().subList(i, i+1), monitor);
							}
						}
						
						if (deriv3Check.getSelection()) {
							
							for (int i = 0; i < data.size(); i++) {
								getPlottingSystem().updatePlot1D(xs.get(i), getDervs3().subList(i, i+1), monitor);
							}
						}
						
					} catch (Exception e) {
						logger.error("Failed to display the Diferential", e);
					} finally {
						updateRunning--;
					}
					
					return Status.OK_STATUS;
				}
			};
		}
		
		updateDerivatives.schedule();
		
	}
	

	private String getTicksFor(int size) {
		final StringBuilder buf = new StringBuilder();
		for (int i = 0; i < size; i++) buf.append("'");
		return buf.toString();
	}

}

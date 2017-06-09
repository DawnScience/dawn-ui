/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dawnsci.plotting.tools;

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.plot.tools.HistoryType;
import org.dawnsci.plotting.util.ColorUtility;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DerivativeTool extends AbstractToolPage  {
	
	//Class for conveniently passing around a x,y pair of datasets
	private class DatasetPair {
		public Dataset x;
		public Dataset y;
		
		public DatasetPair(Dataset xIn, Dataset yIn) {
			x = xIn;
			y= yIn;
		}
	}
	
	//Derivative type
	private enum Derivative {
		FIRST,SECOND
	}
	
	// Statics
	private static final int SMOOTHING = 1;
	
	//Trace/Dataset pair lists
	private List<ITrace> eventTraceList= new ArrayList<ITrace>();
	private List<ITrace> dataTraces = new ArrayList<ITrace>();
	private ArrayList<DatasetPair> dervsPair  = new ArrayList<DatasetPair>();
	private ArrayList<DatasetPair> dervs2Pair  = new ArrayList<DatasetPair>();
	boolean data = false;
	boolean deriv = true;
	boolean deriv2 = false;

	// Logger
	private final static Logger logger = LoggerFactory.getLogger(DerivativeTool.class);

	// GUI Elements
	private Composite composite;
	protected Button dataCheck;
	protected Button derivCheck;
	protected Button deriv2Check;
	private Label infoLabel;
	private boolean duringSync = false;
	private boolean duringDispose = false;

	// Listeners
	private ITraceListener traceListener;
	private SelectionListener updateChecksSelection;

	// Jobs
	private Job updatePlotData;

	// Internal Items
	private boolean isUpdateRunning = false;

	public DerivativeTool() {
		try {
			// Set up the listener for new traces
			this.traceListener = new ITraceListener.Stub() {
				// Response to traces plotted event, a bit complicated, has to deal with a
				// few cases
				@SuppressWarnings("unchecked")
				@Override
				public void tracesAdded(TraceEvent evt) {
					//First, if the event source is not a list or ITrace ignore event
					if (!(evt.getSource() instanceof List<?>) && !(evt.getSource() instanceof ITrace)) {
						return;
					}
					
					//If we are already running, ignore event
					if (!isUpdateRunning) {
						
						//Make a new list for the ITraces in,
						// deal with lists and single traces
						List<ITrace> eventSource = new ArrayList<ITrace>();
						if (evt.getSource() instanceof List<?>)
							eventSource = (List<ITrace>)evt.getSource();
						if (evt.getSource() instanceof ITrace) {
							eventSource.clear();
							eventSource.add((ITrace)evt.getSource());
						}
						
						if (getPlottingSystem() == null) return;
						
						//Cherry pick non history traces and user editable
						// and remove from the plot
						// Done here to minimise the "Jump" between the just plotted data
						// and the derivatives. Would be nicer to have an event before the data
						// is plotted
						List<ITrace> processableTraces = new ArrayList<ITrace>();
						for (ITrace trace: eventSource) {
							if (trace.isUserTrace() && 
									trace.getUserObject() != HistoryType.HISTORY_PLOT &&
									trace.getUserObject() != DerivativeTool.class) {
								
								processableTraces.add(trace);
								getPlottingSystem().removeTrace(trace);
								
							}
						}
							
						//If there is none, return
						if (processableTraces.isEmpty()) return;
						
						//We can now overwrite the eventTraceList with our new one
						eventTraceList = processableTraces;
						
						updatePlot();
					}
				}
				
				@Override
				public void tracesRemoved(TraceEvent evt) {
					eventTraceList.clear();
				}
				
				
			};

		} catch (Exception e) {
			logger.error("Cannot get plotting system!", e);
		}
		
		// Set up the listener for the gui elements	
		updateChecksSelection = new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (!isUpdateRunning) {
					logger.debug("Update plot called from widget");
					updatePlot(dataCheck.getSelection(),derivCheck.getSelection(),deriv2Check.getSelection());
				}
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
		infoLabel.setText("Open in a dedicated view to use with other tools.");

		dataCheck = new Button(composite, SWT.CHECK);
		dataCheck.setText("Display Data");
		dataCheck.setSelection(data);
		dataCheck.addSelectionListener(updateChecksSelection);
		derivCheck = new Button(composite, SWT.CHECK);
		derivCheck.setSelection(deriv);
		derivCheck.setText("Display f'(Data)");
		derivCheck.addSelectionListener(updateChecksSelection);
		deriv2Check = new Button(composite, SWT.CHECK);
		deriv2Check.setSelection(deriv2);
		deriv2Check.setText("Display f''(Data)");
		deriv2Check.addSelectionListener(updateChecksSelection);

		super.createControl(parent);
	}


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
		
		// Return if toolpage not set up
		if (getViewPart() == null) return;
		if (getPlottingSystem() == null) return;
		if (isDisposed()) return;
		getPlottingSystem().addTraceListener(traceListener);

		//This is probably going to lead to the first call to Update Plot
		//We need to get the user traces from the plot and store them for processing,
		// and remove them from the plot
		if (eventTraceList.isEmpty()) {
			for (ITrace trace : getPlottingSystem().getTraces(ILineTrace.class)) {
				if (trace.isUserTrace() && trace.getUserObject() != HistoryType.HISTORY_PLOT
						&& trace.getUserObject() != DerivativeTool.class) {
					eventTraceList.add(trace);
					getPlottingSystem().removeTrace(trace);
				}
			}
		}
		
		if (eventTraceList.isEmpty()) return;

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				logger.debug("Update plot called from activate");
				//Run the plot update on the GUI thread so we can check what needs to be plotted
				updatePlot(dataCheck.getSelection(),derivCheck.getSelection(),deriv2Check.getSelection());					
			}
		});


	}


	@Override
	public void deactivate() {
		
		//remove trace listener
		if (getPlottingSystem() != null) {
			getPlottingSystem().removeTraceListener(traceListener);
		}
		
		//Check not already deactivated to prevent double calls
		
		if (!isActive()) return;
		if (isDisposed()) return;
		super.deactivate();
		//If this is being synced to a dedicated view we dont
		//want to Update the plot
		if (duringSync) {
			duringSync = false;
			return;
		}
		
		if (!duringDispose) {
			logger.debug("Update plot called from deactivate");
			updatePlot(true,false,false);
		}
	}

	public void sync(IToolPage with) {
		if (!with.getClass().equals(getClass())) return;
		//Update dedicated window tool from old tool
		final DerivativeTool other = (DerivativeTool)with;
		//To prevent the old tool updating on deactivate
		other.duringSync = true;
		this.eventTraceList = other.eventTraceList;
		this.dataCheck.setSelection(other.dataCheck.getSelection());
		this.derivCheck.setSelection(other.derivCheck.getSelection());
		this.deriv2Check.setSelection(other.deriv2Check.getSelection());
		logger.debug("Update plot called from sync");
		updatePlot(dataCheck.getSelection(),derivCheck.getSelection(),deriv2Check.getSelection());
		
	}

	@Override
	public Control getControl() {
		if (composite==null) return null;
		return composite;
	}

	@Override
	public void dispose() {
		duringDispose = true;
		deactivate();
		
		//Important to call updateDerivates here over updatePlot
		// If we don't block this thread the plottingSystem become null
		// before we have finished.
		updateDerivatives(true,false,false);
		
		// clear all lists
		if (dataTraces!=null) dataTraces.clear();
		if (eventTraceList!=null) eventTraceList.clear();
		if (dervsPair!=null) dervsPair.clear();
		if (dervs2Pair!=null) dervsPair.clear();

		super.dispose();
	}
	
	private synchronized void updatePlot(boolean dataPlot, boolean derivPlot, boolean deriv2Plot) {
		data = dataPlot;
		deriv = derivPlot;
		deriv2 = deriv2Plot;
		updatePlot();
	}

	private synchronized void updatePlot() {

		if (updatePlotData==null) {
			updatePlotData = new Job("Derviative update") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						isUpdateRunning = true;
						logger.debug("Update Running");
						
						//Calculate all derivatives whether required or not
						// Ignore any non-user traces in the event
						dataTraces.clear();
						dervsPair.clear();
						dervs2Pair.clear();
						if (eventTraceList.isEmpty()) return Status.OK_STATUS;
						for (ITrace trace : eventTraceList) {
								if (!trace.isUserTrace() || trace.getUserObject() == HistoryType.HISTORY_PLOT)
									continue;
								dataTraces.add(trace);
								dervsPair.add(processTrace(trace, Derivative.FIRST));
								dervs2Pair.add(processTrace(trace, Derivative.SECOND));
						}

						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								//Run the plot update on the GUI thread
								updateDerivatives();						
							}
						});
						if (!isActive()) return Status.CANCEL_STATUS;
						return Status.OK_STATUS;
						
					}finally {
						logger.debug("Update Finished In Finally");
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
	
	private synchronized void updateDerivatives(boolean dataPlot, boolean derivPlot, boolean deriv2Plot) {
		data = dataPlot;
		deriv = derivPlot;
		deriv2 = deriv2Plot;
		updateDerivatives();
	}
	
	private synchronized void updateDerivatives() {
		try {
			logger.debug("Updating Derivatives");
			
			if (getPlottingSystem() == null) return;
			
			for (ITrace trace : getPlottingSystem().getTraces(ILineTrace.class)) {
				if (trace.isUserTrace() && trace.getUserObject() != HistoryType.HISTORY_PLOT) {
					getPlottingSystem().removeTrace(trace);
				}
			}
			
			//plot all required data, original data from traces
			// derivative data from Dataset pairs
			if (data) {
				for (ITrace trace : dataTraces) {
					getPlottingSystem().addTrace(trace);
				}
			}
			if (deriv) {
				int ic = dataTraces.size();
				for (DatasetPair dataset : dervsPair) {
					ILineTrace traceNew = getPlottingSystem().createLineTrace(dataset.y.getName());
					traceNew.setUserObject(DerivativeTool.class);
					traceNew.setUserTrace(true);
					traceNew.setData(dataset.x, dataset.y);
					traceNew.setTraceColor(ColorUtility.getSwtColour(ic));
					ic++;
					getPlottingSystem().addTrace(traceNew);
				}
			}
			if (deriv2) {
				int ic = dataTraces.size()*2;
				for (DatasetPair dataset : dervs2Pair) {
					ILineTrace traceNew = getPlottingSystem().createLineTrace(dataset.y.getName());
					traceNew.setUserObject(DerivativeTool.class);
					traceNew.setUserTrace(true);
					traceNew.setTraceColor(ColorUtility.getSwtColour(ic));
					ic++;
					traceNew.setData(dataset.x, dataset.y);
					
					getPlottingSystem().addTrace(traceNew);
				}
			}
			
	
			//Call repaint so the plotting system obeys button for whether rescale
			//should happen or not
			getPlottingSystem().repaint();
			if (isActive())
				getPlottingSystem().addTraceListener(traceListener);
			
			logger.debug("Update Finished In updateDerivatives");
		} catch (Throwable ne) {
			logger.error("Internal error in derivative tool!", ne);
		}
	}
	
	private DatasetPair processTrace(ITrace trace, Derivative type){
		
		// Calculate the derivative from the data in trace,
		// return as an abstract dataset since we dont want to interact with the plot here
		// to generate the traces
		final Dataset traceData =  DatasetUtils.convertToDataset(trace.getData());
		
		//Get x data if present or if not generate index range
		final Dataset x = (trace instanceof ILineTrace) 
				? DatasetUtils.convertToDataset(((ILineTrace)trace).getXData()) 
			    : DatasetFactory.createRange(IntegerDataset.class, 0, traceData.getSize(), 1);

		Dataset derv = null;
		
		if (type == Derivative.FIRST)
			derv = Maths.derivative(x, traceData, SMOOTHING);
		else if (type==Derivative.SECOND)
			derv = Maths.derivative(x,Maths.derivative(x, traceData, SMOOTHING), SMOOTHING);
		
		return new DatasetPair(x,derv);
	}
}
	
	



/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.plotting.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.tool.AbstractToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.plotting.api.trace.ITraceListener;
import org.eclipse.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.IPageSite;

public class DerivativeToolExternalPlot extends AbstractToolPage {

	private IPlottingSystem<Composite> system;
	private ITraceListener traceListener;
	private boolean[] model;
	private DerivativeJob2 job2;
		
	public DerivativeToolExternalPlot() {

		try {
			system = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		this.traceListener = new ITraceListener.Stub() {
			
			@Override
			public void traceAdded(TraceEvent evt) {
				if (!checkEvent(evt)) return;
				DerivativeToolExternalPlot.this.update();
			}
			
			@Override
			public void tracesAdded(TraceEvent evt) {
				if (!checkEvent(evt)) return;
				DerivativeToolExternalPlot.this.update();
			}
			
			@Override
			public void traceUpdated(TraceEvent evt) {
				if (!checkEvent(evt)) return;
				DerivativeToolExternalPlot.this.update();
			}
			
			@Override
			public void tracesUpdated(TraceEvent evt) {
				if (!checkEvent(evt)) return;
				DerivativeToolExternalPlot.this.update();
			}
			
			@Override
			public void traceRemoved(TraceEvent evt) {
				Collection<ITrace> traces = getPlottingSystem().getTraces(ILineTrace.class);
				
				if (traces == null || traces.isEmpty()) {
					system.clear();
					return;
				}
				
				DerivativeToolExternalPlot.this.update();
				
			}
			
			@Override
			public void tracesRemoved(TraceEvent evt) {
				Collection<ITrace> traces = getPlottingSystem().getTraces(ILineTrace.class);
				if (traces == null || traces.isEmpty()) {
					system.clear();
					return;
				}
				DerivativeToolExternalPlot.this.update();
			}
			
		};
		
		model = new boolean[]{false,true,false};
		job2 = new DerivativeJob2(system);
	}
	
	protected boolean checkEvent(TraceEvent evt) {
		
		//First, if the event source is not a list or ITrace ignore event
		if (!(evt.getSource() instanceof List<?>) && !(evt.getSource() instanceof ITrace)) {
			return false;
		}
		List<ITrace> eventSource = new ArrayList<ITrace>();
		if (evt.getSource() instanceof List<?>)
			eventSource = (List<ITrace>)evt.getSource();
		if (evt.getSource() instanceof ITrace) {
			eventSource.clear();
			eventSource.add((ITrace)evt.getSource());
		}
		
		for (ITrace t : eventSource) if (t.getUserObject() instanceof ITrace) return false;
		
		return true;
		
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_1D;
	}

	@Override
	public void createControl(Composite parent) {
		final IPageSite site = getSite();
		
		createActions();
		
		IActionBars actionbars = site!=null?site.getActionBars():null;
		system.createPlotPart(parent, 
				getTitle(), 
				actionbars, 
				PlotType.XY,
				this.getViewPart());

		super.createControl(parent);
	}
	
	private void update() {
		IPlottingSystem<Composite> oSys = getPlottingSystem();
		Collection<ITrace> traces = oSys.getTraces(ILineTrace.class);
		
		if (traces == null || traces.isEmpty())  {
			system.clear();
			return;
		}
//		job2.cancel();
		job2.updateData(traces, model);
		job2.schedule();
		
	}
	
	@Override
	public void activate() {
		if (isActive()) return;
		if (getPlottingSystem() ==  null) return;
		getPlottingSystem().addTraceListener(traceListener);
		update();
		super.activate();
	}
	
	@Override
	public void deactivate() {
		if (getPlottingSystem()!=null) getPlottingSystem().removeTraceListener(traceListener);
		super.deactivate();
	}
	
	private void createActions() {
		//final MenuAction modeSelect= new MenuAction("Select Mode");

		final Action original = new Action("Original",IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				model[0] = isChecked();
				update();
			}
		};
		
		original.setImageDescriptor(Activator.getImageDescriptor("icons/function.png"));
		
		final Action first = new Action("First",IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				model[1] = isChecked();
				update();
			}
		};
		
		first.setImageDescriptor(Activator.getImageDescriptor("icons/firstder.png"));
		
		final Action second = new Action("Second",IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				model[2] = isChecked();
				update();
			}
		};
		
		second.setImageDescriptor(Activator.getImageDescriptor("icons/secondder.png"));
		
		original.setChecked(model[0]);
		first.setChecked(model[1]);
		second.setChecked(model[2]);
		
		getSite().getActionBars().getToolBarManager().add(original);
		getSite().getActionBars().getToolBarManager().add(first);
		getSite().getActionBars().getToolBarManager().add(second);
		
		getSite().getActionBars().getMenuManager().add(original);
		getSite().getActionBars().getMenuManager().add(first);
		getSite().getActionBars().getMenuManager().add(second);
		
	}

	@Override
	public Control getControl() {
		if (system != null) return system.getPlotComposite();
		return null;
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class clazz) {
		if (clazz == IToolPageSystem.class) {
			return system.getAdapter(IToolPageSystem.class);
		} else if (clazz == IPlottingSystem.class) {
			return system;
		} else {
			return super.getAdapter(clazz);
		}
	}

	@Override
	public void setFocus() {
		if (system != null) system.setFocus();
	}
	
	
	private class DerivativeJob2 extends Job {

		private boolean[] model;
		Collection<ITrace> traces;
		
		public DerivativeJob2(IPlottingSystem<Composite> system) {
			super("Derivative Update");
		}
		
		public void updateData(Collection<ITrace> traces, boolean[] model) {
			this.traces = traces;
			this.model = model;
		}

		private ITrace createTrace(Dataset x, Dataset y, ILineTrace trace, int der) {
			system.clear();
			Dataset yout = y.clone();

			for (int i = 0; i < der; i++) {
				yout = Maths.derivative(x, yout, 1);
			}
			
			ILineTrace lt = system.createLineTrace(yout.getName());
			lt.setUserObject(trace);
			lt.setData(x, yout);
			lt.setTraceColor(trace.getTraceColor());
			
			return lt;
		}
		
		
		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			system.clear();
			final List<ITrace> ot = new ArrayList<ITrace>();
			
			for (ITrace trace : traces) {
				if (trace instanceof ILineTrace) {
					if (monitor.isCanceled()) return Status.CANCEL_STATUS;
					final ILineTrace t = (ILineTrace)trace;
					Dataset x = DatasetUtils.convertToDataset(t.getXData());
					Dataset y = DatasetUtils.convertToDataset(t.getYData());
					
					if (x == null || y == null) return Status.CANCEL_STATUS;
					if (x.getSize() != y.getSize()) return Status.CANCEL_STATUS;
					
					if (model[0]) ot.add(createTrace(x, y, t, 0));
					if (model[1]) ot.add(createTrace(x, y, t, 1));
					if (model[2]) ot.add(createTrace(x, y, t, 2));
				}
			}
			
			Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {

					for (ITrace t : ot) {
						if (monitor.isCanceled()) return;
						system.addTrace(t);
					}
					
					system.repaint();						
				}
			});
			
			return Status.OK_STATUS;
		}
		
	}

}

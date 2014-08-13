package org.dawnsci.plotting.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawnsci.plotting.util.ColorUtility;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
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

import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

public class DerivativeToolExternalPlot extends AbstractToolPage {

	IPlottingSystem system;
	private ITraceListener traceListener;
	private boolean[] model;
	
	//Derivative type
	private enum Derivative {
		NONE,FIRST,SECOND
	}
	
	public DerivativeToolExternalPlot() {

		try {
			system = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		this.traceListener = new ITraceListener.Stub() {
			
			public void traceAdded(TraceEvent evt) {
				
				//First, if the event source is not a list or ITrace ignore event
				if (!(evt.getSource() instanceof List<?>) && !(evt.getSource() instanceof ITrace)) {
					return;
				}
				
				List<ITrace> eventSource = new ArrayList<ITrace>();
				if (evt.getSource() instanceof List<?>)
					eventSource = (List<ITrace>)evt.getSource();
				if (evt.getSource() instanceof ITrace) {
					eventSource.clear();
					eventSource.add((ITrace)evt.getSource());
				}
				
				for (ITrace t : eventSource) if (t.getUserObject() instanceof ITrace) return;
				
				DerivativeToolExternalPlot.this.update();
			}
			
			@Override
			public void tracesAdded(TraceEvent evt) {
				//First, if the event source is not a list or ITrace ignore event
				if (!(evt.getSource() instanceof List<?>) && !(evt.getSource() instanceof ITrace)) {
					return;
				}
				List<ITrace> eventSource = new ArrayList<ITrace>();
				if (evt.getSource() instanceof List<?>)
					eventSource = (List<ITrace>)evt.getSource();
				if (evt.getSource() instanceof ITrace) {
					eventSource.clear();
					eventSource.add((ITrace)evt.getSource());
				}
				
				for (ITrace t : eventSource) if (t.getUserObject() instanceof ITrace) return;
				
				DerivativeToolExternalPlot.this.update();
			}
			
			@Override
			public void traceRemoved(TraceEvent evt) {
				IJobManager jobMan = Job.getJobManager();
				Job[] found = jobMan.find(DerivativeToolExternalPlot.this);
				
				for (Job j : found) {
					if (j instanceof DerivativeJob) {
						if (((DerivativeJob)j).isThisTrace(evt.getSource())) {
							j.cancel();
						}
					}
				}
				
				for (ITrace t : system.getTraces(ILineTrace.class)) {
					if (t.getUserObject().equals(evt.getSource())) {
						system.removeTrace(t);
						// autoscale when we remove the trace in case the range of the left over traces is different
						system.autoscaleAxes();
					}
				}
				
			}
			
			@Override
			public void tracesRemoved(TraceEvent evt) {
				IJobManager jobMan = Job.getJobManager();
				Job[] found = jobMan.find(DerivativeToolExternalPlot.this);
				
				for (Job j : found) {
					if (j instanceof DerivativeJob) {
						j.cancel();
					}
				}
				
				system.clear();
			}
			
		};
		
		model = new boolean[]{false,true,false};
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
	}
	
	private void update() {
		IPlottingSystem oSys = getPlottingSystem();
		system.clear();
		Collection<ITrace> traces = oSys.getTraces(ILineTrace.class);
		
		if (traces == null || traces.isEmpty()) return;
		
		if (model[0]) new DerivativeJob(traces, system, Derivative.NONE).schedule();
		
		if (model[1]) new DerivativeJob(traces, system, Derivative.FIRST).schedule();
		
		if (model[2]) new DerivativeJob(traces, system, Derivative.SECOND).schedule();
		
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
		
//		modeSelect.add(original);
//		modeSelect.add(first);
//		modeSelect.add(second);
		
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
			return system;
		} else {
			return super.getAdapter(clazz);
		}
	}

	@Override
	public void setFocus() {
		if (system != null) system.setFocus();
	}
	
	private class DerivativeJob extends Job {
		
		IPlottingSystem system;
		Collection<ITrace> traces;
		Derivative type;

		public DerivativeJob(Collection<ITrace> traces, IPlottingSystem system, Derivative type) {
			super("Derivative Update");
			this.system = system;
			this.traces = traces;
			this.type = type;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			for (ITrace trace : traces) {
				if (trace instanceof ILineTrace) {
					final ILineTrace t = (ILineTrace)trace;
					Dataset x = DatasetUtils.convertToDataset(t.getXData());
					Dataset y = DatasetUtils.convertToDataset(t.getYData());
					
					if (x == null || y == null) return Status.CANCEL_STATUS;
					
					switch (type) {
					case FIRST:
						y = Maths.derivative(x, y, 1);
						break;
					case SECOND:
						y = Maths.derivative(x,Maths.derivative(x, y, 1), 1);
					default:
						break;
					}
					
					final Dataset yf = y;
					final Dataset xf = x;
					
					Display.getDefault().syncExec(new Runnable() {
						
						@Override
						public void run() {
							ILineTrace lt = system.createLineTrace(yf.getName());
							lt.setUserObject(t);
							lt.setData(xf, yf);
							lt.setTraceColor(ColorUtility.getSwtColour(system.getTraces().size()));
							system.addTrace(lt);
							system.repaint();
						}
					});
				}
			}
			
			return Status.OK_STATUS;
		}
		
		public boolean isThisTrace(Object trace) {
			return this.traces.equals(trace);
		}
		
		@Override
		public boolean belongsTo(Object family) {
			return DerivativeToolExternalPlot.this.equals(family);
		}
		
	}

}

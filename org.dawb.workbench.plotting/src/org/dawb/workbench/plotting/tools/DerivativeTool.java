package org.dawb.workbench.plotting.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.AbstractPlottingSystem.ColorOption;
import org.dawb.common.ui.plot.PlotType;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.IPageSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

public class DerivativeTool extends AbstractToolPage  {

	private final static Logger logger = LoggerFactory.getLogger(DerivativeTool.class);
	
	protected AbstractPlottingSystem plotter;
	private   ITraceListener         traceListener;

	
	public DerivativeTool() {
		try {
			plotter = PlottingFactory.getPlottingSystem();
			plotter.setColorOption(ColorOption.NONE);
			plotter.setDatasetChoosingRequired(false);
			this.traceListener = new ITraceListener.Stub() {
				@Override
				public void tracesPlotted(TraceEvent evt) {
					
					if (!(evt.getSource() instanceof List<?>)) {
						return;
					}
					updateDerviatives();
				}
			};
		} catch (Exception e) {
			logger.error("Cannot get plotting system!", e);
		}
	}
	
	@Override
	public void createControl(Composite parent) {


		final IPageSite site = getSite();
		
		plotter.createPlotPart(parent, 
								getTitle(), 
								site.getActionBars(), 
								PlotType.PT1D,
								null);
				
		
		getPlottingSystem().addTraceListener(traceListener);
		
		updateDerviatives();
			
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
	}
	
	@Override
	public Control getControl() {
		if (plotter==null) return null;
		return plotter.getPlotComposite();
	}
	
	public void dispose() {
		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeTraceListener(traceListener);
		}
		if (plotter!=null) plotter.dispose();
		plotter = null;
		super.dispose();
	}


	private Job updateDerivatives;
	
	/**
	 * The user can optionally nominate an x. In this case, we would like to 
	 * use it for the derviative instead of the indices of the data. Therefore
	 * there is some checking here to see if there are x values to plot.
	 * 
	 * Normally everything will be ILineTraces even if the x is indices.
	 */
	private void updateDerviatives() {

		if (updateDerivatives==null) {
			updateDerivatives = new Job("Derviative update") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					final Collection<ITrace>    traces= getPlottingSystem().getTraces();
					final List<AbstractDataset> dervs = new ArrayList<AbstractDataset>(traces.size());

					ITrace firstTrace = null;
					for (ITrace trace : traces) {

						if (firstTrace==null) firstTrace = trace;
						final AbstractDataset plot = trace.getData();
						AbstractDataset x = (trace instanceof ILineTrace) 
								         ? ((ILineTrace)trace).getXData() 
										: AbstractDataset.arange(0, plot.getSize(), 1, AbstractDataset.INT32);

						final AbstractDataset derv = Maths.derivative(x, plot, 1);

						derv.setName("f"+getTicksFor(1)+"{" +plot.getName()+"}");
						dervs.add(derv);
					}
					plotter.clear();

					if (dervs.size()>0 && firstTrace!=null) {

						AbstractDataset x = (firstTrace instanceof ILineTrace) 
								        ? ((ILineTrace)firstTrace).getXData() 
										: AbstractDataset.arange(0, dervs.get(0).getSize(), 1, AbstractDataset.INT32);

						if (x.getName()==null || "".equals(x.getName())) x.setName("Indices");

						// Often people can have data plotted with infinities. We 
						// replace them here just for convenience.
						for (AbstractDataset a : dervs)  DatasetUtils.removeNansAndInfinities(a, new Double(0));
						plotter.createPlot1D(x, dervs, monitor); 
					}

					return Status.OK_STATUS;
				}	
			};
			updateDerivatives.setSystem(true);
			updateDerivatives.setUser(false);
			updateDerivatives.setPriority(Job.INTERACTIVE);
		}


		updateDerivatives.schedule();
	}

	private String getTicksFor(int size) {
		final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < size; i++) buf.append("'");
        return buf.toString();
	}

}

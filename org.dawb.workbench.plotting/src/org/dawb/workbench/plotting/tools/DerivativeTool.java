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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.IPageSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

public class DerivativeTool extends AbstractToolPage  {

	private final static Logger logger = LoggerFactory.getLogger(DerivativeTool.class);
	
	protected AbstractPlottingSystem plotter;
	private ITraceListener traceListener;
	private Composite container;

	
	public DerivativeTool() {
		try {
			plotter = PlottingFactory.getPlottingSystem();
			plotter.setColorOption(ColorOption.NONE);
			plotter.setDatasetChoosingRequired(false);
			this.traceListener = new ITraceListener.Stub() {
				@Override
				public void tracesPlotted(TraceEvent evt) {
					
					if (!(evt.getSource() instanceof List<?>)) return;
					updateDerviatives();
				}
			};
		} catch (Exception e) {
			logger.error("Cannot get plotting system!", e);
		}
	}
	
	@Override
	public void createControl(Composite parent) {

		container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());

		final IPageSite site = getSite();
		
		plotter.createPlotPart(container, 
								getTitle(), 
								site.getActionBars(), 
								PlotType.PT1D,
								null);
		
		updateDerviatives();
		
		getPlottingSystem().addTraceListener(traceListener);
	}

	@Override
	public void setFocus() {
		
	}
	
	@Override
	public Control getControl() {
		return container;
	}
	
	public void dispose() {
		if (getPlottingSystem()!=null) {
			getPlottingSystem().removeTraceListener(traceListener);
		}
		if (plotter!=null) plotter.dispose();
		plotter = null;
		super.dispose();
	}


	
	/**
	 * The user can optionally nominate an x. In this case, we would like to 
	 * use it for the derviative instead of the indices of the data. Therefore
	 * there is some checking here to see if there are x values to plot.
	 * 
	 * Normally everything will be ILineTraces even if the x is indices.
	 */
	private void updateDerviatives() {
		
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

        	plotter.createPlot1D(x, dervs, null); 
        }
	}

	private String getTicksFor(int size) {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < size; i++) buf.append("'");
        return buf.toString();
	}

}

package org.dawb.workbench.plotting.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.AbstractPlottingSystem.ColorOption;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.IPageSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;

public class DerivativeTool extends AbstractToolPage implements ITraceListener {

	private final static Logger logger = LoggerFactory.getLogger(DerivativeTool.class);
	
	protected AbstractPlottingSystem plotter;

	private Composite container;
	
	public DerivativeTool() {
		try {
			plotter = PlottingFactory.getPlottingSystem();
			plotter.setColorOption(ColorOption.NONE);
			plotter.setDatasetChoosingRequired(false);
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
		
		getPlottingSystem().addTraceListener(this);
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
			getPlottingSystem().removeTraceListener(this);
		}
		if (plotter!=null) plotter.dispose();
		plotter = null;
		super.dispose();
	}

	@Override
	public void tracesAltered(TraceEvent evt) {
		
	}

	@Override
	public void tracesCleared(TraceEvent evet) {
		plotter.clear();
	}

	@Override
	public void tracesPlotted(TraceEvent evt) {
		
		if (!(evt.getSource() instanceof List<?>)) return;
		updateDerviatives();
	}
	
	private void updateDerviatives() {
		final Collection<ITrace>    traces= getPlottingSystem().getTraces();
		final List<AbstractDataset> dervs = new ArrayList<AbstractDataset>(traces.size());

        for (ITrace trace : traces) {
			
			final AbstractDataset plot = trace.getData();
			final AbstractDataset derv = Maths.derivative(AbstractDataset.arange(0, plot.getSize(), 1, AbstractDataset.INT32), plot, 1);
			
			derv.setName("f"+getTicksFor(1)+" {" +plot.getName()+"}");
			dervs.add(derv);
		}
        plotter.clear();
        if (dervs.size()>0) {
        	
        	AbstractDataset x = AbstractDataset.arange(0, dervs.get(0).getSize(), 1, AbstractDataset.INT32);
			x.setName("Indices");

        	plotter.createPlot1D(x, dervs, null); 
        }
	}

	private String getTicksFor(int size) {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < size; i++) buf.append("'");
        return buf.toString();
	}

}

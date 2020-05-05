package org.dawnsci.datavis.manipulation.componentfit;

import java.util.Iterator;

import org.dawnsci.plotting.actions.ActionBarWrapper;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class ComponentFitDialog extends Dialog {

	private ComponentFitModel model;
	
	private IPlottingSystem<Composite> fitPlot;
	private IPlottingSystem<Composite> concPlot;
	private IPlottingSystem<Composite> resPlot;
	
	private ComponentFitStackResult result;
	

	public ComponentFitDialog(Shell parentShell, ComponentFitModel model) {
		super(parentShell);
		
        this.model = model;
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Component Fit");
	}
	
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		
		try {
			BundleContext bundleContext =
	                FrameworkUtil.
	                getBundle(this.getClass()).
	                getBundleContext();
			
			IPlottingService plottingService = bundleContext.getService(
					 						   bundleContext.getServiceReference(IPlottingService.class));
			fitPlot = plottingService.createPlottingSystem();
			concPlot = plottingService.createPlottingSystem();
			resPlot = plottingService.createPlottingSystem();
		} catch (Exception e) {
			return container;
		}
		
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite topPane = new Composite(container, SWT.NONE);
		topPane.setLayout(new GridLayout(2, false));
		topPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		ActionBarWrapper abwfit = ActionBarWrapper.createActionBars(topPane, null);
		ActionBarWrapper abwconc = ActionBarWrapper.createActionBars(topPane, null);
		fitPlot.createPlotPart(topPane, "Fit Plot", abwfit, PlotType.XY, null);
		fitPlot.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		concPlot.createPlotPart(topPane, "Concentration Plot", abwconc, PlotType.XY, null);
		concPlot.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		new Label(topPane, SWT.NONE);
		ActionBarWrapper abwres = ActionBarWrapper.createActionBars(topPane, null);
		
		Composite config = new Composite(topPane, SWT.None);
		config.setLayoutData(GridDataFactory.fillDefaults().create());
		config.setLayout(new GridLayout());
		
		Group traceSelectGroup = new Group(config,SWT.None);
		traceSelectGroup.setText("Select Trace");
		traceSelectGroup.setLayoutData(GridDataFactory.fillDefaults().create());
		traceSelectGroup.setLayout(new GridLayout());
		
		Spinner spin = new  Spinner(traceSelectGroup, SWT.None);
		
		resPlot.createPlotPart(topPane, "Residual Plot", abwres, PlotType.XY, null);
		resPlot.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		
		fitPlot.setKeepAspect(false);
		
		result = model.runFit();
		IDataset concentrations = result.getConcentrations();
		
		Iterator<IDataset> it = result.getConcentrationIterator();
		
		while (it.hasNext()) {
			IDataset d = it.next();
			if (d == null) continue;
			ILineTrace t = MetadataPlotUtils.buildLineTrace(d, concPlot);
			concPlot.addTrace(t);
		}
		
		
		IDataset rms = result.getSeriesResidualRMS();
		ILineTrace t = MetadataPlotUtils.buildLineTrace(rms, resPlot);
		resPlot.addTrace(t);
		
		
		concPlot.repaint();
		resPlot.repaint();
		
		
		spin.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		spin.setMinimum(0);
		spin.setMaximum(concentrations.getShape()[1]-1);
		
		spin.addSelectionListener(new SelectionAdapter() 
		 {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				showData(spin.getSelection());
				
			}
			
		});
		
		showData(0);
		
		return container;
	}
	
	private void showData(int i) {

		fitPlot.clear();

		Display display = Display.getCurrent();
		Color black = display.getSystemColor(SWT.COLOR_BLACK);
		Color gray = display.getSystemColor(SWT.COLOR_GRAY);

		ComponentFitResult data = result.getData(i);

		IDataset d = data.getData();
		d.setName("data");
		ILineTrace t0 = MetadataPlotUtils.buildLineTrace(d, fitPlot);
		t0.setLineWidth(2);
		t0.setTraceColor(black);

		Iterator<IDataset> dataIter = data.getFittedDataIterator();
		
		while(dataIter.hasNext()) {
			IDataset next = dataIter.next();
			if (next == null) {
				continue;
			}
			ILineTrace t = MetadataPlotUtils.buildLineTrace(next, fitPlot);
			fitPlot.addTrace(t);
		}

		fitPlot.addTrace(t0);

		IDataset sum = data.getSum();
		
		ILineTrace ts = MetadataPlotUtils.buildLineTrace(sum, fitPlot);
		ts.setTraceType(TraceType.DOT_LINE);
		ts.setTraceColor(gray);
		ts.setLineWidth(2);
		fitPlot.addTrace(ts);

		IDataset residual = data.getResidual();
		
		ILineTrace tr = MetadataPlotUtils.buildLineTrace(residual, fitPlot);
		tr.setTraceType(TraceType.DASH_LINE);
		tr.setTraceColor(gray);
		tr.setLineWidth(2);
		fitPlot.addTrace(tr);

		fitPlot.repaint();
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1000, 800);
	}


	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	public boolean close() {

		disposePlots(fitPlot,concPlot,resPlot);

		return super.close();
	}


	private void disposePlots(IPlottingSystem<?>... plots) {

		for (IPlottingSystem<?> p : plots) {
			if (p != null && !p.isDisposed()) {
				p.dispose();
			}
		}
	}
}

package org.dawnsci.processing.ui;

import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.processing.ui.slice.ProcessingLogDisplay;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessingOutputView extends ViewPart {

	private final static Logger logger = LoggerFactory.getLogger(ProcessingOutputView.class);

	private IPlottingSystem<Composite> output;
	private IPlottingSystem<Composite> display;

	private ProcessingLogDisplay logDisplay;

	@Override
	public void createPartControl(Composite parent) {
		SashForm plotSash = new SashForm(parent, SWT.HORIZONTAL);
		plotSash.setLayout(new FillLayout());

		Composite outputPlotComp = new Composite(plotSash, SWT.NONE);
		outputPlotComp.setLayout(new GridLayout(1, false));
		ActionBarWrapper actionBarWrapper = ActionBarWrapper.createActionBars(outputPlotComp, null);
		Composite outputPlotSubComp = new Composite(outputPlotComp, SWT.NONE);
		outputPlotSubComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		outputPlotSubComp.setLayout(new GridLayout(1, false));

		SashForm displaySash = new SashForm(plotSash, SWT.VERTICAL);
		displaySash.setLayout(new FillLayout());
		Composite displayPlotComp = new Composite(displaySash, SWT.BORDER);
		displayPlotComp.setLayout(new FillLayout());
		logDisplay = new ProcessingLogDisplay(displaySash);

		try {
			output = PlottingFactory.createPlottingSystem();
			output.createPlotPart(outputPlotSubComp, "Slice", actionBarWrapper, PlotType.IMAGE, null);
			output.getPlotComposite().setLayoutData(outputPlotSubComp.getLayoutData());
			
			display = PlottingFactory.createPlottingSystem();
			display.createPlotPart(displayPlotComp, "Display", null, PlotType.XY, null);
		} catch (Exception e) {
			logger.error("cannot create plotting system",e);
		}

		displaySash.setWeights(new int[] {2, 1});
		plotSash.layout();
	}

	@Override
	public void setFocus() {
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (IPlottingSystem.class.equals(adapter)) {
			return (T) output;
		}

		return super.getAdapter(adapter);
	}

	public IPlottingSystem<Composite> getDisplayPlot() {
		return display;
	}

	public ProcessingLogDisplay getLogDisplay() {
		return logDisplay;
	}

	@Override
	public void dispose() {
		super.dispose();
		logDisplay.dispose();
	}
}

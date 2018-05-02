package org.dawnsci.processing.ui;

import org.dawnsci.processing.ui.slice.ProcessingLogDisplay;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
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
	
	private IProcessDisplayHelper displayHelper;
	
	public ProcessingOutputView() {
		try {
			output = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void createPartControl(Composite parent) {
		SashForm plotSash = new SashForm(parent, SWT.HORIZONTAL);
		plotSash.setLayout(new FillLayout());

		Composite outputPlotComp = new Composite(plotSash, SWT.NONE);
		outputPlotComp.setLayout(new GridLayout(1, false));
		Composite outputPlotSubComp = new Composite(outputPlotComp, SWT.NONE);
		outputPlotSubComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		outputPlotSubComp.setLayout(new GridLayout(1, false));

		SashForm displaySash = new SashForm(plotSash, SWT.VERTICAL);
		displaySash.setLayout(new FillLayout());
		Composite displayPlotComp = new Composite(displaySash, SWT.BORDER);
		displayPlotComp.setLayout(new FillLayout());
		logDisplay = new ProcessingLogDisplay(displaySash);
		
		displayHelper = new ProcessDisplayHelperImpl(plotSash, displaySash);
		

		try {
			
			output.createPlotPart(outputPlotSubComp, "Slice", getViewSite().getActionBars(), PlotType.IMAGE, this);
			output.getPlotComposite().setLayoutData(outputPlotSubComp.getLayoutData());
			
			display = PlottingFactory.createPlottingSystem();
			display.createPlotPart(displayPlotComp, "Display", null, PlotType.XY, null);
		} catch (Exception e) {
			logger.error("cannot create plotting system",e);
		}

		displaySash.setWeights(new int[] {2, 1});
		plotSash.setWeights(new int[] {1,0});
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
		if (IToolPageSystem.class == adapter) return output.getAdapter(adapter);
		
		if (IProcessDisplayHelper.class.equals(adapter)) {
			return(T) displayHelper;
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
	
	private class ProcessDisplayHelperImpl implements IProcessDisplayHelper {
		
		private SashForm plotSash;
		private SashForm displaySash;
		
		public ProcessDisplayHelperImpl(SashForm plotSash, SashForm displaySash) {
			this.plotSash = plotSash;
			this.displaySash = displaySash;
		}
		
		@Override
		public IPlottingSystem<Composite> getDisplayPlot() {
			return display;
		}

		@Override
		public ProcessingLogDisplay getLogDisplay() {
			return logDisplay;
		}

		@Override
		public void setDisplayMode(ProcessDisplayOptions mode) {
			
			switch (mode) {
			case OUTPUT_ONLY:
				plotSash.setWeights(new int[] {1,0});
				break;
			case OUTPUT_DISPLAY:
				plotSash.setWeights(new int[] {1,1});
				displaySash.setWeights(new int[] {1,0});
				break;
			case OUTPUT_LOG:
				plotSash.setWeights(new int[] {1,1});
				displaySash.setWeights(new int[] {0,1});
				break;
			case ALL:
				plotSash.setWeights(new int[] {1,1});
				displaySash.setWeights(new int[] {2,1});
				break;
			default:
				break;
			}
			
			displaySash.layout();
			plotSash.layout();
		}
		
	}
}

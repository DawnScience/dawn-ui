package org.dawnsci.dedi.ui.views.plot;

import java.util.Arrays;

import org.dawnsci.dedi.ui.widgets.plotting.Legend;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

public class BeamlineConfigurationPlotView extends ViewPart {
	private IPlottingSystem<Composite> system;
	
	/**
	 * A delegate that takes care of creating the plot and updating it whenever a change occurs in one of the relevant models.
	 * This is a use of the 'Strategy design pattern', which allows to dynamically change the type and properties of the plot.
	 */
	private AbstractBeamlineConfigurationPlot plot;
	/**
	 * Composite where the {@link AbstractBeamlineConfigurationPlot} can put the controls needed to configure it.
	 */
	private Composite plotConfigurationPanel;  
	/**
	 * Composite where the {@link AbstractBeamlineConfigurationPlot} can put its legend.
	 */
	private Legend legend; 
	
	public static final String ID = "dedi.plottingview";
	
	public BeamlineConfigurationPlotView() {
		try {
			system = PlottingFactory.createPlottingSystem(); 
		} catch (Exception e) {
			e.printStackTrace();
			// Creates the view but there will be no plotting system
			system = null;
		}
	}

	
	@Override
	public void createPartControl(Composite parent) {
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayout(new GridLayout(3, false));
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		PageBook plotComposite = new PageBook(sashForm, SWT.NONE);
		system.createPlotPart(plotComposite, getPartName(), getViewSite().getActionBars(), PlotType.IMAGE, this);  
		plotComposite.showPage(system.getPlotComposite());
		
		ScrolledComposite scrolledComposite = new ScrolledComposite( sashForm, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		
		sashForm.setWeights(new int[]{70, 30});
		
		Composite controlsPanel = new Composite(scrolledComposite, SWT.NONE);
		GridLayoutFactory.swtDefaults().spacing(0, 20).numColumns(1).applyTo(controlsPanel);
		
		legend = new Legend(controlsPanel);
		
		plotConfigurationPanel = new Composite(controlsPanel, SWT.NONE);
		plotConfigurationPanel.setLayout(new GridLayout());
		
		plot = new DefaultBeamlineConfigurationPlot(system);
		plot.createPlotControls(plotConfigurationPanel, legend);
		
		parent.layout();
		
	    scrolledComposite.setMinSize( controlsPanel.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
	    controlsPanel.addListener(SWT.Resize, new Listener() {
			int width = -1;
			@Override
			public void handleEvent(Event event) {
				 int newWidth = controlsPanel.getSize().x;
			     if (newWidth != width) {
			        scrolledComposite.setMinHeight(controlsPanel.computeSize(newWidth, SWT.DEFAULT).y);
			        width = newWidth;
			     }
			}
		});
		scrolledComposite.setContent(controlsPanel);	
		
		system.setRescale(false);
		system.setKeepAspect(true); // Keep the aspect ratio.
	}
	
	
	public void setPlotType(AbstractBeamlineConfigurationPlot plot){
		if(this.plot != null)
			removePlot();
		this.plot = plot;
		plot.createPlotControls(plotConfigurationPanel, legend);
		plot.updatePlot();
	}
	
	
	public void removePlot() {
		system.clearRegions();
		for(IRegion region : system.getRegions()) system.removeRegion(region);
		for(ITrace trace : system.getTraces()) system.removeTrace(trace);
		legend.removeAllLegendItems();
		for(Control control : Arrays.asList(plotConfigurationPanel.getChildren()))
			if(!control.isDisposed()) control.dispose();
		plot.dispose(); // Let the plot dispose its resources and unregister listeners.
	}
	
	
	@Override
	public void dispose() {
		plot.dispose();
		plot = null;
		super.dispose();
	}
	

	@Override
	public void setFocus() {
		system.setFocus();
	}
}

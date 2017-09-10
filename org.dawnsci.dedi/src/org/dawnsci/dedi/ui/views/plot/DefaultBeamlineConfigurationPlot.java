package org.dawnsci.dedi.ui.views.plot;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.dawnsci.dedi.configuration.BeamlineConfiguration;
import org.dawnsci.dedi.configuration.calculations.results.controllers.AbstractResultsController;
import org.dawnsci.dedi.configuration.calculations.results.models.ResultsService;
import org.dawnsci.dedi.ui.GuiHelper;
import org.dawnsci.dedi.ui.views.plot.plotters.IBeamlineConfigurationPlotter;
import org.dawnsci.dedi.ui.views.plot.plotters.PhysicalSpacePlotter;
import org.dawnsci.dedi.ui.views.plot.plotters.PixelSpacePlotter;
import org.dawnsci.dedi.ui.views.plot.plotters.QSpacePlotter;
import org.dawnsci.dedi.ui.widgets.plotting.ColourChangeEvent;
import org.dawnsci.dedi.ui.widgets.plotting.ColourChangeListener;
import org.dawnsci.dedi.ui.widgets.plotting.Legend;
import org.dawnsci.dedi.ui.widgets.plotting.LegendItem;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PreferencesUtil;

import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSelectedListener;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSelectionEvent;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSpacing;
import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationFactory;

public class DefaultBeamlineConfigurationPlot extends AbstractBeamlineConfigurationPlot
             implements  PropertyChangeListener, ColourChangeListener, CalibrantSelectedListener {
	
	private BeamlineConfiguration beamlineConfiguration;
	private AbstractResultsController resultsController;
	private IBeamlineConfigurationPlotter plotter;
	
	
	// These strings must be exactly as they are, because I'm using java reflection to 
	// access the corresponding ...IsPlot fields below.
	private String[]  plotItems = {"detector", "beamstop", "cameraTube", "ray", "mask", "calibrant"};
	
	/**
	 * The names that will appear in the ControlsPanel next to the check boxes. 
	 */
	private String[] plotItemNames = {"Detector", "Beamstop", "Camera tube", "Q range", "Mask", "Calibrant"};
	
	// Flags that indicate which of the items should be plotted, initialised to default values.
	protected boolean detectorIsPlot = true;
	protected boolean beamstopIsPlot = true;
	protected boolean cameraTubeIsPlot = true;
	protected boolean rayIsPlot = true;
	protected boolean calibrantIsPlot = false;
	protected boolean maskIsPlot = false;
	
	protected CalibrantSpacing selectedCalibrant;
	private Label selectedCalibrantLabel;
	
			
	// Default colours of the objects to be plotted
	private Color detectorColour = Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
	private Color beamstopColour = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
	private Color clearanceColour = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY);
	private Color cameraTubeColour = Display.getDefault().getSystemColor(SWT.COLOR_YELLOW);
	
	private static final String[] LEGEND_LABELS = {"Detector", "Beamstop", "Clearance", "Camera tube"};
	private Color[] legendColours = {detectorColour, beamstopColour, clearanceColour, cameraTubeColour};
    private List<LegendItem> legendItems;
	
	protected Legend legend;
	protected Composite plotConfigurationPanel;
	
	
	public DefaultBeamlineConfigurationPlot(IPlottingSystem<Composite> system) {
		super(system);
		
		beamlineConfiguration = ResultsService.getInstance().getBeamlineConfiguration();
		
		resultsController = ResultsService.getInstance().getController();
		resultsController.addView(this);
	}
	
	
	@Override
	public void createPlotControls(Composite plotConfigurationPanel, Legend legend) {
		this.legend = legend;
		createLegend();
		
		this.plotConfigurationPanel = plotConfigurationPanel;
		
		createItemCheckBoxes();
		createPlotTypeButtons();
		
		plotConfigurationPanel.layout();
		updatePlot();
	}
	
	
	private void createLegend() {
		legendItems = new ArrayList<>();
		for(int i = 0; i < LEGEND_LABELS.length; i++){
			LegendItem item = legend.addLegendItem(LEGEND_LABELS[i], legendColours[i]);
			item.addColourChangeListener(this);
			legendItems.add(item);
		}
	}
	
	
	private void createItemCheckBoxes() {
		GuiHelper.createLabel(plotConfigurationPanel, "Select the items that should be displayed on the plot:");
		
		for(int i = 0; i < plotItems.length; i++){
			Button button = new Button(plotConfigurationPanel, SWT.CHECK);
			button.setText(plotItemNames[i]);
			String plotItem = plotItems[i];
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e){
					try {
						Field field = DefaultBeamlineConfigurationPlot.class.getDeclaredField(plotItem + "IsPlot");
						field.setBoolean(DefaultBeamlineConfigurationPlot.this, ((Button) e.getSource()).getSelection());
						updatePlot();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
			try {
				Field field = DefaultBeamlineConfigurationPlot.class.getDeclaredField(plotItem + "IsPlot");
				button.setSelection(field.getBoolean(DefaultBeamlineConfigurationPlot.this));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		selectedCalibrant = CalibrationFactory.getCalibrationStandards().getCalibrant(); 
		
		GuiHelper.createLabel(plotConfigurationPanel, "The currently selected calibrant is :");
		selectedCalibrantLabel = GuiHelper.createLabel(plotConfigurationPanel, "");
		if(selectedCalibrant != null) selectedCalibrantLabel.setText(selectedCalibrant.getName());
		Button configureCalibrantButton = new Button(plotConfigurationPanel, SWT.PUSH);
		configureCalibrantButton.setText("Configure calibrant ...");
		configureCalibrantButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(plotConfigurationPanel.getShell(), 
						                "org.dawb.workbench.plotting.preference.diffraction.calibrantPreferencePage", null, null);
				if (pref != null) pref.open();
			}
		});
		
		CalibrationFactory.addCalibrantSelectionListener(this);
	}
	
	
	private void createPlotTypeButtons() {
		/*
		 * Create the panel for selecting the type of plot. 
		 */
		Composite plotTypesPanel = new Composite(plotConfigurationPanel, SWT.NONE);
		plotTypesPanel.setLayout(new GridLayout());		
		
		GuiHelper.createLabel(plotTypesPanel, "Select the type of plot:");
		
		Button physicalSpaceButton = new Button(plotTypesPanel, SWT.RADIO);
		physicalSpaceButton.setText("Axes in mm");
		physicalSpaceButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				if(((Button) e.getSource()).getSelection()) 
					setPlotter(new PhysicalSpacePlotter(DefaultBeamlineConfigurationPlot.this));
		}
		});
		
	    Button pixelSpaceButton = new Button(plotTypesPanel, SWT.RADIO);
	    pixelSpaceButton.setText("Axes in pixels");
	    pixelSpaceButton.addSelectionListener(new SelectionAdapter() {
	    	@Override
			public void widgetSelected(SelectionEvent e){
	    		if(((Button) e.getSource()).getSelection()) 
	    			setPlotter(new PixelSpacePlotter(DefaultBeamlineConfigurationPlot.this));
					
		}
		});
	    
	    Button qSpaceButton = new Button(plotTypesPanel, SWT.RADIO);
	    qSpaceButton.setText("Axes in q (nm^-1)");
	    qSpaceButton.addSelectionListener(new SelectionAdapter() {
	    	@Override
			public void widgetSelected(SelectionEvent e){
	    		if(((Button) e.getSource()).getSelection()) 
	    			setPlotter(new QSpacePlotter(DefaultBeamlineConfigurationPlot.this));
					
		}
		});
		
	    physicalSpaceButton.setSelection(true);
	    plotter = new PhysicalSpacePlotter(this);
	}
	
	
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		updatePlot();
	}
	
	
	
	@Override
	public void colourChanged(ColourChangeEvent event) {
		updatePlot();
	}
	
	
	@Override
	public void calibrantSelectionChanged(CalibrantSelectionEvent evt) {
		selectedCalibrant = CalibrationFactory.getCalibrationStandards().getCalibrant();
		if(selectedCalibrant != null) selectedCalibrantLabel.setText(selectedCalibrant.getName());
		plotConfigurationPanel.layout();
		updatePlot();
	}
	
	
	@Override
	public void updatePlot() {
		plotter.createPlot();
	}
	
	
	protected void clearPlot(){
		system.clearRegions();
		for(IRegion region : system.getRegions()) system.removeRegion(region);
		for(ITrace trace : system.getTraces()) system.removeTrace(trace);
	}

	
	public IPlottingSystem<Composite> getSystem() {
		return system;
	}


	public BeamlineConfiguration getBeamlineConfiguration() {
		return beamlineConfiguration;
	}


	public AbstractResultsController getResultsController() {
		return resultsController;
	}


	public boolean isDetectorPlot() {
		return detectorIsPlot;
	}


	public boolean isBeamstopPlot() {
		return beamstopIsPlot;
	}


	public boolean isCameraTubePlot() {
		return cameraTubeIsPlot;
	}


	public boolean isRayPlot() {
		return rayIsPlot;
	}


	public boolean isCalibrantPlot() {
		return calibrantIsPlot;
	}


	public boolean isMaskPlot() {
		return maskIsPlot;
	}


	public CalibrantSpacing getSelectedCalibrant() {
		return selectedCalibrant;
	}


	public Legend getLegend() {
		return legend;
	}
	
	
	public void setPlotter(IBeamlineConfigurationPlotter plotter) {
		clearPlot();
		this.plotter = plotter;
		boolean rescale = system.isRescale();
		system.setRescale(true);    // Temporarily set rescale to true.
		plotter.createPlot();
		system.setRescale(rescale); // Restore the previous value. 
	}
	

	@Override
	public void dispose() {
		resultsController.removeView(this);
		system = null;
		for(LegendItem item : legendItems) item.removeColourChangeListener(this);
		CalibrationFactory.removeCalibrantSelectionListener(this);
	}
}

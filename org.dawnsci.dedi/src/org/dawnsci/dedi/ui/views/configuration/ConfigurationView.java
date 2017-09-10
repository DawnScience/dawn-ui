package org.dawnsci.dedi.ui.views.configuration;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.dawnsci.dedi.configuration.calculations.results.models.ResultsService;
import org.dawnsci.dedi.configuration.preferences.BeamlineConfigurationBean;
import org.dawnsci.dedi.ui.GuiHelper;
import org.dawnsci.dedi.ui.widgets.units.ComboUnitsProvider;
import org.dawnsci.dedi.ui.widgets.units.TextWithUnits;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.part.ViewPart;
import org.jscience.physics.amount.Amount;


public class ConfigurationView extends ViewPart implements Observer {
	/**
	 * The panel where users can choose one of predefined beamline configuration templates in order to
	 * populate the configuration view with default values, which can then be used by the users
	 * as a starting point for creating their own custom configurations.
	 */
	private BeamlineConfigurationTemplatesPanel templatesPanel;
	
	private BeamPropertiesPanel beamPropertiesPanel;
	
	private TextWithUnits<Angle> angle;
	private Spinner cameraLengthValueSpinner;

	public static final String ID = "dedi.configurationpanel";
	
	
	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		
		ScrolledComposite scrolledComposite = new ScrolledComposite( parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		
		Composite mainPanel = new Composite(scrolledComposite, SWT.NONE);
		mainPanel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(true).spacing(0, 20).applyTo(mainPanel);
		
		
		/*
		 * Create the panels that constitute the configuration view.
		 */
		templatesPanel = new BeamlineConfigurationTemplatesPanel(mainPanel);
		templatesPanel.addObserver(this);
	    new DetectorPanel(mainPanel, templatesPanel);
		new BeamstopPanel(mainPanel, templatesPanel);
		new CameraTubePanel(mainPanel, templatesPanel);
		beamPropertiesPanel = new BeamPropertiesPanel(mainPanel, templatesPanel); 
		
		
		/*
		 * Create any other controls that don't have their own separate panels.
		 */
		
		// A group for the camera length.
		Group cameraGroup = GuiHelper.createGroup(mainPanel, "", 3);
		GuiHelper.createLabel(cameraGroup, "Camera Length:");
		cameraLengthValueSpinner = new Spinner(cameraGroup, SWT.BORDER | SWT.READ_ONLY);
		cameraLengthValueSpinner.addModifyListener(e -> ResultsService.getInstance().getBeamlineConfiguration().
				                                        setCameraLength(cameraLengthValueSpinner.getSelection()/100.0));
		cameraLengthValueSpinner.setValues(145, 120, 970, 2, 25, 1); // Default values used in case no beamline template is selected.
		GuiHelper.createLabel(cameraGroup, "m");  // The unit for the camera length.
		
		
		// A group for the angle that gives the direction in which the q ranges should be calculated.
		Group angleGroup = GuiHelper.createGroup(mainPanel, "", 3);
		ComboUnitsProvider<Angle> combo = new ComboUnitsProvider<>(angleGroup, new ArrayList<>(Arrays.asList(SI.RADIAN, NonSI.DEGREE_ANGLE)));
		angle = new TextWithUnits<>(angleGroup, "Angle:", combo);  
		angle.addAmountChangeListener(this::angleChanged);
		angle.setValue(Amount.valueOf(90, NonSI.DEGREE_ANGLE)); // Default value (including for all the beamline templates).
		GridDataFactory.fillDefaults().span(2, 1).applyTo(angle);
		combo.moveBelow(angle);
		
		
		/*
		 * Layout the controls and configure the scroll bar.
		 */
		mainPanel.layout();
		
		scrolledComposite.setMinSize( mainPanel.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
		mainPanel.addListener(SWT.Resize, new Listener() {
			int width = -1;
			@Override
			public void handleEvent(Event event) {
				 int newWidth = mainPanel.getSize().x;
			     if (newWidth != width) {
			        scrolledComposite.setMinHeight(mainPanel.computeSize(newWidth, SWT.DEFAULT).y);
			        width = newWidth;
			     }
			}
		});
		scrolledComposite.setContent(mainPanel);	
		
		
		/*
		 * Get the currently selected beamline template. 
		 */
		update(null, null);
	}

	
	@Override
	public void setFocus() {
		beamPropertiesPanel.setFocus();
	}
	
	
	/**
	 * A handler for handling angle changes.
	 */
	private void angleChanged(){
		Amount<Angle> newAngle = angle.getValue(SI.RADIAN);
		if(newAngle == null) ResultsService.getInstance().getBeamlineConfiguration().setAngle(null);
		else ResultsService.getInstance().getBeamlineConfiguration().setAngle(newAngle.getEstimatedValue());
	}

	
	@Override
	public void update(Observable o, Object arg) {
		BeamlineConfigurationBean beamlineConfiguration = templatesPanel.getPredefinedBeamlineConfiguration();
		if(beamlineConfiguration == null) return;
		cameraLengthValueSpinner.setValues((int) Math.round(beamlineConfiguration.getMinCameraLength()*100), 
				                           (int) Math.round(beamlineConfiguration.getMinCameraLength()*100), 
				                           (int) Math.round(beamlineConfiguration.getMaxCameraLength()*100), 2, 
				                           (int) Math.round(beamlineConfiguration.getCameraLengthStepSize()*100), 1);
		cameraLengthValueSpinner.setSelection((int) Math.round(beamlineConfiguration.getMinCameraLength()*100));
		ResultsService.getInstance().getBeamlineConfiguration().setMinCameraLength(beamlineConfiguration.getMinCameraLength());
		ResultsService.getInstance().getBeamlineConfiguration().setMaxCameraLength(beamlineConfiguration.getMaxCameraLength());
	}
}

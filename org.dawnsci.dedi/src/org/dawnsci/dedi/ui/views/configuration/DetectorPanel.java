package org.dawnsci.dedi.ui.views.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.dawnsci.dedi.configuration.calculations.results.models.ResultsService;
import org.dawnsci.dedi.configuration.preferences.BeamlineConfigurationBean;
import org.dawnsci.dedi.configuration.preferences.BeamlineConfigurationPreferenceHelper;
import org.dawnsci.dedi.ui.GuiHelper;
import org.dawnsci.dedi.ui.TextUtil;
import org.dawnsci.plotting.tools.preference.detector.DiffractionDetector;
import org.dawnsci.plotting.tools.preference.detector.DiffractionDetectorConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class DetectorPanel implements Observer {
	private static final String TITLE =  "Detector";
	
	/**
	 * The panel where users can choose one of predefined beamline configuration templates.
	 */
	private BeamlineConfigurationTemplatesPanel templatesPanel;
	/**
	 * The list of available detectors.
	 */
	private List<DiffractionDetector> detectors;
	/**
	 * A combo for choosing the detector.
	 */
	private ComboViewer detectorTypesComboViewer;
	/**
	 * A label for displaying the resolution of the currently selected detector.
	 */
	private Label resolutionValueLabel;
	/**
	 * A label for displaying the size of the pixels of the currently selected detector.
	 */
	private Label pixelSizeValueLabel;
	/**
	 * A combo for selecting the units in which to display the size of the pixels of the currently selected detector.
	 */
	private Combo unitsCombo;
	
	private static final List<Unit<Length>> PIXEL_SIZE_UNITS = new ArrayList<>(Arrays.asList(SI.MILLIMETRE, SI.MICRO(SI.METER)));

	
	public DetectorPanel(Composite parent, BeamlineConfigurationTemplatesPanel panel) {
		templatesPanel = panel;
		panel.addObserver(this);
		
		Group detectorGroup = GuiHelper.createGroup(parent, TITLE, 3);
		
		GuiHelper.createLabel(detectorGroup, "Detector type:");
		
		
		/*
		 * Create the combo for the detectors.
		 */
		Combo detectorTypesCombo = new Combo(detectorGroup, SWT.READ_ONLY | SWT.H_SCROLL);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(detectorTypesCombo);
		detectorTypesComboViewer = new ComboViewer(detectorTypesCombo);
		detectorTypesComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		
		
		/*
		 * Create labels for displaying the resolution of the detector.
		 */
		GuiHelper.createLabel(detectorGroup, "Resolution (hxw):");
		resolutionValueLabel = new Label(detectorGroup, SWT.NONE);
		resolutionValueLabel.setText("");
		resolutionValueLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		resolutionValueLabel.setAlignment(SWT.RIGHT);
		new Label(detectorGroup, SWT.NONE); // Placeholder
		
		
		/*
		 * Create labels for displaying the pixel sizes.
		 */
		GuiHelper.createLabel(detectorGroup, "Pixel size:");
		pixelSizeValueLabel = GuiHelper.createLabel(detectorGroup, "");
		
		
		/*
		 * Create the combo for choosing the unit in which the pixel size is displayed.
		 */
		unitsCombo = new Combo(detectorGroup, SWT.READ_ONLY);
		ComboViewer unitsComboViewer = new ComboViewer(unitsCombo);
		unitsComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		unitsComboViewer.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element){
					if(element instanceof Unit<?>){
						@SuppressWarnings("unchecked")
						Unit<Length> unit = (Unit<Length>) element;
						return unit.toString() + " x " + unit.toString();
					}
					return super.getText(element);
				}
		});
		unitsComboViewer.setInput(PIXEL_SIZE_UNITS);
		unitsComboViewer.setSelection(new StructuredSelection(PIXEL_SIZE_UNITS.get(0)));
		unitsCombo.setVisible(false); // The combo should be visible only when a detector is selected.
		
			
	    /*
	     * Event handlers.		
	     */
		
		detectorTypesComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				    if (selection.size() > 0){
				    	unitsCombo.setVisible(true);
				        DiffractionDetector detector = (DiffractionDetector) selection.getFirstElement();
				        resolutionValueLabel.setText(detector.getNumberOfPixelsX() + " x " + detector.getNumberOfPixelsY());
				        @SuppressWarnings("unchecked")
						Unit<Length> unit = (Unit<Length>) unitsComboViewer.getStructuredSelection().getFirstElement();
				        pixelSizeValueLabel.setText(TextUtil.format(detector.getxPixelSize().doubleValue(unit)) + " x " +
				        		                    TextUtil.format(detector.getyPixelSize().doubleValue(unit)));
				        detectorGroup.layout();
				        ResultsService.getInstance().getBeamlineConfiguration().setDetector(detector);
				    } else {
				    	resolutionValueLabel.setText("");
				    	pixelSizeValueLabel.setText("");
				    	unitsCombo.setVisible(false);
				    	ResultsService.getInstance().getBeamlineConfiguration().setDetector(null);
				    }
				}
					
			});
		
		
		 unitsComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			 @Override
			 public void selectionChanged(SelectionChangedEvent event) {
				 IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				 if (selection.size() > 0){
					  @SuppressWarnings("unchecked")
					  Unit<Length> unit = (Unit<Length>) selection.getFirstElement();
					  DiffractionDetector detector = (DiffractionDetector) detectorTypesComboViewer.getStructuredSelection().getFirstElement();
					  if(detector != null){
						  pixelSizeValueLabel.setText(TextUtil.format(detector.getxPixelSize().doubleValue(unit)) + " x " +
					        		                      TextUtil.format(detector.getyPixelSize().doubleValue(unit)));
					      detectorGroup.layout();
					  }
				 }
			 }
		 });
		
		
		 /*
		  * Load detectors from preferences.
		  */
		 getDetectorsFromPreference();
		 detectorGroup.layout();
		 
		 BeamlineConfigurationPreferenceHelper.addDetectorPropertyChangeListener(new IPropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent event) {
					if(event.getProperty() == DiffractionDetectorConstants.DETECTOR){
						getDetectorsFromPreference();
						detectorGroup.layout();
					}
				}
			});
		 
		
		 /*
		  * Get the currently selected beamline template. 
		  */
		 update(null, null);
	}


    private void getDetectorsFromPreference() {
    	detectors = BeamlineConfigurationPreferenceHelper.getDetectorsListFromPreference(); 
		detectorTypesComboViewer.setInput(detectors);
		if(detectors != null && !detectors.isEmpty())
			detectorTypesComboViewer.setSelection(new StructuredSelection(detectors.get(0)));
		else{
			resolutionValueLabel.setText("");
	    	pixelSizeValueLabel.setText("");
	    	unitsCombo.setVisible(false);
	    	ResultsService.getInstance().getBeamlineConfiguration().setDetector(null);
		}
    }
	
    
	@Override
	public void update(Observable o, Object arg) {
		BeamlineConfigurationBean beamlineConfiguration = templatesPanel.getPredefinedBeamlineConfiguration();
		if(beamlineConfiguration == null) return;
		try{
			detectorTypesComboViewer.setSelection(new StructuredSelection(beamlineConfiguration.getDetector()));
		} catch(Exception e){
			if(detectors != null && !detectors.isEmpty()) detectorTypesComboViewer.setSelection(new StructuredSelection(detectors.get(0)));
		}
	}
}

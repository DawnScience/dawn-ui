package org.dawnsci.dedi.configuration.preferences;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawnsci.plotting.tools.preference.detector.DiffractionDetector;
import org.eclipse.richbeans.widgets.scalebox.NumberBox;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.richbeans.widgets.wrappers.ComboWrapper;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class BeamlineConfigurationsComposite extends Composite {
	private TextWrapper name;
	private NumberBox beamstopDiameter;
	private NumberBox beamstopXCentre;
	private NumberBox beamstopYCentre;
	private NumberBox cameraTubeDiameter;
	private NumberBox cameraTubeXCentre;
	private NumberBox cameraTubeYCentre;
	private NumberBox clearance;
	private NumberBox maxWavelength;
	private NumberBox minWavelength;
	private NumberBox maxCameraLength;
	private NumberBox minCameraLength;
	private NumberBox cameraLengthStepSize;
	private ComboWrapperWithoutClearSelection detector;
	
	public BeamlineConfigurationsComposite(Composite parent, int style) {
		super(parent, style);
		
		setLayout(new GridLayout(2, false));
		
		Label nameLabel = new Label(this, SWT.NONE);
		nameLabel.setText("Name");
		
		name = new TextWrapper(this, SWT.BORDER);
		name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		name.setTextLimit(64);
		
		beamstopDiameter = createRangeBox("Beamstop diameter", 0, 100000, "mm");
		beamstopDiameter.setDecimalPlaces(3);
		beamstopDiameter.setName("Beamstop diameter");
		
		beamstopXCentre = createRangeBox("Beamstop x centre", 0, 100000, "pixels");
		beamstopXCentre.setDecimalPlaces(3);
		beamstopXCentre.setName("The x coordinate of the beamstop's centre");
		
		beamstopYCentre = createRangeBox("Beamstop y centre", 0, 1000000, "pixels");
		beamstopYCentre.setDecimalPlaces(3);
		beamstopYCentre.setName("The y coordinate of the beamstop's centre");
		
		cameraTubeDiameter = createRangeBox("Camera tube diameter", 0, 100000, "mm");
		cameraTubeDiameter.setDecimalPlaces(3);
		cameraTubeDiameter.setName("Camera tube diameter");
		
		cameraTubeXCentre = createRangeBox("Camera tube x centre", 0, 100000, "pixels");
		cameraTubeXCentre.setDecimalPlaces(3);
		cameraTubeXCentre.setName("The x coordinate of the camera tube's centre");
		
		cameraTubeYCentre = createRangeBox("Camera tube y centre", 0, 1000000, "pixels");
		cameraTubeYCentre.setDecimalPlaces(3);
		cameraTubeYCentre.setName("The y coordinate of the camera tube's centre");
		
		clearance = createRangeBox("Clearance", 0, 1000000, "pixels");
		clearance.setDecimalPlaces(0);
		clearance.setName("Clearance");
		
		maxWavelength = createRangeBox("Maximum wavelength", 0, 1000000, "nm");
		maxWavelength.setDecimalPlaces(3);
		maxWavelength.setName("Maximum wavelength");
		
		minWavelength = createRangeBox("Minimum wavelength", 0, 1000000, "nm");
		minWavelength.setDecimalPlaces(3);
		minWavelength.setName("Minimum wavelength");
		
		maxCameraLength = createRangeBox("Maximum camera length", 0, 1000000, "m");
		maxCameraLength.setDecimalPlaces(2);
		maxCameraLength.setName("Maximum camera length");
		
		minCameraLength = createRangeBox("Minimum camera length", 0, 1000000, "m");
		minCameraLength.setDecimalPlaces(2);
		minCameraLength.setName("Minimum camera length");
		
		cameraLengthStepSize = createRangeBox("Step size for the camera length", 0, 1000000, "m");
		cameraLengthStepSize.setDecimalPlaces(2);
		cameraLengthStepSize.setName("Step size for the camera length");
		
		Label detectorLabel = new Label(this, SWT.NONE);
		detectorLabel.setText("Detector");
		
		detector = new ComboWrapperWithoutClearSelection(this, SWT.NONE);
		setDetectors();
	    BeamlineConfigurationPreferenceHelper.addDetectorPropertyChangeListener(e -> setDetectors());
	}
	
	
	private void setDetectors(){
		List<DiffractionDetector> detectors = BeamlineConfigurationPreferenceHelper.getDetectorsListFromPreference();
		Map<String, DiffractionDetector> comboItems = new HashMap<>();
		for(DiffractionDetector dd : detectors){
			comboItems.put(dd.getDetectorName(), dd);
		}
		if(detector != null && !detector.isDisposed()) detector.setItems(comboItems);
	}
	
	
	private NumberBox createRangeBox(String label, double lower, double upper, String unit) {
		
		Label lbl = new Label(this, SWT.NONE);
		lbl.setText(label);

		NumberBox rb = new ScaleBox(this, SWT.NONE);
		rb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		rb.setMinimum(lower);
		rb.setMaximum(upper);
		if (unit!=null) rb.setUnit(unit);
		
		return rb;
	}


	public TextWrapper getName() {
		return name;
	}


	public NumberBox getBeamstopDiameter() {
		return beamstopDiameter;
	}


	public NumberBox getBeamstopXCentre() {
		return beamstopXCentre;
	}


	public NumberBox getBeamstopYCentre() {
		return beamstopYCentre;
	}


	public NumberBox getClearance() {
		return clearance;
	}
	
	
	public NumberBox getMaxWavelength(){
		return maxWavelength;
	}
	
	
	public NumberBox getMinWavelength(){
		return minWavelength;
	}
	
	
	public NumberBox getMaxCameraLength(){
		return maxCameraLength;
	}
	
	
	public NumberBox getMinCameraLength(){
		return minCameraLength;
	}
	
	public ComboWrapper getDetector(){
		return detector;
	}
	
	
	public NumberBox getCameraTubeDiameter() {
		return cameraTubeDiameter;
	}


	public NumberBox getCameraTubeXCentre() {
		return cameraTubeXCentre;
	}


	public NumberBox getCameraTubeYCentre() {
		return cameraTubeYCentre;
	}
	
	
	public NumberBox getCameraLengthStepSize(){
		return cameraLengthStepSize;
	}
}

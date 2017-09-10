package org.dawnsci.dedi.ui.views.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;
import javax.measure.unit.BaseUnit;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.dawnsci.dedi.configuration.calculations.results.models.ResultsService;
import org.dawnsci.dedi.configuration.devices.Beamstop;
import org.dawnsci.dedi.configuration.preferences.BeamlineConfigurationBean;
import org.dawnsci.dedi.ui.GuiHelper;
import org.dawnsci.dedi.ui.widgets.units.ComboUnitsProvider;
import org.dawnsci.dedi.ui.widgets.units.LabelUnitsProvider;
import org.dawnsci.dedi.ui.widgets.units.LabelWithUnits;
import org.dawnsci.dedi.ui.widgets.units.TextWithUnits;
import org.dawnsci.plotting.tools.preference.detector.DiffractionDetector;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Spinner;
import org.jscience.physics.amount.Amount;


public class BeamstopPanel implements Observer {
	private BeamlineConfigurationTemplatesPanel templatesPanel;
	
	private LabelWithUnits<Length> beamstopDiameter;
	private Group beamstopPositionGroup;
	private TextWithUnits<Dimensionless> xPositionText;
	private TextWithUnits<Dimensionless> yPositionText;
	
	private Group clearanceGroup;
	Spinner clearanceValueSpinner;
	
	private static final String TITLE =  "Beamstop";
	
	private static final List<Unit<Length>> DIAMETER_UNITS = new ArrayList<>(Arrays.asList(SI.MILLIMETRE, SI.MICRO(SI.METER)));
	
	
	public BeamstopPanel(Composite parent, BeamlineConfigurationTemplatesPanel panel) {
		templatesPanel = panel;
		panel.addObserver(this);
		
		Group beamstopGroup = GuiHelper.createGroup(parent, TITLE, 3);
		
		ComboUnitsProvider<Length> diameterUnitsCombo = new ComboUnitsProvider<>(beamstopGroup, DIAMETER_UNITS);
		beamstopDiameter = new LabelWithUnits<>(beamstopGroup, "Diameter:", diameterUnitsCombo);
		beamstopDiameter.addAmountChangeListener(() -> textChanged());
		GridDataFactory.fillDefaults().span(2, 1).applyTo(beamstopDiameter);
		diameterUnitsCombo.moveBelow(beamstopDiameter);
		
		
		beamstopPositionGroup = GuiHelper.createGroup(beamstopGroup, "Position", 3);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 3;
		beamstopPositionGroup.setLayoutData(data);
		
		
		Unit<Dimensionless> pixel = new BaseUnit<>("Pixel");
		LabelUnitsProvider<Dimensionless> xPixelLabel = new LabelUnitsProvider<>(beamstopPositionGroup, pixel);
		xPositionText = new TextWithUnits<>(beamstopPositionGroup, "x:", xPixelLabel);
		xPositionText.addAmountChangeListener(BeamstopPanel.this :: textChanged);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(xPositionText);
		xPixelLabel.moveBelow(xPositionText);
		
		
		LabelUnitsProvider<Dimensionless> yPixelLabel = new LabelUnitsProvider<>(beamstopPositionGroup, pixel);
		yPositionText = new TextWithUnits<>(beamstopPositionGroup, "y:", yPixelLabel);
		yPositionText.addAmountChangeListener(BeamstopPanel.this :: textChanged);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(yPositionText);
		yPixelLabel.moveBelow(yPositionText);
		
		
		Button positionButton1 = new Button(beamstopPositionGroup, SWT.PUSH);
		positionButton1.setText("Centre of the detector");
		GridDataFactory.fillDefaults().span(3, 1).applyTo(positionButton1);
		positionButton1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				DiffractionDetector detector = ResultsService.getInstance().getBeamlineConfiguration().getDetector();
				if(detector != null){
					xPositionText.setValue(detector.getNumberOfPixelsX()/2.0);
					yPositionText.setValue(detector.getNumberOfPixelsY()/2.0);
				}
			}
		});
		
		Button positionButton2 = new Button(beamstopPositionGroup, SWT.PUSH);
		positionButton2.setText("Centre of the top edge");
		GridDataFactory.fillDefaults().span(3, 1).applyTo(positionButton2);
		positionButton2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				DiffractionDetector detector = ResultsService.getInstance().getBeamlineConfiguration().getDetector();
				if(detector != null){
					xPositionText.setValue(detector.getNumberOfPixelsX()/2.0);
					yPositionText.setValue(0);
				}
			}
		});
		
		
		//Clearance
		clearanceGroup = GuiHelper.createGroup(beamstopGroup, "Clearance", 3);
		GridDataFactory.fillDefaults().span(3, 1).applyTo(clearanceGroup);
		
		GuiHelper.createLabel(clearanceGroup, "Clearance :");
		clearanceValueSpinner = new Spinner(clearanceGroup, SWT.BORDER);
		clearanceValueSpinner.setValues(0, 0, Integer.MAX_VALUE, 0, 1, 1);
		clearanceValueSpinner.addModifyListener(e -> ResultsService.getInstance().getBeamlineConfiguration()
				                                     .setClearance((int) (clearanceValueSpinner.getSelection())));
		GuiHelper.createLabel(clearanceGroup, "pixel(s)");
		
		// Need to update because the predefinedBeamlineConfiguration could have been
		// initialised before this registered as its observer
		update(null, null);
	}
	
		
	private void textChanged(){
		 Amount<Length> diameter = beamstopDiameter.getValue(SI.MILLIMETER);
		 Amount<Dimensionless> xpixels = xPositionText.getValue();
		 Amount<Dimensionless> ypixels = yPositionText.getValue();
		 if(diameter == null || xpixels == null || ypixels == null)
			 ResultsService.getInstance().getBeamlineConfiguration().setBeamstop(null);
		 else
			 ResultsService.getInstance().getBeamlineConfiguration()
			    .setBeamstop(new Beamstop(diameter, xpixels.getEstimatedValue(), ypixels.getEstimatedValue()));
	}

	
	private void setValues(double diameter, double beamstopX, double beamstopY, int clearance){
		 beamstopDiameter.setValue(Amount.valueOf(diameter, SI.MILLIMETER));
		 xPositionText.setValue(beamstopX);
		 yPositionText.setValue(beamstopY);
		 clearanceValueSpinner.setSelection(clearance);
		 clearanceGroup.layout();
		 textChanged();
	}
	
	
	private void clearValues(){
		beamstopDiameter.clear();
		xPositionText.clearText();
		yPositionText.clearText();
		textChanged();
	}
	

	@Override
	public void update(Observable o, Object arg) {
		BeamlineConfigurationBean beamlineConfiguration = templatesPanel.getPredefinedBeamlineConfiguration();
		if(beamlineConfiguration == null) {
			clearValues();
			return;
		}
		setValues(beamlineConfiguration.getBeamstopDiameter(), beamlineConfiguration.getBeamstopXCentre(), 
				 beamlineConfiguration.getBeamstopYCentre(), beamlineConfiguration.getClearance());
	}
	
}

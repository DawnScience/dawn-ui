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
import org.dawnsci.dedi.configuration.devices.CameraTube;
import org.dawnsci.dedi.configuration.preferences.BeamlineConfigurationBean;
import org.dawnsci.dedi.ui.GuiHelper;
import org.dawnsci.dedi.ui.widgets.units.ComboUnitsProvider;
import org.dawnsci.dedi.ui.widgets.units.LabelUnitsProvider;
import org.dawnsci.dedi.ui.widgets.units.LabelWithUnits;
import org.dawnsci.dedi.ui.widgets.units.TextWithUnits;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.jscience.physics.amount.Amount;


public class CameraTubePanel implements Observer {
	private BeamlineConfigurationTemplatesPanel templatesPanel; 
	
	private Group cameraTubeGroup;
	private LabelWithUnits<Length> cameraTubeDiameter;
	private TextWithUnits<Dimensionless> xPositionText;
	private TextWithUnits<Dimensionless> yPositionText;
	
	private static final String TITLE =  "Camera tube";
	
	private static final List<Unit<Length>> DIAMETER_UNITS = new ArrayList<>(Arrays.asList(SI.MILLIMETRE, SI.MICRO(SI.METER)));
	
	
	public CameraTubePanel(Composite parent, BeamlineConfigurationTemplatesPanel panel){
		templatesPanel = panel;
		panel.addObserver(this);
		
		cameraTubeGroup = GuiHelper.createGroup(parent, TITLE, 3);
		
		
		ComboUnitsProvider<Length> diameterUnitsCombo = new ComboUnitsProvider<>(cameraTubeGroup, DIAMETER_UNITS);
		cameraTubeDiameter = new LabelWithUnits<>(cameraTubeGroup, "Diameter:", diameterUnitsCombo);
		cameraTubeDiameter.addAmountChangeListener(() -> textChanged());
		GridDataFactory.fillDefaults().span(2, 1).applyTo(cameraTubeDiameter);
		diameterUnitsCombo.moveBelow(cameraTubeDiameter);
		
		
		Group cameraTubePositionGroup = GuiHelper.createGroup(cameraTubeGroup, "Position", 3);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 3;
		cameraTubePositionGroup.setLayoutData(data);
		
		
		Unit<Dimensionless> pixel = new BaseUnit<>("Pixel");
		LabelUnitsProvider<Dimensionless> xPixelLabel = new LabelUnitsProvider<>(cameraTubePositionGroup, pixel);
		xPositionText = new TextWithUnits<>(cameraTubePositionGroup, "x:", xPixelLabel);
		xPositionText.addAmountChangeListener(CameraTubePanel.this :: textChanged);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(xPositionText);
		xPixelLabel.moveBelow(xPositionText);
		
		
		LabelUnitsProvider<Dimensionless> yPixelLabel = new LabelUnitsProvider<>(cameraTubePositionGroup, pixel);
		yPositionText = new TextWithUnits<>(cameraTubePositionGroup, "y:", yPixelLabel);
		yPositionText.addAmountChangeListener(CameraTubePanel.this :: textChanged);
		GridDataFactory.fillDefaults().span(2, 1).applyTo(yPositionText);
		yPixelLabel.moveBelow(yPositionText);
		
		update(null, null);
	}
	
	
	private void textChanged(){
		 Amount<Length> diameter = cameraTubeDiameter.getValue(SI.MILLIMETER);
		 Amount<Dimensionless> xpixels = xPositionText.getValue();
		 Amount<Dimensionless> ypixels = yPositionText.getValue();
		 if(diameter == null || xpixels == null || ypixels == null || diameter.getEstimatedValue() == 0)
			 ResultsService.getInstance().getBeamlineConfiguration().setCameraTube(null);
		 else
			 ResultsService.getInstance().getBeamlineConfiguration()
			    .setCameraTube(new CameraTube(diameter, xpixels.getEstimatedValue(), ypixels.getEstimatedValue()));
	}
	
	
	private void setValues(double diameter, double cameraTubeX, double cameraTubeY){
		 cameraTubeDiameter.setValue(Amount.valueOf(diameter, SI.MILLIMETER));
		 xPositionText.setValue(cameraTubeX);
		 yPositionText.setValue(cameraTubeY);
		 textChanged();
	}
	
	
	private void clearValues(){
		cameraTubeDiameter.clear();
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
		if(beamlineConfiguration.getCameraTubeDiameter() == 0){
			setValues(0, 0, 0);
			cameraTubeGroup.setEnabled(false);
		}
		else {
			setValues(beamlineConfiguration.getCameraTubeDiameter(), beamlineConfiguration.getCameraTubeXCentre(), 
				 beamlineConfiguration.getCameraTubeYCentre());
			cameraTubeGroup.setEnabled(true);
		}
	}
}

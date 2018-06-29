package org.dawnsci.dedi.ui.views.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;

import org.dawnsci.dedi.configuration.calculations.results.models.ResultsService;
import org.dawnsci.dedi.configuration.devices.CameraTube;
import org.dawnsci.dedi.configuration.preferences.BeamlineConfigurationBean;
import org.dawnsci.dedi.ui.GuiHelper;
import org.dawnsci.dedi.ui.widgets.units.ComboUnitsProvider;
import org.dawnsci.dedi.ui.widgets.units.LabelUnitsProvider;
import org.dawnsci.dedi.ui.widgets.units.LabelWithUnits;
import org.dawnsci.dedi.ui.widgets.units.TextWithUnits;
import org.eclipse.dawnsci.analysis.api.unit.UnitUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public class CameraTubePanel implements Observer {
	private BeamlineConfigurationTemplatesPanel templatesPanel;

	private Group cameraTubeGroup;
	private LabelWithUnits<Length> cameraTubeDiameter;
	private TextWithUnits<Dimensionless> xPositionText;
	private TextWithUnits<Dimensionless> yPositionText;

	private static final String TITLE =  "Camera tube";

	private static final List<Unit<Length>> DIAMETER_UNITS = Arrays.asList(UnitUtils.MILLIMETRE, UnitUtils.MICROMETRE);

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

		Unit<Dimensionless> pixel = UnitUtils.PIXEL;
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

	private void textChanged() {
		Quantity<Length> diameter = cameraTubeDiameter.getValue(UnitUtils.MILLIMETRE);
		Quantity<Dimensionless> xpixels = xPositionText.getValue();
		Quantity<Dimensionless> ypixels = yPositionText.getValue();
		if (diameter == null || xpixels == null || ypixels == null || diameter.getValue().doubleValue() == 0)
			ResultsService.getInstance().getBeamlineConfiguration().setCameraTube(null);
		else
			ResultsService.getInstance().getBeamlineConfiguration().setCameraTube(
					new CameraTube(diameter, xpixels.getValue().doubleValue(), ypixels.getValue().doubleValue()));
	}

	private void setValues(double diameter, double cameraTubeX, double cameraTubeY) {
		cameraTubeDiameter.setValue(UnitUtils.getQuantity(diameter, UnitUtils.MILLIMETRE));
		xPositionText.setValue(cameraTubeX);
		yPositionText.setValue(cameraTubeY);
		textChanged();
	}

	private void clearValues() {
		cameraTubeDiameter.clear();
		xPositionText.clearText();
		yPositionText.clearText();
		textChanged();
	}

	@Override
	public void update(Observable o, Object arg) {
		BeamlineConfigurationBean beamlineConfiguration = templatesPanel.getPredefinedBeamlineConfiguration();
		if (beamlineConfiguration == null) {
			clearValues();
			return;
		}
		if (beamlineConfiguration.getCameraTubeDiameter() == 0) {
			setValues(0, 0, 0);
			cameraTubeGroup.setEnabled(false);
		} else {
			setValues(beamlineConfiguration.getCameraTubeDiameter(), beamlineConfiguration.getCameraTubeXCentre(),
					beamlineConfiguration.getCameraTubeYCentre());
			cameraTubeGroup.setEnabled(true);
		}
	}
}

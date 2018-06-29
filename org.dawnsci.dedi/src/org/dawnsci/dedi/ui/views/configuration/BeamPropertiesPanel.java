package org.dawnsci.dedi.ui.views.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.function.Predicate;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;

import org.dawnsci.dedi.configuration.calculations.results.models.ResultsService;
import org.dawnsci.dedi.configuration.calculations.scattering.BeamEnergy;
import org.dawnsci.dedi.configuration.calculations.scattering.BeamQuantity.BeamQuantities;
import org.dawnsci.dedi.configuration.calculations.scattering.Wavelength;
import org.dawnsci.dedi.configuration.preferences.BeamlineConfigurationBean;
import org.dawnsci.dedi.ui.GuiHelper;
import org.dawnsci.dedi.ui.TextUtil;
import org.dawnsci.dedi.ui.widgets.units.ComboUnitsProvider;
import org.dawnsci.dedi.ui.widgets.units.IAmountInputValidator;
import org.dawnsci.dedi.ui.widgets.units.TextWithUnits;
import org.eclipse.dawnsci.analysis.api.unit.UnitUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import si.uom.NonSI;
import si.uom.SI;
import tec.units.indriya.function.QuantityFunctions;
import tec.units.indriya.unit.MetricPrefix;

public class BeamPropertiesPanel implements Observer {
	private BeamlineConfigurationTemplatesPanel templatesPanel;

	private TextWithUnits<Energy> energy;
	private TextWithUnits<Length> wavelength;
	private Quantity<Length> minWavelength;
	private Quantity<Length> maxWavelength;
	private Quantity<Energy> minEnergy;
	private Quantity<Energy> maxEnergy;
	private Label minWavelengthLabel;
	private Label maxWavelengthLabel;

	private boolean isEdited = true;

	private static final List<Unit<Energy>> ENERGY_UNITS = Arrays.asList(MetricPrefix.KILO(NonSI.ELECTRON_VOLT), NonSI.ELECTRON_VOLT);
	private static final List<Unit<Length>> WAVELENGTH_UNITS = Arrays.asList(MetricPrefix.NANO(SI.METRE), NonSI.ANGSTROM);

	class QuantityValidator<T extends Quantity<T>> implements IAmountInputValidator<T> {
		private Predicate<Quantity<T>> predicate;

		public QuantityValidator(T min, T max) {
			predicate = QuantityFunctions.isGreaterThan(min).and(QuantityFunctions.isLesserThan(max));

		}

		@Override
		public boolean isValid(Quantity<T> input) {
			return predicate.test(input);
		}
	}

	public BeamPropertiesPanel(Composite parent, BeamlineConfigurationTemplatesPanel panel) {
		templatesPanel = panel;
		panel.addObserver(this);

		Group beamlineQuantityGroup = GuiHelper.createGroup(parent, "Beam properties", 3);

		minWavelength = UnitUtils.getQuantity(0, SI.METRE);
		maxWavelength = UnitUtils.getQuantity(3, SI.METRE);
		minEnergy = UnitUtils.getQuantity(0, SI.JOULE);
		maxEnergy = UnitUtils.getQuantity(3, SI.JOULE);

		ComboUnitsProvider<Energy> energyUnitsCombo = new ComboUnitsProvider<>(beamlineQuantityGroup, ENERGY_UNITS);
		energy = new TextWithUnits<>(beamlineQuantityGroup, "Energy", energyUnitsCombo, new QuantityValidator(minEnergy, maxEnergy));
		energy.addAmountChangeListener(() -> textChanged(BeamQuantities.ENERGY));
		GridDataFactory.fillDefaults().span(2, 1).applyTo(energy);
		energyUnitsCombo.moveBelow(energy);

		ComboUnitsProvider<Length> wavelengthUnitsCombo = new ComboUnitsProvider<>(beamlineQuantityGroup, WAVELENGTH_UNITS);
		wavelength = new TextWithUnits<>(beamlineQuantityGroup, "Wavelength", wavelengthUnitsCombo, new QuantityValidator(minWavelength, maxWavelength));
		wavelength.addAmountChangeListener(() -> textChanged(BeamQuantities.WAVELENGTH));
		GridDataFactory.fillDefaults().span(2, 1).applyTo(wavelength);
		wavelengthUnitsCombo.moveBelow(wavelength);

		GuiHelper.createLabel(beamlineQuantityGroup, "Minimum allowed wavelength:");
		minWavelengthLabel = GuiHelper.createLabel(beamlineQuantityGroup, "");
		GuiHelper.createLabel(beamlineQuantityGroup, "");

		GuiHelper.createLabel(beamlineQuantityGroup, "Maximum allowed wavelength:");
		maxWavelengthLabel = GuiHelper.createLabel(beamlineQuantityGroup, "");
		GuiHelper.createLabel(beamlineQuantityGroup, "");

		setToolTipTextsAndLabels();

		wavelength.addUnitsChangeListener(this::setToolTipTextsAndLabels);
		energy.addUnitsChangeListener(this::setToolTipTextsAndLabels);

		update(null, null);
	}

	private void textChanged(BeamQuantities q){
		if(isEdited){
			try{ 
				isEdited = false;
				switch(q){
				case ENERGY:
					wavelength.setValue(new BeamEnergy(energy.getValue()).toWavelength().getValue().to(SI.METRE));
					ResultsService.getInstance().getBeamlineConfiguration().setWavelength(wavelength.getValue(SI.METRE).getValue().doubleValue());
					break;
				case WAVELENGTH:
					energy.setValue(new Wavelength(wavelength.getValue()).to(new BeamEnergy()).getValue().to(SI.JOULE));
					ResultsService.getInstance().getBeamlineConfiguration().setWavelength(wavelength.getValue(SI.METRE).getValue().doubleValue());
					break;
				default:
			}
			} catch(NullPointerException e){
				switch(q){
					case ENERGY:
						wavelength.clearText();
						ResultsService.getInstance().getBeamlineConfiguration().setWavelength(null);
						break;
					case WAVELENGTH:
						energy.clearText();
						ResultsService.getInstance().getBeamlineConfiguration().setWavelength(null);
						break;
					default:
				}
			} finally {
				isEdited = true;
			}
		}
	}


	@Override
	public void update(Observable o, Object arg) {
		BeamlineConfigurationBean beamlineConfiguration = templatesPanel.getPredefinedBeamlineConfiguration();
		if(beamlineConfiguration == null) return;
		minWavelength = UnitUtils.getQuantity(beamlineConfiguration.getMinWavelength()*1.0e-9, SI.METRE);
		maxWavelength = UnitUtils.getQuantity(beamlineConfiguration.getMaxWavelength()*1.0e-9, SI.METRE);
		minEnergy = new Wavelength(maxWavelength).to(new BeamEnergy()).getValue().to(SI.JOULE);
		maxEnergy = new Wavelength(minWavelength).to(new BeamEnergy()).getValue().to(SI.JOULE);
		ResultsService.getInstance().getBeamlineConfiguration().setMaxWavelength(UnitUtils.convert(maxWavelength, SI.METRE));
		ResultsService.getInstance().getBeamlineConfiguration().setMinWavelength(UnitUtils.convert(minWavelength, SI.METRE));
		setToolTipTextsAndLabels();
	}

	private void setToolTipTextsAndLabels(){
		if(minEnergy == null || maxEnergy == null || minWavelength == null || maxWavelength == null){
			energy.setToolTipText("");
			wavelength.setToolTipText("");
			minWavelengthLabel.setText("");
			maxWavelengthLabel.setText("");
			return;
		}
		Unit<Energy> eUnit = energy.getCurrentUnit();
		energy.setToolTipText("Min energy: " + TextUtil.format(UnitUtils.convert(minEnergy, eUnit)) + 
				"\nMax energy: " + TextUtil.format(UnitUtils.convert(maxEnergy, eUnit)));
		Unit<Length> wUnit = wavelength.getCurrentUnit();
		minWavelengthLabel.setText(TextUtil.format(UnitUtils.convert(minWavelength, wUnit)) + " " + wUnit.toString());
		maxWavelengthLabel.setText(TextUtil.format(UnitUtils.convert(maxWavelength, wUnit)) + " " + wUnit.toString());
		wavelength.setToolTipText("Min wavelength: " + TextUtil.format(UnitUtils.convert(minWavelength, wUnit)) + 
				"\nMax wavelength: " + TextUtil.format(UnitUtils.convert(maxWavelength, wUnit)));
	}

	public void setFocus() {
		energy.setFocus();
	}
}

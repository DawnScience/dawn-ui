package org.dawnsci.dedi.ui.views.configuration;

import org.dawnsci.dedi.configuration.calculations.results.models.ResultsService;
import org.dawnsci.dedi.configuration.calculations.scattering.BeamEnergy;
import org.dawnsci.dedi.configuration.preferences.BeamlineConfigurationBean;
import org.dawnsci.dedi.ui.GuiHelper;
import org.dawnsci.dedi.ui.TextUtil;
import org.dawnsci.dedi.ui.widgets.units.ComboUnitsProvider;
import org.dawnsci.dedi.ui.widgets.units.TextWithUnits;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.jscience.physics.amount.Amount;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import static org.dawnsci.dedi.configuration.calculations.scattering.BeamQuantity.Quantities;

public class BeamPropertiesPanel implements Observer {
	private BeamlineConfigurationTemplatesPanel templatesPanel;
	
	private TextWithUnits<Energy> energy;
	private TextWithUnits<Length> wavelength;
	private Amount<Length> minWavelength;
	private Amount<Length> maxWavelength;
	private Amount<Energy> minEnergy;
	private Amount<Energy> maxEnergy;
	private Label minWavelengthLabel;
	private Label maxWavelengthLabel;
	
	private boolean isEdited = true;
	
	private static final List<Unit<Energy>> ENERGY_UNITS = new ArrayList<>(Arrays.asList(SI.KILO(NonSI.ELECTRON_VOLT), NonSI.ELECTRON_VOLT));
	private static final List<Unit<Length>> WAVELENGTH_UNITS = new ArrayList<>(Arrays.asList(SI.NANO(SI.METER), NonSI.ANGSTROM));

	
	public BeamPropertiesPanel(Composite parent, BeamlineConfigurationTemplatesPanel panel) {
		templatesPanel = panel;
		panel.addObserver(this);
		
		Group beamlineQuantityGroup = GuiHelper.createGroup(parent, "Beam properties", 3);
		
		minWavelength = Amount.valueOf(0, SI.METER);
		maxWavelength = Amount.valueOf(3, SI.METER);
		minEnergy = Amount.valueOf(0, SI.JOULE);
		maxEnergy = Amount.valueOf(3, SI.JOULE);
		
		
		ComboUnitsProvider<Energy> energyUnitsCombo = new ComboUnitsProvider<>(beamlineQuantityGroup, ENERGY_UNITS);
		energy = new TextWithUnits<>(beamlineQuantityGroup, "Energy", energyUnitsCombo,
						             input ->  input.isLessThan(maxEnergy) && input.isGreaterThan(minEnergy));
		energy.addAmountChangeListener(() -> textChanged(Quantities.ENERGY));
		GridDataFactory.fillDefaults().span(2, 1).applyTo(energy);
		energyUnitsCombo.moveBelow(energy);
		
		
		ComboUnitsProvider<Length> wavelengthUnitsCombo = new ComboUnitsProvider<>(beamlineQuantityGroup, WAVELENGTH_UNITS);
		wavelength = new TextWithUnits<>(beamlineQuantityGroup, "Wavelength", wavelengthUnitsCombo,
				                         input -> input.isLessThan(maxWavelength) && input.isGreaterThan(minWavelength));
		wavelength.addAmountChangeListener(() -> textChanged(Quantities.WAVELENGTH));
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
	
	
	private void textChanged(Quantities q){
		if(isEdited){
			try{ 
				isEdited = false;
				switch(q){
				case ENERGY:
					wavelength.setValue(new BeamEnergy(energy.getValue())
							.toWavelength().getValue().to(SI.METER));
					ResultsService.getInstance().getBeamlineConfiguration().setWavelength(wavelength.getValue(SI.METER).getEstimatedValue());
					break;
				case WAVELENGTH:
					energy.setValue(new org.dawnsci.dedi.configuration.calculations.scattering.Wavelength(wavelength.getValue())
							.to(new BeamEnergy()).getValue().to(SI.JOULE));
					ResultsService.getInstance().getBeamlineConfiguration().setWavelength(wavelength.getValue(SI.METER).getEstimatedValue());
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
		minWavelength = Amount.valueOf(beamlineConfiguration.getMinWavelength()*1.0e-9, SI.METER);
		maxWavelength = Amount.valueOf(beamlineConfiguration.getMaxWavelength()*1.0e-9, SI.METER);
		minEnergy = new org.dawnsci.dedi.configuration.calculations.scattering.Wavelength(maxWavelength)
				      .to(new BeamEnergy()).getValue().to(SI.JOULE);
		maxEnergy = new org.dawnsci.dedi.configuration.calculations.scattering.Wavelength(minWavelength)
			          .to(new BeamEnergy()).getValue().to(SI.JOULE);
		ResultsService.getInstance().getBeamlineConfiguration().setMaxWavelength(maxWavelength.doubleValue(SI.METER));
		ResultsService.getInstance().getBeamlineConfiguration().setMinWavelength(minWavelength.doubleValue(SI.METER));
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
		energy.setToolTipText("Min energy: " + TextUtil.format(minEnergy.doubleValue(energy.getCurrentUnit())) + 
				              "\nMax energy: " + TextUtil.format(maxEnergy.doubleValue(energy.getCurrentUnit())));
		minWavelengthLabel.setText(TextUtil.format(minWavelength.doubleValue(wavelength.getCurrentUnit())) + " " + wavelength.getCurrentUnit().toString());
		maxWavelengthLabel.setText(TextUtil.format(maxWavelength.doubleValue(wavelength.getCurrentUnit())) + " " + wavelength.getCurrentUnit().toString());
		wavelength.setToolTipText("Min wavelength: " + TextUtil.format(minWavelength.doubleValue(wavelength.getCurrentUnit())) + 
	              "\nMax wavelength: " + TextUtil.format(maxWavelength.doubleValue(wavelength.getCurrentUnit())));
	}
	
	
	public void setFocus() {
		energy.setFocus();
	}
}

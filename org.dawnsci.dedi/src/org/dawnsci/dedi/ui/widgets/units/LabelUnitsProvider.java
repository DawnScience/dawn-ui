package org.dawnsci.dedi.ui.widgets.units;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.dawnsci.dedi.ui.GuiHelper;
import org.eclipse.swt.widgets.Composite;

public class LabelUnitsProvider<T extends Quantity> extends WidgetUnitsProvider<T> {
	public LabelUnitsProvider(Composite parent, Unit<T> unit) {
		super(parent);
		currentUnit = unit;
		GuiHelper.createLabel(this, currentUnit.toString());
	}
}
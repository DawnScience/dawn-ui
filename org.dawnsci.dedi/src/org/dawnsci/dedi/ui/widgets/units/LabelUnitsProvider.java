package org.dawnsci.dedi.ui.widgets.units;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.dawnsci.dedi.ui.GuiHelper;
import org.eclipse.swt.widgets.Composite;

public class LabelUnitsProvider<T extends Quantity<T>> extends WidgetUnitsProvider<T> {
	public LabelUnitsProvider(Composite parent, Unit<T> unit) {
		super(parent);
		currentUnit = unit;
		GuiHelper.createLabel(this, currentUnit.toString());
	}
}
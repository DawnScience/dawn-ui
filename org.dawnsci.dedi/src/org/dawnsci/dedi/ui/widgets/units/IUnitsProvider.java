package org.dawnsci.dedi.ui.widgets.units;

import javax.measure.Quantity;
import javax.measure.Unit;

public interface IUnitsProvider<T extends Quantity<T>> {

	public Unit<T> getCurrentUnit();

	public void addUnitsChangeListener(IUnitsChangeListener listener);

	public void removeUnitsChangeListener(IUnitsChangeListener listener);
}
package org.dawnsci.dedi.ui.widgets.units;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;


public interface IUnitsProvider<T extends Quantity> {
	
	public Unit<T> getCurrentUnit();
	
	public void addUnitsChangeListener(IUnitsChangeListener listener);
	
	public void removeUnitsChangeListener(IUnitsChangeListener listener);
}
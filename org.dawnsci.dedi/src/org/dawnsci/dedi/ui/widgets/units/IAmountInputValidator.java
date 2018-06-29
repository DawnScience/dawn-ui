package org.dawnsci.dedi.ui.widgets.units;

import javax.measure.Quantity;

public interface IAmountInputValidator<T extends Quantity<T>> {
	public boolean isValid(Quantity<T> input);
}

package org.dawnsci.dedi.ui.widgets.units;

import javax.measure.quantity.Quantity;

import org.jscience.physics.amount.Amount;

public interface IAmountInputValidator<T extends Quantity> {
	public boolean isValid(Amount<T> input);
}

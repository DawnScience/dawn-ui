package org.dawb.workbench.plotting.tools.diffraction;

import java.util.EventListener;

import javax.measure.quantity.Quantity;

public interface AmountListener extends EventListener {

	void amountChanged(AmountEvent<? extends Quantity> evt);
}

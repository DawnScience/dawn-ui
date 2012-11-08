package org.dawb.workbench.plotting.tools.diffraction;

import java.util.EventListener;

import javax.measure.quantity.Quantity;

public interface AmountListener<E extends Quantity> extends EventListener {

	void amountChanged(AmountEvent<E> evt);
}

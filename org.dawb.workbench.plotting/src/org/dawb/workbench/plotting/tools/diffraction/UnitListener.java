package org.dawb.workbench.plotting.tools.diffraction;

import java.util.EventListener;

import javax.measure.quantity.Quantity;

public interface UnitListener extends EventListener {

	void unitChanged(UnitEvent<? extends Quantity> evt);
}

package org.dawnsci.common.widgets.periodictable;

import java.util.EventListener;

public interface IPeriodicTableButtonPressedListener extends EventListener {
	public void buttonPressed(PeriodicTableButtonPressedEvent event);
}

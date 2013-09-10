package org.dawnsci.slicing.api.system;

import java.util.EventListener;

public interface AxisChoiceListener extends EventListener{

	/**
	 * Called when the user changes their axis choice.
	 * @param evt
	 */
	public void axisChoicePerformed(AxisChoiceEvent evt);
}

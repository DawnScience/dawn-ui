package org.dawb.workbench.ui.diffraction.table;

import java.util.EventListener;


public interface IDiffractionDataListener extends EventListener {

	public void dataChanged(DiffractionDataChanged event);
	
}
